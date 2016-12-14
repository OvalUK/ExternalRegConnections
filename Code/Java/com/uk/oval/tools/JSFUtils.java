package com.uk.oval.tools;

import java.lang.reflect.Method;
import java.util.List;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage; //import javax.faces.component.UIComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.extlib.component.dialog.UIDialog;

import lotus.domino.Base;
import lotus.domino.NotesException;
import lotus.notes.NotesBase;

public class JSFUtils {
	// custom
	public static void showDialog(String dialogID) {
		((UIDialog) findComponent(dialogID)).show();
	}

	public static void hideDialog(String dialogID) {
		((UIDialog) findComponent(dialogID)).hide();
	}

	/**
	 * The method creates a {@link javax.faces.el.ValueBinding} from the
	 * specified value binding expression and returns its current value.<br>
	 * <br>
	 * If the expression references a managed bean or its properties and the
	 * bean has not been created yet, it gets created by the JSF runtime.
	 * 
	 * @param ref
	 *            value binding expression, e.g. #{Bean1.property}
	 * @return value of ValueBinding throws
	 *         javax.faces.el.ReferenceSyntaxException if the specified
	 *         <code>ref</code> has invalid syntax
	 */
	public static void addValidationError(String[] controlIDs, String msg) {
		addValidationError(msg);
		for (int i = 0; i < controlIDs.length; i++) {
			UIInput component = (UIInput) JSFUtils.findComponent(controlIDs[i]);
			component.setValid(false);
		}
	}

	public static void addValidationError(String controlID, String msg) {
		addValidationError(msg);
		if (null != controlID) {
			UIInput component = (UIInput) JSFUtils.findComponent(controlID);
			// String id = component.getClientId(facesContext);
			component.setValid(false);
		}
	}
	
	public static void addValidationError(String msg) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, new javax.faces.application.FacesMessage(
				FacesMessage.SEVERITY_ERROR, msg, null));
	}
	public static void addInfo(String msg) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		facesContext.addMessage(null, new javax.faces.application.FacesMessage(
				FacesMessage.SEVERITY_INFO, msg, null));
	}

	public static Object getBindingValue(String ref) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		return application.createValueBinding(ref).getValue(context);
	}

	/**
	 * The method creates a {@link javax.faces.el.ValueBinding} from the
	 * specified value binding expression and sets a new value for it.<br>
	 * <br>
	 * If the expression references a managed bean and the bean has not been
	 * created yet, it gets created by the JSF runtime.
	 * 
	 * @param ref
	 *            value binding expression, e.g. #{Bean1.property}
	 * @param newObject
	 *            new value for the ValueBinding throws
	 *            javax.faces.el.ReferenceSyntaxException if the specified
	 *            <code>ref</code> has invalid syntax
	 */
	public static void setBindingValue(String ref, Object newObject) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		ValueBinding binding = application.createValueBinding(ref);
		binding.setValue(context, newObject);
	}

	/**
	 * The method returns the value of a global JavaScript variable.
	 * 
	 * @param varName
	 *            variable name
	 * @return value
	 * @throws javax.faces.el.EvaluationException
	 *             if an exception is thrown while resolving the variable name
	 */
	public static Object getVariableValue(String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(
				context, varName);
	}

	/**
	 * Finds an UIComponent by its component identifier in the current component
	 * tree.
	 * 
	 * @param compId
	 *            the component identifier to search for
	 * @return found UIComponent or null
	 * 
	 * @throws NullPointerException
	 *             if <code>compId</code> is null
	 */

	public static UIComponent findComponent(String compId) {
		return findComponent(FacesContext.getCurrentInstance().getViewRoot(),
				compId);
	}

	/**
	 * Finds an UIComponent by its component identifier in the component tree
	 * below the specified <code>topComponent</code> top component.
	 * 
	 * @param topComponent
	 *            first component to be checked
	 * @param compId
	 *            the component identifier to search for
	 * @return found UIComponent or null
	 * 
	 * @throws NullPointerException
	 *             if <code>compId</code> is null
	 */

	@SuppressWarnings("unchecked")
	public static UIComponent findComponent(UIComponent topComponent,
			String compId) {
		if (compId == null)
			throw new NullPointerException(
					"Component identifier cannot be null");

		if (compId.equals(topComponent.getId()))
			return topComponent;

		if (topComponent.getChildCount() > 0) {
			List<UIComponent> childComponents = topComponent.getChildren();

			for (UIComponent currChildComponent : childComponents) {
				UIComponent foundComponent = findComponent(currChildComponent,
						compId);
				if (foundComponent != null)
					return foundComponent;
			}
		}
		return null;
	}
	
	
	

	public static void incinerate(Object... dominoObjects) {
		for (Object dominoObject : dominoObjects) {
			if (null != dominoObject) {
				if (dominoObject instanceof Base) {
					try {
						((Base) dominoObject).recycle();
					} catch (NotesException recycleSucks) {
						// optionally log exception
					}
				}
			}
		}
	}

	// Via
	// http://stackoverflow.com/questions/12740889/what-is-the-least-expensive-way-to-test-if-a-view-has-been-recycled
	public static boolean isRecycled(final Base object) {
		if (!(object instanceof NotesBase)) {
			// No reason to test non-NotesBase objects -> isRecycled = true
			return true;
		}

		try {
			NotesBase notesObject = (NotesBase) object;
			Method isDead = notesObject.getClass().getSuperclass()
					.getDeclaredMethod("isDead");
			isDead.setAccessible(true);

			return (Boolean) isDead.invoke(notesObject);
		} catch (Throwable exception) {
		}

		return true;
	}
}