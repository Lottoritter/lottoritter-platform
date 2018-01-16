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
package de.lottoritter.business.lotteries.spiel77;

import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.CalculatedNumber;
import de.lottoritter.business.lotteries.EmbeddedTicket;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Lottery;
import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.platform.cdi.CDIBeanService;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@XmlRootElement(name = "spiel77")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Embedded
public class Spiel77Ticket extends Ticket<Spiel77Field, Spiel77Lottery, Spiel77Drawing> implements EmbeddedTicket {

    private static final long serialVersionUID = 5599964703741131794L;


    private List<Spiel77Field> fields = new ArrayList<>();

    @Transient
    private MainTicket mainTicket;


    public Spiel77Ticket() {
        this.setLotteryIdentifier(Spiel77Lottery.IDENTIFIER);
    }

    public Spiel77Ticket(MainTicket mainTicket) {
        this();
        this.mainTicket = mainTicket;
        Spiel77Field field = new Spiel77Field();
        field.setSelectedNumbers(Arrays.asList(Arrays.copyOfRange(mainTicket.getNumber(), 0, 7)));
        fields.add(field);
    }


    @Override
    public Spiel77Lottery getLottery() {
        return CDIBeanService.getInstance().getCDIBean(Spiel77Lottery.class);
    }

    @Override
    public void addField(Spiel77Field field) {
        throw new UnsupportedOperationException("Not available for Spiel77Ticket");
    }

    @Override
    public List<Spiel77Field> getFields() {
        return fields;
    }

    @Override
    public void sortFieldNumbers() {
        // ATTENTION: never sort the numbers of the fields!!!
    }

    @Override
    public int calculateTicketPrice(PriceList currentPriceList, int multiplicator) {
        return currentPriceList.getPriceSpiel77() * multiplicator;
    }

    @Override
    public int calculateTicketFee(PriceList currentPriceList, int mulitplicator) {
        return 0;
    }

    @Override
    public boolean adjust() {
        return false;
    }

    @Override
    public boolean isValidForCurrentDate(ZonedDateTime currentDateTime) {
        throw new RuntimeException("Spiel77 is not main-ticket");
    }

    @Override
    public boolean canAcceptAdditionalLottery(String additionalLottery) {
        return false;
    }

    @Override
    public List<CalculatedField> calculateStrokes(Spiel77Drawing drawing) {
        List<CalculatedField> calculatedFields = new ArrayList<>();
        List<Integer> drawnNumbers = Arrays.asList(drawing.getNumbers());
        Spiel77Field field = getFields().get(0);
        CalculatedField calculatedField = new CalculatedField(field);
        for (Integer integer : field.getSelectedNumbers()) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(integer, drawnNumbers.contains(integer)));
        }
        boolean notHit = false;
        for (int i = 6; i >= 0 ; i--) {
            if (notHit) {
                calculatedField.getNumbers().get(i).setHit(false);
                continue;
            }
            if (! calculatedField.getNumbers().get(i).isHit()) {
                notHit = true;
            }
        }
        calculatedFields.add(calculatedField);
        return calculatedFields;
    }


    @Override
    public void validate() {
        if (getFields() == null || getFields().isEmpty() || getFields().size() != 1) {
            throw new RuntimeException("Kein gültiges Spiel77-Ticket");
        }
        final Spiel77Field spiel77Field = getFields().get(0);
        if (getParentTicket() != null) {
            spiel77Field.setParentTicket(getParentTicket());
        }
        if (! spiel77Field.isValid()) {
            throw new RuntimeException("Keine gültige Spiel77-Losnummer");
        }
    }

    @Override
    @XmlTransient
    public void setParentTicket(MainTicket mainTicket) {
        this.mainTicket = mainTicket;
    }

    @Override
    public MainTicket getParentTicket() {
        return this.mainTicket;
    }

    @Override
    @XmlTransient
    public String getMainLotteryIdentifier() {
        return German6aus49Lottery.IDENTIFIER;
    }

    @XmlElement
    public void setFields(List<Spiel77Field> fields) {
        this.fields = fields;
    }
}
