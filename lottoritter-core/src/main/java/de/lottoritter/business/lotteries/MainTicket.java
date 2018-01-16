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
package de.lottoritter.business.lotteries;

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotField;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Ticket;
import de.lottoritter.business.lotteries.keno.KenoTicket;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Entity(value = "tickets", noClassnameStored = false) // important to store the classname here
@XmlSeeAlso({ German6aus49Ticket.class, EuroJackpotField.class, KenoTicket.class})
public abstract class MainTicket<F extends Field, L extends Lottery, D extends Drawing>  extends Ticket<F, L , D> {

    private static final long serialVersionUID = 747004158777690027L;


    private ObjectId playerId;

    @XmlElement
    private String shoppingCartId;

    private ObjectId ref = new ObjectId(); // relevant for addressing the ticket in the shoppingcart

    @XmlElement
    private TicketState state = TicketState.INITIAL;

    @XmlElement
    private String drawingType; // german6aus49we, german6aus49sa, german6aus49wesa

    @ZonedDateTimeEurope
    private ZonedDateTime startingDate;

    @ZonedDateTimeEurope
    private ZonedDateTime endingDate;

    @XmlElement
    private boolean permaTicket;

    private PspCode pspCode;

    private String subscriptionId;

    @ZonedDateTimeEurope
    private ZonedDateTime lastSubscriptionSyncDate;

    @XmlElement
    private int durationOrBillingPeriod;

    @XmlElement
    private Integer[] number;

    @XmlTransient
    private Price overallPriceInCent;

    @XmlTransient
    private Price overallFeeInCent;


    private String provinceOfPlayer;



    public MainTicket() {
    }

    public MainTicket(String shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    @PostLoad
    public void initPersistentObject() {
        for (Ticket ticket : getEmbeddedTickets()) {
            if (ticket instanceof EmbeddedTicket) {
                EmbeddedTicket embeddedTicket = (EmbeddedTicket) ticket;
                embeddedTicket.setParentTicket(this);
            }
        }
    }

    public abstract void initStartingAndEndingDate();

    public abstract int getNumberOfDrawings();

    public abstract Price getTotalTicketPrice();


    public void initTicket() {
        setState(TicketState.INITIAL);
        sortFieldNumbers();
        if (! getEmbeddedTickets().isEmpty()) {
            for (Ticket embeddedTicket : getEmbeddedTickets()) {
                ((EmbeddedTicket)embeddedTicket).setParentTicket(this);
                embeddedTicket.sortFieldNumbers();
            }
        }
        initStartingAndEndingDate();
        validate();
        calculateAllTicketPrices();
    }

    public void removeAdditionalLottery(String additionalLottery) {
        if (getEmbeddedTicket(additionalLottery) != null) {
            getEmbeddedTickets().removeIf(ticket -> ticket.getLotteryIdentifier().equals(additionalLottery));
        }
    }

    public void addAdditionalLottery(String newAdditionalLotteries) {
        this.getEmbeddedTickets().add(getLottery().createNewEmbeddedTicket(this, newAdditionalLotteries));
    }


    public ObjectId getPlayerId() {
        return playerId;
    }

    public void setPlayerId(ObjectId playerId) {
        this.playerId = playerId;
    }

    public String getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(String shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public ObjectId getRef() {
        return ref;
    }

    public TicketState getState() {
        return state;
    }

    public void setState(TicketState state) {
        this.state = state;
    }

    public String getDrawingType() {
        return drawingType;
    }

    public void setDrawingType(String drawingType) {
        this.drawingType = drawingType;
    }

    public ZonedDateTime getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(ZonedDateTime startingDate) {
        this.startingDate = startingDate;
    }

    public ZonedDateTime getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(ZonedDateTime endingDate) {
        this.endingDate = endingDate;
    }

    public boolean isPermaTicket() {
        return permaTicket;
    }

    public void setPermaTicket(boolean permaTicket) {
        this.permaTicket = permaTicket;
    }

    public PspCode getPspCode() {
        return pspCode;
    }

    public void setPspCode(PspCode pspCode) {
        this.pspCode = pspCode;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public ZonedDateTime getLastSubscriptionSyncDate() {
        return lastSubscriptionSyncDate;
    }

    public void setLastSubscriptionSyncDate(ZonedDateTime lastSubscriptionSyncDate) {
        this.lastSubscriptionSyncDate = lastSubscriptionSyncDate;
    }

    public int getDurationOrBillingPeriod() {
        return durationOrBillingPeriod;
    }

    public void setDurationOrBillingPeriod(int durationOrBillingPeriod) {
        this.durationOrBillingPeriod = durationOrBillingPeriod;
    }

    public Integer[] getNumber() {
        return number;
    }

    public void setNumber(Integer[] number) {
        this.number = number;
    }

    public String getProvinceOfPlayer() {
        return provinceOfPlayer;
    }

    public void setProvinceOfPlayer(String provinceOfPlayer) {
        this.provinceOfPlayer = provinceOfPlayer;
    }

    public Price getOverallPriceInCent() {
        return overallPriceInCent;
    }

    public void setOverallPriceInCent(Price overallPriceInCent) {
        this.overallPriceInCent = overallPriceInCent;
    }

    public Price getOverallFeeInCent() {
        return overallFeeInCent;
    }

    public void setOverallFeeInCent(Price overallFeeInCent) {
        this.overallFeeInCent = overallFeeInCent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MainTicket)) return false;
        if (!super.equals(o)) return false;
        MainTicket<?, ?, ?> that = (MainTicket<?, ?, ?>) o;
        return permaTicket == that.permaTicket &&
                durationOrBillingPeriod == that.durationOrBillingPeriod &&
                Objects.equals(playerId, that.playerId) &&
                Objects.equals(shoppingCartId, that.shoppingCartId) &&
                Objects.equals(ref, that.ref) &&
                state == that.state &&
                Objects.equals(drawingType, that.drawingType) &&
                Objects.equals(startingDate, that.startingDate) &&
                Objects.equals(endingDate, that.endingDate) &&
                Objects.equals(subscriptionId, that.subscriptionId) &&
                Objects.equals(lastSubscriptionSyncDate, that.lastSubscriptionSyncDate) &&
                Arrays.equals(number, that.number) &&
                Objects.equals(overallPriceInCent, that.overallPriceInCent) &&
                Objects.equals(overallFeeInCent, that.overallFeeInCent) &&
                Objects.equals(provinceOfPlayer, that.provinceOfPlayer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerId, shoppingCartId, ref, state, drawingType, startingDate, endingDate, permaTicket, subscriptionId, lastSubscriptionSyncDate, durationOrBillingPeriod, number, overallPriceInCent, overallFeeInCent, provinceOfPlayer);
    }
}
