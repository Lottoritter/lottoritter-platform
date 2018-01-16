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
package de.lottoritter.business.lotteries.german6aus49;

import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.lotteries.spiel77.Spiel77Ticket;
import de.lottoritter.business.lotteries.super6.Super6Ticket;
import de.lottoritter.business.payment.entity.PriceList;

import de.lottoritter.business.temporal.control.DateTimeService;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Locale;

/**
 * @author Ulrich Cech
 */
public class German6aus49TicketTest {


    @Test
    public void testToJson() {
        German6aus49Ticket ticket = new German6aus49Ticket();
        ticket.addField(new German6aus49Field(1, new Integer[]{1,2,3,4,5,6}));
        ticket.addField(new German6aus49Field(2, new Integer[]{10,11,12,13,14,15}));
        ticket.setState(TicketState.INITIAL);
        ticket.setDrawingType("german6aus49wesa");
        ticket.setPermaTicket(true);
        ticket.setDurationOrBillingPeriod(1);
        ticket.setNumber(new Integer[]{1,2,3,4,5,6,7});
        final String expected = "{\"type\":\"german6Aus49Ticket\",\"lotteryIdentifier\":\"german6aus49\",\"embeddedTickets\":{},\"state\":\"INITIAL\",\"drawingType\":\"german6aus49wesa\",\"permaTicket\":true,\"durationOrBillingPeriod\":1,\"number\":[1,2,3,4,5,6,7],\"fields\":[{\"fieldNumber\":1,\"selectedNumbers\":[1,2,3,4,5,6],\"selectedAdditionalNumbers\":[]},{\"fieldNumber\":2,\"selectedNumbers\":[10,11,12,13,14,15],\"selectedAdditionalNumbers\":[]}]}";
        assertThat(ticket.toJson(German6aus49Ticket.class), is(expected));
    }

    @Test
    public void testFromString() {
        String ticketJSON = "{\"lotteryIdentifier\":\"german6aus49\",\"state\":\"INITIAL\",\"fields\":[{\"fieldNumber\":1,\"selectedNumbers\":[1,2,3,4,5,6]},{\"fieldNumber\":2,\"selectedNumbers\":[10,11,12,13,14,15]},{\"fieldNumber\":3,\"selectedNumbers\":[20,21,22,23,24,25]}],\"drawingType\":\"german6aus49wesa\",\"permaTicket\":true,\"durationOrBillingPeriod\":1,\"number\":[1,2,3,4,5,6,7]}";
        German6aus49Ticket ticket = Ticket.fromString(ticketJSON, German6aus49Ticket.class);
        assertThat(ticket, notNullValue(German6aus49Ticket.class));
        assertThat(ticket.getFields().size(), is(3));
        assertThat(ticket.getLotteryIdentifier(), is(German6aus49Lottery.IDENTIFIER));
        assertThat(ticket.getState(), CoreMatchers.is(TicketState.INITIAL));

        ticketJSON = "{\"lotteryIdentifier\":\"german6aus49\",\"fields\":[{\"fieldNumber\":1,\"selectedNumbers\":[27,19,5,11,20,41]},{\"fieldNumber\":2,\"selectedNumbers\":[28,21,30,22,3,49]},{\"fieldNumber\":3,\"selectedNumbers\":[4,16,45,1,10,26]}],\"drawingType\":\"german6aus49wesa\",\"additionalLotteries\":[],\"permaTicket\":false,\"durationOrBillingPeriod\":1,\"number\":[4,0,3,0,2,4,1]}";
        ticket = Ticket.fromString(ticketJSON, German6aus49Ticket.class);
        final German6aus49Ticket spyTicket = Mockito.spy(ticket);
        PriceList priceList = getCurrentPriceList();
        Mockito.doReturn(priceList).when(spyTicket).getCurrentPriceList(null);

        German6aus49Lottery lottery = new German6aus49Lottery();
        final German6aus49Lottery spyLottery = Mockito.spy(lottery);
        Mockito.when(spyLottery.getDateTimeService()).thenReturn(new DateTimeService());

        Mockito.doReturn(spyLottery).when(spyTicket).getLottery();

        spyTicket.initTicket();
        assertThat(spyTicket, notNullValue(German6aus49Ticket.class));
        assertThat(spyTicket.getFields().size(), is(3));
        assertThat(spyTicket.getLotteryIdentifier(), is(German6aus49Lottery.IDENTIFIER));
        assertThat(spyTicket.getState(), CoreMatchers.is(TicketState.INITIAL));
        assertThat(spyTicket.getTotalTicketPrice(), CoreMatchers.is(new Price(700, Currency.getInstance(Locale.GERMANY))));
    }

    private PriceList getCurrentPriceList() {
        PriceList priceList = new PriceList();
        priceList.setPricePerField(100);
        priceList.setFeeFirstDrawing(60);
        priceList.setFeeSecondDrawing(40);
        priceList.setFeeGluecksspirale(60);
        priceList.setLotteryIdentifier(German6aus49Lottery.IDENTIFIER);
        priceList.setPriceSuper6(125);
        priceList.setPriceSpiel77(250);
        priceList.setPriceGluecksspirale(500);
        return priceList;
    }

