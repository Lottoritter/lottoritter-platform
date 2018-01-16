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
package de.lottoritter.business.shoppingcart.control;

import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.payment.boundary.PriceFormatter;
import de.lottoritter.business.payment.control.PriceListController;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import de.lottoritter.platform.Current;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * @author Ulrich Cech
 */
@SessionScoped
public class ShoppingCartSession implements Serializable {

    private static final long serialVersionUID = 8718733745624310292L;

    @EJB
    ShoppingCartRepository shoppingCartRepository;

    @Inject @Current
    Instance<Player> currentPlayer;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    PriceListController priceListController;

    @Inject
    Datastore datastore;

    @Inject
    PriceFormatter priceFormatter;


    private ShoppingCart currentShoppingCart = new ShoppingCart();


    public ShoppingCartSession() {
    }


    public void addTicket(final MainTicket ticket) {
        currentShoppingCart.addTicket(ticket, datastore);
        final Player player = currentPlayer.get();
        if (player != null) {
            activityLogController.saveActivityLog(player, ActivityType.SHOPPINGCART_ADD_TICKET,
                    "shoppingcartId", currentShoppingCart.getId().toHexString(),
                    "ticketRef", ticket.getRef().toHexString(),
                    "ticketLotteryIdentifier", ticket.getLotteryIdentifier(),
                    "ticketNumber", StringUtils.join(ticket.getNumber(), ""),
                    "ticketPriceInCent", "" + ticket.getTotalTicketPrice().getAmountInCent());
            ticket.setPlayerId(player.getId());
            final Query<ShoppingCart> query = datastore.createQuery(ShoppingCart.class);
            query.and(
                    query.criteria("_id").equal(currentShoppingCart.getId()),
                    query.criteria("ticketList.ref").equal(ticket.getRef())
            );
            datastore.update(
                    query,
                    datastore.createUpdateOperations(ShoppingCart.class)
                            .set("ticketList.$.playerId", player.getId())
            );
            if (currentShoppingCart.getPlayerId() == null) {
                activityLogController.saveActivityLog(player, ActivityType.SHOPPINGCART_ATTACH_PLAYER,
                        "shoppingcartId", currentShoppingCart.getId().toHexString());
                currentShoppingCart.setPlayerId(player.getId());
                datastore.update(
                        datastore.createQuery(ShoppingCart.class).field("_id").equal(currentShoppingCart.getId()),
                        datastore.createUpdateOperations(ShoppingCart.class).set("playerId", player.getId())
                );
            }
        }
    }

    public void removeTicket(final MainTicket ticket) {
        currentShoppingCart.removeTicket(ticket, datastore);
        activityLogController.saveActivityLog(ticket.getPlayerId(), ActivityType.SHOPPINGCART_REMOVE_TICKET,
                "shoppingcartId", currentShoppingCart.getId().toHexString(),
                "ticketRef", ticket.getRef().toHexString(),
                "ticketLotteryIdentifier", ticket.getLotteryIdentifier(),
                "ticketNumber", StringUtils.join(ticket.getNumber(), ""),
                "ticketPriceInCent", "" + ticket.getTotalTicketPrice().getAmountInCent());
    }

    public List<MainTicket> getTicketList() {
        return currentShoppingCart.getTicketList();
    }

    public Price getTotalPrice() {
        return currentShoppingCart.getTotalPrice();
    }

    public String getTotalPriceFormatted() {
        return getPriceFormatted(getTotalPrice());
    }

    public String getPriceFormatted(Price price) {
        return priceFormatter.getPriceFormatted(price);
    }

    public void handleUntrackedSessionTickets() {
        final Player player = currentPlayer.get();
        final ShoppingCart currentLocalShoppingCart = shoppingCartRepository.getShoppingCartForPlayer(player);
        if ((getTicketList() != null) && (! getTicketList().isEmpty())) {
            for (MainTicket ticket : getTicketList()) {
                ticket.setPlayerId(player.getId());
                ticket.setShoppingCartId(currentLocalShoppingCart.getId().toHexString());
                currentLocalShoppingCart.addTicket(ticket, datastore);
                activityLogController.saveActivityLog(player, ActivityType.SHOPPINGCART_ADD_UNTRACKED_SESSION_TICKETS,
                        "shoppingcartId", currentLocalShoppingCart.getId().toHexString(),
                        "ticketRef", ticket.getRef().toHexString(),
                        "ticketLotteryIdentifier", ticket.getLotteryIdentifier(),
                        "ticketNumber", StringUtils.join(ticket.getNumber(), ""),
                        "ticketPriceInCent", "" + ticket.getTotalTicketPrice().getAmountInCent());
            }
        }
        this.currentShoppingCart = currentLocalShoppingCart;
    }

    public int adjustTicketsInShoppingCart() {
        int counter = 0;
        for (MainTicket ticket : getTicketList()) {
            if (ticket.adjust()) {
                activityLogController.saveActivityLog(ticket.getPlayerId(), ActivityType.SHOPPINGCART_ADJUST_TICKET,
                        "shoppingcart", currentShoppingCart.getId().toHexString(),
                        "ticketRef", ticket.getRef().toHexString(),
                        "ticketLotteryIdentifier", ticket.getLotteryIdentifier(),
                        "ticketNumber", StringUtils.join(ticket.getNumber(), ""),
                        "ticketPriceInCent", "" + ticket.getTotalTicketPrice().getAmountInCent());
                ++counter;
                final Query<ShoppingCart> query = datastore.createQuery(ShoppingCart.class);
                query.and(
                        query.criteria("_id").equal(currentShoppingCart.getId()),
                        query.criteria("ticketList.ref").equal(ticket.getRef())
                );
                datastore.update(
                        query,
                        datastore.createUpdateOperations(ShoppingCart.class)
                                .set("ticketList.$", ticket)
                );
            }
        }
        return counter;
    }

    public ShoppingCart getCurrentShoppingCart() {
        return currentShoppingCart;
    }

    public void toggleAdditionalLottery(MainTicket ticket, String additionalLottery) {
        if (ticket.canAcceptAdditionalLottery(additionalLottery)) {
            List<MainTicket> ticketList = currentShoppingCart.getTicketList();
            Optional<MainTicket> optionalTicket = ticketList.stream().filter(t -> t.getRef().equals(ticket.getRef())).findAny();
            optionalTicket.ifPresent(t -> {
                if (t.getEmbeddedTicket(additionalLottery) != null) {
                    t.removeAdditionalLottery(additionalLottery);
                } else {
                    t.addAdditionalLottery(additionalLottery);
                }
                t.calculateAllTicketPrices();
                currentShoppingCart.updateTotalPriceInCent();
            });
            datastore.update(
                    datastore.createQuery(ShoppingCart.class).field("_id").equal(currentShoppingCart.getId()),
                    datastore.createUpdateOperations(ShoppingCart.class)
                            .set("amountInCent", currentShoppingCart.getAmountInCent())
                            .set("ticketList", currentShoppingCart.getTicketList())
            );
        }
    }
}
