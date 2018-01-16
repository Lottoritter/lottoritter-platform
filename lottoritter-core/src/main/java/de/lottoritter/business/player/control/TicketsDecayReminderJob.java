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
package de.lottoritter.business.player.control;

import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.lotteries.DrawingType;
import de.lottoritter.business.lotteries.Lottery;
import de.lottoritter.business.lotteries.LotteryManager;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.control.ShoppingCartRepository;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Christopher Schmidt
 */
@Singleton
@Startup
@DependsOn("ConfigurationController")
public class TicketsDecayReminderJob {

    private static final Logger logger = Logger.getLogger(TicketsDecayReminderJob.class.getName());

    private static final int MINUTES_BEFORE_DEADLINE_REMINDER = 30;

    @Inject
    Datastore datastore;

    @Inject
    @Configurable(value = "runJobs", defaultValue = "false")
    Instance<Boolean> runJobs;

    @Inject
    private MailController mailController;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    private ShoppingCartRepository shoppingCartRepository;

    @Inject
    private LotteryManager lotteryManager;

    @Inject
    private UserRepository userRepository;

    @Schedule(hour = "*", minute = "*/15", second = "*", persistent = false)
    public void execute() {
        if (runJobs.get()) {
            activityLogController.saveActivityLog(new ObjectId(), ActivityType.JOB_TICKET_DECAY, "text", "Starting TicketsDecayReminder job...");
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
            List<Lottery> closingLottery = lotteryManager.getLotteries().stream().filter(lottery -> lottery.getNextDrawing(null, null).isBefore(now.plusMinutes(MINUTES_BEFORE_DEADLINE_REMINDER))).collect(Collectors.toList());
            if (closingLottery.isEmpty()) {
                return;
            }
            try {
                List<ShoppingCart> shoppingCarts = shoppingCartRepository.getAllShoppingsCartWithTickets();

                for (ShoppingCart shoppingCart : shoppingCarts) {
                    List<Ticket> tickets = shoppingCart.getTicketList().stream().filter(t -> t.getLottery().getNextDrawing(null, DrawingType.fromType(t.getDrawingType())).isBefore(t.getStartingDate().plusMinutes(MINUTES_BEFORE_DEADLINE_REMINDER))).collect(Collectors.toList());

                    if (! tickets.isEmpty()) {
                        Player player = userRepository.findById(shoppingCart.getPlayerId());
                        mailController.sendTicketsDecayReminderMail(player);
                    }
                }
            } catch (Exception ex) {
                logger.severe("Error occurred while executing TicketsDecayReminderJob.\r\n" + ex.getMessage());
            }
        }
    }
}
