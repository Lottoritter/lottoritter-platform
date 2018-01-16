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
package de.lottoritter.business.oauth.google.control;

import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.oauth.OAuthUserProfileDTO;
import de.lottoritter.business.oauth.control.OAuthController;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuthProviderType;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Properties;

/**
 * @author Ulrich Cech
 */
@RequestScoped
@Named
public class GoogleOAuthController extends OAuthController {

    @Inject
    @Configurable(value = "oauth_google_clientId", defaultValue = "")
    Instance<String> clientId;

    @Inject
    @Configurable(value = "oauth_google_clientSecret", defaultValue = "")
    Instance<String> clientSecret;


    public String createInitialOAuthRequest() {
        Properties parameters = new Properties();
        parameters.setProperty("access_type", "offline");
        return super.createInitialOAuthRequest(getClientId(), OAuthProviderType.GOOGLE,
                configServerBaseUri.get() + "/api/google/oauth", "email profile", parameters);
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
                .firstName(getStringValue(userProfileJson, "given_name"))
                .lastName(getStringValue(userProfileJson, "family_name"))
                .name(getStringValue(userProfileJson, "name"))
                .locale(getStringValue(userProfileJson, "locale"))
                .build();
    }

    public String getClientId() {
        return clientId.get();
    }

    public String getClientSecret() {
        return clientSecret.get();
    }
}
