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
package de.lottoritter.presentation.shoppingcart.control;

import de.lottoritter.business.lotteries.Field;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.payment.control.BankController;
import de.lottoritter.business.payment.control.PaymentControllerProxy;
import de.lottoritter.business.payment.control.PaymentLimitsController;
import de.lottoritter.business.payment.control.PaymentTransactionAbortedException;
import de.lottoritter.business.payment.entity.PaymentServiceProvider;
import de.lottoritter.business.payment.entity.PaymentType;
import de.lottoritter.business.payment.entity.PlayerPayment;
import de.lottoritter.business.player.control.UserSession;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;
import de.lottoritter.platform.Current;
import de.lottoritter.platform.ResourceBundleRepository;
import de.lottoritter.presentation.AbstractViewController;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class ShoppingCartViewController extends AbstractViewController implements Serializable {

    private static final long serialVersionUID = -3895671483826532982L;

    @Inject
    UserSession userSession;

    @Inject
    @Current
    Player currentPlayer;

    @Inject
    PaymentControllerProxy paymentControllerProxy;

    @Inject
    ShoppingCartSession shoppingCartSession;

    @Inject
    BankController bankController;

    @Inject
    PaymentLimitsController paymentLimitsController;

    @Inject
    ResourceBundleRepository resourceBundleRepository;

    private String paymillToken;

    private String paymillTokenSepa;

    // [a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]{7}([a-zA-Z0-9]?){0,16}
    private String ibanString;

    private String bicString;

    private boolean isCreateNewPaymentSelected;


    public ShoppingCartViewController() {
    }

    @PostConstruct
    private void init() {
        isCreateNewPaymentSelected = true;
        if (currentPlayer != null) {
            PaymentServiceProvider paymill = currentPlayer.getPaymentServiceProviderWithCode("PAYMILL");
            if (paymill == null) {
                return;
            }
            isCreateNewPaymentSelected = paymill.getPlayerPaymentList().isEmpty();
        }
    }


    public void removeTicket(MainTicket ticket) {
        shoppingCartSession.removeTicket(ticket);
    }

    @SuppressWarnings("unused")
    public void fillBIC(AjaxBehaviorEvent event) {
        setBicString(bankController.getBICCodeFromIBAN(getIbanString()));
    }

    public String submitNewPlayerPayment(final String formId, String cmdButtonId, String paymentTypeString) {
        try {
            paymentControllerProxy.getSynchronizer().checkForNewSubscriptionTransactions(currentPlayer);
            paymentLimitsController.checkPaymentLimit(currentPlayer, shoppingCartSession.getCurrentShoppingCart(), userSession.getLocale());
            paymentControllerProxy.getController().processNewPlayerPayment(currentPlayer, getPaymillToken(), PaymentType.valueOf(paymentTypeString));
            return "/sec/goodluck.xhtml?faces-redirect=true";
        } catch (PaymentTransactionAbortedException e) {
            FacesContext.getCurrentInstance().addMessage(formId + ":" + cmdButtonId,
                    new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.shoppingcart.payment.failed", userSession.getLocale())));
        } catch (EJBException ex) {
            Exception causedByEx = ex.getCausedByException();
            if (causedByEx instanceof PaymentTransactionAbortedException) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.shoppingcart.payment.failed", userSession.getLocale())));
            } else if (causedByEx instanceof RuntimeException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(causedByEx.getMessage()));
            }
        }
        return null;
    }


    public String submitExistingPlayerPayment(final String formId, String cmdButtonId) {
        try {
            PlayerPayment currentPlayerPayment = getSelectedPlayerPaymentFromRequest(formId);
            paymentControllerProxy.getSynchronizer().checkForNewSubscriptionTransactions(currentPlayer);
            paymentLimitsController.checkPaymentLimit(currentPlayer, shoppingCartSession.getCurrentShoppingCart(), userSession.getLocale());
            paymentControllerProxy.getController().processExistingPlayerPayment(currentPlayer, currentPlayerPayment);
            return "/sec/goodluck.xhtml?faces-redirect=true";
        } catch (PaymentTransactionAbortedException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.shoppingcart.payment.failed", userSession.getLocale())));
        } catch (EJBException ex) {
            Exception causedByEx = ex.getCausedByException();
            if (causedByEx instanceof PaymentTransactionAbortedException) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(resourceBundleRepository.getDefaultLocalized("user.shoppingcart.payment.failed", userSession.getLocale())));
            } else if (causedByEx instanceof RuntimeException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(causedByEx.getMessage()));
            }
        }
        return null;
    }

    private PlayerPayment getSelectedPlayerPaymentFromRequest(String formId) {
        String externalIdFromRequest = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(formId + ":selectedPaymentMethod");
        Optional<PlayerPayment> optionalPlayerPayment = currentPlayer.getPaymentServiceProviderWithCode(getPrimaryPSP()).getPlayerPaymentList().stream().filter(p -> p.getExternalId().equals(externalIdFromRequest)).findFirst();
        if (!optionalPlayerPayment.isPresent()) {
            throw new IllegalStateException("PlayerPayment does not exist.");
        }
        return optionalPlayerPayment.get();
    }

    public boolean isPaymillTokenExists() {
        return StringUtils.isNotBlank(paymillToken);
    }

    public boolean isPaymentInLimitForPlayer() {
        try {
            paymentLimitsController.checkPaymentLimit(currentPlayer, shoppingCartSession.getCurrentShoppingCart(), userSession.getLocale());
            return true;
        } catch (EJBException ejb) {
            final Exception causedByException = ejb.getCausedByException();
            if (causedByException instanceof RuntimeException) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(causedByException.getMessage()));
            }
            return false;
        }
    }



    public int getTotalPriceInCent() {
        return shoppingCartSession.getTotalPrice().getAmountInCent();
    }

    public String getTotalPriceCurrency() {
        return shoppingCartSession.getTotalPrice().getCurrency().getCurrencyCode();
    }

    public String getTotalPriceFormatted() {
        return shoppingCartSession.getTotalPriceFormatted();
    }

    public String getPriceFormatted(Price price) {
        return shoppingCartSession.getPriceFormatted(price);
    }

    public int getCountTickets() {
        return shoppingCartSession.getTicketList().size();
    }

    public String getIbanString() {
        return ibanString;
    }

    public void setIbanString(String ibanString) {
        this.ibanString = ibanString;
    }

    public String getPaymillToken() {
        return paymillToken;
    }

    public void setPaymillToken(String paymillToken) {
        this.paymillToken = paymillToken;
    }

    public String getPaymillTokenSepa() {
        return paymillTokenSepa;
    }

    public void setPaymillTokenSepa(String paymillTokenSepa) {
        this.paymillTokenSepa = paymillTokenSepa;
    }

    public String getBicString() {
        return bicString;
    }

    public void setBicString(String bicString) {
        this.bicString = bicString;
    }

    public List<MainTicket> getTickets() {
        return shoppingCartSession.getCurrentShoppingCart().getTicketList();
    }

    public long getAmountPlacedFields(Ticket ticket) {
        return ticket.getFields().stream().filter(f -> ((Field) f).isValidFilled()).count();
    }

    public String getStartDate(MainTicket ticket) {
        return ticket.getStartingDate().format(DateTimeFormatter.ofPattern("dd.MM"));
    }

    public void toggleAdditionalLottery(MainTicket ticket, String additionalLottery) {
        shoppingCartSession.toggleAdditionalLottery(ticket, additionalLottery);
    }

    public boolean isCreateNewPaymentSelected() {
        return isCreateNewPaymentSelected;
    }

    public void selectUseExistingPaymentTab() {
        isCreateNewPaymentSelected = false;
    }

    public void selectCreateNewPaymentTab() {
        isCreateNewPaymentSelected = true;
    }

    public String getPrimaryPSP() {
        return paymentControllerProxy.getController().getPspCode().name();
    }
}
