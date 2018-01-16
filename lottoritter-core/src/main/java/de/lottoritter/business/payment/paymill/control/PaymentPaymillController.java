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
package de.lottoritter.business.payment.paymill.control;

import com.paymill.context.PaymillContext;
import com.paymill.models.Client;
import com.paymill.models.Offer;
import com.paymill.models.PaymillList;
import com.paymill.models.Subscription;
import com.paymill.models.Transaction;
import com.paymill.services.SubscriptionService;
import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.payment.control.AbstractPaymentController;
import de.lottoritter.business.payment.control.PaymentTransactionAbortedException;
import de.lottoritter.business.payment.entity.CardType;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentServiceProvider;
import de.lottoritter.business.payment.entity.PaymentState;
import de.lottoritter.business.payment.entity.PaymentType;
import de.lottoritter.business.payment.entity.PlayerPayment;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Stateless
public class PaymentPaymillController extends AbstractPaymentController {

    private static final Logger logger = Logger.getLogger(PaymentPaymillController.class.getName());

    @Inject
    @Configurable(value = "payment.paymill.apikey", defaultValue = "")
    Instance<String> paymillApiKey;


    public PaymentPaymillController() {
    }


    /**
     * Process for payment with existing credit card.
     */
    public void processNewPlayerPayment(Player player, String initialPaymentToken, PaymentType paymentType)
            throws PaymentTransactionAbortedException {
        ShoppingCart currentUserShoppingCart = shoppingCartSession.get().getCurrentShoppingCart();
        ShoppingCart tempShoppingCart = currentUserShoppingCart.clone();
        PaymillContext paymillContext = new PaymillContext(paymillApiKey.get());
        String externalClientId = getOrCreatePaymillClientId(player, paymillContext);
        if ((initialPaymentToken != null) && (initialPaymentToken.trim().length() > 0)) {
            PlayerPayment playerPayment = processNormalTicketsWithNewPlayerPayment(initialPaymentToken, paymentType,
                    player, currentUserShoppingCart, paymillContext, externalClientId);
            processPermaTicketsWithNewPlayerPayment(playerPayment, initialPaymentToken, paymentType, player,
                    currentUserShoppingCart, paymillContext, externalClientId);
            mailController.sendPurchaseConfirmationMail(tempShoppingCart, player);
        } else {
            throw new PaymentTransactionAbortedException("No payment token returned from PSP.");
        }
    }


    private PlayerPayment processNormalTicketsWithNewPlayerPayment(String initialPaymentToken, PaymentType paymentType,
                                                                   Player player, ShoppingCart currentUserShoppingCart,
                                                                   PaymillContext paymillContext,
                                                                   String externalClientId) throws PaymentTransactionAbortedException {
        final List<MainTicket> normalTicketList = currentUserShoppingCart.getNormalTicketList();
        if ((normalTicketList != null) && !normalTicketList.isEmpty()) {
            final com.paymill.models.Payment paymillPayment =
                    paymillContext.getPaymentService().createWithTokenAndClient(initialPaymentToken, externalClientId);
            PlayerPayment playerPayment = createNewPlayerPayment(player, paymentType, paymillPayment);
            Payment payment =
                    createPayment(playerPayment, player, initialPaymentToken, currentUserShoppingCart.getNormalTicketList(),
                                  currentUserShoppingCart, PspCode.PAYMILL);
            try {
                final Transaction transaction =
                        paymillContext.getTransactionService()
                                .createWithPaymentAndClient(paymillPayment.getId(),
                                        externalClientId,
                                        payment.getAmountInCent(), payment.getCurrency(),
                                        payment.getTransactionDescription());
                processTransaction(payment, transaction, currentUserShoppingCart);
                if (payment.getState() != PaymentState.SUCCESS) {
                    throw new PaymentTransactionAbortedException(payment.getErrorText());
                }
                return playerPayment;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Technical error: " + ex.getMessage() + "\r\n" + payment.toString(), ex);
                throw new PaymentTransactionAbortedException("Technical error", ex);
            }
        }
        return null;
    }

    private void processPermaTicketsWithNewPlayerPayment(final PlayerPayment playerPayment, final String initialPaymentToken,
                                                         final PaymentType paymentType, Player player,
                                                         final ShoppingCart currentUserShoppingCart,
                                                         final PaymillContext paymillContext,
                                                         final String externalClientId) {
        PlayerPayment currentPlayerPayment = playerPayment;
        if (currentPlayerPayment == null) {
            final com.paymill.models.Payment paymillPayment =
                    paymillContext.getPaymentService().createWithTokenAndClient(initialPaymentToken, externalClientId);
            currentPlayerPayment = createNewPlayerPayment(player, paymentType, paymillPayment);
        }
        processPermaTicketsWithExistingPlayerPayment(player, currentPlayerPayment, currentUserShoppingCart, paymillContext);
    }

