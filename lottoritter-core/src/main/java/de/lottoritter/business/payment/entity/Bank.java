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
package de.lottoritter.business.payment.entity;

import java.util.Objects;

import org.mongodb.morphia.annotations.Entity;

/**
 * @author Ulrich Cech
 */
@Entity(value = "bank", noClassnameStored = true)
public class Bank {

    private String blz;

    private String description;

    private String plz;

    private String city;

    private String shortDescription;

    private String bic;


    @SuppressWarnings({"WeakerAccess"})
    public Bank() {
    }

    public Bank(String blz, String description, String plz, String city, String shortDescription, String bic) {
        this.blz = blz;
        this.description = description;
        this.plz = plz;
        this.city = city;
        this.shortDescription = shortDescription;
        this.bic = bic;
    }

    public String getBlz() {
        return blz;
    }

    public void setBlz(String blz) {
        this.blz = blz;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bank)) return false;
        Bank bank = (Bank) o;
        return Objects.equals(blz, bank.blz) &&
                Objects.equals(description, bank.description) &&
                Objects.equals(plz, bank.plz) &&
                Objects.equals(city, bank.city) &&
                Objects.equals(shortDescription, bank.shortDescription) &&
                Objects.equals(bic, bank.bic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blz, description, plz, city, shortDescription, bic);
    }
}
