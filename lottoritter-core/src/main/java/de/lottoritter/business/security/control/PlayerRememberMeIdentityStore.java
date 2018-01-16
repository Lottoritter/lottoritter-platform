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
package de.lottoritter.business.security.control;

import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.control.UserSession;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.PlayerLogin;
import de.lottoritter.business.player.entity.Token;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;
import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.platform.http.HttpServletRequestService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.credential.RememberMeCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.RememberMeIdentityStore;
import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import static de.lottoritter.business.player.entity.TokenType.REMEMBER_ME;
import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

/**
 * @author Ulrich Cech
 */
@ApplicationScoped
public class PlayerRememberMeIdentityStore implements RememberMeIdentityStore {

    @Inject
    Instance<HttpServletRequest> request;

    @Inject
    Instance<UserSession> userSession;

    @Inject
    Instance<ShoppingCartSession> shoppingCartSession;

    @Inject
    Datastore datastore;

    @Inject
    UserRepository userRepository;

    @Inject
    DateTimeService dateTimeService;

    @Inject
    HttpServletRequestService httpServletRequestService;


    @Override
    public CredentialValidationResult validate(RememberMeCredential rmc) {
        Player dbPlayer = this.userRepository.findByToken(new Token(new ObjectId(rmc.getToken()), REMEMBER_ME));
        if (dbPlayer != null) {
            Set<String> roleNames = new HashSet<>();
            dbPlayer.getRoles().forEach(r -> roleNames.add(r.name()));
            userSession.get().setPlayer(dbPlayer);
            createUserLogin(dbPlayer, httpServletRequestService.getIpAddressFromRequest(), httpServletRequestService.getUserAgentFromRequest());
            shoppingCartSession.get().handleUntrackedSessionTickets();
            return new CredentialValidationResult(new CallerPrincipal(dbPlayer.getEmail()), roleNames);
        } else {
            return INVALID_RESULT;
        }
    }

    public void createUserLogin(final Player player, final String ipAddress, final String userAgent) {
        final PlayerLogin playerLogin = new PlayerLogin(player, ipAddress, userAgent);
        datastore.save(playerLogin); // initial persist
    }


    @Override
    public String generateLoginToken(CallerPrincipal cp, Set<String> set) {
        Token token = new Token(new ObjectId(), REMEMBER_ME);
        final ZonedDateTime created = dateTimeService.getDateTimeNowEurope();
        token.setCreated(created);
        token.setValidTo(created.plusDays(30));
        final Player dbPlayer = userRepository.findUserByEmail(cp.getName());
        if (dbPlayer != null) {
            userRepository.addTokenToPlayer(dbPlayer, token);
        }
        return token.getHash().toString();
    }

    @Override
    public void removeLoginToken(String loginToken) {
        userRepository.removeToken(new Token(new ObjectId(loginToken), REMEMBER_ME));
    }

}
