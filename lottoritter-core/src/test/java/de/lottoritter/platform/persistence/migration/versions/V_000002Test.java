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
package de.lottoritter.platform.persistence.migration.versions;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import de.lottoritter.business.player.entity.PlzCityState;
import de.lottoritter.platform.persistence.FongoDbPersistenceTest;

/**
 * @author Ulrich Cech
 */
public class V_000002Test extends FongoDbPersistenceTest {

    @Test
    public void testExecuteMigration() throws Exception {
        V_000002 cut = new V_000002();
        final V_000002 spyCut = Mockito.spy(cut);
        InputStream is = getInputStream();
        Mockito.doReturn(is).when(spyCut).getInputStream();
        spyCut.executeMigration(getDatastore());

        final List<PlzCityState> cities = getDatastore().createQuery(PlzCityState.class).asList();
        assertThat(cities, notNullValue());
        assertThat(cities.size(), is(6));
        assertThat(cities.get(3).getCity(), is("Ratzeburg"));
        assertThat(cities.get(3).getCityCode(), is("23909"));
        assertThat(cities.get(3).getState(), is("Schleswig-Holstein"));
    }

    private InputStream getInputStream() {
        return de.lottoritter.platform.persistence.migration.test_versions.V_000000.class.getResourceAsStream("test_OpenGeoDB_bundesland_plz_ort_de.csv");
    }

}