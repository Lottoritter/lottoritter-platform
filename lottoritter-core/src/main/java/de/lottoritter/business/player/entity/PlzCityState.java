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

import java.util.Objects;

import org.mongodb.morphia.annotations.Entity;

/**
 * @author Ulrich Cech
 */
@Entity(value = "plz_city_state", noClassnameStored = true)
public class PlzCityState {

    private String cityCode;

    private String city;

    private String state;


    public PlzCityState() {
    }

    public PlzCityState(String cityCode, String city, String state) {
        this.cityCode = cityCode;
        this.city = city;
        this.state = state;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlzCityState)) return false;
        PlzCityState that = (PlzCityState) o;
        return Objects.equals(cityCode, that.cityCode) &&
                Objects.equals(city, that.city) &&
                Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityCode, city, state);
    }
}
