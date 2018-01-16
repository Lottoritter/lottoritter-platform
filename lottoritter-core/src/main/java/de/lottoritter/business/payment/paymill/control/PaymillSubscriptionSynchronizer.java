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
import com.paymill.models.PaymillList;
import com.paymill.models.Subscription;
import com.paymill.models.Transaction;
import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.payment.control.AbstractPaymentSubscriptionSynchronizer;
import de.lottoritter.business.payment.control.PaymentControllerProxy;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentState;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Singleton
@Startup
@DependsOn("ConfigurationController")
public class PaymillSubscriptionSynchronizer extends AbstractPaymentSubscriptionSynchronizer {

    private static final Logger logger = Logger.getLogger(PaymillSubscriptionSynchronizer.class.getName());

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    PaymentControllerProxy paymentControllerProxy;

    @Inject
    ActivityLogController activityLogController;

    @Inject @Configurable(value = "runJobs", defaultValue = "false")
    Instance<Boolean> runJobs;

    @Inject @Configurable(value = "payment.paymill.apikey", defaultValue = "")
    Instance<String> paymillApiKey;


    PaymillContext paymillContext = null;


    public PaymillSubscriptionSynchronizer() {
    }


    @PostConstruct
    private void init() {
        paymillContext = new PaymillContext(paymillApiKey.get());
    }


    @Schedule(hour = "*", minute = "30")
    public void checkForNewSubscriptionTransactions() {
        if ((paymentControllerProxy.getController().getPspCode() == PspCode.PAYMILL) && runJobs.get()) {
            activityLogController.saveActivityLog(new ObjectId(), ActivityType.PAYMENT_SUBSCRIPTION_SYNCHRONIZE,
                    "text", "Starting Paymill-subscription synchronizer...");
            checkForNewSubscriptionTransactions(null);
        }
    }

    @Override
    public void checkForNewSubscriptionTransactions(final Player player) {
        try {
            final Query<MainTicket> query = datastore.createQuery(MainTicket.class).disableValidation();
            if (player == null) {
                query.and(
                        query.criteria("state").equal(TicketState.RUNNING),
                        query.criteria("permaTicket").equal(true));
            } else {
                query.and(
                        query.criteria("playerId").equal(player.getId()),
                        query.criteria("state").equal(TicketState.RUNNING),
                        query.criteria("permaTicket").equal(true));
            }
            final List<MainTicket> permaTicketList = query.asList();
            for (MainTicket ticket : permaTicketList) {
                final Subscription subscription = paymillContext.getSubscriptionService().get(ticket.getSubscriptionId());
                final String subscriptionId = subscription.getId();
                final String clientId = subscription.getClient().getId();
                final ZonedDateTime now = dateTimeService.getDateTimeNowEurope();
                ZonedDateTime lastSubscriptionSyncDate = ticket.getLastSubscriptionSyncDate();
                if (lastSubscriptionSyncDate == null) {
                    lastSubscriptionSyncDate = ticket.getCreated().plusMinutes(1);
                }
                PaymillList<Transaction> transactionList =
                        paymillContext.getTransactionService().list(Transaction.createFilter()
                        .byClientId(clientId)
                        .byCreatedAt(Date.from(lastSubscriptionSyncDate.toInstant()), Date.from(now.toInstant())), null);
                ticket.setLastSubscriptionSyncDate(now);
                datastore.update(
                        datastore.createQuery(Ticket.class).field("_id").equal(ticket.getId()),
                        datastore.createUpdateOperations(Ticket.class).set("lastSubscriptionSyncDate", now)
                );
                for (Transaction transaction : transactionList.getData()) {
                    if (transaction.getDescription().contains(subscriptionId)) {
                        Payment payment = createSubscriptionPayment(ticket.getPlayerId(), ticket);
                        processSubscriptionTransaction(payment, transaction);
                        if ((payment.getState() != PaymentState.INVALID) && !isPaymentExists(payment)) {
                            datastore.save(payment); // initial create payment-object
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.severe("Error occurred while executing PaymillSubscriptionSynchronizer.\r\n" + ex.getMessage());
        }
    }

    private boolean isPaymentExists(final Payment payment) {
        final Query<Payment> query = datastore.createQuery(Payment.class);
        query.and(
                query.criteria("playerId").equal(payment.getPlayerId()),
                query.criteria("psp").equal(payment.getPsp()),
                query.criteria("paymentType").equal(payment.getPaymentType()),
                query.criteria("amountInCent").equal(payment.getAmountInCent()),
                query.criteria("currency").equal(payment.getCurrency()),
                query.criteria("payedAt").equal(payment.getPayedAt()),
                query.criteria("transactionId").equal(payment.getTransactionId()),
                query.criteria("clientId").equal(payment.getClientId()),
                query.criteria("paymentId").equal(payment.getPaymentId())
        );
        final List<Payment> payments = query.asList();
        return (! payments.isEmpty());
    }


    private Payment createSubscriptionPayment(final ObjectId playerId, MainTicket ticket) {
        Payment payment = new Payment(playerId, PspCode.PAYMILL);
        payment.setTicketList(Collections.singletonList(ticket));
        final Price totalPrice = ticket.getTotalTicketPrice();
        payment.setAmountInCent(totalPrice.getAmountInCent());
        payment.setCurrency(totalPrice.getCurrency().getCurrencyCode());
        payment.setTransactionDescription("ref:" + new ObjectId().toHexString());
        return payment;
    }

    private void processSubscriptionTransaction(Payment payment, Transaction transaction) {
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
    }

}
