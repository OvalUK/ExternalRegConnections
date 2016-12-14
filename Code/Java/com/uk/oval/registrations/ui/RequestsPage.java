package com.uk.oval.registrations.ui;

import java.io.Serializable;

import com.uk.oval.registrations.RegistrationException;
import com.uk.oval.registrations.RegistrationRequest;
import com.uk.oval.registrations.RegistrationService;
import com.uk.oval.registrations.Request;
import com.uk.oval.tools.JSFUtils;

import eu.linqed.debugtoolbar.DebugToolbar;

public class RequestsPage implements Serializable{

	private static final DebugToolbar log = DebugToolbar.get();
	
	private static final long serialVersionUID = -2578274892535669135L;
	private RegistrationService regService;
	
	public RequestsPage() {
		regService = new RegistrationService();
	}
	
	public void resendMail(String docID) {
		Request request;
		try {
			request = regService.getRequestByKey(docID);
			regService.sendRequestEmail(request);
			JSFUtils.addInfo("Email sent");
		} catch (RegistrationException e) {
			JSFUtils.addValidationError("Email not sent");
			log.error(e);
		}
	}
	
	public void addUserToCommunity(String docID) {
		try {
			RegistrationRequest request = (RegistrationRequest)regService.getRequestByKey(docID);
			regService.addUserToCommunity(request);
			JSFUtils.addInfo("User added");
		} catch (Exception e) {
			JSFUtils.addValidationError("User not added");
			log.error(e);
		}
	}
}
