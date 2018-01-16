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
package de.lottoritter.business.lotteries.plus5;

import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.CalculatedNumber;
import de.lottoritter.business.lotteries.EmbeddedTicket;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.keno.KenoLottery;
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
@XmlRootElement(name = "plus5")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Embedded
public class Plus5Ticket extends Ticket<Plus5Field, Plus5Lottery, Plus5Drawing> implements EmbeddedTicket {

    private static final long serialVersionUID = -6356851414425834045L;

    private List<Plus5Field> fields = new ArrayList<>();

    @Transient
    private MainTicket mainTicket;


    public Plus5Ticket() {
        this.setLotteryIdentifier(Plus5Lottery.IDENTIFIER);
    }

    public Plus5Ticket(MainTicket mainTicket) {
        this();
        this.mainTicket = mainTicket;
        Plus5Field field = new Plus5Field();
        field.setSelectedNumbers(Arrays.asList(mainTicket.getNumber()));
        fields.add(field);
    }


    @Override
    public Plus5Lottery getLottery() {
        return CDIBeanService.getInstance().getCDIBean(Plus5Lottery.class);
    }

    @Override
    public void addField(Plus5Field field) {
        throw new UnsupportedOperationException("Not available for Plus5Ticket");
    }

    @Override
    public List<Plus5Field> getFields() {
        return fields;
    }

    @Override
    public void sortFieldNumbers() {
        // ATTENTION: never sort the numbers of the fields!!!
    }

    @Override
    public int getTicketPriceMultiplicator() {
        return getParentTicket().getTicketPriceMultiplicator();
    }

    @Override
    public int calculateTicketPrice(PriceList currentPriceList, int multiplicator) {
        return currentPriceList.getPricePlus5() * multiplicator;
    }

    @Override
    public int calculateTicketFee(PriceList currentPriceList, int mulitplicator) {
        return currentPriceList.getFeePlus5();
    }

    @Override
    public boolean adjust() {
        return false;
    }

    @Override
    public boolean isValidForCurrentDate(ZonedDateTime currentDateTime) {
        throw new RuntimeException("Plus5 is not main-ticket");
    }

    @Override
    public boolean canAcceptAdditionalLottery(String additionalLottery) {
        return false;
    }

    @Override
    public List<CalculatedField> calculateStrokes(Plus5Drawing drawing) {
        List<CalculatedField> calculatedFields = new ArrayList<>();
        List<Integer> drawnNumbers = Arrays.asList(drawing.getNumbers());
        Plus5Field field = getFields().get(0);
        CalculatedField calculatedField = new CalculatedField(field);
        for (Integer integer : field.getSelectedNumbers()) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(integer, drawnNumbers.contains(integer)));
        }
        boolean notHit = false;
        for (int i = 4; i >= 0 ; i--) {
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
            throw new RuntimeException("Kein gültiges Plus5-Ticket");
        }
        final Plus5Field plus5Field = getFields().get(0);
        if (getParentTicket() != null) {
            plus5Field.setParentTicket(getParentTicket());
        }
        if (! plus5Field.isValid()) {
            throw new RuntimeException("Keine gültige Plus5-Losnummer");
        }
    }

    @Override
    @XmlTransient
    public void setParentTicket(MainTicket mainTicket) {
        this.mainTicket = mainTicket;
    }

    @Override
    @XmlTransient
    public MainTicket getParentTicket() {
        return this.mainTicket;
    }

    @Override
    public String getMainLotteryIdentifier() {
        return KenoLottery.IDENTIFIER;
    }

    @XmlElement
    public void setFields(List<Plus5Field> fields) {
        this.fields = fields;
    }
}
