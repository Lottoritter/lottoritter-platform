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
package de.lottoritter.business.shoppingcart.entity;

import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import de.lottoritter.platform.persistence.PersistentEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ulrich Cech
 */
@Entity(value = "shoppingcart", noClassnameStored = true)
public class ShoppingCart extends PersistentEntity implements Cloneable {

    private static final long serialVersionUID = 5271720253537869077L;

    private ShoppingCartState state = ShoppingCartState.OPEN;

    private ObjectId playerId;

    private int amountInCent;

    private String currency = Currency.getInstance(Locale.GERMANY).getCurrencyCode();

    private List<MainTicket> ticketList = new ArrayList<>();

    @ZonedDateTimeEurope
    private ZonedDateTime payedAt;


    public ShoppingCart() {
    }

    public ShoppingCart(Player currentPlayer) {
        super();
        this.playerId = currentPlayer.getId();
    }


    public void addTicket(final MainTicket ticket, Datastore datastore) {
        if (ticket != null) {
            ticket.initTicket();
            ticketList.add(ticket);
            updateTotalPriceInCent();
            datastore.update(
                    datastore.createQuery(ShoppingCart.class).field("_id").equal(getId()),
                    datastore.createUpdateOperations(ShoppingCart.class)
                            .addToSet("ticketList", ticket)
                            .set("amountInCent", getAmountInCent())
            );
        }
    }

    public void removeTicket(final MainTicket ticket, final Datastore datastore) {
        if (ticket != null) {
            ticketList.remove(ticket);
            updateTotalPriceInCent();
            datastore.update(
                    datastore.createQuery(ShoppingCart.class).field("_id").equal(getId()),
                    datastore.createUpdateOperations(ShoppingCart.class)
                            .set("ticketList", ticketList)
                            .set("amountInCent", getAmountInCent())
            );
        }
    }

    public void removeTickets(final List<MainTicket> ticketList, final Datastore datastore) {
        for (MainTicket ticket : ticketList) {
            removeTicket(ticket, datastore);
        }
    }

    public boolean hasPermaTicket() {
        return getTicketList().stream().filter(MainTicket::isPermaTicket).count() > 0;
    }

    public void updateTotalPriceInCent() {
        final Price totalPrice = getTotalPrice();
        setAmountInCent(totalPrice.getAmountInCent());
//        setCurrency(totalPrice.getCurrency().getCurrencyCode()); // currently only EUR supported
    }

    public Price getTotalPrice() {
        return new Price(ticketList.stream().mapToInt(t -> t.getTotalTicketPrice().getAmountInCent()).sum());
    }

    public Price getTotalPriceForTicketList(List<MainTicket> ticketList) {
        return new Price(ticketList.stream()
                .mapToInt(ticket -> ticket.getTotalTicketPrice().getAmountInCent()).sum());
    }

    public List<MainTicket> getNormalTicketList() {
        return Collections.unmodifiableList(
                this.ticketList.stream().filter(ticket -> !ticket.isPermaTicket()).collect(Collectors.toList()));
    }

    public List<MainTicket> getPermaTicketList() {
        return Collections.unmodifiableList(
                this.ticketList.stream().filter(MainTicket::isPermaTicket).collect(Collectors.toList()));
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

    public List<MainTicket> getTicketList() {
        return Collections.unmodifiableList(ticketList);
    }

    public void setTicketList(List<MainTicket> ticketList) {
        this.ticketList = ticketList;
    }

    public ShoppingCartState getState() {
        return state;
    }

    public void setState(ShoppingCartState state) {
        this.state = state;
    }

    public ObjectId getPlayerId() {
        return playerId;
    }

    public void setPlayerId(ObjectId playerId) {
        this.playerId = playerId;
    }

    public ZonedDateTime getPayedAt() {
        return payedAt;
    }

    public void setPayedAt(ZonedDateTime payedAt) {
        this.payedAt = payedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShoppingCart)) return false;
        if (!super.equals(o)) return false;
        ShoppingCart that = (ShoppingCart) o;
        return amountInCent == that.amountInCent &&
                state == that.state &&
                Objects.equals(playerId, that.playerId) &&
                Objects.equals(currency, that.currency) &&
                Objects.equals(ticketList, that.ticketList) &&
                Objects.equals(payedAt, that.payedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), state, playerId, amountInCent, currency, ticketList, payedAt);
    }

    @Override
    public ShoppingCart clone() {
        ShoppingCart shoppingCart;
        try {
            shoppingCart = (ShoppingCart) super.clone();
        } catch (CloneNotSupportedException e) {
            shoppingCart = new ShoppingCart();
        }
        shoppingCart.setTicketList(new ArrayList<>(ticketList));
        shoppingCart.setAmountInCent(amountInCent);
        shoppingCart.setPayedAt(payedAt);
        shoppingCart.setCurrency(currency);
        shoppingCart.setState(state);
        shoppingCart.setPlayerId(playerId);
        return shoppingCart;
    }
}
