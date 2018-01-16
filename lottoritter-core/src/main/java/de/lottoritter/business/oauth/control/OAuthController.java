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
package de.lottoritter.business.oauth.control;

import com.ocpsoft.pretty.PrettyContext;
import com.ocpsoft.pretty.faces.config.mapping.UrlMapping;
import com.ocpsoft.pretty.faces.util.PrettyURLBuilder;
import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.configuration.control.ConfigurationController;
import de.lottoritter.business.oauth.OAuthToken;
import de.lottoritter.business.oauth.OAuthUserProfileDTO;
import de.lottoritter.business.oauth.entity.OAuthData;
import de.lottoritter.business.oauth.entity.OAuthProvider;
import de.lottoritter.business.player.control.OAuthAuthenticatedPlayer;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.control.ShoppingCartSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.faces.application.ViewExpiredException;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
public abstract class OAuthController {

    private static final Logger logger = Logger.getLogger(ConfigurationController.class.getName());

    @Inject @OAuthAuthenticatedPlayer
    Event<Player> setAuthenticatedPlayerToSession;

    @Inject @Configurable(value = "server_base_uri", defaultValue = "http://localhost:8080")
    protected Instance<String> configServerBaseUri;

    @Inject
    private ShoppingCartSession shoppingCartSession;


    public String createInitialOAuthRequest(final String clientId, final OAuthProviderType providerType,
                                            final String redirectURI, final String scope, final Properties parameters) {
        try {
            final OAuthClientRequest.AuthenticationRequestBuilder requestBuilder =
                    OAuthClientRequest.authorizationProvider(providerType);
            requestBuilder
                    .setResponseType("code")
                    .setState("1234") // TODO  the state can be later checked in receiveAuthenticationCallback()
                    .setClientId(clientId)
                    .setRedirectURI(redirectURI)
                    .setScope(scope);
            if (parameters != null && !parameters.isEmpty()) {
                parameters.forEach(((o, o2) -> requestBuilder.setParameter((String) o, (String) o2)));
            }
            OAuthClientRequest request = requestBuilder.buildQueryMessage();
            return request.getLocationUri();
        } catch (OAuthSystemException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    public void checkAndHandleAuthenticationError(final HttpServletRequest httpServletRequest,
                                                  final HttpServletResponse httpServletResponse, OAuthProvider oAuthProvider) {
        String error = httpServletRequest.getParameter("error");
        if ((error != null) && ("access_denied".equals(error.trim()))) {
            try {
                HttpSession session = httpServletRequest.getSession();
                session.setAttribute("oauthError", true);
                session.setAttribute("oauthProvider", oAuthProvider.name());
                httpServletResponse.sendRedirect(configServerBaseUri.get() + "/registration");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public void handleRegistrationOrLogin(final HttpServletRequest httpServletRequest,
                                          final HttpServletResponse httpServletResponse,
                                          Player player,
                                          OAuthUserProfileDTO userProfile,
                                          OAuthData oAuthData) {
        try {
            final HttpSession session = httpServletRequest.getSession(false);
            if (session == null) {
                throw new ViewExpiredException();
            }
            if (player == null) {
                // we will have a registration call
                Player newPlayer = new Player();
                newPlayer.setoAuthData(oAuthData);
                newPlayer.setEmail(userProfile.getEmail());
                session.setAttribute("oauthPlayer", newPlayer);
                httpServletResponse.sendRedirect(configServerBaseUri.get() + "/registration");
            } else {
                // we will have a login call
                setAuthenticatedPlayerToSession.fire(player);
                shoppingCartSession.handleUntrackedSessionTickets();
                String redirectUrl = (String) session.getAttribute("url");
                if (StringUtils.isBlank(redirectUrl)) {
                    httpServletResponse.sendRedirect(configServerBaseUri.get() + "/index");
                } else {
                    PrettyContext prettyContext = PrettyContext.getCurrentInstance(httpServletRequest);
                    PrettyURLBuilder builder = new PrettyURLBuilder();
                    UrlMapping mapping = null;
                    for (UrlMapping urlMapping : prettyContext.getConfig().getMappings()) {
                        if (urlMapping.getViewId().equals(redirectUrl)) {
                            mapping = urlMapping;
                            break;
                        }
                    }
                    String targetURL = builder.build(mapping, true, (Object[]) null);
                    targetURL = httpServletResponse.encodeRedirectURL(targetURL);
                    httpServletResponse.sendRedirect(targetURL);
                }
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public OAuthUserProfileDTO getUserProfile(final OAuthToken oAuthToken, final String requestUrl) {
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthClientRequest bearerClientRequest;
        try {
            bearerClientRequest = new OAuthBearerClientRequest(requestUrl)
                    .setAccessToken(oAuthToken.getAccessToken())
                    .buildQueryMessage();
            OAuthResourceResponse resourceResponse =
                    oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            if (resourceResponse.getResponseCode() == Response.Status.OK.getStatusCode()) {
                return transformJsonResponseToUserProfile(resourceResponse);
            }
        } catch (OAuthSystemException | OAuthProblemException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    protected abstract OAuthUserProfileDTO transformJsonResponseToUserProfile(OAuthResourceResponse resourceResponse);

    protected String getStringValue(JsonObject jsonObject, String key) {
        if (jsonObject.containsKey(key)) {
            return jsonObject.getString(key);
        }
        return null;
    }

    protected int getIntegerValue(JsonObject jsonObject, String key) {
        if (jsonObject.containsKey(key)) {
            return jsonObject.getInt(key);
        }
        return -9999;
    }

}
