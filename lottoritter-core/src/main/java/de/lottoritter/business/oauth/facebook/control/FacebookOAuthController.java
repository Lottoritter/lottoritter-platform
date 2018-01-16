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
package de.lottoritter.business.oauth.facebook.control;

import java.io.StringReader;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import de.lottoritter.business.configuration.control.Configurable;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;

import de.lottoritter.business.oauth.OAuthUserProfileDTO;
import de.lottoritter.business.oauth.control.OAuthController;

/**
 * @author Ulrich Cech
 */
@RequestScoped
@Named
public class FacebookOAuthController extends OAuthController {

    @Inject
    @Configurable(value = "oauth_facebook_clientId", defaultValue = "")
    Instance<String> clientId;

    @Inject
    @Configurable(value = "oauth_facebook_clientSecret", defaultValue = "")
    Instance<String> clientSecret;


    public FacebookOAuthController() {}


    public String createInitialOAuthRequest() {
        return super.createInitialOAuthRequest(getClientId(), OAuthProviderType.FACEBOOK,
                configServerBaseUri.get() + "/api/facebook/oauth", "public_profile email", null);
    }

    @Override
    protected OAuthUserProfileDTO transformJsonResponseToUserProfile(OAuthResourceResponse resourceResponse) {
        JsonObject userProfileJson;
        try (JsonReader jsonReader = Json.createReader(new StringReader(resourceResponse.getBody()))) {
            userProfileJson = jsonReader.readObject();
        }
        return new OAuthUserProfileDTO.Builder()
                .id(getStringValue(userProfileJson, "id"))
                .email(getStringValue(userProfileJson, "email"))
                .firstName(getStringValue(userProfileJson, "first_name"))
                .lastName(getStringValue(userProfileJson, "last_name"))
                .name(getStringValue(userProfileJson, "name"))
                .gender(getStringValue(userProfileJson, "gender"))
                .build();
    }

    public String getClientId() {
        return clientId.get();
    }

    public String getClientSecret() {
        return clientSecret.get();
    }
}
