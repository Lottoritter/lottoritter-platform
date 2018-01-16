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

import de.lottoritter.business.lotteries.Field;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketHistoryWrapper;
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.payment.control.PaymentLimitsController;
import de.lottoritter.business.payment.control.PaymentTransactionAbortedException;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentLimits;
import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.control.UserSession;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.profile.control.ProfileController;
import de.lottoritter.platform.Current;
import de.lottoritter.platform.ResourceBundleRepository;
import de.lottoritter.presentation.AbstractViewController;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */

@Named
@ViewScoped
public class ProfileViewController extends AbstractViewController implements Serializable {

    private static final long serialVersionUID = -5855266122682173822L;

    private static final Logger logger = Logger.getLogger(ProfileViewController.class.getName());

    @Inject
    UserSession userSession;

    @Inject
    UserRepository userRepository;

    @Inject
    ResourceBundleRepository resourceBundleRepository;

    @Inject
    PaymentLimitsController paymentLimitsController;

    @Inject
    ProfileController profileController;

    @Inject @Current
    transient Player player;

    @Inject
    private MailController mailController;

    private ChangePasswordDataHolder changePasswordDataHolder = new ChangePasswordDataHolder();

    private ChangePersonalDataDataHolder changePersonalDataDataHolder = new ChangePersonalDataDataHolder();

    private PaymentLimits newPaymentLimits;

    List<Payment> paymentHistoryList;

    List<Ticket> activeTicketList;

    List<TicketHistoryWrapper> ticketHistoryList;


    public ProfileViewController() {
    }

    @PostConstruct
    private void init() {
        newPaymentLimits = new PaymentLimits();
        newPaymentLimits.setMonthLimitInEuro(player.getPaymentLimits().getMonthLimitInEuro());
        newPaymentLimits.setWeekLimitInEuro(player.getPaymentLimits().getWeekLimitInEuro() != null ? new Integer(player.getPaymentLimits().getWeekLimitInEuro()) : null);
        newPaymentLimits.setDayLimitInEuro(player.getPaymentLimits().getDayLimitInEuro() != null ? new Integer(player.getPaymentLimits().getDayLimitInEuro()) : null);
        fillChangePersonalDataDataHolder();
    }


    public void changePersonalData(String formId) {
        try {
            profileController.changePersonalData(player, changePersonalDataDataHolder.getEmail());
            FacesContext.getCurrentInstance()
                    .addMessage(formId + ":updatePlayerPersonalData",
                            new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.profile.tabs.playerdata.changed_succesful", userSession.getLocale())));
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

    public void resetPersonalData() {
        fillChangePersonalDataDataHolder();
    }

    private void fillChangePersonalDataDataHolder() {
        this.changePersonalDataDataHolder.email = player.getEmail();
    }

    public void changePassword(final String formId) {
        try {
            userRepository.changePassword(player, changePasswordDataHolder.oldPassword,
                    changePasswordDataHolder.newPassword,
                    changePasswordDataHolder.newPasswordAgain);
            FacesContext.getCurrentInstance()
                    .addMessage(formId + ":changePasswordBtn",
                            new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.change_password.successMessage", userSession.getLocale())));
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

    public void changeLimits(final String formId) {
        try {
            final boolean changedPaymentLimits = paymentLimitsController.changePaymentLimits(player, newPaymentLimits);
            if (changedPaymentLimits) {
                FacesContext.getCurrentInstance()
                        .addMessage(formId + ":changeLimitBtn",
                                new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.profile.tabs.limits.changed", userSession.getLocale())));
                mailController.sendLimitsChangedMail(player);
            }
        }  catch (Exception ex) {
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

    public void blockAccount() {
        profileController.blockPlayer(player);
        mailController.sendSelfExclusionConfirmationMail(player);
    }

    public void cancelPermaTicket(MainTicket mainTicket) {
        try {
            profileController.cancelPermaTicket(mainTicket);
            activeTicketList = null;
        } catch (PaymentTransactionAbortedException e) {
            // TODO  IMPORTANT: show popup?
        }
    }

    public List<Payment> getPaymentHistoryList() {
        if (paymentHistoryList == null) {
            paymentHistoryList = profileController.getPaymentHistoryListForPlayer(player);
        }
        return paymentHistoryList;
    }

    public List<Ticket> getActiveTicketList() {
        if (activeTicketList == null) {
                activeTicketList = profileController.getActiveTicketListForPlayer(player);
        }
        return activeTicketList;
    }

    public List<TicketHistoryWrapper> getTicketHistoryList() {
        if (ticketHistoryList == null) {
            ticketHistoryList = profileController.getTicketHistoryForPlayer(player);
        }
        return ticketHistoryList;
    }

    public long getAmountPlacedFields(Ticket ticket) {
        return ticket.getFields().stream().filter(f -> ((Field) f).isValidFilled()).count();
    }

    public ChangePasswordDataHolder getChangePasswordDataHolder() {
        return changePasswordDataHolder;
    }

    public ChangePersonalDataDataHolder getChangePersonalDataDataHolder() {
        return changePersonalDataDataHolder;
    }

    public PaymentLimits getNewPaymentLimits() {
        return newPaymentLimits;
    }

    public String getAccountBalanceForPlayerInCent() {
        return profileController.getAccountBalanceForPlayerFormatted(player);
    }


    public static class ChangePersonalDataDataHolder implements Serializable {
        private static final long serialVersionUID = -123927079286330044L;

        private String email;


        public ChangePersonalDataDataHolder() {
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class ChangePasswordDataHolder implements Serializable {
        private static final long serialVersionUID = -2266274324915037453L;
        private String oldPassword;
        private String newPassword;
        private String newPasswordAgain;

        public ChangePasswordDataHolder() {
        }


        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getNewPasswordAgain() {
            return newPasswordAgain;
        }

        public void setNewPasswordAgain(String newPasswordAgain) {
            this.newPasswordAgain = newPasswordAgain;
        }
    }

}

