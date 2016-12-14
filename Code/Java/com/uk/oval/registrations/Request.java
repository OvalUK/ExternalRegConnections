package com.uk.oval.registrations;

import java.io.Serializable;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.uk.oval.tools.JSFUtils;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import eu.linqed.debugtoolbar.DebugToolbar;

public class Request implements Serializable {

	private static final long serialVersionUID = 4394872209015491442L;
	private String noteID;
	private String email;
	protected String type = null;
	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public final static String REQUESTTYPE_PASSWORDRESET = "passwordreset";
	public final static String REQUESTTYPE_REGISTRATION = "registration";
	public final static String REQUESTSTATUS_PENDING = "pending-user";
	public final static String REQUESTSTATUS_PENDING_CONNECTIONS = "pending-connections";
	public final static String REQUESTSTATUS_COMPLETE = "complete";
	public final static String REQUESTSTATUS_USED = "used";

	private static final DebugToolbar log = DebugToolbar.get();

	public Request() {
		status = REQUESTSTATUS_PENDING; // default
	}

	public Request(Document doc) throws RegistrationException {
		loadFromDoc(doc);
	}

	public Boolean getIsValid() {
		return !StringUtil.isEmpty(type)
				&& !StringUtil.equals(status, REQUESTSTATUS_USED);
	}

	public String getNoteID() {
		return noteID;
	}

	public String getKey() {
		return noteID;
	}

	public void setKey(String noteID) {
		this.noteID = noteID;
	}

	public void setNoteID(String noteID) {
		this.noteID = noteID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		String url;
		if (REQUESTTYPE_PASSWORDRESET.equals(type)) {
			url = "/resetPassword.xsp";
		} else {
			url = "/registration.xsp";
		}
		url += "?key=" + noteID;
		return url;
	}

	public void save() throws RegistrationException {
		// validate state
		if (StringUtil.isEmpty(email))
			throw new IllegalArgumentException("Email must not be empty");
		if (StringUtil.isEmpty(type))
			throw new IllegalArgumentException("Type must not be empty");

		Database database = null;
		Document doc = null;
		try {
			database = ExtLibUtil.getCurrentSessionAsSigner()
					.getCurrentDatabase();
			if (StringUtil.isEmpty(noteID)) {
				doc = database.createDocument();
				doc.replaceItemValue("Form", "Request");
			} else {
				doc = database.getDocumentByUNID(noteID);
			}
			doc.replaceItemValue("Email", email);
			doc.replaceItemValue("Type", type);
			doc.replaceItemValue("Status", status);
			saveAdditional(doc);
			doc.save();
			noteID = doc.getUniversalID();

		} catch (NotesException e) {
			log.error(e);
			throw new RegistrationException("Unable to save request");
		} finally {
			JSFUtils.incinerate(doc);
		}
	}

	protected void saveAdditional(Document doc) throws NotesException {
	}

	protected void loadFromDocAdditional(Document doc) throws NotesException {
	}

	public void loadFromDoc(Document doc) throws RegistrationException {
		// validate state
		if (null == doc)
			throw new IllegalArgumentException("Doc must not be empty");

		try {
			noteID = doc.getUniversalID();
			email = doc.getItemValueString("Email");
			type = doc.getItemValueString("Type");
			status = doc.getItemValueString("Status");

			loadFromDocAdditional(doc);

		} catch (NotesException e) {
			log.error(e);
			throw new RegistrationException("Unable to load request");
		}
	}

}
