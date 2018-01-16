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
package de.lottoritter.business.tickets.control;

import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.TicketState;
import de.lottoritter.business.temporal.control.DateTimeService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 *
 * @author Ulrich Cech
 */
@Singleton
@Startup
@DependsOn("ConfigurationController")
public class TicketChecker {

    private static final Logger logger = Logger.getLogger(TicketChecker.class.getName());

    @Inject
    Datastore datastore;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    ActivityLogController activityLogController;

    @Inject @Configurable(value = "runJobs", defaultValue = "false")
    Instance<Boolean> runJobs;


    public TicketChecker() {
    }


    @Schedule(hour = "*", minute = "45")
    public void closeFinishedTickets() {
        if (runJobs.get()) {
            activityLogController.saveActivityLog(new ObjectId(), ActivityType.JOB_TICKET_CLOSING, "text", "Starting Ticker-checker job...");
            try {
                Query<MainTicket> query = datastore.createQuery(MainTicket.class);
                query.filter("state", TicketState.RUNNING.name());
                final List<MainTicket> tickets = query.asList();
                for (MainTicket ticket : tickets) {
                    if (ticket.isValidForCurrentDate(dateTimeService.getDateTimeNowEurope())) {
                        ticket.setState(TicketState.CLOSED);
                        datastore.update(
                                datastore.createQuery(Ticket.class).field("_id").equal(ticket.getId()),
                                datastore.createUpdateOperations(Ticket.class).set("state", TicketState.CLOSED)
                        );
                        activityLogController.saveActivityLog(ticket.getId(), ActivityType.JOB_TICKET_CLOSED);
                    }
                }
            } catch (Exception ex) {
                logger.severe("Error occurred while executing TicketChecker.\r\n" + ex.getMessage());
            }
        }
    }

}
