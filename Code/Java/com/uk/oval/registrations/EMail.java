package com.uk.oval.registrations;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.View;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.uk.oval.tools.JSFUtils;
import com.uk.oval.tools.Keyword;
import com.uk.oval.tools.Settings;

import eu.linqed.debugtoolbar.DebugToolbar;

public class EMail extends com.ibm.xsp.utils.EMail {

	private static final DebugToolbar log = DebugToolbar.get();
	private static final Settings settings = Settings.getBean("securesettings");

	private String url = "";

	public final static String EMAILTYPE_PASSWORDRESET_CONTINUE = "passwordreset_continue";
	public final static String EMAILTYPE_REGISTRATION_CONTINUE = "registration_continue";


	public static String getUserEmail(String userName) {
		String result = null;
		Database nabDB = null;
		View nabView = null;
		Document nabDoc = null;
		try {

			nabDB = ExtLibUtil.getCurrentSessionAsSigner().getDatabase(
					ExtLibUtil.getCurrentDatabase().getServer(), "names.nsf",
					false);
			nabView = nabDB.getView("($Users)");
			nabDoc = nabView.getDocumentByKey(userName);
			result = nabDoc.getItemValueString("InternetAddress");
		} catch (Exception e) {
			log.error(e);
		} finally {
			JSFUtils.incinerate(nabDB, nabView, nabDoc);
		}
		return result;
	}

	public EMail(final String emailType, Request request) {
		super();
		this.setSaveEmail(getSaveEmails());

		this.url = request.getUrl();

		Keyword emailConfig = (Keyword) settings.get("email_" + emailType);
		if (null == emailConfig) {
			throw new Error("Email config not available for " + emailType);
		} else {

			this.setSubject(emailConfig.get("value1").toString());
			this.setSenderEmail(getDefaultSenderEmail());
			this.setSenderName(getDefaultSenderName());
			this.setContentHTML(emailConfig.get("valueRT").toString());
			this.setContentHTML("<p>" + getLinkHTML() + "</p>");
			this.setSaveEmail(getSaveEmails());
		}

	}

	@Override
	public void send() throws RegistrationException {
		try {
			super.send();
			log.debug("Email sent");
		} catch (Exception e) {
			log.error(e);
			throw new RegistrationException("Email not sent");
		}
	}

	private String getLinkHTML() {
		HttpServletRequest req = (HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest();

		// domain + path + link to doc
		String urlFull = req.getScheme() + "://" + req.getServerName()
				+ req.getContextPath() + url;

		return "<a href=\"" + urlFull + "\">Continue</a>";
	}

	public static Boolean getSaveEmails() {
		Keyword res = (Keyword) settings.get("email_save");
		if (null == res) {
			return false;
		} else {
			return res.get("value1").toString().equals("true");
		}
	}

	public static String getDefaultSenderEmail() {
		return settings.get("email_from").get("value2").toString();
	}

	public static String getDefaultSenderName() {
		return settings.get("email_from").get("value1").toString();
	}
}
