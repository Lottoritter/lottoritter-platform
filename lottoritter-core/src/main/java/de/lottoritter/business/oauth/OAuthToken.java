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
package de.lottoritter.business.oauth;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
public class OAuthToken {

    private String accessToken;

    private String refreshToken;

    private String idToken;

    private long expires;

    private String userIdentifier;


    public OAuthToken() {
    }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifierFromBody(String body) {
        JsonObject jsonObject;
        try (JsonReader reader = Json.createReader(new StringReader(body))) {
            jsonObject = reader.readObject();
        }
        this.userIdentifier = jsonObject.getJsonObject("user").getString("id");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthToken)) return false;
        OAuthToken oAuthToken = (OAuthToken) o;
        return expires == oAuthToken.expires &&
                Objects.equals(accessToken, oAuthToken.accessToken) &&
                Objects.equals(refreshToken, oAuthToken.refreshToken) &&
                Objects.equals(idToken, oAuthToken.idToken) &&
                Objects.equals(userIdentifier, oAuthToken.userIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken, idToken, expires, userIdentifier);
    }
}
