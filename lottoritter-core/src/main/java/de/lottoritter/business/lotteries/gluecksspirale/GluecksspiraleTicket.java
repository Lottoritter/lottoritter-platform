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
package de.lottoritter.business.lotteries.gluecksspirale;

import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.CalculatedNumber;
import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.EmbeddedTicket;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketState;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ulrich Cech
 */
@XmlRootElement(name = "gluecksspirale")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Embedded
public class GluecksspiraleTicket extends MainTicket<GluecksspiraleField, GluecksspiraleLottery, GluecksspiraleDrawing> implements EmbeddedTicket {

    private static final long serialVersionUID = 2058998809909037371L;

    private List<GluecksspiraleField> fields = new ArrayList<>();

    @Transient
    private MainTicket mainTicket;


    public GluecksspiraleTicket() {
        this.setLotteryIdentifier(GluecksspiraleLottery.IDENTIFIER);
    }

    public GluecksspiraleTicket(MainTicket mainTicket) {
        this();
        this.mainTicket = mainTicket;
        GluecksspiraleField field = new GluecksspiraleField();
        field.setSelectedNumbers(Arrays.asList(Arrays.copyOfRange(mainTicket.getNumber(), 0, 7)));
        fields.add(field);
    }

    @Override
    public GluecksspiraleLottery getLottery() {
        return CDIBeanService.getInstance().getCDIBean(GluecksspiraleLottery.class);
    }

    @Override
    public void addField(GluecksspiraleField field) {
        throw new UnsupportedOperationException("Not available for Glücksspirale-Ticket");
    }

    @Override
    public List<GluecksspiraleField> getFields() {
        return fields;
    }

    @Override
    public void sortFieldNumbers() {
        // ATTENTION: never sort the numbers of the fields!!!
    }

    @Override
    public int calculateTicketPrice(PriceList currentPriceList, int multiplicator) {
        if (mainTicket != null) {
            return currentPriceList.getPriceGluecksspirale() * mainTicket.getDurationOrBillingPeriod();
        } else {
            return currentPriceList.getPriceGluecksspirale() * getDurationOrBillingPeriod();
        }
    }

    @Override
    public int calculateTicketFee(PriceList currentPriceList, int mulitplicator) {
        return currentPriceList.getFeeGluecksspirale();
    }

