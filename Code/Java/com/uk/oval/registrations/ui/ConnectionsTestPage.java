package com.uk.oval.registrations.ui;

import java.io.Serializable;

import com.uk.oval.registrations.ConnectionsService;
import com.uk.oval.registrations.Person;
import com.uk.oval.registrations.RegistrationException;
import com.uk.oval.registrations.RegistrationService;
import com.uk.oval.tools.JSFUtils;

public class ConnectionsTestPage implements Serializable {

	private static final long serialVersionUID = 1L;

	private ConnectionsService conService;
	private Person person;

	public ConnectionsTestPage() {
		conService = new ConnectionsService();
		person = new Person();
	}

	public ConnectionsService getConService() {
		return conService;
	}

	public void setConService(ConnectionsService conService) {
		this.conService = conService;
	}

	public Boolean getIsAuthenticated() {
		return conService.getIsAuthenticated();
	}

	public void registerInConnections() {
		try {
			RegistrationService regService = new RegistrationService();
			regService.registerInConnections(person.getEmail());
			JSFUtils.addInfo("Profile created");
		} catch (RegistrationException e) {
			JSFUtils.addValidationError(e.getMessage());
		}
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
}
