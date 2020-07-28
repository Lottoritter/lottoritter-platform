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
package de.lottoritter.business.player.entity;

import de.lottoritter.platform.persistence.PersistentEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Stores the login data for a user.<br/>
 * There can by multiple UserLogin-entries per user, because a user can be logged in from
 * various devices (eg. desktop, mobile phone).
 *
 * @author Ulrich Cech
 */
@Entity(value = "playerlogins", noClassnameStored = true)
@Indexes({
        @Index(fields = @Field("playerId"),
                options = @IndexOptions(name = "playerId_1", background = true))
})
public class PlayerLogin extends PersistentEntity {

    private static final long serialVersionUID = -8385692724234542386L;

    private ObjectId playerId;

    private String ipAddress;

    private String userAgent;

    private ZonedDateTime zonedDateTime;


    public PlayerLogin() {}

    public PlayerLogin(final Player player, String ipAddress, String userAgent) {
        this();
        this.playerId = player.getId();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"));
    }


    public ObjectId getPlayerId() {
        return playerId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerLogin)) return false;
        if (!super.equals(o)) return false;
        PlayerLogin that = (PlayerLogin) o;
        return Objects.equals(playerId, that.playerId) &&
                Objects.equals(ipAddress, that.ipAddress) &&
                Objects.equals(userAgent, that.userAgent) &&
                Objects.equals(zonedDateTime, that.zonedDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerId, ipAddress, userAgent, zonedDateTime);
    }
}
