package com.uk.oval.tools;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;


public class BaseVariableResolver extends VariableResolver {

	private final VariableResolver delegate;

	public BaseVariableResolver(VariableResolver resolver) {
		delegate = resolver;
	}

	@Override
	public Object resolveVariable(FacesContext context, String name)
			throws EvaluationException {
		try {
			if ("dBar".equals(name)) {
				return delegate.resolveVariable(context, "log");
			} else {
				return delegate.resolveVariable(context, name);
			}
		} catch (Exception e) {
			// Add error handling
			return delegate.resolveVariable(context, name);
		}
	}

}