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

import de.lottoritter.business.temporal.control.DateTimeService;
import de.lottoritter.business.temporal.entity.ZonedDateTimeEurope;
import de.lottoritter.platform.cdi.CDIBeanService;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

import java.time.ZonedDateTime;
import java.util.Base64;

/**
 * Represents the activation code of an user's account.
 *
 * @author Ulrich Cech
 */
@Embedded
public class UserActivation {

    private static final int NUMBER_OF_DAYS = 10; // TODO must be configurable in DB

    private String code;

    @ZonedDateTimeEurope
    private ZonedDateTime validTo;

    @Transient
    Player owner;


    @SuppressWarnings("unused")
    public UserActivation() {}

    public UserActivation(final Player owner) {
        setOwner(owner);
        this.code = Base64.getEncoder().encodeToString(owner.getEmail().getBytes())
                + "-" + Base64.getEncoder().encodeToString(new ObjectId().toHexString().getBytes());
        DateTimeService dateTimeService = CDIBeanService.getInstance().getCDIBean(DateTimeService.class);
        this.validTo = dateTimeService.getFutureDate(dateTimeService.getDateTimeNowEurope(), NUMBER_OF_DAYS, true);
    }

    public boolean isValid() {
        DateTimeService dateTimeService = CDIBeanService.getInstance().getCDIBean(DateTimeService.class);
        return ! dateTimeService.getDateTimeNowEurope().isAfter(this.getValidTo());
    }


    public String getCode() {
        return code;
    }

    public ZonedDateTime getValidTo() {
        return validTo;
    }

    void setOwner(final Player owner) {
        this.owner = owner;
    }

}
