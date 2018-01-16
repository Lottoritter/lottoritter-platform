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
package de.lottoritter.business.drawings.control;

import de.lottoritter.business.drawings.entity.DrawingResult;
import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.Drawing;
import de.lottoritter.business.lotteries.DrawingsManager;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Stateless
public class DrawingsResultController {

    private static final Logger logger = Logger.getLogger(DrawingsResultController.class.getName());


    @Inject
    DrawingsManager drawingsManager;

    @Inject
    Datastore datastore;


    public void executeCalculation(final Lottery lottery, final ZonedDateTime timestampOfDrawing) {
        final Drawing drawingForLotteryAndTimestamp = drawingsManager.getDrawingForLotteryAndTimestamp(lottery
                .getIdentifier(), timestampOfDrawing);
        if (drawingForLotteryAndTimestamp == null) {
            logger.severe(MessageFormat.format("No drawing found for lottery <{0}> and timestamp <{1}>", lottery.getIdentifier(), timestampOfDrawing));
            return;
        }
        // all tickets, which are either permaTickets or whose end-date is greater than the current date
        final Query<MainTicket> query = datastore.createQuery(MainTicket.class);
        query.disableValidation();
        query.and(
                query.criteria("lotteryIdentifier").equal(lottery.getIdentifier()),
                query.or(
                        query.criteria("permaTicket").equal(true),
                        query.criteria("endingDate").greaterThanOrEq(timestampOfDrawing)
                )
        );
        final List<MainTicket> allTicketsForLotteryDrawing = query.asList();
        List<DrawingResult> results = new LinkedList<>();
        for (MainTicket ticket : allTicketsForLotteryDrawing) {
            List<CalculatedField> calculatedFields = ticket.calculateStrokes(drawingForLotteryAndTimestamp);
            List<CalculatedField> wonFields = new LinkedList<>();
            for (CalculatedField calculatedField : calculatedFields) {
                lottery.getWinCalculator().calculateRank(lottery.getIdentifier(), calculatedField);
                if (calculatedField.getWinningRank() != null) {
                    wonFields.add(calculatedField);
                }
            }
            if (! wonFields.isEmpty()) {
                results.add(new DrawingResult(drawingForLotteryAndTimestamp, ticket, wonFields));
            }
            // calculate embedded tickets
            for (Ticket embeddedTicket : (List<Ticket>) ticket.getEmbeddedTickets()) {
                final Drawing embeddedDrawingForLotteryAndTimestamp =
                        drawingsManager.getDrawingForLotteryAndTimestamp(embeddedTicket.getLotteryIdentifier(), timestampOfDrawing);
                List<CalculatedField> wonFieldsOfEmbeddedTicket = new LinkedList<>();
                List<CalculatedField> calculatedFieldsOfEmbeddedTicket = embeddedTicket.calculateStrokes(embeddedDrawingForLotteryAndTimestamp);
                for (CalculatedField calculatedFieldOfEmbeddedTicket : calculatedFieldsOfEmbeddedTicket) {
                    Lottery lotteryOfEmbeddedTicket = embeddedTicket.getLottery();
                    lotteryOfEmbeddedTicket.getWinCalculator().calculateRank(lotteryOfEmbeddedTicket.getIdentifier(), calculatedFieldOfEmbeddedTicket);
                    if (calculatedFieldOfEmbeddedTicket.getWinningRank() != null) {
                        wonFieldsOfEmbeddedTicket.add(calculatedFieldOfEmbeddedTicket);
                    }
                }
                if (! wonFieldsOfEmbeddedTicket.isEmpty()) {
                    results.add(new DrawingResult(drawingForLotteryAndTimestamp, embeddedTicket, wonFieldsOfEmbeddedTicket));
                }
            }
        }
        if (! results.isEmpty()) {
            datastore.save(results);
        }
    }

}
