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
import de.lottoritter.business.player.entity.PasswordChangeGroup;
import de.lottoritter.business.player.entity.PasswordGroup;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.PlayerPostCreationEvent;
import de.lottoritter.business.player.entity.RoleId;
import de.lottoritter.business.player.entity.Token;
import de.lottoritter.business.player.entity.TokenType;
import de.lottoritter.business.player.entity.UserActivation;
import de.lottoritter.business.player.entity.UserState;
import de.lottoritter.business.security.control.PasswordService;
import de.lottoritter.business.validation.control.ValidationController;
import de.lottoritter.platform.persistence.encryption.EncryptedFieldValueWrapper;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Provides bulk operations for {@link Player}.
 *
 * @author Ulrich Cech
 */
@Stateless
public class UserRepository implements Serializable {

    private static final long serialVersionUID = 920386049401733324L;

    @Inject
    Datastore datastore;

    @Inject
    ActivityLogController activityLogController;

    @Inject
    PasswordService passwordService;

    @Inject
    MailController mailController;

    @Inject @PlayerPostCreationEvent
    Event<Player> playerEvent;


    public UserRepository() {
    }


    public Player register(final Player player) {
        player.addRole(RoleId.USER);
        player.setState(UserState.REGISTERED);
        passwordService.setupPasswordToUser(player, player.getPassword());
        player.validateRegistrationFields();
        player.setActivation(new UserActivation(player));
        datastore.save(player); // this should be the only place, where the whole player-object is persisted! Use update-statements for further changes
        activityLogController.saveActivityLog(player, ActivityType.REGISTRATION_SUCCESS,
                "playerId", player.getId().toHexString());
        playerEvent.fire(player);
        return player;
    }

    public boolean activateAccount(final String code) {
        final Query<Player> query = datastore.createQuery(Player.class);
        query.field("activation.code").equal(code);
        Player player = query.get();
        if ((player != null) && player.getActivation().isValid()) {
            datastore.update(query,
                    datastore.createUpdateOperations(Player.class)
                            .set("state", UserState.ACTIVATED).unset("activation"));
            activityLogController.saveActivityLog(player, ActivityType.REGISTRATION_ACTIVATION_SUCCESS);
            player.setState(UserState.ACTIVATED);
            player.setActivation(null);
            mailController.sendActivationConfirmedMail(player);
            return true;
        }
        return false;
    }

    public boolean approveEmail(final String code) {
        final Query<Player> query = datastore.createQuery(Player.class);
        query.field("activation.code").equal(code);
        Player player = query.get();
        if ((player != null) && player.getActivation().isValid()) {
            datastore.update(query,
                    datastore.createUpdateOperations(Player.class).unset("activation"));
            return true;
        }
        return false;
    }

    public void changePassword(Player player, final String oldPassword, final String newPassword, final String newPasswordAgain) {
        player.setOldPassword(oldPassword);
        player.setPassword(newPassword);
        player.setPasswordAgain(newPasswordAgain);
        ValidationController.get(Player.class).processBeanValidationForGroup(player, PasswordGroup.class, PasswordChangeGroup.class);

        final String pwHash = passwordService.createMD5(newPassword);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class)
                        .set("pwHash", pwHash)
                        .set("pwRenewHash", null)
        );
        player.setPwHash(pwHash);
        player.setPwRenewHash(null);
        activityLogController.saveActivityLog(player, ActivityType.CHANGE_PASSWORD);
    }

    public List<Player> getAllUsers() {
        return datastore.find(Player.class).asList();
    }

    public Player findById(final String objectId) {
        return datastore.get(Player.class, new ObjectId(objectId));
    }

    public Player findById(ObjectId id) {
        return datastore.get(Player.class, id);
    }

    public Player findUserByUsernameAndPwHash(final String eMail, final String pwHash) {
        final Query<Player> query = datastore.createQuery(Player.class);
        query.disableValidation().and(
                query.criteria("email").equal(new EncryptedFieldValueWrapper(eMail)),
                query.criteria("pwHash").equal(pwHash)
        );
        return query.get();
    }

    public Player findUserByEmail(final String eMail) {
        return datastore.find(Player.class).disableValidation().field("email").equal(new EncryptedFieldValueWrapper(eMail)).get();
    }

    public Player findUserByOAuthId(final String oauthId) {
        return datastore.find(Player.class).filter("oAuthData.oauthId", oauthId).get();
    }

    public void resetPasswordStart(final Player player) {
        String newPwHash = passwordService.createMD5(new ObjectId().toString());
        player.setPwRenewHash(newPwHash);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).set("pwRenewHash", newPwHash)
        );
    }

    public void resetPasswordChange(final Player player, final String newPassword) {
        final String pwHash = passwordService.createMD5(newPassword);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).set("pwHash", pwHash).unset("pwRenewHash")
        );
        player.setPwHash(pwHash); // could be later encrypted with external key
        player.setPwRenewHash(null);
    }

    public Player findByRenewPwHash(final String code) {
        return datastore.find(Player.class).filter("pwRenewHash", code).get();
    }

    public Player findByToken(final Token token) {
        final Player dbPlayer = datastore.find(Player.class).filter("tokenList.hash", token.getHash()).get();
        if (dbPlayer != null) {
            final Optional<Token> matchingToken =
                    dbPlayer.getTokenList().stream().filter(t -> t.getHash().equals(token.getHash())).findFirst();
            if (matchingToken.isPresent()) {
                if (matchingToken.get().isValid()) {
                    return dbPlayer;
                } else {
                    removeToken(dbPlayer, matchingToken.get());
                }
            }
        }
        return null;
    }

    public void addTokenToPlayer(final Player player, final Token token) {
        player.addToken(token);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).addToSet("tokenList", token)
        );
    }

    public void removeToken(final Player player, final Token token) {
        player.removeToken(token);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).removeAll("tokenList", token)
        );
    }

    public void removeToken(final Token token) {
        Player player = findByToken(token);
        if (player != null) {
            removeToken(player, token);
        }
    }

    public void removeAllRememberMeTokens(final Player player) {
        player.getTokenList().removeIf(t -> TokenType.REMEMBER_ME.equals(t.getType()));
        Token ref = new Token();
        ref.setType(TokenType.REMEMBER_ME);
        datastore.update(
                datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                datastore.createUpdateOperations(Player.class).removeAll("tokenList", ref)
        );
    }
}
