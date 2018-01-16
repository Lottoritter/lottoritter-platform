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
package de.lottoritter.business.payment.entity;

import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import de.lottoritter.platform.ResourceBundleRepository;
import de.lottoritter.platform.cdi.CDIBeanService;
import de.lottoritter.platform.persistence.PersistentEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Entity(value = "payments", noClassnameStored = true)
public class Payment extends PersistentEntity {

    private static final long serialVersionUID = -3631121097430238064L;

    private ObjectId playerId;

    List<MainTicket> ticketList;

    private PspCode psp;

    private PaymentType paymentType;

    private CardType cardType;

    private int amountInCent; // value of the shoppingcart in Euro-Cent, 420 = 4,20€

    private String currency = "EUR";  // currently, only EUR is supported

    private ZonedDateTime payedAt;

    private PaymentState state;

    private String paymentToken;

    @ZonedDateTimeEurope
    private ZonedDateTime paymentTokenCreated;

    private String transactionId; // ID of the transaction of the PSP

    private String transactionDescription; // description for this transaction

    @ZonedDateTimeEurope
    private ZonedDateTime transactionCreated; // Timestamp of the creation date from the PSP

    private String transactionState; // state of the transaction from the PSP

    private String clientId; // client_id of the transaction from the PSP

    private String paymentId; // the payment-reference of the transaction from the PSPS

    private String transactionResponseCode; // the transaction response code from the PSP

    private String transactionResponseCodeDetails; // the transaciton response code details from the PSP

    private String errorText;


    public Payment() {
    }

    public Payment(Player player, PspCode paymentServiceProvider) {
        this(player.getId(), paymentServiceProvider);
    }

    public Payment(ObjectId playerId, PspCode paymentServiceProvider) {
        this.playerId = playerId;
        this.psp = paymentServiceProvider;
    }


