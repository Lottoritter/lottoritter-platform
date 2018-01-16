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
package de.lottoritter.business.lotteries;

import java.io.Serializable;

import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleTicket;
import de.lottoritter.business.lotteries.plus5.Plus5Ticket;
import de.lottoritter.business.lotteries.spiel77.Spiel77Ticket;
import de.lottoritter.business.lotteries.super6.Super6Ticket;
import de.lottoritter.business.payment.entity.PriceList;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Ulrich Cech
 */
@XmlSeeAlso({ Super6Ticket.class, Spiel77Ticket.class, GluecksspiraleTicket.class, Plus5Ticket.class})
public interface EmbeddedTicket extends Serializable {

    String getLotteryIdentifier();

    String getMainLotteryIdentifier();

    int calculateTicketFee(PriceList currentPriceList, int ticketPriceMultiplicator);

    int calculateTicketPrice(PriceList currentPriceList, int ticketPriceMultiplicator);

    void validate();

    void setParentTicket(MainTicket mainTicket);

    MainTicket getParentTicket();

}
