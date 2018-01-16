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
package de.lottoritter.business.payment.control;

import de.lottoritter.business.lotteries.LotteryManager;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentType;
import de.lottoritter.business.payment.entity.PlayerPayment;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;


/**
 * @author Ulrich Cech
 */
public abstract class AbstractPaymentController {

    @Inject
    protected Datastore datastore;

    @Inject
    protected DateTimeService dateTimeService;

    @Inject
    protected Instance<ShoppingCartSession> shoppingCartSession;

    @Inject
    protected MailController mailController;

    @Inject
    protected LotteryManager lotteryManager;



    public abstract void cancelSubscription(MainTicket mainTicket) throws PaymentTransactionAbortedException;

    public abstract void processNewPlayerPayment(Player player, String initialPaymentToken, PaymentType paymentType)
            throws PaymentTransactionAbortedException;

    public abstract void processExistingPlayerPayment(final Player player, final PlayerPayment playerPayment)
            throws PaymentTransactionAbortedException;

    public abstract PspCode getPspCode();



    // create Payment-Booking
    protected Payment createPayment(final PlayerPayment playerPayment, final Player player,
                                  final String initialPaymentToken, final List<MainTicket> ticketSubList,
                                  final ShoppingCart shoppingCart, PspCode pspCode) {
        Payment payment = new Payment(player, pspCode);
        payment.setTicketList(ticketSubList);
        final Price totalPrice = shoppingCart.getTotalPriceForTicketList(ticketSubList);
        payment.setPaymentType(playerPayment.getType());
        payment.setCardType(playerPayment.getCardType());
        payment.setAmountInCent(totalPrice.getAmountInCent());
        payment.setCurrency(totalPrice.getCurrency().getCurrencyCode());
        if (initialPaymentToken != null) {
            payment.setPaymentToken(initialPaymentToken);
            payment.setPaymentTokenCreated(dateTimeService.getDateTimeNowUTC());
        }
        payment.setTransactionDescription("ref:" + new ObjectId().toHexString());
        return payment;
    }

}