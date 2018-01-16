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
package de.lottoritter.platform.persistence;

import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import org.bson.types.ObjectId;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PrePersist;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
public abstract class PersistentEntity implements Serializable {

    private static final long serialVersionUID = -8346526742641366019L;

    private static DateTimeService dateTimeService = new DateTimeService();

    @Id
    private ObjectId id;

    @Version
    @DiffIgnore
    private Long version;

    @DiffIgnore
    @ZonedDateTimeEurope
    private ZonedDateTime created;

    @DiffIgnore
    @ZonedDateTimeEurope
    private ZonedDateTime lastUpdate;

    @Transient
    private boolean omitValidation;


    @PrePersist
    protected void prePersist() {
        if (created == null) {
            created = dateTimeService.getDateTimeNowEurope();
        }
        this.lastUpdate = dateTimeService.getDateTimeNowEurope();
        validate();
    }

    protected void validate() {}


    public ObjectId getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    @XmlTransient
    public boolean isOmitValidation() {
        return omitValidation;
    }

    public void setOmitValidation(boolean omitValidation) {
        this.omitValidation = omitValidation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentEntity)) return false;
        PersistentEntity that = (PersistentEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
