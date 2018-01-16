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

import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.payment.control.PaymentLimitsController;
import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.security.control.LoginFailedException;
import de.lottoritter.business.security.control.PasswordService;
import de.lottoritter.platform.ResourceBundleRepository;

import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class LoginViewController extends AbstractLoginViewController implements Serializable {

    private static final long serialVersionUID = 36334755271869730L;

    private static final Logger logger = Logger.getLogger(LoginViewController.class.getName());


    @Inject
    SecurityContext securityContext;

    @Inject
    PasswordService passwordService;

    @Inject
    PaymentLimitsController paymentLimitsController;

    @Inject
    UserRepository userRepository;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    ResourceBundleRepository resourceBundleRepository;


    private Player loginRequest = new Player();


    @Produces
    @Named("userToLogin")
    public Player getUserToLogin() {
        return this.loginRequest;
    }

    public void login(String redirectUrl) {
        try {
            loginRequest.validateLoginFields();
            passwordService.setupPasswordToUser(loginRequest, loginRequest.getPassword());
            Credential credential = new UsernamePasswordCredential(loginRequest.getEmail(), new Password(loginRequest.getPwHash()));
            // this will call the security configuration to authorize the user
            final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            final boolean loginToContinue = Boolean.parseBoolean(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("loginToContinue"));
            AuthenticationStatus status = this.securityContext.authenticate(
                    (HttpServletRequest) externalContext.getRequest(),
                    (HttpServletResponse) externalContext.getResponse(),
                    AuthenticationParameters.withParams()
                            .credential(credential)
                            .newAuthentication(!loginToContinue)
                            .rememberMe(loginRequest.isRememberMe())
            );
            if (status.equals(AuthenticationStatus.SUCCESS)) {
                final Player dbPlayer = userRepository.findUserByEmail(loginRequest.getEmail());
                activityLogController.saveActivityLog(dbPlayer, ActivityType.LOGIN_SUCCESS,
                        "playerId", dbPlayer.getId().toHexString());
                paymentLimitsController.adjustLimitsForPlayer(dbPlayer);
                if (shoppingCartSession.adjustTicketsInShoppingCart() > 0) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.login.postprocess.updateTicketsInShoppingCart", userSession.getLocale())));
                }
                loginPostAction(dbPlayer, redirectUrl,"/");
            } else {
                throw new LoginFailedException(new Locale(loginRequest.getLocaleCode()));
            }
        } catch (LoginFailedException loginFailedEx) {
            handleMessageForComponent(loginFailedEx.getMessage(), "loginForm", "loginCmd");
        } catch (Exception ex) {
            ConstraintViolationException validationExceptions;
            if (ex instanceof ConstraintViolationException) {
                validationExceptions = (ConstraintViolationException) ex;
            } else {
                validationExceptions = findConstraintViolationException(ex);
            }
            if (validationExceptions != null) {
                handleValidationErrors(validationExceptions, "loginForm");
            } else {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public void logout() {
        final Player player = userSession.getPlayer();
        activityLogController.saveActivityLog(player, ActivityType.LOGOUT,
                "playerId", player.getId().toHexString());
        userRepository.removeAllRememberMeTokens(player);
        final ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        try {
            externalContext.invalidateSession();
            externalContext.redirect("/");
        } catch (IllegalStateException | IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
