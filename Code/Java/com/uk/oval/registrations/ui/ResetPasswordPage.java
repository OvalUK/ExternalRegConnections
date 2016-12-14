package com.uk.oval.registrations.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lotus.domino.NotesException;

import com.ibm.xsp.designer.context.XSPContext;
import com.uk.oval.registrations.Person;
import com.uk.oval.registrations.RegistrationException;
import com.uk.oval.registrations.RegistrationService;
import com.uk.oval.registrations.Request;
import com.uk.oval.tools.JSFUtils;
import com.uk.oval.tools.Settings;

public class ResetPasswordPage implements Serializable {

	private static final long serialVersionUID = 7626058658193614461L;

	private RegistrationService regService;
	private Request request;
	private Person person;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public ResetPasswordPage() {
		regService = new RegistrationService();
		request = null;
		person = new Person();
	}

	@SuppressWarnings("unchecked")
	public void beforePageLoad() throws IOException, NotesException {
		// see if the &key= param was used
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		Map<String, String> paramValues = externalContext
				.getRequestParameterMap();

		if (paramValues.containsKey("key")) {
			String key = paramValues.get("key").toString();
			try {
				request = regService.getRequestByKey(key);
				person.setEmail(request.getEmail());
			} catch (RegistrationException e) {
				JSFUtils.addValidationError("Key not valid");
				request = new Request();
			}
		} else {
			request = new Request();
		}
	}

	public void createPasswordResetRequest() {
		try {
			regService.createPasswordResetRequest(person.getEmail());
			FacesContext facesContext = FacesContext.getCurrentInstance();
			XSPContext context = XSPContext.getXSPContext(facesContext);
			context.redirectToPage(
					"/message.xsp?msg=message_resetrequestsubmitted", false);
		} catch (RegistrationException e) {
			JSFUtils.addValidationError(e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public String resetPassword() {
		try {
			regService.resetPassword(request, person.getPassword());

			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			Map<Object, Object> sessionScope = externalContext.getSessionMap();

			String url = Settings.getBean("securesettings").get(
					"connections_login_url").get("value1").toString();
			sessionScope.put("url", url);

			XSPContext context = XSPContext.getXSPContext(facesContext);
			context.redirectToPage("/message.xsp?msg=message_passwordreset",
					false);
			return "";
		} catch (RegistrationException e) {
			JSFUtils.addValidationError(e.getMessage());
			return "";
		}
	}
}
