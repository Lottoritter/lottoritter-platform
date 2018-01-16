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
package de.lottoritter.business.activitylog.entity;

import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import de.lottoritter.platform.persistence.PersistentEntity;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Entity(value = "activitylog", noClassnameStored = true)
public class ActivityLog extends PersistentEntity {

    private static final long serialVersionUID = 6650274790147153519L;


    private ObjectId playerId;

    @ZonedDateTimeEurope
    private ZonedDateTime timestamp;

    private ActivityType activityType;

    private ActivityFamily activityFamily;

    private Document data;


    @SuppressWarnings({"WeakerAccess"})
    public ActivityLog() {
    }

    public ActivityLog(ObjectId playerId, ZonedDateTime timestamp, ActivityType activityType) {
        this.playerId = playerId;
        this.timestamp = timestamp;
        this.activityType = activityType;
        this.activityFamily = activityType.getActivityFamily();
    }


    public ObjectId getPlayerId() {
        return playerId;
    }

    public void setPlayerId(ObjectId playerId) {
        this.playerId = playerId;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public Document getData() {
        return data;
    }

    public void setData(Document data) {
        this.data = data;
    }

    public ActivityFamily getActivityFamily() {
        return activityFamily;
    }

    public void setActivityFamily(ActivityFamily activityFamily) {
        this.activityFamily = activityFamily;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActivityLog)) return false;
        if (!super.equals(o)) return false;
        ActivityLog that = (ActivityLog) o;
        return Objects.equals(playerId, that.playerId) &&
                Objects.equals(timestamp, that.timestamp) &&
                activityType == that.activityType &&
                activityFamily == that.activityFamily &&
                Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), playerId, timestamp, activityType, activityFamily, data);
    }
}
