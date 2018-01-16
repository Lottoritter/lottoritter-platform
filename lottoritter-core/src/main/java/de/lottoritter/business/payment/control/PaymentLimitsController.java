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

import com.mongodb.BasicDBObject;
import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.payment.entity.Payment;
import de.lottoritter.business.payment.entity.PaymentLimitDayGroup;
import de.lottoritter.business.payment.entity.PaymentLimitMonthGroup;
import de.lottoritter.business.payment.entity.PaymentLimitWeekGroup;
import de.lottoritter.business.payment.entity.PaymentLimits;
import de.lottoritter.business.payment.entity.PaymentState;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.business.validation.control.ValidationController;
import de.lottoritter.platform.ResourceBundleRepository;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.aggregation.Group;
import org.mongodb.morphia.query.Query;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Ulrich Cech
 */
@Stateless
public class PaymentLimitsController {

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    private ResourceBundleRepository resourceBundleRepository;


    private Javers javers = JaversBuilder.javers().registerValueObject(Player.class).build();


    public PaymentLimitsController() {
    }


    public boolean changePaymentLimits(Player player, PaymentLimits newPaymentLimits) {
        PaymentLimits oldLimits = new PaymentLimits(player.getPaymentLimits());
        boolean changed = false;
        if ((newPaymentLimits.getMonthLimitInEuro() != null)
                && !newPaymentLimits.getMonthLimitInEuro().equals(player.getPaymentLimits().getMonthLimitInEuro())) {
            changePaymentMonthLimit(player, newPaymentLimits);
            changed = true;
        }
        if ((newPaymentLimits.getWeekLimitInEuro() != null)
                && !newPaymentLimits.getWeekLimitInEuro().equals(player.getPaymentLimits().getWeekLimitInEuro())) {
            changePaymentWeekLimit(player, newPaymentLimits);
            changed = true;
        }
        if ((newPaymentLimits.getDayLimitInEuro() != null)
                && !newPaymentLimits.getDayLimitInEuro().equals(player.getPaymentLimits().getDayLimitInEuro())) {
            changePaymentDayLimit(player, newPaymentLimits);
            changed = true;
        }
        logLimitChanges(player, oldLimits, newPaymentLimits, ActivityType.CHANGE_LIMITS);
        return changed;
    }

    private void changePaymentDayLimit(Player player, PaymentLimits limits) {
        if (limits.getDayLimitInEuro().equals(player.getPaymentLimits().getDayLimitInEuro())) {
            return; // no changes
        }
        ValidationController.get(PaymentLimits.class)
                .processBeanValidationForGroup(limits, PaymentLimitDayGroup.class);
        final Integer existingDayLimitInEuro = player.getPaymentLimits().getDayLimitInEuro();
        // if existing limit is null (initial no limit set), or the new limit is lower than the existing limit,
        //  then the limit is active at once
        if (existingDayLimitInEuro == null || limits.getDayLimitInEuro() < existingDayLimitInEuro ) {
            player.getPaymentLimits().setDayLimitInEuro(limits.getDayLimitInEuro());
        } else {  // new limit is higher then existing limit, then the limit is active in (now + 7 days).
            if (limits.getDayLimitInEuro() > existingDayLimitInEuro) {
                player.getPaymentLimits().setNewDayLimitInEuro(limits.getDayLimitInEuro());
                ZonedDateTime utc = dateTimeService.getDateTimeNowUTC();
                utc = utc.plus(7, ChronoUnit.DAYS);
                player.getPaymentLimits().setNewDayLimitValidFrom(utc);
                limits.setNewDayLimitValidFrom(player.getPaymentLimits().getNewDayLimitValidFrom());
            }
        }
        savePlayerPaymentLimits(player, player.getPaymentLimits());
    }

    private void changePaymentWeekLimit(Player player, PaymentLimits limits) {
        if (limits.getWeekLimitInEuro().equals(player.getPaymentLimits().getWeekLimitInEuro())) {
            return; // no changes
        }
        ValidationController.get(PaymentLimits.class)
                .processBeanValidationForGroup(limits, PaymentLimitWeekGroup.class);
        final Integer existingWeekLimitInEuro = player.getPaymentLimits().getWeekLimitInEuro();
        // if existing limit is null (initial no limit set), or the new limit is lower than the existing limit,
        //  then the limit is active at once
        if (existingWeekLimitInEuro == null || limits.getWeekLimitInEuro() < existingWeekLimitInEuro ) {
            player.getPaymentLimits().setWeekLimitInEuro(limits.getWeekLimitInEuro());
        } else {  // new limit is higher then existing limit, then the limit is active in (now + 7 days).
            if (limits.getWeekLimitInEuro() > existingWeekLimitInEuro) {
                player.getPaymentLimits().setNewWeekLimitInEuro(limits.getWeekLimitInEuro());
                ZonedDateTime utc = dateTimeService.getDateTimeNowUTC();
                utc = utc.plus(7, ChronoUnit.DAYS);
                player.getPaymentLimits().setNewWeekLimitValidFrom(utc);
                limits.setNewWeekLimitValidFrom(player.getPaymentLimits().getNewWeekLimitValidFrom());
            }
        }
        savePlayerPaymentLimits(player, player.getPaymentLimits());
    }

