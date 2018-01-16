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
package de.lottoritter.presentation.admin.dashboard.control;

import de.lottoritter.business.payment.control.PaymentStatisticsController;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class MngmDashboardViewController implements Serializable {

    private static final long serialVersionUID = 7394368712873096346L;

    @Inject
    PaymentStatisticsController paymentStatisticsController;

    @Inject
    DateTimeService dateTimeService;

    private BarChartModel paymentYearOverviewBarChartModel = new BarChartModel();


    TicketsPaymentsOverviewDataHolder ticketsPaymentsOverviewDataHolder;

    ZonedDateTime currentDateTime;


    public MngmDashboardViewController() {
    }


    @PostConstruct
    public void init() {
        currentDateTime = dateTimeService.getDateTimeNowEurope();
        ticketsPaymentsOverviewDataHolder = new TicketsPaymentsOverviewDataHolder();
        createTicketsPaymentsOverviewChart();
    }

    private void createTicketsPaymentsOverviewChart() {
        paymentYearOverviewBarChartModel = new BarChartModel();
        ChartSeries ticketsOfMonthsOfCurrentYearDataSeries = new ChartSeries();
        ticketsOfMonthsOfCurrentYearDataSeries.setLabel("Tickets");
        ticketsOfMonthsOfCurrentYearDataSeries.set("Januar", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[0]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("Februar", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[1]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("März", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[2]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("April", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[3]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("Mai", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[4]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("Juni", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[5]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("Juli", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[6]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("August", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[7]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("September", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[8]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("Oktober", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[9]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("November", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[10]);
        ticketsOfMonthsOfCurrentYearDataSeries.set("Dezember", ticketsPaymentsOverviewDataHolder.sumOfTicketsForMonthOfCurrentYear[11]);
        ChartSeries paymentSumOfMonthsOfCurrentYearDataSeries = new ChartSeries();
        paymentSumOfMonthsOfCurrentYearDataSeries.setLabel("Payments");
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Januar", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[0]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Februar", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[1]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("März", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[2]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("April", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[3]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Mai", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[4]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Juni", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[5]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Juli", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[6]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("August", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[7]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("September", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[8]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Oktober", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[9]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("November", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[10]));
        paymentSumOfMonthsOfCurrentYearDataSeries.set("Dezember", convertToEuro(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear[11]));

        paymentYearOverviewBarChartModel.addSeries(ticketsOfMonthsOfCurrentYearDataSeries);
        paymentYearOverviewBarChartModel.addSeries(paymentSumOfMonthsOfCurrentYearDataSeries);

        paymentYearOverviewBarChartModel.setTitle("Tickets/Payments per Month");
        paymentYearOverviewBarChartModel.setLegendPosition("ne");
        Axis yAxis = paymentYearOverviewBarChartModel.getAxis(AxisType.Y);
        yAxis.setLabel("Amount in EUR");
        yAxis.setMin(0);
        yAxis.setMax(convertToEuro(getMaxPaymentMonthValue(ticketsPaymentsOverviewDataHolder.sumOfPaymentsForMonthOfCurrentYear)));
    }

    private BigDecimal convertToEuro(Integer value) {
        return new BigDecimal(BigInteger.valueOf(value)).divide(new BigDecimal("100.00"), 2,
                BigDecimal.ROUND_CEILING);
    }

    private int getMaxPaymentMonthValue(Integer[] values) {
        Optional<Integer> maxValue = Arrays.stream(values).max(Integer::compareTo);
        return maxValue.orElse(0);
    }

    public BarChartModel getPaymentYearOverviewBarChartModel() {
        return paymentYearOverviewBarChartModel;
    }

    public String getCurrentMonthName() {
        return dateTimeService.getMonthNameFor(currentDateTime);
    }

    public Integer getCurrentYear() {
        return currentDateTime.getYear();
    }

    public TicketsPaymentsOverviewDataHolder getTicketsPaymentsOverviewDataHolder() {
        return ticketsPaymentsOverviewDataHolder;
    }

    public class TicketsPaymentsOverviewDataHolder {
        private Integer[] sumOfPaymentsForMonthOfCurrentYear;
        private Integer[] sumOfTicketsForMonthOfCurrentYear;
        private BigDecimal paymentSumOfYear = BigDecimal.ZERO;
        private Integer amountOfTicketsYear = 0;


        public TicketsPaymentsOverviewDataHolder() {
            sumOfPaymentsForMonthOfCurrentYear =
                    paymentStatisticsController.getSumOfPaymentsForMonthOfCurrentYear(
                            dateTimeService.getBeginningOfCurrentYear(), dateTimeService.getEndingOfCurrentYear());
            paymentSumOfYear = convertToEuro(Arrays.stream(sumOfPaymentsForMonthOfCurrentYear).mapToInt(Integer::intValue).sum());
            sumOfTicketsForMonthOfCurrentYear =
                    paymentStatisticsController.getSumOfTicketsForMonthOfCurrentYear(
                            dateTimeService.getBeginningOfCurrentYear(), dateTimeService.getEndingOfCurrentYear());
            amountOfTicketsYear = Arrays.stream(sumOfTicketsForMonthOfCurrentYear).mapToInt(Integer::intValue).sum();
        }

        public BigDecimal getPaymentSumOfCurrentMonth() {
            return convertToEuro(sumOfPaymentsForMonthOfCurrentYear[(currentDateTime.getMonthValue() - 1)]);
        }

        public Integer getTicketsOfCurrentMonth() {
            return sumOfTicketsForMonthOfCurrentYear[(currentDateTime.getMonthValue() - 1)];
        }

        public BigDecimal getPaymentSumOfYear() {
            return paymentSumOfYear;
        }

        public void setPaymentSumOfYear(BigDecimal paymentSumOfYear) {
            this.paymentSumOfYear = paymentSumOfYear;
        }

        public Integer getAmountOfTicketsYear() {
            return amountOfTicketsYear;
        }

        public void setAmountOfTicketsYear(Integer amountOfTicketsYear) {
            this.amountOfTicketsYear = amountOfTicketsYear;
        }

        public Integer[] getSumOfPaymentsForMonthOfCurrentYear() {
            return sumOfPaymentsForMonthOfCurrentYear;
        }

        public Integer[] getSumOfTicketsForMonthOfCurrentYear() {
            return sumOfTicketsForMonthOfCurrentYear;
        }
    }
}
