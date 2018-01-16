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
package de.lottoritter.business.oauth.facebook.boundary;

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
import org.apache.oltu.oauth2.client.response.GitHubTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import de.lottoritter.business.oauth.OAuthToken;
import de.lottoritter.business.oauth.OAuthUserProfileDTO;
import de.lottoritter.business.oauth.entity.OAuthData;
import de.lottoritter.business.oauth.entity.OAuthProvider;
import de.lottoritter.business.oauth.facebook.control.FacebookOAuthController;
import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.rest.AbstractRestResource;

/**
 * @author Ulrich Cech
 */
@Stateless
@LocalBean
@Path("facebook")
public class FacebookOAuthResource extends AbstractRestResource implements Serializable {

    private static final long serialVersionUID = 3034431620236876692L;

    private static final Logger logger = Logger.getLogger(FacebookOAuthResource.class.getName());


    @Inject
    UserRepository userRepository;

    @Inject // request-scoped
    FacebookOAuthController facebookOAuthController;


    public FacebookOAuthResource() {
    }



    /**
     * This is the callback endpoint, when a user has authenticated at the authentication service (Google, Facebook,...)
     * and the services returns with the callback, which is provided in the initial authentication call (and which
     * must match the callback-URI configured at the authentication service).
     */
    @Path("oauth")
    @GET
    public Response receiveAuthenticationCallback() {
        facebookOAuthController.checkAndHandleAuthenticationError(httpServletRequest, httpServletResponse, OAuthProvider.FACEBOOK);
        OAuthToken tokens = getTokenFromAuthenticationCode(httpServletRequest);
        OAuthData oAuthData = new OAuthData(OAuthProvider.FACEBOOK);
        try {
            final OAuthUserProfileDTO userProfile = facebookOAuthController.getUserProfile(tokens, "https://graph.facebook.com/me?fields=id,name,first_name,last_name,gender,email");
            if (userProfile != null) {
                oAuthData.setOauthId(userProfile.getUserId());
                oAuthData.setEmail(userProfile.getEmail());
                Player player = userRepository.findUserByOAuthId(oAuthData.getOauthId());
                facebookOAuthController.handleRegistrationOrLogin(httpServletRequest, httpServletResponse, player, userProfile, oAuthData);
                return Response.ok().build();
            }
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
                    .tokenProvider(OAuthProviderType.FACEBOOK)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(facebookOAuthController.getClientId())
                    .setClientSecret(facebookOAuthController.getClientSecret())
                    .setRedirectURI(configServerBaseUri.get() + "/api/facebook/oauth")
                    .setScope("public_profile email")
                    .setCode(code)
                    .buildBodyMessage();
            request.addHeader("Content-Length", Integer.toString(0));
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            long currentTime = System.currentTimeMillis();
            GitHubTokenResponse oAuthResponse = oAuthClient.accessToken(request, GitHubTokenResponse.class);
            tokens.setAccessToken(oAuthResponse.getAccessToken());
            long expires = 0;
            try {
                expires = Long.parseLong(oAuthResponse.getParam("expires"));
                tokens.setExpires(currentTime + expires);
            } catch (NumberFormatException nfex) {
                // TODO  how to deal with the error? default expiration?
            }
        } catch (OAuthProblemException | OAuthSystemException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return tokens;
    }

}
