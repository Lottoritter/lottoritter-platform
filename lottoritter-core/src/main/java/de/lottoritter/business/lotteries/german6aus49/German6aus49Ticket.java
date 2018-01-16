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

import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.CalculatedNumber;
import de.lottoritter.business.lotteries.DrawingType;
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
import java.util.List;

import static de.lottoritter.business.lotteries.DrawingType.GERMAN6AUS49WESA;
import static de.lottoritter.business.lotteries.DrawingType.fromType;
import static de.lottoritter.business.lotteries.DrawingType.isValid;

/**
 * @author Christopher Schmidt
 */

@Entity(value = "tickets", noClassnameStored = false) // important to store the classname here
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class German6aus49Ticket extends MainTicket<German6aus49Field, German6aus49Lottery, German6aus49Drawing> {

    private static final long serialVersionUID = 8696352398054657477L;

    private List<German6aus49Field> fields = new ArrayList<>();


    public German6aus49Ticket() {
        setLotteryIdentifier(German6aus49Lottery.IDENTIFIER);
    }


    @Override
    public void sortFieldNumbers() {
        for (German6aus49Field field : getFields()) {
            if (field.isValidFilled()) {
                Collections.sort(field.getSelectedNumbers());
            }
        }
    }

    @Override
    public void initStartingAndEndingDate() {
        setStartingDate(getLottery().getNextDrawing(null, fromType(getDrawingType())));
        calculateAndSetEndingDate();
    }

    @Override
    public int getNumberOfDrawings() {
        final DrawingType drawingType = fromType(getDrawingType());
        final int duration = getDurationOrBillingPeriod();
        final int multiplier = (drawingType == GERMAN6AUS49WESA) ? 2 : 1;
        return (duration * multiplier);
    }

    @Override
    public int getTicketPriceMultiplicator() {
        int multiplicatorDuration = getDurationOrBillingPeriod();
        return multiplicatorDuration * (GERMAN6AUS49WESA.getTypeAsString().equals(getDrawingType()) ? 2 : 1);
    }

    @Override
    public int calculateTicketPrice(PriceList currentPriceList, int multiplicator) {
        int amountOfFields = (int) getFields().stream().filter(Field::isValidFilled).count();
        return amountOfFields * (currentPriceList.getPricePerField() * multiplicator);
    }

    @Override
    public int calculateTicketFee(PriceList currentPriceList, int multiplicator) {
        int fee = currentPriceList.getFeeFirstDrawing();
        if (GERMAN6AUS49WESA.getTypeAsString().equals(getDrawingType())) {
            fee += currentPriceList.getFeeSecondDrawing();
        }
        return fee;
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
        if (!isOmitValidation()) {
            // at least one field
            if (getFields() == null || getFields().isEmpty()) {
                throw new RuntimeException("Mind 1 Feld");
            }
            // all fields must be valid
            for (German6aus49Field field : getFields()) {
                if (!field.isValid()) {
                    throw new RuntimeException("Feld nicht vollständig");
                }
            }
            // ticket number must be valid
            if ((getNumber() == null) || (getNumber().length != 7)) {
                throw new RuntimeException("TicketNummer nicht vollständig");
            }
            for (Integer num : getNumber()) {
                if ((num == null) || (num < 0) || (num > 9)) {
                    throw new RuntimeException("TicketNummer nicht gültig");
                }
            }
            // drawing type must be an available option
            DrawingType drawingType = fromType(getDrawingType());
            if (!isValid(getDrawingType())) {
                throw new RuntimeException("Kein gültiger Ziehungstag.");
            }
            // startingDate
            if (getStartingDate() == null || getStartingDate().isBefore(getLottery().getDateTimeNow())
                    || !drawingType.isValidForDate(getStartingDate())) {
                throw new RuntimeException("Ungültiges Startdatum für das Ticket.");
            }
            // duration must be an available selection
            int[] availableDurations = new int[]{1, 2, 3, 4, 5, 8};
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
    }

    @Override
    public boolean adjust() {
        final German6aus49Lottery lottery = getLottery();
        final DrawingType drawingType = fromType(getDrawingType());
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
        if (getState() == TicketState.INITIAL) {
            if (currentDateTime.isAfter(getStartingDate())) {
                setStartingDate(lottery.getNextDrawing(currentDateTime, drawingType));
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
            final German6aus49Lottery german6aus49Lottery = getLottery();
            final DrawingType drawingType = fromType(getDrawingType());
            final int looping = getNumberOfDrawings() - 1;
            ZonedDateTime temp = getStartingDate();
            for (int i = 0; i < looping; i++) {
                temp = german6aus49Lottery.getNextDrawing(temp, drawingType);
            }
            setEndingDate(temp);
        }
    }


    public boolean isValidForCurrentDate(ZonedDateTime currentDateTime) {
        // be aware of permaTickets (endingDate = null)
        return getEndingDate() == null || getEndingDate().isAfter(currentDateTime);
    }

    @Override
    public boolean canAcceptAdditionalLottery(String additionalLotteryIdentifier) {
        return getLottery().getPossibleAdditionalLotteries().contains(additionalLotteryIdentifier);
//        return additionalLotteryIdentifier.equals(GluecksspiraleLottery.IDENTIFIER) && !getDrawingType().contains("sa");
    }


    @Override
    public List<CalculatedField> calculateStrokes(German6aus49Drawing drawing) {
        List<CalculatedField> calculatedFields = new ArrayList<>();
        List<Integer> additionalNumberList = Arrays.asList(drawing.getSuperzahl());
        List<Integer> drawnNumbers = Arrays.asList(drawing.getNumbers());
        for (Field field : getFields()) {
            if (field.isValidFilled()) {
                CalculatedField calculatedField = new CalculatedField(field);
                for (Integer integer : field.getSelectedNumbers()) {
                    calculatedField.addCalculatedNumber(new CalculatedNumber(integer, drawnNumbers.contains(integer)));
                }
                for (Integer integer : field.getSelectedAdditionalNumbers()) {
                    calculatedField.addCalculatedAdditionalNumber(new CalculatedNumber(integer, additionalNumberList.contains(integer)));
                }
                calculatedFields.add(calculatedField);
            }
        }
        return calculatedFields;
    }

    @Override
    public German6aus49Lottery getLottery() {
        return CDIBeanService.getInstance().getCDIBean(German6aus49Lottery.class);
    }


    private boolean isAdditionalLotterySelected(String additionalLotteryIdentifier) {
        return getEmbeddedTickets().stream().anyMatch(t -> t.getLotteryIdentifier().equals(additionalLotteryIdentifier));
    }

    @Override
    public List<German6aus49Field> getFields() {
        return this.fields;
    }

    @XmlElement
    public void setFields(List<German6aus49Field> fields) {
        this.fields = fields;
    }

    @Override
    public void addField(German6aus49Field field) {
        this.fields.add(field);
    }
}