    @Test
    public void testFromString1() {
        String ticketJSON = "{\n" +
                "  \"lotteryIdentifier\": \"german6aus49\",\n" +
                "  \"fields\": [\n" +
                "    {\n" +
                "      \"fieldNumber\": 1,\n" +
                "      \"selectedNumbers\": [\n" +
                "        29,\n" +
                "        10,\n" +
                "        17,\n" +
                "        15,\n" +
                "        7,\n" +
                "        34\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"fieldNumber\": 2,\n" +
                "      \"selectedNumbers\": [\n" +
                "        20,\n" +
                "        16,\n" +
                "        2,\n" +
                "        6,\n" +
                "        40,\n" +
                "        1\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"drawing\": \"german6aus49wesa\",\n" +
                "  \"embeddedTickets\": [\n" +
                "    {\n" +
                "      \"super6\": {\n" +
                "        \"lotteryIdentifier\": \"super6\",\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"fieldNumber\": 1,\n" +
                "            \"selectedNumbers\": [\n" +
                "              1,\n" +
                "              2,\n" +
                "              3,\n" +
                "              4,\n" +
                "              5,\n" +
                "              6\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"spiel77\": {\n" +
                "        \"lotteryIdentifier\": \"spiel77\",\n" +
                "        \"fields\": [\n" +
                "          {\n" +
                "            \"fieldNumber\": 1,\n" +
                "            \"selectedNumbers\": [\n" +
                "              7,\n" +
                "              7,\n" +
                "              7,\n" +
                "              7,\n" +
                "              7,\n" +
                "              7,\n" +
                "              7\n" +
                "            ]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"permaTicket\": false,\n" +
                "  \"durationOrBillingPeriod\": 1,\n" +
                "  \"number\": [\n" +
                "    1,\n" +
                "    2,\n" +
                "    3,\n" +
                "    4,\n" +
                "    5,\n" +
                "    6,\n" +
                "    7\n" +
                "  ]\n" +
                "}";
//        String ticketJSON = "{\"lotteryIdentifier\":\"german6aus49\",\"fields\":[{\"fieldNumber\":1,\"selectedNumbers\":[29,10,17,15,7,34]},{\"fieldNumber\":2,\"selectedNumbers\":[20,16,2,6,40,1]}],\"drawing\":\"german6aus49wesa\",\"embeddedTickets\":[{\"super6\":\"{\"lotteryIdentifier\":\"super6\",\"fields\":[{\"fieldNumber\":1,\"selectedNumbers\":[1,2,3,4,5,6]}}],\"permaTicket\":false,\"durationOrBillingPeriod\":1,\"number\":[1,2,3,4,5,6,7]}";
        German6aus49Ticket ticket = Ticket.fromString(ticketJSON, German6aus49Ticket.class);
        assertThat(ticket, notNullValue(German6aus49Ticket.class));
        assertThat(ticket.getEmbeddedTickets().size(), is(2));
        assertThat(ticket.getEmbeddedTickets().get(0).getClass(), equalTo(Super6Ticket.class));
        assertThat(ticket.getEmbeddedTickets().get(1).getClass(), equalTo(Spiel77Ticket.class));
    }

    @Test
    public void testAdjust() {
        German6aus49Ticket ticket = new German6aus49Ticket();
        ticket.addField(new German6aus49Field(1, new Integer[]{1,2,3,4,5,6}));
        ticket.addField(new German6aus49Field(2, new Integer[]{10,11,12,13,14,15}));
        ticket.setState(TicketState.INITIAL);
        ticket.setDrawingType("german6aus49we");
        ticket.setPermaTicket(true);
        ticket.setDurationOrBillingPeriod(1);
        ticket.setNumber(new Integer[]{1,2,3,4,5,6,7});

        final German6aus49Ticket german6aus49Ticket = Mockito.spy(ticket);
        PriceList priceList = getCurrentPriceList();

        German6aus49Lottery lottery = new German6aus49Lottery();
        final German6aus49Lottery spyLottery = Mockito.spy(lottery);
        Mockito.when(spyLottery.getDateTimeService()).thenReturn(new DateTimeService());

        Mockito.doReturn(spyLottery).when(german6aus49Ticket).getLottery();
        Mockito.doReturn(priceList).when(german6aus49Ticket).getCurrentPriceList(null);

        german6aus49Ticket.initTicket();
        german6aus49Ticket.setStartingDate(spyLottery.getDateTimeNow().minusDays(1));
        german6aus49Ticket.adjust();

        ZonedDateTime expected = spyLottery.getNextDrawing(null, DrawingType.GERMAN6AUS49WE);
        assertThat(german6aus49Ticket.getStartingDate(), is(expected));
    }

}