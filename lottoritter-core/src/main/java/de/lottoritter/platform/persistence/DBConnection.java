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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import de.lottoritter.business.temporal.control.CurrencyConverter;
import de.lottoritter.business.temporal.control.ZonedDateTimeUTCtoEuropeConverter;
import de.lottoritter.platform.persistence.encryption.EncryptedFieldConverter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * https://docs.mongodb.org/manual/tutorial/enable-authentication/
 * https://docs.mongodb.org/manual/tutorial/manage-users-and-roles/
 *
 * @author Ulrich Cech
 */
@Singleton
@Startup
public class DBConnection {

    private static MongoClient mongo;   // is thread-safe because of @Singleton
    private static Datastore datastore; // is thread-safe because of @Singleton

    public static final String DB_NAME = "lottoritter";


    public DBConnection() {
    }


    @PostConstruct
    public void init() {
        if ((mongo == null) && (datastore == null)) {
            mongo = initializeMongoDbConnection();
            Morphia morphia = new Morphia();
            datastore = morphia.createDatastore(mongo, DB_NAME);
            morphia.getMapper().getOptions().setMapSubPackages(true);
            morphia.getMapper().getOptions().setStoreEmpties(true);
            morphia.mapPackage("de.lottoritter", true);
            morphia.getMapper().getConverters().addConverter(ZonedDateTimeUTCtoEuropeConverter.class);
            morphia.getMapper().getConverters().addConverter(CurrencyConverter.class);
            morphia.getMapper().getConverters().addConverter(EncryptedFieldConverter.class);
            datastore.ensureIndexes(true);
        }
    }

    MongoClient initializeMongoDbConnection() {
        String dbServerName = System.getenv("lottoritter_dbServerName");
        if (dbServerName != null) {
            int dbServerPort = Integer.parseInt(System.getenv("lottoritter_dbServerPort"));
            List<MongoCredential> credentials = new ArrayList<>();
            return new MongoClient(new ServerAddress(dbServerName, dbServerPort), credentials);
        } else {
            MongoClientURI uri = new MongoClientURI("mongodb://"
                    + System.getenv("lottoritter_dbUser") + ":"
                    + System.getenv("lottoritter_dbPassword")
                    + "@lor-cluster-shard-00-00-us2rg.mongodb.net:27017,lor-cluster-shard-00-01-us2rg.mongodb.net:27017,lor-cluster-shard-00-02-us2rg.mongodb.net:27017/"
                    + DB_NAME + "?ssl=true&replicaSet=LOR-Cluster-shard-0&authSource=admin");
            return new MongoClient(uri);
        }
    }

    @Produces @RequestScoped
    public Datastore getDatastore() {
        return datastore;
    }

    public MongoDatabase getMongoDatabase() {
        return mongo.getDatabase(DB_NAME);
    }

}