    private void changePaymentMonthLimit(Player player, PaymentLimits limits) {
        if (limits.getMonthLimitInEuro().equals(player.getPaymentLimits().getMonthLimitInEuro())) {
            return; // no changes
        }
        ValidationController.get(PaymentLimits.class)
                .processBeanValidationForGroup(limits, PaymentLimitMonthGroup.class);
        final Integer existingMonthLimitInEuro = player.getPaymentLimits().getMonthLimitInEuro();
        // if existing limit is null (initial no limit set), or the new limit is lower than the existing limit,
        //  then the limit is active at once
        if (existingMonthLimitInEuro == null || limits.getMonthLimitInEuro() < existingMonthLimitInEuro) {
            player.getPaymentLimits().setMonthLimitInEuro(limits.getMonthLimitInEuro());
        } else {  // new limit is higher then existing limit, then the limit is active in (now + 7 days).
            if (limits.getMonthLimitInEuro() > existingMonthLimitInEuro) {
                player.getPaymentLimits().setNewMonthLimitInEuro(limits.getMonthLimitInEuro());
                ZonedDateTime utc = dateTimeService.getDateTimeNowUTC();
                utc = utc.plus(7, ChronoUnit.DAYS);
                player.getPaymentLimits().setNewMonthLimitValidFrom(utc);
                limits.setNewMonthLimitValidFrom(player.getPaymentLimits().getNewMonthLimitValidFrom());
            }
        }
        savePlayerPaymentLimits(player, player.getPaymentLimits());
    }

