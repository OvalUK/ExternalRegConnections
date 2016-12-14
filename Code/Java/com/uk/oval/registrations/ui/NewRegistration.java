package com.uk.oval.registrations.ui;

import java.io.Serializable;
import java.util.ArrayList;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.StringUtil;
import com.ibm.sbt.services.client.connections.communities.Community;
import com.ibm.xsp.designer.context.XSPContext;
import com.uk.oval.registrations.ConnectionsService;
import com.uk.oval.registrations.Person;
import com.uk.oval.registrations.RegistrationService;
import com.uk.oval.registrations.Request;
import com.uk.oval.tools.JSFUtils;

import eu.linqed.debugtoolbar.DebugToolbar;

public class NewRegistration implements Serializable {

	private static final DebugToolbar log = DebugToolbar.get();

	private static final long serialVersionUID = -463980669581763331L;
	private RegistrationService regService;
	private ConnectionsService conService;
	private Person person;
	private Request request;
	private String communityNameSearch;
	private ArrayList<Community> communities;
	private String communityGUID;
	private Boolean addToCommunity = false;
	private Community community = null;

	public ArrayList<Community> getCommunities() {
		return communities;
	}

	public void setCommunities(ArrayList<Community> communities) {
		this.communities = communities;
	}

	public void findCommunities() {

		if (StringUtil.isEmpty(communityNameSearch)) {
			JSFUtils.addValidationError("Please enter a community name");
		} else {
			try {
				communities = new ArrayList<Community>(getConService()
						.findCommunitiesByName(communityNameSearch));
			} catch (Exception e) {
				JSFUtils.addValidationError("Unable to find commmunities");
				log.error(e);
			}
		}
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public NewRegistration() {
		request = new Request();
		person = new Person();
	}

	public Boolean isExternal(Community community) {
		return ConnectionsService.isExternal(community);
	}

	public void registerUser(Person person) {
		boolean ok = true;
		try {
			// validation
			if (addToCommunity && StringUtil.isEmpty(communityGUID)) {
				JSFUtils.addValidationError("Please select a community");
			} else {
				// double check community
				if (addToCommunity) {
					community = getConService().getCommunity(communityGUID);
					if (!ConnectionsService.isExternal(community)) {
						JSFUtils
								.addValidationError("Selected community does not allow external access");
						ok = false;
					} else {
						person.setCommunityGUID(communityGUID);
						person.setCommunityName(community.getTitle());
					}
				}
				if (ok) {
					getRegService().registerUser(person);
					FacesContext facesContext = FacesContext
							.getCurrentInstance();
					XSPContext context = XSPContext.getXSPContext(facesContext);
					context
							.redirectToPage(
									"/message.xsp?msg=message_newregistrationsubmitted",
									false);
				}
			}
		} catch (Exception e) {
			JSFUtils.addValidationError(e.getMessage());
		}
	}

	public String getCommunityNameSearch() {
		return communityNameSearch;
	}

	public void setCommunityNameSearch(String communityNameSearch) {
		this.communityNameSearch = communityNameSearch;
	}

	public String getCommunityGUID() {
		return communityGUID;
	}

	public void setCommunityGUID(String communityGUID) {
		this.communityGUID = communityGUID;
	}

	public Boolean getAddToCommunity() {
		return addToCommunity;
	}

	public void setAddToCommunity(Boolean addToCommunity) {
		this.addToCommunity = addToCommunity;
	}

	public Community getCommunity() {
		return community;
	}

	public void setCommunity(Community community) {
		this.community = community;
	}

	public RegistrationService getRegService() {
		if (null == regService) {
			regService = new RegistrationService();
		}
		return regService;
	}

	public void setRegService(RegistrationService regService) {
		this.regService = regService;
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
