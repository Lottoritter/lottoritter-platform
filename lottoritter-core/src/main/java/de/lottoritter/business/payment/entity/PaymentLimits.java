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

import de.lottoritter.business.validation.control.FutureDate;
import org.mongodb.morphia.annotations.Embedded;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Embedded
public class PaymentLimits {

    public static final Integer MAX_MONTH_LIMIT_IN_EURO = 1000;

    @Min(value = 0, message = "{payment_month_limit_min}", groups = {Default.class, PaymentLimitMonthGroup.class})
    @Max(value = 1000, message = "{payment_month_limit_max}", groups = {Default.class, PaymentLimitMonthGroup.class})
    private Integer monthLimitInEuro = MAX_MONTH_LIMIT_IN_EURO;

    @Min(value = 0, message = "{payment_new_month_limit_min}", groups = {Default.class, PaymentLimitMonthGroup.class})
    @Max(value = 1000, message = "{payment_new_month_limit_max}", groups = {Default.class, PaymentLimitMonthGroup.class})
    private Integer newMonthLimitInEuro;

    @FutureDate(message="", groups = {Default.class, PaymentLimitMonthGroup.class})
    private ZonedDateTime newMonthLimitValidFrom;

    @Min(value = 0, message = "{payment_week_limit_min}", groups = {Default.class, PaymentLimitWeekGroup.class})
    @Max(value = 1000, message = "{payment_week_limit_max}", groups = {Default.class, PaymentLimitWeekGroup.class})
    private Integer weekLimitInEuro;

    @Min(value = 0, message = "{payment_new_week_limit_min}", groups = {Default.class, PaymentLimitWeekGroup.class})
    @Max(value = 1000, message = "{payment_new_week_limit_max}", groups = {Default.class, PaymentLimitWeekGroup.class})
    private Integer newWeekLimitInEuro;

    @FutureDate(message="", groups = {Default.class, PaymentLimitWeekGroup.class})
    private ZonedDateTime newWeekLimitValidFrom;

    @Min(value = 0, message = "{payment_day_limit_min}", groups = {Default.class, PaymentLimitDayGroup.class})
    @Max(value = 1000, message = "{payment_day_limit_max}", groups = {Default.class, PaymentLimitDayGroup.class})
    private Integer dayLimitInEuro;

    @Min(value = 0, message = "{payment_new_day_limit_min}", groups = {Default.class, PaymentLimitDayGroup.class})
    @Max(value = 1000, message = "{payment_new_day_limit_max}", groups = {Default.class, PaymentLimitDayGroup.class})
    private Integer newDayLimitInEuro;

    @FutureDate(message="{}", groups = {Default.class, PaymentLimitDayGroup.class})
    private ZonedDateTime newDayLimitValidFrom;


    public PaymentLimits() {
    }

    public PaymentLimits(PaymentLimits paymentLimits) {
        this.monthLimitInEuro = paymentLimits.monthLimitInEuro;
        this.newMonthLimitInEuro = paymentLimits.newMonthLimitInEuro;
        this.newMonthLimitValidFrom = paymentLimits.newMonthLimitValidFrom;
        this.weekLimitInEuro = paymentLimits.weekLimitInEuro;
        this.newWeekLimitInEuro = paymentLimits.newWeekLimitInEuro;
        this.newWeekLimitValidFrom = paymentLimits.newWeekLimitValidFrom;
        this.dayLimitInEuro = paymentLimits.dayLimitInEuro;
        this.newDayLimitInEuro = paymentLimits.newDayLimitInEuro;
        this.newDayLimitValidFrom = paymentLimits.newDayLimitValidFrom;
    }


    public String getMonthLimitFormatted() {
        return getValueFormatted(this.monthLimitInEuro);
    }

    public String getWeekLimitFormatted() {
        return getValueFormatted(this.weekLimitInEuro);
    }

    public String getDayLimitFormatted() {
        return getValueFormatted(this.dayLimitInEuro);
    }

    private String getValueFormatted(Integer value) {
        if (value == null) {
            return "--";
        }
        return value + " " + Currency.getInstance(Locale.GERMANY).getCurrencyCode();
    }


    public Integer getMonthLimitInEuro() {
        return monthLimitInEuro;
    }

    public void setMonthLimitInEuro(Integer monthLimitInEuro) {
        this.monthLimitInEuro = monthLimitInEuro;
    }

    public Integer getNewMonthLimitInEuro() {
        return newMonthLimitInEuro;
    }

    public void setNewMonthLimitInEuro(Integer newMonthLimitInEuro) {
        this.newMonthLimitInEuro = newMonthLimitInEuro;
    }

    public ZonedDateTime getNewMonthLimitValidFrom() {
        return newMonthLimitValidFrom;
    }

    public void setNewMonthLimitValidFrom(ZonedDateTime newMonthLimitValidFrom) {
        this.newMonthLimitValidFrom = newMonthLimitValidFrom;
    }

    public Integer getWeekLimitInEuro() {
        return weekLimitInEuro;
    }

    public void setWeekLimitInEuro(Integer weekLimitInEuro) {
        this.weekLimitInEuro = weekLimitInEuro;
    }

    public Integer getNewWeekLimitInEuro() {
        return newWeekLimitInEuro;
    }

    public void setNewWeekLimitInEuro(Integer newWeekLimitInEuro) {
        this.newWeekLimitInEuro = newWeekLimitInEuro;
    }

    public ZonedDateTime getNewWeekLimitValidFrom() {
        return newWeekLimitValidFrom;
    }

    public void setNewWeekLimitValidFrom(ZonedDateTime newWeekLimitValidFrom) {
        this.newWeekLimitValidFrom = newWeekLimitValidFrom;
    }

    public Integer getDayLimitInEuro() {
        return dayLimitInEuro;
    }

    public void setDayLimitInEuro(Integer dayLimitInEuro) {
        this.dayLimitInEuro = dayLimitInEuro;
    }

    public Integer getNewDayLimitInEuro() {
        return newDayLimitInEuro;
    }

    public void setNewDayLimitInEuro(Integer newDayLimitInEuro) {
        this.newDayLimitInEuro = newDayLimitInEuro;
    }

    public ZonedDateTime getNewDayLimitValidFrom() {
        return newDayLimitValidFrom;
    }

    public void setNewDayLimitValidFrom(ZonedDateTime newDayLimitValidFrom) {
        this.newDayLimitValidFrom = newDayLimitValidFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentLimits)) return false;
        PaymentLimits that = (PaymentLimits) o;
        return Objects.equals(monthLimitInEuro, that.monthLimitInEuro) &&
                Objects.equals(newMonthLimitInEuro, that.newMonthLimitInEuro) &&
                Objects.equals(newMonthLimitValidFrom, that.newMonthLimitValidFrom) &&
                Objects.equals(weekLimitInEuro, that.weekLimitInEuro) &&
                Objects.equals(newWeekLimitInEuro, that.newWeekLimitInEuro) &&
                Objects.equals(newWeekLimitValidFrom, that.newWeekLimitValidFrom) &&
                Objects.equals(dayLimitInEuro, that.dayLimitInEuro) &&
                Objects.equals(newDayLimitInEuro, that.newDayLimitInEuro) &&
                Objects.equals(newDayLimitValidFrom, that.newDayLimitValidFrom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(monthLimitInEuro, newMonthLimitInEuro, newMonthLimitValidFrom, weekLimitInEuro,
                newWeekLimitInEuro, newWeekLimitValidFrom, dayLimitInEuro, newDayLimitInEuro, newDayLimitValidFrom);
    }
}
