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
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.player.entity.PasswordGroup;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.validation.control.ValidationController;

import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
@Named
@Model
public class ResetPasswordController implements Serializable {

    private static final long serialVersionUID = 4258369597067530774L;

    private static final Logger logger = Logger.getLogger(ResetPasswordController.class.getName());

    @Inject
    UserRepository userRepository;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    private MailController mailController;


    public ResetPasswordController() {
    }


    public boolean startPasswordChangeProcess(Player changePasswordPlayer) {
        ValidationController.get(Player.class).processBeanValidationForProperty(changePasswordPlayer, "email");
        final Player user = userRepository.findUserByEmail(changePasswordPlayer.getEmail());
        if (user != null) {
            userRepository.resetPasswordStart(user);
            mailController.sendResetPasswordRequestMail(user);
            activityLogController.saveActivityLog(user, ActivityType.CHANGE_PASSWORD_START_SUCCESS);
            return true;
        } else {
            activityLogController.saveActivityLog((Player)null, ActivityType.CHANGE_PASSWORD_START_FAILED,
                    "username", changePasswordPlayer.getEmail());
            return false;
        }
    }

    public boolean isValidResetPasswordCode(String requestCode) {
        final String decodedCode = new String(Base64.getDecoder().decode(requestCode));
        Player user = userRepository.findByRenewPwHash(decodedCode);
        return user != null;
    }

    public void changePassword(Player changePasswordPlayer, String requestCode) {
        ValidationController.get(Player.class).processBeanValidationForGroup(changePasswordPlayer, PasswordGroup.class);
        final String decodedCode = new String(Base64.getDecoder().decode(requestCode));
        Player player = userRepository.findByRenewPwHash(decodedCode);
        if (player != null) {
            userRepository.resetPasswordChange(player, changePasswordPlayer.getPassword());
            activityLogController.saveActivityLog(player, ActivityType.CHANGE_PASSWORD_ACTIVATIONCODE_SUCCESS);
            mailController.sendResetPasswordConfirmationMail(player);
        } else {
            logger.log(Level.SEVERE, "Player to this activation-code not found.");
            activityLogController.saveActivityLog((Player)null, ActivityType.CHANGE_PASSWORD_ACTIVATIONCODE_FAILED,
                    "playerId", changePasswordPlayer.getId().toHexString(), "activationCode", decodedCode);
            throw new RuntimeException("Player to this activation-code not found.");
        }
    }
}