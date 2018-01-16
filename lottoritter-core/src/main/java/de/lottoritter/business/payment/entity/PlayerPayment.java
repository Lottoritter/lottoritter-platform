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

import org.mongodb.morphia.annotations.Embedded;

/**
 * @author Ulrich Cech
 */
@Embedded
public class PlayerPayment {

    private PspCode psp;

    private String externalId;

    private PaymentType type; // creditcard, debit

    private CardType cardType; // visa, mastercard, sepa

    private int expireMonth;

    private int expireYear;

    private String cardOrAccountHolder;

    private String last4;

    private String blz;

    private String accountNumber;

    private String iban;

    private String bic;

    private String clientId; // reference to the client in the PSP


    public PlayerPayment() {
    }


    public boolean isSEPA() {
        return (this.iban != null) && (this.iban.length() > 10);
    }

    public void setPsp(PspCode psp) {
        this.psp = psp;
    }

    public PspCode getPsp() {
        return psp;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public PaymentType getType() {
        return type;
    }

    public void setType(PaymentType type) {
        this.type = type;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public int getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(int expireMonth) {
        this.expireMonth = expireMonth;
    }

    public int getExpireYear() {
        return expireYear;
    }

    public void setExpireYear(int expireYear) {
        this.expireYear = expireYear;
    }

    public String getCardOrAccountHolder() {
        return cardOrAccountHolder;
    }

    public void setCardOrAccountHolder(String cardOrAccountHolder) {
        this.cardOrAccountHolder = cardOrAccountHolder;
    }

    public String getLast4() {
        return last4;
    }

    public void setLast4(String last4) {
        this.last4 = last4;
    }

    public String getBlz() {
        return blz;
    }

    public void setBlz(String blz) {
        this.blz = blz;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
