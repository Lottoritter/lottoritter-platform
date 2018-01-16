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

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Ticket;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
public class TicketHistoryWrapper {

    private MainTicket mainTicket;

    private Map<String, Drawing> drawingsForLottery = new HashMap<>();

    private Map<String, List<CalculatedField>> calculatedFieldsForLottery = new HashMap<>();


    public TicketHistoryWrapper() {
    }

    public TicketHistoryWrapper(MainTicket mainTicket) {
        this.mainTicket = mainTicket;
    }


    public Drawing getDrawingForTicket(final String lotteryIdentifier) {
        for (Map.Entry<String, Drawing> entry : drawingsForLottery.entrySet()) {
            if(lotteryIdentifier.equals(entry.getKey())){
                return entry.getValue();
            }
        }
        return null;
    }

    public void addDrawing(String lotteryIdentifier, Drawing drawing) {
        this.drawingsForLottery.put(lotteryIdentifier, drawing);
    }

    public void calculateTicketForStrokes() {
        calculatedFieldsForLottery = new HashMap<>();
        calculatedFieldsForLottery.put(mainTicket.getLotteryIdentifier(), mainTicket.calculateStrokes(getDrawingForTicket(mainTicket.getLotteryIdentifier())));
        for (Ticket embeddedTicket : (List<Ticket>) mainTicket.getEmbeddedTickets()) {
            calculatedFieldsForLottery.put(embeddedTicket.getLotteryIdentifier(), embeddedTicket.calculateStrokes(getDrawingForTicket(embeddedTicket.getLotteryIdentifier())));
        }
    }

    public MainTicket getMainTicket() {
        return mainTicket;
    }

    public void setMainTicket(MainTicket mainTicket) {
        this.mainTicket = mainTicket;
    }

//    public boolean strokeAdditionalNumber(Integer number) {
//        Drawing drawing = getDrawingForTicket(EuroJackpotLottery.IDENTIFIER); // TODO ulrich: do not hard-code here
//        if (! drawing.hasAdditionalNumbers()) {
//            return false;
//        }
//        Integer[] resultNumbers = drawing.getAdditionalNumbers();
//        List<Integer> result = Arrays.asList(resultNumbers);
//        return result.contains(number);
//    }
//
//    public boolean strokeNumberForAdditionalLottery(Drawing drawing, Integer ticketNumberIndex, Integer number) {
//        if (!(mainTicket instanceof German6aus49Ticket)) {
//            return false;
//        }
//        Integer[] results = drawing.getNumbers();
//        Integer result = results[ticketNumberIndex];
//        boolean numberOnIndexStroked = Objects.equals(result, number);
//        Integer[] selectedNumbers = new Integer[0];
//        // TODO ulrich: IMPORTANT reactivate
////        selectedNumbers = ticket.getAdditionalLotteryTicketForLotteryIdentifier(drawing.getLotteryIdentifier()).getNumbers();
//        return numberOnIndexStroked && prevNumbersStroked(ticketNumberIndex, selectedNumbers, results);
//    }

//    private boolean prevNumbersStroked(Integer index, Integer[] selectedNumbers, Integer[] resultNumbers) {
//        boolean allPrevStroked = true;
//        for (int i=index; i < selectedNumbers.length; ++i) {
//            allPrevStroked = allPrevStroked && (Objects.equals(resultNumbers[i], selectedNumbers[i]));
//        }
//        return allPrevStroked;
//    }

    public List<CalculatedField> getCalculatedFieldsForLottery(final String lotteryIdentifier) {
        return calculatedFieldsForLottery.get(lotteryIdentifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketHistoryWrapper)) return false;
        TicketHistoryWrapper that = (TicketHistoryWrapper) o;
        return Objects.equals(mainTicket, that.mainTicket) &&
                Objects.equals(drawingsForLottery, that.drawingsForLottery) &&
                Objects.equals(calculatedFieldsForLottery, that.calculatedFieldsForLottery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mainTicket, drawingsForLottery, calculatedFieldsForLottery);
    }
}
