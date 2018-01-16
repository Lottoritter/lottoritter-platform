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

import com.github.fakemongo.Fongo;
import com.mongodb.MongoClient;
import de.lottoritter.business.temporal.control.CurrencyConverter;
import de.lottoritter.business.temporal.control.ZonedDateTimeUTCtoEuropeConverter;
import de.lottoritter.platform.persistence.encryption.EncryptedFieldConverter;
import org.junit.Before;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * @author Ulrich Cech
 */
public abstract class FongoDbPersistenceTest {

    private MongoClient mongoClient;

    private Datastore datastore;

    @Before
    public void initFongo() {
        Fongo fongo = new Fongo("MongoTest1");
        mongoClient = fongo.getMongo();
        Morphia morphia = new Morphia();
        datastore = morphia.createDatastore(mongoClient, "lottoritter");
        morphia.getMapper().getOptions().setMapSubPackages(true);
        morphia.mapPackage("de.lottoritter", true);
        morphia.getMapper().getConverters().addConverter(ZonedDateTimeUTCtoEuropeConverter.class);
        morphia.getMapper().getConverters().addConverter(CurrencyConverter.class);
        morphia.getMapper().getConverters().addConverter(EncryptedFieldConverter.class);
    }


    protected MongoClient getMongoClient() {
        return mongoClient;
    }

    protected Datastore getDatastore() {
        return datastore;
    }
}
