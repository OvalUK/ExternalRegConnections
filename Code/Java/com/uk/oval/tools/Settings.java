package com.uk.oval.tools;

import static com.ibm.xsp.extlib.util.ExtLibUtil.resolveVariable;

import java.io.Serializable;
import java.util.HashMap;

import javax.faces.context.FacesContext;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.Session;
import lotus.domino.View;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

///import eu.linqed.debugtoolbar.DebugToolbar;

@SuppressWarnings("unchecked")
public class Settings extends HashMap implements Serializable {
	private static final long serialVersionUID = 4080691140514119052L;

	// private static final DebugToolbar log = DebugToolbar.get();
	// can't use the log here

	// use session as signer?
	private Boolean secure = false;

	public Boolean getSecure() {
		return secure;
	}

	public void setSecure(Boolean secure) {
		this.secure = secure;
	}

	// -----------------------------
	// allow the bean instance to be called from other java classes
	public static Settings getBean() {
		return getBean("settings");
	}
	public static Settings getBean(String name) {
		return (Settings) resolveVariable(FacesContext.getCurrentInstance(),
				name);
	}

	public String getLogLevel() {
		return "debug";
	}

	// --------------------------------

	@Override
	public Keyword get(Object key) {
		/*
		 * if ("logLevel".equals(key)) { Keyword tmp = new Keyword();
		 * tmp.put("value1", "debug"); return tmp; }
		 */
		// Check if we have they key
		Keyword keyword = (Keyword) super.get(key);
		if (null == keyword) {
			// log.debug(key + " not in cache: " + keyword, "Settings.get");
			keyword = getKeywordValue((String) key);
			put(key, keyword);
		} else {
			// log.debug(key + " in cache: " + keyword, "Settings.get");
		}
		return keyword;
	}

	private Keyword getKeywordValue(String key) {

		Session session = null;
		Keyword keyword = new Keyword(key);
		Database database = null;
		View view = null;
		Document doc = null;
		try {
			if (secure) {
				session= ExtLibUtil.getCurrentSessionAsSigner();
			} else {
				session = ExtLibUtil.getCurrentSession();
			}
			session.setConvertMime(false);
			database = session.getCurrentDatabase();
			view = database.getView("Settings");
			doc = view.getDocumentByKey(key, true);

			// ignore docs with readers fields if secure - even if the current
			// user is an admin user with access
			if (null != doc
					&& (StringUtil.isEmpty(doc.getItemValueString("Readers")) || secure)) {

				MIMEEntity mime = doc.getMIMEEntity("ValueRT");
				if (mime != null) {
					keyword.put("valueRT", mime.getContentAsText());
				}
				// Store required properties.
				keyword.put("value1", doc.getItemValueString("Value1"));
				keyword.put("value1MV", doc.getItemValue("Value1"));
				keyword.put("value2", doc.getItemValueString("Value2"));
				keyword.put("value2MV", doc.getItemValue("Value2"));
				keyword.put("value3", doc.getItemValueString("Value3"));
				// set properties for path and server for db keywords
				if (key.startsWith("db")) {
					keyword.put("path", keyword.get("value1"));
					keyword.put("server", keyword.get("value2"));
				}
			} else {
				// log.warn(key + " not found", "Settings.get");
				keyword.put("value1", "");
				keyword.put("value1MV", "");
				keyword.put("value2", "");
				keyword.put("value2MV", "");
				keyword.put("value3", "");
			}
			session.setConvertMime(true);
		} catch (Exception e) {
			// log.error(e);
			e.printStackTrace();
		} finally {
			JSFUtils.incinerate(doc, view);
		}
		return keyword;
	}
}
