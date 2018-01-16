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
package de.lottoritter.presentation.player.control;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import com.ocpsoft.pretty.faces.util.PrettyURLBuilder;
import de.lottoritter.business.player.control.UserSession;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.PlayerLogin;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;
import de.lottoritter.platform.http.HttpServletRequestService;
import de.lottoritter.presentation.AbstractViewController;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author Ulrich Cech
 */
public class AbstractLoginViewController extends AbstractViewController {


    @Inject
    Datastore datastore;

    @Inject
    HttpServletRequestService httpServletRequestService;

    @Inject
    UserSession userSession;

    @Inject
    ShoppingCartSession shoppingCartSession;


    public void loginPostAction(Player player, String redirectUrl, String defaultRedirectUrl) throws IOException {
        userSession.setPlayer(player);
        createUserLogin(player, httpServletRequestService.getIpAddressFromRequest(), httpServletRequestService.getUserAgentFromRequest());
        shoppingCartSession.handleUntrackedSessionTickets();
        handleRedirectUrl(redirectUrl, defaultRedirectUrl);
    }

    public void createUserLogin(final Player player, final String ipAddress, final String userAgent) {
        final PlayerLogin playerLogin = new PlayerLogin(player, ipAddress, userAgent);
        datastore.save(playerLogin); // initial persist
    }


    void handleRedirectUrl(String redirectUrl, String defaultUrl) throws IOException {
        if (StringUtils.isBlank(redirectUrl)) {
            FacesContext.getCurrentInstance().getExternalContext().redirect(defaultUrl);
        } else {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            session.removeAttribute("url");
            PrettyContext prettyContext = PrettyContext.getCurrentInstance(FacesContext.getCurrentInstance());
            PrettyURLBuilder builder = new PrettyURLBuilder();
            UrlMapping mapping = null;
            for (UrlMapping urlMapping : prettyContext.getConfig().getMappings()) {
                if (redirectUrl.equals(urlMapping.getViewId())) {
                    mapping = urlMapping;
                    break;
                }
            }
            String targetURL = builder.build(mapping, true, (Object[]) null);
            FacesContext.getCurrentInstance().getExternalContext().redirect(targetURL);
        }
    }

}