    /**
     * Process for payment with existing credit card.
     */
    public void processExistingPlayerPayment(final Player player, final PlayerPayment playerPayment)
            throws PaymentTransactionAbortedException {
        ShoppingCart currentUserShoppingCart = shoppingCartSession.get().getCurrentShoppingCart();
        ShoppingCart tempShoppingCart = currentUserShoppingCart.clone();
        PaymillContext paymillContext = new PaymillContext(paymillApiKey.get());
        processNormalTicketsWithExistingPlayerPayment(player, playerPayment, currentUserShoppingCart, paymillContext);
        processPermaTicketsWithExistingPlayerPayment(player, playerPayment, currentUserShoppingCart, paymillContext);
        mailController.sendPurchaseConfirmationMail(tempShoppingCart, player);
    }


    public void cancelSubscription(MainTicket mainTicket) throws PaymentTransactionAbortedException {
        try {
            PaymillContext paymillContext = new PaymillContext(paymillApiKey.get());
            SubscriptionService subscriptionService = paymillContext.getSubscriptionService();
            subscriptionService.cancel(mainTicket.getSubscriptionId());
            final Lottery lottery = lotteryManager.getLotteryByIdentifier(mainTicket.getLotteryIdentifier());
            lottery.setTicketStateAfterSubscriptionCancelled(mainTicket);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Technical error while cancelling subscription for ticket <"
                    + mainTicket.getId() + ">\r\n" + ex.getMessage() + "\r\n", ex);
            throw new PaymentTransactionAbortedException("Technical error while cancelling subscription", ex);
        }
    }

    private void processNormalTicketsWithExistingPlayerPayment(Player player, PlayerPayment playerPayment,
                                                               ShoppingCart currentUserShoppingCart,
                                                               PaymillContext paymillContext) throws PaymentTransactionAbortedException {
        final List<MainTicket> normalTicketList = currentUserShoppingCart.getNormalTicketList();
        if (! normalTicketList.isEmpty()) {
            Payment payment =
                    createPayment(playerPayment, player, null, normalTicketList, currentUserShoppingCart, PspCode.PAYMILL);
            final Transaction transaction =
                    paymillContext.getTransactionService()
                            .createWithPaymentAndClient(playerPayment.getExternalId(),
                                    playerPayment.getClientId(),
                                    payment.getAmountInCent(),
                                    payment.getCurrency());
            try {
                processTransaction(payment, transaction, currentUserShoppingCart);
                if (payment.getState() != PaymentState.SUCCESS) {
                    throw new PaymentTransactionAbortedException(payment.getErrorText());
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Technical error: " + ex.getMessage() + "\r\n" + payment.toString(), ex);
                throw new PaymentTransactionAbortedException("Technical error", ex);
            }
        }
    }

    private void processPermaTicketsWithExistingPlayerPayment(Player player, PlayerPayment playerPayment,
                                                              ShoppingCart currentUserShoppingCart,
                                                              PaymillContext paymillContext) {
        final List<MainTicket> permaTicketList = currentUserShoppingCart.getPermaTicketList();
        for (MainTicket ticket : permaTicketList) {
            final Price totalTicketPrice = ticket.getTotalTicketPrice();
            final String name = "Client#" + playerPayment.getClientId();
            final Offer offer = paymillContext.getOfferService().create(totalTicketPrice.getAmountInCent(),
                    totalTicketPrice.getCurrency().getCurrencyCode(),
                    ticket.getDurationOrBillingPeriod() + " WEEK", name);
            final Subscription subscription =
                    paymillContext.getSubscriptionService().create(playerPayment.getExternalId(),
                            playerPayment.getClientId(), offer.getId(), totalTicketPrice.getAmountInCent(),
                            totalTicketPrice.getCurrency().getCurrencyCode(),
                            null, null, name, null);
            ticket.setSubscriptionId(subscription.getId());
            final Date startAt = new Date();
            startAt.setTime(startAt.getTime() - 10000);
            final Date endAt = new Date();
            endAt.setTime(endAt.getTime() + 20000);
            boolean search = true;
            PaymillList<Transaction> list = new PaymillList<>();
            int counter = 0;
            while (search && counter < 10) {
                ++counter;
                list = paymillContext.getTransactionService().list(Transaction.createFilter()
                        .byClientId(playerPayment.getClientId()).byCreatedAt(startAt, endAt), null);
                if (list.getDataCount() != 0) {
                    search = false;
                }
            }
            if (counter >= 10) {
                throw new RuntimeException("Could not get successful created transaction.");
            }
            boolean ok = false;
            for (Transaction transaction : list.getData()) {
                if (transaction.getDescription().contains(subscription.getId())) {
                    Payment payment = createPayment(playerPayment, player, null,
                                                    Collections.singletonList(ticket), currentUserShoppingCart,
                                                    PspCode.PAYMILL);
                    processTransaction(payment, transaction, currentUserShoppingCart);
                    ok = true;
                }
            }
            if (! ok) {
                throw new RuntimeException("Could not get successful created transaction.");
            }
        }
    }

    // getting the externalClientId
    private String getOrCreatePaymillClientId(Player player, PaymillContext paymillContext) {
        final PaymentServiceProvider paymentServiceProvider = player.getPaymentServiceProviderWithCode(PspCode.PAYMILL);
        String externalClientId;
        if (paymentServiceProvider == null) {
            Client paymillClient = paymillContext.getClientService().createWithEmail(player.getEmail());
            externalClientId = paymillClient.getId();
            final PaymentServiceProvider newPaymentServiceProvider = new PaymentServiceProvider(PspCode.PAYMILL, externalClientId);
            player.addPaymentServiceProvider(newPaymentServiceProvider);
            datastore.update(
                    datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                    datastore.createUpdateOperations(Player.class).addToSet("paymentServiceProviderList", newPaymentServiceProvider)
            );
        } else {
            externalClientId = paymentServiceProvider.getExternalClientId();
        }
        return externalClientId;
    }


    // Zahlungsmittel anlegen
    private PlayerPayment createNewPlayerPayment(Player player, PaymentType type, com.paymill.models.Payment paymillPayment) {
        PlayerPayment newPlayerPayment = new PlayerPayment();
        newPlayerPayment.setExternalId(paymillPayment.getId());
        newPlayerPayment.setType(type);
        newPlayerPayment.setClientId(paymillPayment.getClient().getId());
        if (PaymentType.CREDITCARD == type) {
            newPlayerPayment.setCardType(CardType.valueOfString(paymillPayment.getCardType().getValue()));
            newPlayerPayment.setCardOrAccountHolder(paymillPayment.getCardHolder());
            newPlayerPayment.setExpireMonth(paymillPayment.getExpireMonth());
            newPlayerPayment.setExpireYear(paymillPayment.getExpireYear());
            newPlayerPayment.setLast4(paymillPayment.getLast4());
        }
        if (PaymentType.SEPA == type) {
            newPlayerPayment.setCardType(CardType.SEPA);
            newPlayerPayment.setAccountNumber(paymillPayment.getAccount());
            newPlayerPayment.setBlz(paymillPayment.getCode());
            newPlayerPayment.setIban(paymillPayment.getIban());
            newPlayerPayment.setBic(paymillPayment.getBic());
            newPlayerPayment.setCardOrAccountHolder(paymillPayment.getHolder());
        }
        final PaymentServiceProvider paymentServiceProvider = player.getPaymentServiceProviderWithCode(PspCode.PAYMILL);
        paymentServiceProvider.addPlayerPayment(newPlayerPayment);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).set("paymentServiceProviderList", player.getPaymentServiceProviderList())
        );
        return newPlayerPayment;
    }

    private void processTransaction(Payment payment, Transaction transaction, ShoppingCart shoppingCart) {
        if (transaction != null) {
            payment.setTransactionId(transaction.getId());
            payment.setTransactionCreated(dateTimeService.convertDateToZonedDateTimeEurope(transaction.getCreatedAt()));
            payment.setTransactionState(transaction.getStatus().getValue());
            payment.setPaymentId(transaction.getPayment().getId());
            payment.setClientId(transaction.getClient().getId());
            payment.setTransactionResponseCode(Integer.toString(transaction.getResponseCode()));
            payment.setTransactionResponseCodeDetails(transaction.getResponseCodeDetail());
            if (transaction.isSuccessful()) {
                if (transaction.getStatus() != Transaction.Status.CLOSED || transaction.getFraud()) {
                    if (transaction.getFraud()) {
                        payment.setState(PaymentState.ERROR);
                        payment.setErrorText("Fraud detected.");
                    }
                    payment.setState(PaymentState.ERROR);
                    payment.setErrorText("Status not closed.");
                } else {
                    // everything OK for payment process
                    payment.setState(PaymentState.SUCCESS);
                    payment.setPayedAt(dateTimeService.getDateTimeNowUTC());
                    // set ticket state to RUNNING and save the tickets in the tickets-collection
                    for (MainTicket ticket : payment.getTicketList()) {
                        ticket.setPspCode(PspCode.PAYMILL);
                        ticket.setState(TicketState.RUNNING);
                    }
                }
            } else {
                payment.setState(PaymentState.ERROR);
                payment.setErrorText("Transaction not successful: "
                        + transaction.getResponseCode() + ";" + transaction.getResponseCodeDetail());
            }
        } else {
            payment.setState(PaymentState.INVALID);
            payment.setErrorText("No transaction returned from PSP.");
        }
        if (payment.getState() != PaymentState.INVALID) {
            datastore.save(payment); // initial persist
            if (payment.getState() == PaymentState.SUCCESS) {
                datastore.save(payment.getTicketList()); // initial persist of all new tickets
                shoppingCart.removeTickets(payment.getTicketList(), datastore);
                datastore.update(
                        datastore.createQuery(ShoppingCart.class).field("_id").equal(shoppingCart.getId()),
                        datastore.createUpdateOperations(ShoppingCart.class)
                                .set("ticketList", shoppingCart.getTicketList())
                );
            }
        }
    }

    @Override
    public PspCode getPspCode() {
        return PspCode.PAYMILL;
    }
}
