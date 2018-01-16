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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;

import com.github.fakemongo.Fongo;

/**
 * @author Ulrich Cech
 */
public class DBConnectionTest extends FongoDbPersistenceTest {

    @Test
    public void getDatastoreAndMongoDatabase() throws Exception {
        DBConnection cut = new DBConnection();
        final DBConnection spyCut = spy(cut);
        doReturn(getMongoClient()).when(spyCut).initializeMongoDbConnection();
        spyCut.init();
        assertThat(spyCut.getDatastore(), notNullValue());
        assertThat(spyCut.getMongoDatabase().getName(), is(DBConnection.DB_NAME));
    }

}