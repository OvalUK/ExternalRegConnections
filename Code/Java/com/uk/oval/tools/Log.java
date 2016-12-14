package com.uk.oval.tools;

import javax.faces.context.FacesContext;

import eu.linqed.debugtoolbar.DebugToolbar;

import static com.ibm.xsp.extlib.util.ExtLibUtil.*;

public final class Log {
	public static DebugToolbar get() {
		return (DebugToolbar) resolveVariable(FacesContext.getCurrentInstance(), "Log");
	}
}
