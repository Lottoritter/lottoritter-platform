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
package de.lottoritter.business.profile.control;

import com.mongodb.BasicDBObject;
import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.lotteries.Drawing;
import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.DrawingsManager;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.LotteryManager;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketHistoryWrapper;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.payment.control.CurrencyFormatter;
import de.lottoritter.business.payment.control.PaymentTransactionAbortedException;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentState;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.ProfileChangeGroup;
import de.lottoritter.business.player.entity.UserActivation;
import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.business.validation.control.ValidationController;
import de.lottoritter.platform.persistence.encryption.EncryptedFieldValueWrapper;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.Sort;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Ulrich Cech
 */
@Stateless
public class ProfileController {

    @Inject
    Datastore datastore;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    MailController mailController;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    DrawingsManager drawingsManager;

    @Inject
    LotteryManager lotteryManager;

    @Inject
    CurrencyFormatter currencyFormatter;


    private Javers javers = JaversBuilder.javers().registerValueObject(Player.class).build();


    public void blockPlayer(final Player currentPlayer) {
        currentPlayer.setBlocked(true);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(currentPlayer.getId()),
                datastore.createUpdateOperations(Player.class).set("blocked", true)
        );
        activityLogController.saveActivityLog(currentPlayer, ActivityType.SELF_SUSPEND);
    }

    public void changePersonalData(final Player currentPlayer, final String email) {
        Player changePersonalDataPlayer = new Player();
        changePersonalDataPlayer.setEmail(email);
        ValidationController.get(Player.class).processBeanValidationForGroup(changePersonalDataPlayer, ProfileChangeGroup.class);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(currentPlayer.getId()),
                datastore.createUpdateOperations(Player.class)
                        .set("email", new EncryptedFieldValueWrapper(changePersonalDataPlayer.getEmail()))
        );
        final Diff compare = javers.compare(currentPlayer, changePersonalDataPlayer);
        List<String> changedData = new ArrayList<>();
        for (ValueChange valueChange : compare.getChangesByType(ValueChange.class)) {
            changedData.add(valueChange.getPropertyName());
            changedData.add("oldValue:" + (valueChange.getLeft() != null ? valueChange.getLeft().toString() : "")
                    + ";newValue:" + (valueChange.getRight() != null ? valueChange.getRight().toString() : ""));
        }
        activityLogController.saveActivityLog(currentPlayer, ActivityType.CHANGE_PROFILE, changedData.toArray(new String[changedData.size()]));
        if (!currentPlayer.getEmail().equals(changePersonalDataPlayer.getEmail())) {
            currentPlayer.setEmail(changePersonalDataPlayer.getEmail());
            currentPlayer.setActivation(new UserActivation(currentPlayer));
            datastore.update(
                    datastore.createQuery(Player.class).field("_id").equal(currentPlayer.getId()),
                    datastore.createUpdateOperations(Player.class)
                            .set("activation", new UserActivation(currentPlayer))
            );
            mailController.sendEmailChangedMail(currentPlayer);
        }
    }

    public List<Payment> getPaymentHistoryListForPlayer(Player currentPlayer) {
        final Query<Payment> paymentQuery = datastore.find(Payment.class);
        paymentQuery.field("playerId").equal(currentPlayer.getId());
        return paymentQuery.order("-payedAt").asList();
    }

    public List<Ticket> getActiveTicketListForPlayer(final Player currentPlayer) {
        final Query<Ticket> query = datastore.createQuery(Ticket.class).disableValidation();
        query.or(
                query.and(
                        query.criteria("playerId").equal(currentPlayer.getId()),
                        query.criteria("state").equal(TicketState.RUNNING),
                        query.criteria("endingDate").greaterThan(dateTimeService.getDateTimeNowUTC())),
                query.and(
                        query.criteria("playerId").equal(currentPlayer.getId()),
                        query.criteria("state").equal(TicketState.RUNNING),
                        query.criteria("permaTicket").equal(true)
                )
        );
        return query.order("-endingDate").asList();
    }

    // TODO should be optimized because of long running task
    public List<TicketHistoryWrapper> getTicketHistoryForPlayer(final Player currentPlayer) {
        List<TicketHistoryWrapper> ticketHistoryWrapperList = new LinkedList<>();
        Query<MainTicket> query = datastore.createQuery(MainTicket.class);
        query.filter("playerId", currentPlayer.getId());
        query.order(Sort.descending("startingDate"));
        final List<MainTicket> tickets = query.asList();
        final Map<String, Set<MainTicket>> lotteryToTicketsMap = tickets.stream()
                .collect(Collectors.groupingBy(MainTicket::getLotteryIdentifier,
                        Collectors.mapping(Function.identity(), Collectors.toSet())));
        for (Map.Entry<String, Set<MainTicket>> entry : lotteryToTicketsMap.entrySet()) {
            final String lotteryIdentifier = entry.getKey();
            final Lottery lottery = lotteryManager.getLotteryByIdentifier(entry.getKey());
            final Set<MainTicket> ticketSet = entry.getValue();
            final Optional<MainTicket> min = ticketSet.stream().min(Comparator.comparing(MainTicket::getStartingDate));
            final Optional<MainTicket> max = ticketSet.stream().filter(t -> t.getEndingDate() != null).max(Comparator.comparing(MainTicket::getEndingDate));
            final Optional<MainTicket> endless = ticketSet.stream().filter(t -> t.getEndingDate() == null).findFirst();
            if (!min.isPresent()) {
                // cannot be the case and would be a workflow-error, but returns an empty list for not breaking the GUI
                return new ArrayList<>();
            }
            final ZonedDateTime minStartingDate = min.get().getStartingDate();
            ZonedDateTime endingDate;
            if (endless.isPresent()) {
                endingDate = dateTimeService.getDateTimeNowUTC();
            } else {
                //noinspection ConstantConditions
                endingDate = max.get().getEndingDate();
            }
            // getting all drawings for necessary time range
            Map<ZonedDateTime, Drawing> drawingsForLottery = drawingsManager.getDrawingForLotteryAndTimeRange(lotteryIdentifier, minStartingDate, endingDate);
            for (MainTicket ticket : ticketSet) {
                ZonedDateTime sd = ticket.getStartingDate();
                while ((sd != null) && (sd.isBefore(endingDate) || sd.isEqual(endingDate))) {
                    Drawing drawing = drawingsForLottery.get(sd);
                    if (drawing != null) {
                        TicketHistoryWrapper ticketHistoryWrapper = new TicketHistoryWrapper(ticket);
                        ticketHistoryWrapper.addDrawing(drawing.getLotteryIdentifier(), drawing);
                        if (! ticket.getEmbeddedTickets().isEmpty()) {
                            for (Ticket t : (List<Ticket>) ticket.getEmbeddedTickets()) {
                                final Drawing drawingForAdditionalLottery = drawingsManager.getDrawingForLotteryAndTimestamp(t.getLotteryIdentifier(), sd);
                                if (drawingForAdditionalLottery != null) {
                                    ticketHistoryWrapper.addDrawing(drawingForAdditionalLottery.getLotteryIdentifier(), drawingForAdditionalLottery);
                                }
                            }
                        }
                        ticketHistoryWrapper.calculateTicketForStrokes();
                        ticketHistoryWrapperList.add(ticketHistoryWrapper);
                    }
                    sd = lottery.getNextDrawing(sd, DrawingType.fromType(ticket.getDrawingType()));
                }
            }
        }
        ticketHistoryWrapperList.sort((o1, o2) -> o2.getDrawingForTicket(o2.getMainTicket().getLotteryIdentifier()).getDate()
                .compareTo(o1.getDrawingForTicket(o1.getMainTicket().getLotteryIdentifier()).getDate()));
        return ticketHistoryWrapperList;
    }

    public void cancelPermaTicket(final MainTicket mainTicket) throws PaymentTransactionAbortedException {
        mainTicket.getPspCode().getPaymentController().cancelSubscription(mainTicket);
    }

    public String getAccountBalanceForPlayerFormatted(final Player currentPlayer) {
        int amountInCent = 0;
        final Query<Payment> query = datastore.createQuery(Payment.class);
        query.and(
                query.criteria("playerId").equal(currentPlayer.getId()),
                query.criteria("state").equal(PaymentState.SUCCESS),
                query.criteria("payedAt").exists());
        final AggregationPipeline aggregation = datastore.createAggregation(Payment.class)
                .match(query).group(Group.grouping("amountInCentSum", Group.sum("amountInCent")));
        final Iterator<BasicDBObject> aggregate = aggregation.aggregate(BasicDBObject.class);
        if (aggregate.hasNext()) {
            BasicDBObject db = aggregate.next();
            amountInCent = db.getInt("amountInCentSum");
            amountInCent *= -1;
        }
        return currencyFormatter.convertCentToEuroFormatted(amountInCent);
    }
}
