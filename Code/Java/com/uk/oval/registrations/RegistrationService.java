package com.uk.oval.registrations;

import java.io.Serializable;
import java.util.UUID;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.uk.oval.tools.JSFUtils;
import com.uk.oval.tools.Settings;

import eu.linqed.debugtoolbar.DebugToolbar;

public class RegistrationService implements Serializable {

	private static final long serialVersionUID = 3970190959892268797L;

	private static final DebugToolbar log = DebugToolbar.get();
	private static final Settings settings = Settings.getBean("securesettings");
	private ConnectionsService conService;

	public RegistrationService() {
	}

	private Database getNABdb() throws NotesException {
		Database dbNab = null;
		// get the external db
		String nabPath = settings.get("database_externaldirectory").get(
				"value1").toString();
		if (StringUtil.isEmpty(nabPath)) {
			throw new Error("Missing External Directory DB value");
		} else {
			dbNab = ExtLibUtil.getCurrentSessionAsSigner().getDatabase(
					ExtLibUtil.getCurrentDatabase().getServer(), nabPath);

			if (!dbNab.isOpen()) {
				throw new Error("External Directory not found - " + nabPath);
			}
		}
		return dbNab;
	}

	public void registerUser(Person person) throws RegistrationException {
		RegistrationRequest request = null;

		// generate random password
		person.setPassword(UUID.randomUUID().toString());
		person.setHasDummyPassword(true);

		// create the person doc
		createPersonInNAB(person);

		// create password reset request
		request = new RegistrationRequest();
		request.setEmail(person.getEmail());
		request.setCommunityGUID(person.getCommunityGUID());
		request.setCommunityName(person.getCommunityName());
		request.save();

		// send mail
		sendRequestEmail(request);

		if (StringUtil.equals(settings.get(
				"registration_createprofileimmediately").get("value1"), "true")) {
			// add the person into connections
			registerInConnections(person);
		}

	}

	public void sendRequestEmail(Request request) throws RegistrationException {

		// email link to password
		EMail email;
		if (request.getType().equals(Request.REQUESTTYPE_REGISTRATION)) {
			email = new EMail(EMail.EMAILTYPE_REGISTRATION_CONTINUE, request);
		} else if (request.getType().equals(Request.REQUESTTYPE_PASSWORDRESET)) {
			email = new EMail(EMail.EMAILTYPE_PASSWORDRESET_CONTINUE, request);
		} else {
			throw new RegistrationException("Invalid request type");
		}
		email.setSendTo(request.getEmail());
		email.send();
	}

	public void registerInConnections(String email)
			throws RegistrationException {
		// validate
		if (StringUtil.isEmpty(email))
			throw new IllegalArgumentException("Email is empty");

		// get person doc
		Database dbNab = null;
		View viewPeople = null;
		Document doc = null;
		try {
			dbNab = getNABdb();
			viewPeople = dbNab.getView("($Users)");
			doc = viewPeople.getDocumentByKey(email);
			if (null == doc) {
				throw new RegistrationException("Person record not found for "
						+ email);
			} else {
				Person person = new Person();
				person.loadFromDoc(doc);
				registerInConnections(person);
			}
		} catch (NotesException e) {
			log.error(e);
		} finally {
			JSFUtils.incinerate(doc, viewPeople, dbNab);
		}

	}

	public void registerInConnections(Person person)
			throws RegistrationException {
		try {
			getConService().createProfile(person);
		} catch (Exception e) {
			log.error(e);
			throw new RegistrationException("Unable to register user");
		}
	}