    public String getPriceFormatted() {
        BigDecimal sum;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN);
        nf.setMinimumFractionDigits(2);
        DecimalFormat df = (DecimalFormat) nf;
        sum = new BigDecimal(BigInteger.valueOf(getAmountInCent())).divide(new BigDecimal("100.00"), 2,
                BigDecimal.ROUND_CEILING);
        return df.format(sum) + " " + getCurrency();
    }

    public List<String> getTicketsFormatted(Locale locale) {
        final ResourceBundleRepository resourceBundleRepository =
                CDIBeanService.getInstance().getCDIBean(ResourceBundleRepository.class);
        long counterNormalTicket = getTicketList().stream().filter(t -> !t.isPermaTicket()).count();
        long counterPermaTicket = getTicketList().size() - counterNormalTicket;
        List<String> result = new ArrayList<>();
        if (counterNormalTicket == 1) {
            result.add(counterNormalTicket + " "
                    + resourceBundleRepository.getDefaultLocalized("user.profile.tabs.payments.paymentsFormatted.normalTicket", locale));
        }
        if (counterNormalTicket > 1) {
            result.add(counterNormalTicket + " "
                    + resourceBundleRepository.getDefaultLocalized("user.profile.tabs.payments.paymentsFormatted.normalTickets", locale));
        }
        if (counterPermaTicket == 1) {
            result.add(counterPermaTicket + " "
                    + resourceBundleRepository.getDefaultLocalized("user.profile.tabs.payments.paymentsFormatted.normalPermaTicket", locale));
        }
        if (counterPermaTicket > 1) {
            result.add(counterPermaTicket + " "
                    + resourceBundleRepository.getDefaultLocalized("user.profile.tabs.payments.paymentsFormatted.normalPermaTickets", locale));
        }
        return result;
    }


    public ObjectId getPlayerId() {
        return playerId;
    }

    public void setPlayerId(ObjectId playerId) {
        this.playerId = playerId;
    }

    public List<MainTicket> getTicketList() {
        return ticketList;
    }

    public void setTicketList(List<MainTicket> ticketList) {
        this.ticketList = ticketList;
    }

    public PspCode getPsp() {
        return psp;
    }

    public void setPsp(PspCode psp) {
        this.psp = psp;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public int getAmountInCent() {
        return amountInCent;
    }

    public void setAmountInCent(int amountInCent) {
        this.amountInCent = amountInCent;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ZonedDateTime getPayedAt() {
        return payedAt;
    }

    public void setPayedAt(ZonedDateTime payedAt) {
        this.payedAt = payedAt;
    }

    public PaymentState getState() {
        return state;
    }

    public void setState(PaymentState state) {
        this.state = state;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public ZonedDateTime getPaymentTokenCreated() {
        return paymentTokenCreated;
    }

    public void setPaymentTokenCreated(ZonedDateTime paymentTokenCreated) {
        this.paymentTokenCreated = paymentTokenCreated;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public ZonedDateTime getTransactionCreated() {
        return transactionCreated;
    }

    public void setTransactionCreated(ZonedDateTime transactionCreated) {
        this.transactionCreated = transactionCreated;
    }

    public String getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(String transactionState) {
        this.transactionState = transactionState;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionResponseCode() {
        return transactionResponseCode;
    }

    public void setTransactionResponseCode(String transactionResponseCode) {
        this.transactionResponseCode = transactionResponseCode;
    }

    public String getTransactionResponseCodeDetails() {
        return transactionResponseCodeDetails;
    }

    public void setTransactionResponseCodeDetails(String transactionResponseCodeDetails) {
        this.transactionResponseCodeDetails = transactionResponseCodeDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        if (!super.equals(o)) return false;
        Payment payment = (Payment) o;
        return amountInCent == payment.amountInCent &&
                Objects.equals(playerId, payment.playerId) &&
                Objects.equals(ticketList, payment.ticketList) &&
                psp == payment.psp &&
                paymentType == payment.paymentType &&
                Objects.equals(currency, payment.currency) &&
                Objects.equals(payedAt, payment.payedAt) &&
                Objects.equals(paymentToken, payment.paymentToken) &&
                Objects.equals(paymentTokenCreated, payment.paymentTokenCreated) &&
                Objects.equals(transactionId, payment.transactionId) &&
                Objects.equals(transactionDescription, payment.transactionDescription) &&
                Objects.equals(transactionCreated, payment.transactionCreated) &&
                Objects.equals(transactionState, payment.transactionState) &&
                Objects.equals(clientId, payment.clientId) &&
                Objects.equals(paymentId, payment.paymentId) &&
                Objects.equals(transactionResponseCode, payment.transactionResponseCode) &&
                Objects.equals(transactionResponseCodeDetails, payment.transactionResponseCodeDetails) &&
                Objects.equals(errorText, payment.errorText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerId, ticketList, psp, paymentType, amountInCent,
                currency, payedAt, paymentToken, paymentTokenCreated, transactionId, transactionDescription,
                transactionCreated, transactionState, clientId, paymentId, transactionResponseCode,
                transactionResponseCodeDetails, errorText);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("playerId", playerId)
                .append("ticketList", ticketList)
                .append("psp", psp)
                .append("paymentType", paymentType)
                .append("cardType", cardType)
                .append("amountInCent", amountInCent)
                .append("currency", currency)
                .append("payedAt", payedAt)
                .append("state", state)
                .append("paymentToken", paymentToken)
                .append("paymentTokenCreated", paymentTokenCreated)
                .append("transactionId", transactionId)
                .append("transactionDescription", transactionDescription)
                .append("transactionCreated", transactionCreated)
                .append("transactionState", transactionState)
                .append("clientId", clientId)
                .append("paymentId", paymentId)
                .append("transactionResponseCode", transactionResponseCode)
                .append("transactionResponseCodeDetails", transactionResponseCodeDetails)
                .append("errorText", errorText)
                .toString();
    }
}
