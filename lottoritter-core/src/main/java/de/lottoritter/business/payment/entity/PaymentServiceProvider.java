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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Embedded
public class PaymentServiceProvider {

    private PspCode pspCode;

    private String externalClientId;

    @Embedded
    private List<PlayerPayment> playerPaymentList;


    public PaymentServiceProvider() {
    }


    public PaymentServiceProvider(PspCode pspCode, String externalClientId) {
        this.pspCode = pspCode;
        this.externalClientId = externalClientId;
    }


    public PspCode getPspCode() {
        return pspCode;
    }

    public void setPspCode(PspCode pspCode) {
        this.pspCode = pspCode;
    }

    public String getExternalClientId() {
        return externalClientId;
    }

    public void setExternalClientId(String externalClientId) {
        this.externalClientId = externalClientId;
    }

    public List<PlayerPayment> getPlayerPaymentList() {
        if (this.playerPaymentList == null) {
            this.playerPaymentList = new ArrayList<>();
        }
        return Collections.unmodifiableList(playerPaymentList);
    }

    public void setPlayerPaymentList(List<PlayerPayment> playerPaymentList) {
        this.playerPaymentList = playerPaymentList;
    }

    public void addPlayerPayment(PlayerPayment newPlayerPayment) {
        if (this.playerPaymentList == null) {
            this.playerPaymentList = new ArrayList<>();
        }
        this.playerPaymentList.add(newPlayerPayment);
    }
}
