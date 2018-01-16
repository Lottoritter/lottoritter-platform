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

import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * @author Ulrich Cech
 */
@Embedded
public class Token {

    private ObjectId hash;

    private TokenType type;

    @ZonedDateTimeEurope
    private ZonedDateTime validTo;

    @ZonedDateTimeEurope
    private ZonedDateTime created;

    private String ipAddress;


    public Token() {
    }

    public Token(ObjectId hash, TokenType type) {
        this.hash = hash;
        this.type = type;
    }


    public boolean isValid() {
        DateTimeService dateTimeService = CDIBeanService.getInstance().getCDIBean(DateTimeService.class);
        return ! dateTimeService.getDateTimeNowEurope().isAfter(this.getValidTo());
    }


    public ObjectId getHash() {
        return hash;
    }

    public void setHash(ObjectId hash) {
        this.hash = hash;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public ZonedDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(ZonedDateTime validTo) {
        this.validTo = validTo;
    }

    public ZonedDateTime getCreated() {
        return created;
    }

    public void setCreated(ZonedDateTime created) {
        this.created = created;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token)) return false;
        Token token = (Token) o;
        return Objects.equals(hash, token.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