    @Override
    public boolean adjust() {
        if (isMainTicket()) {
            final GluecksspiraleLottery lottery = getLottery();
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
        return false;
    }

    @Override
    public boolean isValidForCurrentDate(ZonedDateTime currentDateTime) {
        // be aware of permaTickets (endingDate = null)
        return getEndingDate() == null || getEndingDate().isAfter(currentDateTime);
    }

    @Override
    public boolean canAcceptAdditionalLottery(String additionalLottery) {
        return false;
    }

    @Override
    public List<CalculatedField> calculateStrokes(GluecksspiraleDrawing drawing) {
        if (drawing == null) {
            return new ArrayList<>();
        }
        List<CalculatedField> calculatedFields = new ArrayList<>();
        GluecksspiraleField field = getFields().get(0);
        CalculatedField calculatedField = new CalculatedField(field);

        final Integer[] selectedNumbers = field.getSelectedNumbers().toArray(new Integer[field.getSelectedNumbers().size()]);
        Integer[] selectedNumbersForComparison = field.getSelectedNumbers().toArray(new Integer[field.getSelectedNumbers().size()]);

        Integer[] gluecksspiraleNumber = drawing.getGluecksspiraleNumber(7, 2);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            for (Integer integer : field.getSelectedNumbers()) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(integer, true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }
        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(7, 1);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            for (Integer integer : field.getSelectedNumbers()) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(integer, true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }

        selectedNumbersForComparison = Arrays.copyOfRange(selectedNumbersForComparison, 1, selectedNumbers.length);
        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(6, 2);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            for (int i = 1; i < field.getSelectedNumbers().size() ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(i), true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }
        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(6, 1);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            for (int i = 1; i < field.getSelectedNumbers().size() ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(i), true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }

        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(5, 1);
        selectedNumbersForComparison = Arrays.copyOfRange(selectedNumbers, 2, selectedNumbers.length);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(1), false));
            for (int i = 2; i < selectedNumbers.length ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(selectedNumbers[i], true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }

        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(4, 1);
        selectedNumbersForComparison = Arrays.copyOfRange(selectedNumbers, 3, selectedNumbers.length);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(1), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(2), false));
            for (int i = 3; i < selectedNumbers.length ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(selectedNumbers[i], true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }

        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(3, 1);
        selectedNumbersForComparison = Arrays.copyOfRange(selectedNumbers, 4, selectedNumbers.length);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(1), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(2), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(3), false));
            for (int i = 4; i < selectedNumbers.length ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(selectedNumbers[i], true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }

        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(2, 1);
        selectedNumbersForComparison = Arrays.copyOfRange(selectedNumbers, 5, selectedNumbers.length);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(1), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(2), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(3), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(4), false));
            for (int i = 5; i < selectedNumbers.length ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(selectedNumbers[i], true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }

        gluecksspiraleNumber = drawing.getGluecksspiraleNumber(1, 1);
        selectedNumbersForComparison = Arrays.copyOfRange(selectedNumbers, 6, selectedNumbers.length);
        if (Arrays.equals(selectedNumbersForComparison, gluecksspiraleNumber)) {
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(0), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(1), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(2), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(3), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(4), false));
            calculatedField.addCalculatedNumber(new CalculatedNumber(field.getSelectedNumbers().get(5), false));
            for (int i = 6; i < selectedNumbers.length ; i++) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(selectedNumbers[i], true));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        } else {
            for (Integer selectedNumber : selectedNumbers) {
                calculatedField.addCalculatedNumber(new CalculatedNumber(selectedNumber, false));
            }
            calculatedFields.add(calculatedField);
            return calculatedFields;
        }
    }


    @Override
    public void validate() {
        if (mainTicket != null) {
            if (getFields() == null || getFields().isEmpty() || getFields().size() != 1) {
                throw new RuntimeException("Kein gültiges Glücksspirale-Ticket");
            }
            final GluecksspiraleField gluecksspiraleField = getFields().get(0);
            if (! isMainTicket()) {
                gluecksspiraleField.setParentTicket(getParentTicket());
            }
            if (! gluecksspiraleField.isValid()) {
                throw new RuntimeException("Keine gültige Glücksspirale-Losnummer");
            }
            if (!DrawingType.isDrawingTypeForGermanGluecksspirale(getParentTicket().getDrawingType())) {
                throw new RuntimeException("Gluecksspirale nur bei Samstag-Ziehungen");
            }
        }
    }

    @Override
    @XmlTransient
    public void setParentTicket(MainTicket mainTicket) {
        this.mainTicket = mainTicket;
        GluecksspiraleField field = new GluecksspiraleField();
        field.setSelectedNumbers(Arrays.asList(Arrays.copyOfRange(mainTicket.getNumber(), 0, 7)));
        fields.clear();
        fields.add(field);

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
    public void setFields(List<GluecksspiraleField> fields) {
        this.fields = fields;
    }

    @Override
    public void initStartingAndEndingDate() {
        if (isMainTicket()) {
            final ZonedDateTime nextDrawing = getLottery().getNextDrawing(null, null);
            setStartingDate(nextDrawing);
            calculateAndSetEndingDate();
        }
    }

    @Override
    public int getNumberOfDrawings() {
        if (isMainTicket()) {
            return getDurationOrBillingPeriod();
        } else {
            return 0;
        }
    }

    @Override
    public Price getTotalTicketPrice() {
        if (isMainTicket()) {
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
        return new Price(0);
    }

    private void calculateAndSetEndingDate() {
        if (isMainTicket()) {
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
    }

    private boolean isMainTicket() {
        return (getParentTicket() == null);
    }
}