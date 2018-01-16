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

import de.lottoritter.business.player.entity.Title;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Ulrich Cech
 */
public class OAuthUserProfileDTO {

    private static final Logger logger = Logger.getLogger(OAuthUserProfileDTO.class.getName());


    private String userId;

    private String email;

    private String firstName;

    private String lastName;

    private String name;

    private Title title;

    private boolean verified;

    private String locale;


    public OAuthUserProfileDTO() {
    }


    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
    }

    public Title getTitle() {
        return title;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OAuthUserProfileDTO)) return false;
        OAuthUserProfileDTO that = (OAuthUserProfileDTO) o;
        return verified == that.verified &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(email, that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(title, that.title) &&
                Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, email, firstName, lastName, name, title, verified, locale);
    }


    public static class Builder {

        private OAuthUserProfileDTO profile = new OAuthUserProfileDTO();

        public Builder id(String id) {
            profile.userId = id;
            return this;
        }

        public Builder email(String email) {
            profile.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            profile.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            profile.lastName = lastName;
            return this;
        }

        public Builder name(String name) {
            profile.name = name;
            return this;
        }

        public Builder gender(String gender) {
            if (StringUtils.isNotBlank(gender)) {
                try {
                    profile.title = Title.valueOf(gender.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    logger.log(Level.SEVERE, MessageFormat.format("Cannot find a matching title to gender {0}", gender), ex);
                }
            }
            return this;
        }

        public Builder verified(boolean verified) {
            profile.verified = verified;
            return this;
        }

        public Builder locale(String locale) {
            profile.locale = locale;
            return this;
        }

        public OAuthUserProfileDTO build() {
            return profile;
        }
    }
}