    private void savePlayerPaymentLimits(final Player player, final PaymentLimits limits) {
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).set("paymentLimits", limits)
        );
    }

    private void logLimitChanges(Player player, PaymentLimits oldLimits, PaymentLimits newLimits, ActivityType activityType) {
        final Diff compare = javers.compare(oldLimits, newLimits);
        List<String> changedData = new ArrayList<>();
        for (ValueChange valueChange : compare.getChangesByType(ValueChange.class)) {
            changedData.add(valueChange.getPropertyName());
            changedData.add("oldValue:" + valueChange.getLeft().toString()
                    + ";newValue:" + ((valueChange.getRight() != null) ? valueChange.getRight().toString(): ""));
        }
        activityLogController.saveActivityLog(player, activityType, changedData.toArray(new String[changedData.size()]));
    }

    public void checkPaymentLimit(final Player player, ShoppingCart shoppingCart, Locale locale) {
        // ATTENTION: this order of checks (day, week, month) is important!
        if (player.isBlocked()) {
            throw new RuntimeException(resourceBundleRepository.getDefaultLocalized( "user.profile.tabs.limits.checkPaymentLimit.blocked", locale));
        }
        if (isDayLimitReached(player, shoppingCart.getAmountInCent())) {
            throw new RuntimeException(resourceBundleRepository.getDefaultLocalized("user.profile.tabs.limits.checkPaymentLimit.dayLimitReched", locale));
        }
        if (isWeekLimitReached(player, shoppingCart.getAmountInCent())) {
            throw new RuntimeException(resourceBundleRepository.getDefaultLocalized("user.profile.tabs.limits.checkPaymentLimit.weekLimitReched", locale));
        }
        if (isMonthLimitReached(player, shoppingCart.getAmountInCent())) {
            throw new RuntimeException(resourceBundleRepository.getDefaultLocalized("user.profile.tabs.limits.checkPaymentLimit.monthLimitReched", locale));
        }
    }


    boolean isDayLimitReached(final Player player, final Integer amountInCentOfCurrentShoppingCart) {
        final ZonedDateTime now = dateTimeService.getDateTimeNowEurope();
        final ZonedDateTime beginDate = dateTimeService.getBeginningTimestampOfDayUTC(now);
        final ZonedDateTime endDate = dateTimeService.getEndingTimestampOfDayUTC(now);
        return isLimitReached(player, amountInCentOfCurrentShoppingCart, beginDate, endDate,
                getLimitFromOptional(player.getPaymentLimits().getDayLimitInEuro()));
    }

    boolean isWeekLimitReached(final Player player, final Integer amountInCentOfCurrentShoppingCart) {
        final ZonedDateTime now = dateTimeService.getDateTimeNowEurope();
        final ZonedDateTime beginDate = dateTimeService.getBeginningTimestampOfWeekUTC(now);
        final ZonedDateTime endDate = dateTimeService.getEndingTimestampOfWeekUTC(now);
        return isLimitReached(player, amountInCentOfCurrentShoppingCart, beginDate, endDate,
                getLimitFromOptional(player.getPaymentLimits().getWeekLimitInEuro()));
    }

    boolean isMonthLimitReached(final Player player, final Integer amountInCentOfCurrentShoppingCart) {
        final ZonedDateTime now = dateTimeService.getDateTimeNowEurope();
        final ZonedDateTime beginDate = dateTimeService.getBeginningTimestampOfMonthUTC(now);
        final ZonedDateTime endDate = dateTimeService.getEndingTimestampOfMonthUTC(now);
        return isLimitReached(player, amountInCentOfCurrentShoppingCart, beginDate, endDate,
                getLimitFromOptional(player.getPaymentLimits().getMonthLimitInEuro()));
    }

    private Integer getLimitFromOptional(Integer playerLimit) {
        Optional<Integer> limit = Optional.ofNullable(playerLimit);
        return limit.orElse(PaymentLimits.MAX_MONTH_LIMIT_IN_EURO);
    }

    private boolean isLimitReached(final Player player, final Integer amountInCentOfCurrentShoppingCart,
                                   final ZonedDateTime beginDateUTC, final ZonedDateTime endDateUTC, Integer currentLimit) {
        // all shoppingcarts for a given player, which have a state of CLOSED and payedAt is set and where
        //  payedAt is in the range of the current month
        final Query<Payment> query = datastore.createQuery(Payment.class);
        query.and(
                query.criteria("playerId").equal(player.getId()),
                query.criteria("state").equal(PaymentState.SUCCESS),
                query.criteria("payedAt").exists(),
                query.criteria("payedAt").greaterThanOrEq(beginDateUTC),
                query.criteria("payedAt").lessThanOrEq(endDateUTC));
        final AggregationPipeline aggregation = datastore.createAggregation(Payment.class)
                .match(query).group(Group.grouping("amountInCentSum", Group.sum("amountInCent")));
        final Iterator<BasicDBObject> aggregate = aggregation.aggregate(BasicDBObject.class);
        if (aggregate.hasNext()) {
            BasicDBObject db = aggregate.next();
            Integer sum = db.getInt("amountInCentSum");
            Integer currentLimitInCent = currentLimit * 100;
            return (sum + amountInCentOfCurrentShoppingCart) > currentLimitInCent;
        }
        return false;
    }

    /**
     * Is called every time the player logs in. It checks, if the validFrom of a paymentLimit is reached and sets the
     * new value to the current value and sets new value and validFrom to null.
     */
    public void adjustLimitsForPlayer(final Player player) {
        final ZonedDateTime now = dateTimeService.getDateTimeNowUTC();
        final PaymentLimits oldLimits = new PaymentLimits(player.getPaymentLimits());
        final PaymentLimits newPaymentLimits = player.getPaymentLimits();
        // check for activating new month-limit
        boolean changed = false;
        if ((newPaymentLimits.getNewMonthLimitValidFrom() != null) && (newPaymentLimits.getNewMonthLimitValidFrom().isBefore(now))) {
            newPaymentLimits.setMonthLimitInEuro(newPaymentLimits.getNewMonthLimitInEuro());
            newPaymentLimits.setNewMonthLimitInEuro(null);
            newPaymentLimits.setNewMonthLimitValidFrom(null);
            changed = true;
        }
        // check for activating new week-limit
        if ((newPaymentLimits.getNewWeekLimitValidFrom() != null) && (newPaymentLimits.getNewWeekLimitValidFrom().isBefore(now))) {
            newPaymentLimits.setWeekLimitInEuro(newPaymentLimits.getNewWeekLimitInEuro());
            newPaymentLimits.setNewWeekLimitInEuro(null);
            newPaymentLimits.setNewWeekLimitValidFrom(null);
            changed = true;
        }
        // check for activating new day-limit
        if ((newPaymentLimits.getNewDayLimitValidFrom() != null) && (newPaymentLimits.getNewDayLimitValidFrom().isBefore(now))) {
            newPaymentLimits.setDayLimitInEuro(newPaymentLimits.getNewDayLimitInEuro());
            newPaymentLimits.setNewDayLimitInEuro(null);
            newPaymentLimits.setNewDayLimitValidFrom(null);
            changed = true;
        }
        if (changed) {
            savePlayerPaymentLimits(player, newPaymentLimits);
            logLimitChanges(player, oldLimits, newPaymentLimits, ActivityType.ADJUST_LIMITS);
        }
    }

}
