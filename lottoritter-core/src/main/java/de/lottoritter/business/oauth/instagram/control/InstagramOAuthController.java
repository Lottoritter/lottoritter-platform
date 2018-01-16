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
package de.lottoritter.business.oauth.instagram.control;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import de.lottoritter.business.configuration.control.Configurable;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import de.lottoritter.business.oauth.OAuthUserProfileDTO;
import de.lottoritter.business.oauth.control.OAuthController;

/**
 * @author Ulrich Cech
 */
@RequestScoped
@Named
public class InstagramOAuthController extends OAuthController {

    private static final Logger logger = Logger.getLogger(InstagramOAuthController.class.getName());

    @Inject
    @Configurable(value = "oauth_instagram_clientId", defaultValue = "")
    Instance<String> clientId;

    @Inject
    @Configurable(value = "oauth_instagram_clientSecret", defaultValue = "")
    Instance<String> clientSecret;


    public InstagramOAuthController() {
    }


    public String createInitialOAuthRequest() {
        try {
            OAuthClientRequest request =
                    OAuthClientRequest.authorizationProvider(OAuthProviderType.INSTAGRAM)
                            .setResponseType("code")
                            .setClientId(getClientId())
                            .setRedirectURI(configServerBaseUri.get() + "/api/instagram/oauth")
                            .setScope("basic")
                            .buildQueryMessage();
            return request.getLocationUri();
        } catch (OAuthSystemException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    protected OAuthUserProfileDTO transformJsonResponseToUserProfile(OAuthResourceResponse resourceResponse) {
        JsonObject userProfileJson;
        try (JsonReader jsonReader = Json.createReader(new StringReader(resourceResponse.getBody()))) {
            userProfileJson = jsonReader.readObject();
        }
        if (userProfileJson.containsKey("meta")
                && ((getIntegerValue(userProfileJson.getJsonObject("meta"), "code")) == 200)
                && (userProfileJson.containsKey("data"))) {
            final JsonObject data = userProfileJson.getJsonObject("data");
            final OAuthUserProfileDTO.Builder userProfileBuilder = new OAuthUserProfileDTO.Builder();
            final String full_name = getStringValue(data, "full_name");
            String firstname = "";
            String lastname = "";
            if (full_name != null) {
                final String[] fullnameParts = full_name.split("\\s");
                if (fullnameParts.length > 0) {
                    lastname = fullnameParts[fullnameParts.length -1];
                    firstname = String.join(" ", Arrays.copyOfRange(fullnameParts, 0, fullnameParts.length -1));
                }
            }
            return userProfileBuilder
                    .id(getStringValue(data, "id"))
                    .name(full_name)
                    .firstName(firstname)
                    .lastName(lastname)
                    .build();
        } else {
            logger.severe(MessageFormat.format("Instagram returned an unrecognized JSON structure: \r\n {0}", userProfileJson));
            return null;
        }
    }

    public String getClientId() {
        return clientId.get();
    }

    public String getClientSecret() {
        return clientSecret.get();
    }

    //    {
//        "meta": {
//            "code":200
//        },
//        "data": {
//            "username":"uc1974",
//            "bio":"",
//            "website":"",
//            "profile_picture": "https://scontent.cdninstagram.com/t51.2885-19/11906329_960233084022564_1448528159_a.jpg",
//            "full_name": "Batman 4",
//            "counts": {
//                "media":0,
//                "followed_by":0,
//                "follows":0
//            },
//            "id": "4370866750"
//        }
//    }

}
