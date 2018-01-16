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
package de.lottoritter.presentation.player.control;

import de.lottoritter.business.player.control.ResetPasswordController;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.ResourceBundleRepository;
import de.lottoritter.presentation.AbstractViewController;

import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@ViewScoped
@Named
public class ResetPasswordViewController extends AbstractViewController implements Serializable {

    private static final long serialVersionUID = 8879493969641117076L;

    private static final Logger logger = Logger.getLogger(ResetPasswordViewController.class.getName());

    @Inject
    ResetPasswordController resetPasswordController;

    @Inject
    ResourceBundleRepository resourceBundleRepository;

    private Player playerForChangePassword = new Player();

    private String code; // query parameter of the request

    private boolean initFlag; // query parameter of the request

    private boolean initSuccessFlag; // query parameter of the request

    private boolean changeFlag; // query parameter of the request

    private boolean changePasswordSuccessFlag;


    public ResetPasswordViewController() {
    }


    @Produces
    @Named("playerToChangePassword")
    public Player getPlayerForChangePassword() {
        return playerForChangePassword;
    }


    public void startProcess(final String formId, final String cmdId) {
        try {
            if (resetPasswordController.startPasswordChangeProcess(playerForChangePassword)) {
                setInitSuccessFlag(true);
                String sendCodeSuccessMessage = resourceBundleRepository.getDefaultLocalized("user.change_password.send_reset_code", FacesContext.getCurrentInstance().getViewRoot().getLocale());
                FacesContext.getCurrentInstance().addMessage(formId + ":" + cmdId,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, sendCodeSuccessMessage, ""));
            }
        } catch (Exception ex) {
            ConstraintViolationException validationExceptions;
            if (ex instanceof ConstraintViolationException) {
                validationExceptions = (ConstraintViolationException) ex;
            } else {
                validationExceptions = findConstraintViolationException(ex);
            }
            if (validationExceptions != null) {
                handleValidationErrors(validationExceptions, formId);
            } else {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public void checkCode() {
        if (isChangeFlag() && !resetPasswordController.isValidResetPasswordCode(getCode())) {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            try {
                externalContext.redirect(externalContext.getApplicationContextPath() + "/index.xhtml?faces-redirect=true");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public void changePassword(final String formId, final String cmdId) {
        try {
            resetPasswordController.changePassword(getPlayerForChangePassword(), getCode());
            setChangePasswordSuccessFlag(true);
            String changePasswordSuccessMessage = resourceBundleRepository.getDefaultLocalized("user.change_password.successMessage", FacesContext.getCurrentInstance().getViewRoot().getLocale());
            FacesContext.getCurrentInstance().addMessage(formId + ":" + cmdId,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, changePasswordSuccessMessage, ""));
        } catch (Exception ex) {
            ConstraintViolationException validationExceptions;
            if (ex instanceof ConstraintViolationException) {
                validationExceptions = (ConstraintViolationException) ex;
            } else {
                validationExceptions = findConstraintViolationException(ex);
            }
            if (validationExceptions != null) {
                handleValidationErrors(validationExceptions, formId);
            } else {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isInitFlag() {
        return initFlag;
    }

    public void setInitFlag(boolean initFlag) {
        this.initFlag = initFlag;
    }

    public boolean isChangeFlag() {
        return changeFlag;
    }

    public void setChangeFlag(boolean changeFlag) {
        this.changeFlag = changeFlag;
    }

    public boolean isInitSuccessFlag() {
        return initSuccessFlag;
    }

    public void setInitSuccessFlag(boolean initSuccessFlag) {
        this.initSuccessFlag = initSuccessFlag;
    }

    public boolean isChangePasswordSuccessFlag() {
        return changePasswordSuccessFlag;
    }

    public void setChangePasswordSuccessFlag(boolean changePasswordSuccessFlag) {
        this.changePasswordSuccessFlag = changePasswordSuccessFlag;
    }
}
