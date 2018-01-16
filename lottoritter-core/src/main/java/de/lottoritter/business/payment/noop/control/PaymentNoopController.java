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
package de.lottoritter.business.payment.noop.control;

import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.payment.control.AbstractPaymentController;
import de.lottoritter.business.payment.control.PaymentTransactionAbortedException;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentState;
import de.lottoritter.business.payment.entity.PaymentType;
import de.lottoritter.business.payment.entity.PlayerPayment;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import org.bson.types.ObjectId;

import javax.ejb.Stateless;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@Stateless
public class PaymentNoopController extends AbstractPaymentController {


    public PaymentNoopController() {
    }


    @Override
    public void cancelSubscription(MainTicket mainTicket) throws PaymentTransactionAbortedException {
        final Lottery lottery = lotteryManager.getLotteryByIdentifier(mainTicket.getLotteryIdentifier());
        lottery.setTicketStateAfterSubscriptionCancelled(mainTicket);
    }

    @Override
    public void processNewPlayerPayment(Player player, String initialPaymentToken, PaymentType paymentType)
            throws PaymentTransactionAbortedException {
        // not applicable for NO_OP mode
    }

    @Override
    public void processExistingPlayerPayment(Player player, PlayerPayment playerPayment)
            throws PaymentTransactionAbortedException {
        ShoppingCart currentUserShoppingCart = shoppingCartSession.get().getCurrentShoppingCart();
        ShoppingCart tempShoppingCart = currentUserShoppingCart.clone();
        processNormalTicketsWithExistingPlayerPayment(player, playerPayment, currentUserShoppingCart);
        processPermaTicketsWithExistingPlayerPayment(player, playerPayment, currentUserShoppingCart);
        mailController.sendPurchaseConfirmationMail(tempShoppingCart, player);
    }

    @Override
    public PspCode getPspCode() {
        return PspCode.NO_OP;
    }


    private void processNormalTicketsWithExistingPlayerPayment(Player player, PlayerPayment playerPayment,
                                                               ShoppingCart currentUserShoppingCart) {
        final List<MainTicket> normalTicketList = currentUserShoppingCart.getNormalTicketList();
        if (! normalTicketList.isEmpty()) {
            Payment payment = createPayment(playerPayment, player, null,
                                            normalTicketList, currentUserShoppingCart, PspCode.NO_OP);
            processTransaction(payment, currentUserShoppingCart);
        }
    }


    private void processTransaction(Payment payment, ShoppingCart shoppingCart) {
        payment.setTransactionId(new ObjectId().toString());
        payment.setTransactionCreated(dateTimeService.convertDateToZonedDateTimeEurope(new Date()));
        payment.setTransactionState("closed");
        payment.setPaymentId("");
        payment.setClientId("");
        payment.setTransactionResponseCode(PspCode.NO_OP.name());
        payment.setTransactionResponseCodeDetails(PspCode.NO_OP.name());
        // everything OK for payment process
        payment.setState(PaymentState.SUCCESS);
        payment.setPayedAt(dateTimeService.getDateTimeNowUTC());
        // set ticket state to RUNNING and save the tickets in the tickets-collection
        for (MainTicket ticket : payment.getTicketList()) {
            ticket.setPspCode(PspCode.PAYMILL);
            ticket.setState(TicketState.RUNNING);
        }
        datastore.save(payment); // initial persist
        datastore.save(payment.getTicketList()); // initial persist of all new tickets
        shoppingCart.removeTickets(payment.getTicketList(), datastore);
        datastore.update(
                datastore.createQuery(ShoppingCart.class).field("_id").equal(shoppingCart.getId()),
                datastore.createUpdateOperations(ShoppingCart.class)
                        .set("ticketList", shoppingCart.getTicketList())
        );
    }

    private void processPermaTicketsWithExistingPlayerPayment(Player player, PlayerPayment playerPayment,
                                                              ShoppingCart currentUserShoppingCart) {
        final List<MainTicket> permaTicketList = currentUserShoppingCart.getPermaTicketList();
        for (MainTicket ticket : permaTicketList) {
            ticket.setSubscriptionId(new ObjectId().toString());
            Payment payment = createPayment(playerPayment, player, null,
                    Collections.singletonList(ticket), currentUserShoppingCart, PspCode.NO_OP);
            processTransaction(payment, currentUserShoppingCart);
        }
    }

}
