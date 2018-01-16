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
package de.lottoritter.business.oauth.instagram.boundary;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import de.lottoritter.business.oauth.OAuthToken;
import de.lottoritter.business.oauth.OAuthUserProfileDTO;
import de.lottoritter.business.oauth.entity.OAuthData;
import de.lottoritter.business.oauth.entity.OAuthProvider;
import de.lottoritter.business.oauth.instagram.control.InstagramOAuthController;
import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.rest.AbstractRestResource;

/**
 * @author Ulrich Cech
 */
@Stateless
@LocalBean
@Path("instagram")
public class InstagramOAuthResource extends AbstractRestResource implements Serializable {

    private static final long serialVersionUID = 4685929666255287056L;

    private static final Logger logger = Logger.getLogger(InstagramOAuthResource.class.getName());


    @Inject
    UserRepository userRepository;

    @Inject
    InstagramOAuthController instagramOAuthController;


    public InstagramOAuthResource() {
    }


    /**
     * This is the callback endpoint, when a user has authenticated at the authentication service (Google, Facebook,...)
     * and the services returns with the callback, which is provided in the initial authentication call (and which
     * must match the callback-URI configured at the authentication service).
     */
    @Path("oauth")
    @GET
    public Response receiveAuthenticationCallback() {
        instagramOAuthController.checkAndHandleAuthenticationError(httpServletRequest, httpServletResponse, OAuthProvider.INSTAGRAM);
        OAuthToken tokens = getTokenFromAuthenticationCode(httpServletRequest);
        OAuthData oAuthData = new OAuthData(OAuthProvider.INSTAGRAM);
        final OAuthUserProfileDTO userProfile =
                instagramOAuthController.getUserProfile(tokens, "https://api.instagram.com/v1/users/" + tokens.getUserIdentifier() + "/");
        try {
            oAuthData.setOauthId(userProfile.getUserId());
            oAuthData.setEmail(userProfile.getEmail());
            Player player = userRepository.findUserByOAuthId(oAuthData.getOauthId());
            instagramOAuthController.handleRegistrationOrLogin(httpServletRequest, httpServletResponse, player, userProfile, oAuthData);
            return Response.ok().build();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    private OAuthToken getTokenFromAuthenticationCode(HttpServletRequest httpServletRequest) {
        OAuthToken tokens = new OAuthToken();
        OAuthAuthzResponse authResponseFromProvider;
        try {
            authResponseFromProvider = OAuthAuthzResponse.oauthCodeAuthzResponse(httpServletRequest);
            String code = authResponseFromProvider.getCode();
            OAuthClientRequest request = OAuthClientRequest
                    .tokenProvider(OAuthProviderType.INSTAGRAM)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(instagramOAuthController.getClientId())
                    .setClientSecret(instagramOAuthController.getClientSecret())
                    .setRedirectURI(configServerBaseUri.get() + "/api/instagram/oauth")
                    .setCode(code)
                    .buildBodyMessage();
            request.addHeader("Content-Length", Integer.toString(0));
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            final OAuthJSONAccessTokenResponse oAuthJSONAccessTokenResponse = oAuthClient.accessToken(request);
            tokens.setAccessToken(oAuthJSONAccessTokenResponse.getAccessToken());
            tokens.setUserIdentifierFromBody(oAuthJSONAccessTokenResponse.getBody());
        } catch (OAuthProblemException | OAuthSystemException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return tokens;
    }

}