	public void createPersonInNAB(Person person) throws RegistrationException {
		Database dbNab = null;
		View viewPeople = null;
		Document doc = null;
		View viewFullName = null;
		View viewServerAccess = null;
		ViewEntry existingPerson = null;
		Item itemFullname = null;
		try {

			dbNab = getNABdb();
			viewPeople = dbNab.getView("($Users)"); // ($VIMPeople)'
			// make sure the person doesn't already exist
			existingPerson = viewPeople.getEntryByKey(person.getFullName());
			if (null != existingPerson) {
				throw new RegistrationException(
						"Person already exists with name - "
								+ person.getFullName());
			} else {
				log.debug("ok to create");
				doc = dbNab.createDocument();
				doc.replaceItemValue("Form", "Person");
				doc.replaceItemValue("Type", "Person");
				doc.replaceItemValue("FirstName", person.getFirstName());
				doc.replaceItemValue("LastName", person.getLastName());
				doc.replaceItemValue("o", "External");

				itemFullname = doc.replaceItemValue("FullName", person
						.getFullName());
				itemFullname.appendToTextList(person.getCommonName());
				itemFullname.appendToTextList(person.getEmail());

				doc.replaceItemValue("ShortName", person.getCommonName());
				doc.replaceItemValue("InternetAddress", person.getEmail());
				doc.replaceItemValue("MailSystem", "5");
				doc.replaceItemValue("DefaultCommunity", person
						.getCommunityName());
				doc.replaceItemValue("DefaultCommunityGUID", person
						.getCommunityGUID());
				doc
						.replaceItemValue("HTTPPassword", ExtLibUtil
								.getCurrentSession().hashPassword(
										person.getPassword()));
				if (person.getHasDummyPassword()) {
					doc.replaceItemValue("DummyPassword", "Yes");
				}
				doc.computeWithForm(false, false);
				doc.save();

				// load computed fields back in
				person.loadFromDoc(doc);

				/**
				 * 'If person document is succesffully created 'complete
				 * realtime update by refreshing $FullName 'and $ServerAccess
				 * views.
				 **/
				viewFullName = dbNab.getView("($LDAPCN)");
				viewFullName.refresh();
				viewServerAccess = dbNab.getView("($ServerAccess)");
				viewServerAccess.refresh();

				// update FT index for ldap
				dbNab.updateFTIndex(false);

				person.setDocID(doc.getUniversalID());

				log.debug("person added");
			}

		} catch (RegistrationException e) {
			throw e;
		} catch (Exception e) {
			log.error(e);
			throw new RegistrationException("Unable to create NAB record");
		} finally {
			JSFUtils.incinerate(viewServerAccess, viewFullName, doc,
					viewPeople, dbNab, existingPerson);
		}
	}

	public Request createPasswordResetRequest(String email)
			throws RegistrationException {
		// validate arguments
		if (StringUtil.isEmpty(email))
			throw new IllegalArgumentException("Email must not be empty");

		Request request = null;

		// get person doc
		Database dbNab = null;
		View viewPeople = null;
		Document doc = null;
		try {
			dbNab = getNABdb();
			viewPeople = dbNab.getView("($Users)");
			doc = viewPeople.getDocumentByKey(email);
			if (null == doc) {
				throw new RegistrationException("Person record not found for "
						+ email);
			} else {
				// create password reset request
				request = new Request();
				request.setType(Request.REQUESTTYPE_PASSWORDRESET);
				request.setEmail(email);
				request.save();

				// email link to password
				sendRequestEmail(request);

			}
		} catch (NotesException e) {
			log.error(e);
		} finally {
			JSFUtils.incinerate(doc, viewPeople, dbNab);
		}
		return request;
	}

	public void addUserToCommunity(RegistrationRequest request)
			throws RegistrationException {

		try {
			getConService().addMemberToCommunity(request.getCommunityGUID(), request
					.getEmail(), true);
			request.setStatus(Request.REQUESTSTATUS_COMPLETE);
			request.save();
		} catch (Exception e) {
			log.error(e);
			throw new RegistrationException("Unable to add user to community");
		}
	}

	public void completeRegistration(RegistrationRequest request,
			String password) throws RegistrationException {
		// validate arguments
		if (null == request)
			throw new IllegalArgumentException("Request must not be empty");
		if (!request.getIsValid())
			throw new IllegalArgumentException("Request not valid");

		resetPassword(request.getEmail(), password);
		request.setStatus(Request.REQUESTSTATUS_USED);
		request.save();

		// add user to connections
		if (!StringUtil.isEmpty(request.getCommunityGUID())) {
			try {
				getConService().addMemberToCommunity(request.getCommunityGUID(),
						request.getEmail());
			} catch (Exception e) {
				log.error(e);
				throw new RegistrationException(
						"Unable to add user to community");
			}
		}

	}

