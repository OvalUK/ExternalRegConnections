package com.uk.oval.registrations.ui;

import java.io.Serializable;

import com.ibm.sbt.services.client.connections.communities.Community;
import com.uk.oval.registrations.Person;
import com.uk.oval.registrations.RegistrationException;
import com.uk.oval.registrations.RegistrationService;
import com.uk.oval.registrations.Request;
import com.uk.oval.tools.JSFUtils;

public class TestRegistrations implements Serializable {

	private static final long serialVersionUID = 7626058658193614461L;

	private RegistrationService regService;
	private Person person;
	private Request request;
	private Boolean addToCommunity = false;
	private Community community = null;
	
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

	public TestRegistrations() {
		regService = new RegistrationService();
		person = new Person();
		request = new Request();
	}
	
	public void registerUser(Person person) {
		try {
			regService.createPersonInNAB(person);
			JSFUtils.addInfo("Person created");
		} catch (RegistrationException e) {
			JSFUtils.addValidationError("inFirstName", e.getMessage());
		}
	}
	
	public void resetPassword(Person person) {
		try {
			regService.resetPassword(person.getEmail(), person.getPassword());
			JSFUtils.addInfo("Password reset");
		} catch (RegistrationException e) {
			JSFUtils.addValidationError(e.getMessage());
		}
	}
	
	public void createPasswordResetRequest() {
		try {
			Request request = regService.createPasswordResetRequest(person.getEmail());
			JSFUtils.addInfo("Request created: " + request.getNoteID());
		} catch (RegistrationException e) {
			JSFUtils.addValidationError(e.getMessage());
		}
	}
	
	public void getRequest(Request request) {
		try {
			this.request = regService.getRequestByKey(request.getKey());
		} catch (RegistrationException e) {
			JSFUtils.addValidationError(e.getMessage());
		}
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
	
	
}
