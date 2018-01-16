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

import de.lottoritter.business.player.control.RegistrationService;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.ResourceBundleRepository;
import org.bson.types.ObjectId;

import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class RegistrationViewController extends AbstractLoginViewController implements Serializable {

    private static final long serialVersionUID = 8580788281318276923L;

    private static final Logger logger = Logger.getLogger(RegistrationViewController.class.getName());

    public static final String OAUTH_EMAIL = "oauthEmail";

    public static final String OAUTH_PLAYER = "oauthPlayer";

    public static final String OAUTH_ERROR = "oauthError";

    public static final String OAUTH_PROVIDER = "oauthProvider";


    @Inject
    ResourceBundleRepository resourceBundleRepository;

    @Inject
    RegistrationService registrationService;


    private Player playerToRegister;

    private boolean oauthRegistration;


    public RegistrationViewController() {
        this.playerToRegister = new Player();
    }


    @Produces
    @Named("playerToRegister")
    public Player getPlayerToRegister() {
        return playerToRegister;
    }

    public void prefillRegistrationDataFromOAuth(ComponentSystemEvent componentSystemEvent) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        final ExternalContext externalContext = facesContext.getExternalContext();
        final HttpSession session = (HttpSession) externalContext.getSession(false);
        Object r = ((HttpServletRequest) facesContext.getExternalContext().getRequest()).getParameter("r");
        if (r != null) {
            session.removeAttribute(RegistrationViewController.OAUTH_PLAYER);
        }
        Object oauthError = session.getAttribute(OAUTH_ERROR);
        if (oauthError != null && oauthError instanceof Boolean && (Boolean) oauthError) {
            Object oauthProvider = session.getAttribute(OAUTH_PROVIDER);
            session.removeAttribute(RegistrationViewController.OAUTH_ERROR);
            String errorMessage = resourceBundleRepository.getDefaultLocalized("registration.oauth.failed", facesContext.getViewRoot().getLocale(), oauthProvider);
            facesContext.addMessage("registrationForm:registerCmd", new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, ""));
        }
        Object oauthPlayer = session.getAttribute(OAUTH_PLAYER);
        if (oauthPlayer != null) {
            this.playerToRegister = ((Player) oauthPlayer);
            String dummyPw = ObjectId.get().toHexString();
            getPlayerToRegister().setPassword(dummyPw);
            getPlayerToRegister().setPasswordAgain(dummyPw);
            oauthRegistration = true;
        }
    }

    public String register(final Player playerToRegister, String redirectUrl) {
        final String ipAddress = httpServletRequestService.getIpAddressFromRequest();
        playerToRegister.setRegistrationIpAddress(ipAddress);
        try {
            Player player = registrationService.register(playerToRegister);
            loginPostAction(player, redirectUrl, "regsuccess.xhtml?faces-redirect=true");
        } catch (Exception ex) {
            ConstraintViolationException validationExceptions;
            if (ex instanceof ConstraintViolationException) {
                validationExceptions = (ConstraintViolationException) ex;
            } else {
                validationExceptions = findConstraintViolationException(ex);
            }
            if (validationExceptions != null) {
                handleValidationErrors(validationExceptions, "registrationForm");
            } else {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return null;
    }

    public boolean isOauthRegistration() {
        return oauthRegistration;
    }

}
