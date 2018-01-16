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
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.PlayerPostCreationEvent;
import de.lottoritter.business.player.entity.UserState;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Singleton
@Startup
@DependsOn("ConfigurationController")
public class SendWelcomeMailJob {

    private static final Logger logger = Logger.getLogger(SendWelcomeMailJob.class.getName());

    @Inject
    Datastore datastore;

    @Inject
    ActivityLogController activityLogController;

    @Inject @Configurable(value = "runJobs", defaultValue = "false")
    Instance<Boolean> runJobs;

    @Inject
    private MailController mailController;

    @Schedule(hour = "*", minute = "*", second = "30", persistent = false)
    public void execute() {
        if (runJobs.get()) {
            try {
                activityLogController.saveActivityLog(new ObjectId(), ActivityType.REGISTRATION_WELCOME_MAIL_JOB, "text", "Starting WelcomeMail job...");
                final List<Player> playerList = datastore.find(Player.class).field("state").equal(UserState.REGISTERED).asList();
                playerList.forEach(this::sendActivationRequestMail);
            } catch (Exception ex) {
                logger.severe("Error occurred while executing SendWelcomeMailJob.\r\n" + ex.getMessage());
            }
        }
    }

    public void sendActivationRequestMail(@Observes @PlayerPostCreationEvent Player player) {
        mailController.sendActivationRequestMail(player);
    }
}
