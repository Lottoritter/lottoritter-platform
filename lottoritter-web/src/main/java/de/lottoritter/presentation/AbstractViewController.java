/*
 * Copyright 2017 Ulrich Cech & Christopher Schmidt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.lottoritter.presentation;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ulrich Cech
 */
public class AbstractViewController {

    protected ConstraintViolationException findConstraintViolationException(Throwable ex) {
        if (ex != null) {
            if (ex instanceof ConstraintViolationException) {
                return (ConstraintViolationException) ex;
            } else {
                return findConstraintViolationException(ex.getCause());
            }
        } else {
            return null;
        }
    }

    protected void handleValidationErrors(final ConstraintViolationException validationExceptions, final String formId) {
        for (ConstraintViolation<?> violation : validationExceptions.getConstraintViolations()) {
            UIComponent form = FacesContext.getCurrentInstance().getViewRoot().findComponent(formId);
            if (form != null) {
                // replace dots with underscore, because JSF-IDs cannot contain dots and BeanValidation returns
                //   for example 'address.street' as getPropertyPath()
                final String path = violation.getPropertyPath().toString().replaceAll("\\.", "\\_");
                FacesContext.getCurrentInstance().addMessage(
                        form.getClientId() + ":" + checkPathForReplacement(path),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, violation.getMessage(), null));
            }
        }
    }

    protected void handleMessageForComponent(final String errorMessage, final String formId, final String componentId) {
        UIComponent form = FacesContext.getCurrentInstance().getViewRoot().findComponent(formId);
        if (form != null) {
            FacesContext.getCurrentInstance().addMessage(
                    form.getClientId() + ":" + componentId,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, null));
        }
    }

    public void removeMessage(AjaxBehaviorEvent event) {
        if (event.getSource() instanceof UIComponent) {
            UIComponent component = (UIComponent) event.getSource();
            Iterator<FacesMessage> messageIterator =
                    FacesContext.getCurrentInstance().getMessages(component.getClientId());
            while (messageIterator.hasNext()) {
                messageIterator.remove();
            }
        }
    }

    public boolean isValidComponent(String clientId) {
        final List<FacesMessage> messageList = FacesContext.getCurrentInstance().getMessageList();
        return (messageList == null) || messageList.isEmpty() || FacesContext.getCurrentInstance().getMessageList(clientId).isEmpty();
    }

    private String checkPathForReplacement(final String path) {
        if ("birthdateAsString".equals(path)) {
            return "birthdate";
        }
        return path;
    }

}
