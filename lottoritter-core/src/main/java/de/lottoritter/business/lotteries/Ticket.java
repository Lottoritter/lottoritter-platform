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

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotField;
import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotTicket;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Ticket;
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleTicket;
import de.lottoritter.business.lotteries.keno.KenoTicket;
import de.lottoritter.business.lotteries.plus5.Plus5Ticket;
import de.lottoritter.business.lotteries.spiel77.Spiel77Ticket;
import de.lottoritter.business.lotteries.super6.Super6Ticket;
import de.lottoritter.business.payment.control.PriceListController;
import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.platform.cdi.CDIBeanService;
import de.lottoritter.platform.persistence.PersistentEntity;
import org.bson.types.ObjectId;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Ulrich Cech
 */
@Entity(value = "tickets", noClassnameStored = false) // important to store the classname here
@XmlSeeAlso({ German6aus49Ticket.class, EuroJackpotField.class, Super6Ticket.class, Spiel77Ticket.class,
        GluecksspiraleTicket.class, KenoTicket.class, Plus5Ticket.class})
public abstract class Ticket<F extends Field, L extends Lottery, D extends Drawing> extends PersistentEntity {

    private static final Class[] TICKET_CLASSES = {German6aus49Ticket.class,
                                                   EuroJackpotTicket.class,
                                                   EuroJackpotField.class,
                                                   Super6Ticket.class,
                                                   Spiel77Ticket.class,
                                                   GluecksspiraleTicket.class,
                                                   KenoTicket.class,
                                                   Plus5Ticket.class};


    private static final long serialVersionUID = -5689447275625529363L;

    @XmlElement
    private String lotteryIdentifier;

    @XmlElement
    private Price ticketPrice;

    @XmlElement
    private Price ticketFee;

    @XmlElement
    private ObjectId priceListId;

    @Embedded
    @XmlElementWrapper
    @XmlAnyElement(lax = true)
    private List<Ticket> embeddedTickets = new ArrayList<>();


    public Ticket() {
    }


    public abstract L getLottery();

    public abstract void addField(F field);

    public abstract List<F> getFields();

    public abstract void sortFieldNumbers();

    public abstract void validate();

    public abstract boolean adjust();

    public abstract int calculateTicketPrice(PriceList currentPriceList, int multiplicator);

    public abstract int calculateTicketFee(PriceList currentPriceList, int multiplicator);

    public abstract boolean isValidForCurrentDate(ZonedDateTime currentDateTime);

    public abstract boolean canAcceptAdditionalLottery(String additionalLottery);

    public abstract List<CalculatedField> calculateStrokes(D drawing);


    public void calculateAllTicketPrices() {
        final PriceList currentPriceList = getCurrentPriceList(getPriceListId());
        final int ticketPriceMultiplicator = getTicketPriceMultiplicator();
        setTicketFee(new Price(calculateTicketFee(currentPriceList, ticketPriceMultiplicator)));
        setTicketPrice(new Price(calculateTicketPrice(currentPriceList, ticketPriceMultiplicator)));
        // TODO ulrich: delegate price calculation and setting to concrete ticket class
        for (Ticket embeddedTicket : embeddedTickets) {
            embeddedTicket.setTicketFee(new Price(embeddedTicket.calculateTicketFee(currentPriceList, ticketPriceMultiplicator)));
            embeddedTicket.setTicketPrice(new Price(embeddedTicket.calculateTicketPrice(currentPriceList, ticketPriceMultiplicator)));
        }
    }

    public int getTicketPriceMultiplicator() {
        return 1;
    }


    public PriceList getCurrentPriceList(ObjectId priceListId) {
        final PriceListController priceListController = CDIBeanService.getInstance().getCDIBean(PriceListController.class);
        if (priceListId == null) {
            final PriceList priceList = priceListController.getPriceListForLottery(getLotteryIdentifier());
            setPriceListId(priceList.getId());
            return priceList;
        } else {
            return priceListController.getPriceListForId(priceListId);
        }
    }

    public static <T extends MainTicket> T fromString(String currentTicketAsString, Class<T> ticketClass) {
        try {
            JAXBContext jc = JAXBContext.newInstance(TICKET_CLASSES);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
            StringReader reader = new StringReader(currentTicketAsString);
            StreamSource source = new StreamSource(reader);
            return unmarshaller.unmarshal(source, ticketClass).getValue();
        } catch (JAXBException ex) {
            // TODO ulrich: logger
            throw new RuntimeException(ex);
        }
    }

    public <T extends Ticket> String toJson(Class<T> ticketClass) {
        try {
            JAXBContext jc = JAXBContext.newInstance(ticketClass);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
            marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
            StringWriter writer = new StringWriter();
            marshaller.marshal(this, writer);
            return writer.toString();
        } catch (JAXBException ex) {
            // TODO ulrich: logger
            throw new RuntimeException(ex);
        }
    }

    public Ticket getEmbeddedTicket(String lotteryIdentifier) {
        final Optional<Ticket> first = getEmbeddedTickets().stream().filter(t -> t.getLotteryIdentifier().equals(lotteryIdentifier)).findFirst();
        return first.orElse(null);
    }

    public String getLotteryIdentifier() {
        return lotteryIdentifier;
    }

    public void setLotteryIdentifier(String lotteryIdentifier) {
        this.lotteryIdentifier = lotteryIdentifier;
    }

    public ObjectId getPriceListId() {
        return priceListId;
    }

    public void setPriceListId(ObjectId priceListId) {
        this.priceListId = priceListId;
    }

    public Price getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(Price ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public Price getTicketFee() {
        return ticketFee;
    }

    public void setTicketFee(Price ticketFee) {
        this.ticketFee = ticketFee;
    }

    public List<Ticket> getEmbeddedTickets() {
        return embeddedTickets;
    }

    public void setEmbeddedTickets(List<Ticket> embeddedTickets) {
        this.embeddedTickets = embeddedTickets;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ticket)) return false;
        if (!super.equals(o)) return false;
        Ticket<?, ?, ?> ticket = (Ticket<?, ?, ?>) o;
        return Objects.equals(lotteryIdentifier, ticket.lotteryIdentifier) &&
                Objects.equals(ticketPrice, ticket.ticketPrice) &&
                Objects.equals(ticketFee, ticket.ticketFee) &&
                Objects.equals(priceListId, ticket.priceListId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lotteryIdentifier, ticketPrice, ticketFee, priceListId);
    }
}
