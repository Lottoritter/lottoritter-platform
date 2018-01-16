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

import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.RoleId;
import de.lottoritter.platform.Current;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.SecurityContext;
import java.io.Serializable;
import java.util.Locale;

/**
 * @author Ulrich Cech
 */
@Named
@SessionScoped
public class UserSession implements Serializable {

    private static final long serialVersionUID = -7229541970352879978L;

    @Inject
    SecurityContext securityContext;

    private Player player;

    private String securedRequestedPage;

    private Locale locale = Locale.GERMAN;


    public UserSession() {
    }


    @Produces @Current
    @Named("currentUser")
    public Player getPlayer() {
        return this.player;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public boolean isLoggedIn() {
        return (securityContext.getCallerPrincipal() != null) && (getPlayer() != null)
                && securityContext.getCallerPrincipal().getName().equals(getPlayer().getEmail())
                && securityContext.isCallerInRole(RoleId.USER.name());
    }

    public void setSecuredRequestedPage(String uri) {
        this.securedRequestedPage = uri;
    }

    public void setOAuthAuthenticatedPlayerToSession(@Observes @OAuthAuthenticatedPlayer Player player) {
        this.setPlayer(player);
    }

    public void changeLocale(String languageString) {
        this.locale = new Locale(languageString);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }

    public Locale getLocale() {
        return locale;
    }
}
