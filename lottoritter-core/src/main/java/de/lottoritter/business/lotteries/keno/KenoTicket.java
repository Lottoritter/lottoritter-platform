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
package de.lottoritter.business.lotteries.keno;

import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.CalculatedNumber;
import de.lottoritter.business.lotteries.Field;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.payment.entity.PriceList;
import de.lottoritter.platform.cdi.CDIBeanService;
import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */

@Entity(value = "tickets", noClassnameStored = false) // important to store the classname here
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class KenoTicket extends MainTicket<KenoField, KenoLottery, KenoDrawing> {

    private static final long serialVersionUID = 2452704128170502166L;

    private List<KenoField> fields = new ArrayList<>();

    private int bet;


    public KenoTicket() {
        setLotteryIdentifier(KenoLottery.IDENTIFIER);
    }


    @Override
    public void sortFieldNumbers() {
        for (KenoField field : getFields()) {
            if (field.isValidFilled()) {
                Collections.sort(field.getSelectedNumbers());
            }
            Collections.sort(field.getSelectedAdditionalNumbers());
        }
    }

    @Override
    public void initStartingAndEndingDate() {
        final ZonedDateTime nextDrawing = getLottery().getNextDrawing(null, null);
        setStartingDate(nextDrawing);
        calculateAndSetEndingDate();
    }

    @Override
    public int getNumberOfDrawings() {
        return getDurationOrBillingPeriod();
    }

    @Override
    public int getTicketPriceMultiplicator() {
        return getDurationOrBillingPeriod();
    }

    @Override
    public int calculateTicketPrice(PriceList currentPriceList, int multiplicator) {
        int amountOfFields = (int) getFields().stream().filter(Field::isValidFilled).count();
        return amountOfFields * getBet() * multiplicator;
    }

    @Override
    public int calculateTicketFee(PriceList currentPriceList, int multiplicator) {
        return currentPriceList.getFeeFirstDrawing();
    }

    @Override
    public Price getTotalTicketPrice() {
        // sum all ticket prices and all ticket fees
        int sumPrice = getTicketPrice().getAmountInCent();
        int sumFee = getTicketFee().getAmountInCent();
        if (! getEmbeddedTickets().isEmpty()) {
            for (Ticket embeddedTicket : getEmbeddedTickets()) {
                sumPrice += embeddedTicket.getTicketPrice().getAmountInCent();
                sumFee += embeddedTicket.getTicketFee().getAmountInCent();
            }
        }
        return new Price(sumPrice + sumFee);
    }

    @Override
    public void validate() {
        // at least one field
        if (getFields() == null || getFields().isEmpty()) {
            throw new RuntimeException("Mind 1 Feld");
        }
        // all fields must be valid
        for (KenoField field : getFields()) {
            if (!field.isValid()) {
                throw new RuntimeException("Feld nicht vollständig");
            }
        }
        // ticket number must be valid
        if ((getNumber() == null) || (getNumber().length != 5)) {
            throw new RuntimeException("TicketNummer nicht vollständig");
        }
        for (Integer num : getNumber()) {
            if ((num == null) || (num < 0) || (num > 9)) {
                throw new RuntimeException("TicketNummer nicht gültig");
            }
        }
        // TODO ulrich: REACTIVATE drawingType
        // drawing type must be an available option
//        DrawingType drawingType = fromType(getDrawingType());
//        if (!isValid(getDrawingType())) {
//            throw new RuntimeException("Kein gültiger Ziehungstag.");
//        }
        // startingDate
        if (getStartingDate() == null || getStartingDate().isBefore(getLottery().getDateTimeNow())) {
            throw new RuntimeException("Ungültiges Startdatum für das Ticket.");
        }
        // duration must be an available selection
        int[] availableDurations = new int[] {1,2,3,4,5,6,7};
        boolean ok = false;
        for (int availableDuration : availableDurations) {
            if (availableDuration == getDurationOrBillingPeriod()) {
                ok = true;
                break;
            }
        }
        if (!ok) {
            throw new RuntimeException("Die Laufzeit oder das Abbuchungsintervall sind ungültig");
        }
        for (Ticket embeddedTicket : getEmbeddedTickets()) {
            embeddedTicket.validate();
        }
    }

    @Override
    public boolean adjust() {
        final KenoLottery lottery = getLottery();
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        if (getState() == TicketState.INITIAL) {
            if (currentDateTime.isAfter(getStartingDate())) {
                final ZonedDateTime nextDrawing = lottery.getNextDrawing(currentDateTime, null);
                setStartingDate(nextDrawing);
                calculateAndSetEndingDate();
                return true;
            }
        }
        return false;
    }


    private void calculateAndSetEndingDate() {
        if (isPermaTicket()) {
            setEndingDate(null);
        } else {
            final int looping = getNumberOfDrawings() - 1;
            ZonedDateTime temp = getStartingDate();
            for (int i = 0; i < looping; i++) {
                temp = getLottery().getNextDrawing(temp, null);
            }
            setEndingDate(temp);
        }
    }

    @Override
    public boolean isValidForCurrentDate(ZonedDateTime currentDateTime) {
        // be aware of permaTickets (endingDate = null)
        return getEndingDate() == null || getEndingDate().isAfter(currentDateTime);
    }

    @Override
    public boolean canAcceptAdditionalLottery(String additionalLotteryIdentifier) {
        return getLottery().getPossibleAdditionalLotteries().contains(additionalLotteryIdentifier);
    }

    @Override
    public List<CalculatedField> calculateStrokes(KenoDrawing drawing) {
        List<Integer> numberList = Arrays.asList(drawing.getNumbers());
        List<CalculatedField> wonFields = new LinkedList<>();
        for (KenoField kenoField : getFields()) {
            if (kenoField.isValidFilled()) {
                CalculatedField calculatedField = new CalculatedField(kenoField);
                for (Integer integer : kenoField.getSelectedNumbers()) {
                    calculatedField.addCalculatedNumber(new CalculatedNumber(integer, numberList.contains(integer)));
                }
                wonFields.add(calculatedField);
            }
        }
        return wonFields;
    }

    @Override
    public KenoLottery getLottery() {
        return CDIBeanService.getInstance().getCDIBean(KenoLottery.class);
    }

    @Override
    public List<KenoField> getFields() {
        return this.fields;
    }

    @XmlElement
    public void setFields(List<KenoField> fields) {
        this.fields = fields;
    }

    @Override
    public void addField(KenoField field) {
        this.fields.add(field);
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KenoTicket)) return false;
        if (!super.equals(o)) return false;
        KenoTicket that = (KenoTicket) o;
        return bet == that.bet &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), fields, bet);
    }
}