	public void resetPassword(Request request, String password)
			throws RegistrationException {
		// validate arguments
		if (null == request)
			throw new IllegalArgumentException("Request must not be empty");
		if (!request.getIsValid())
			throw new IllegalArgumentException("Request not valid");

		resetPassword(request.getEmail(), password);
		if (Request.REQUESTTYPE_REGISTRATION.equals(request.getType())) {
			String communityGUID = ((RegistrationRequest) request).getCommunityGUID();
			if (StringUtil.isEmpty(communityGUID)) {
				request.setStatus(Request.REQUESTSTATUS_USED);
			} else {
				//see if we can add them to connections yet...
				if (StringUtil
						.equals(settings.get("registration_addtocommunityimmediately")
								.get("value1"), "true")) {
					// add the person into their default commnity
					try {
						getConService().addMemberToCommunity(communityGUID,
								request.getEmail(), false);
						log.debug("User added to community");
						request.setStatus(Request.REQUESTSTATUS_USED);
					} catch (Exception e) {
						log.error(e);
						request.setStatus(Request.REQUESTSTATUS_PENDING_CONNECTIONS);
					}
				} else {
				request.setStatus(Request.REQUESTSTATUS_PENDING_CONNECTIONS);
				}
			}
		} else {
			request.setStatus(Request.REQUESTSTATUS_USED);
		}

		request.save();
	}

	public Request getRequestByKey(String key) throws RegistrationException {
		// validate state
		if (StringUtil.isEmpty(key))
			throw new IllegalArgumentException("Key must not be empty");

		Request request = null;
		Database database = null;
		Document doc = null;
		try {
			database = ExtLibUtil.getCurrentSessionAsSigner()
					.getCurrentDatabase();

			doc = database.getDocumentByUNID(key);
			if (!StringUtil.equals("Request", doc.getItemValueString("Form"))) {
				throw new RegistrationException("Invalid request");
			}
			String type = doc.getItemValueString("Type");
			if (Request.REQUESTTYPE_REGISTRATION.equals(type)) {
				request = new RegistrationRequest(doc);
			} else if (Request.REQUESTTYPE_PASSWORDRESET.equals(type)) {
				request = new Request(doc);
			} else {
				throw new RegistrationException("Invalid request type");
			}

		} catch (NotesException e) {
			log.error(e);
			throw new RegistrationException("Unable to load request");
		} finally {
			JSFUtils.incinerate(doc);
		}

		return request;

	}

	public void resetPassword(String name, String password)
			throws RegistrationException {
		// validate arguments
		if (StringUtil.isEmpty(name))
			throw new IllegalArgumentException("Name must not be empty");
		if (StringUtil.isEmpty(password))
			throw new IllegalArgumentException("Password must not be empty");

		// revalidate password
		if (!validatePassword(password)) {
			throw new RegistrationException("Password is invalid");
		}
		// get person doc
		Database dbNab = null;
		View viewPeople = null;
		Document doc = null;
		try {
			dbNab = getNABdb();
			viewPeople = dbNab.getView("($Users)");
			doc = viewPeople.getDocumentByKey(name);
			if (null == doc) {
				throw new RegistrationException("Person record not found for "
						+ name);
			} else {
				// update password
				doc.replaceItemValue("HTTPPassword", ExtLibUtil
						.getCurrentSession().hashPassword(password));
				doc.removeItem("DummyPassword");
				doc.save();
			}
		} catch (NotesException e) {
			log.error(e);
		} finally {
			JSFUtils.incinerate(doc, viewPeople, dbNab);
		}
	}

	public static Boolean validatePassword(String password) {
		// get the password validation reg ex
		String pattern = settings.get("validation_password").get("value2")
				.toString();
		if (StringUtil.isEmpty(pattern)) {
			throw new Error("Password validation reg ex setting is missing");
		}
		Boolean rtn = password.matches(pattern);
		return rtn;
	}

	public ConnectionsService getConService() {
		if (null == conService) {
			conService = new ConnectionsService();
		}
		return conService;
	}

	public void setConService(ConnectionsService conService) {
		this.conService = conService;
	}
}
