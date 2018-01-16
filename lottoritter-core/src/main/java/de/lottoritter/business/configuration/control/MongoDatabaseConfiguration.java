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
package de.lottoritter.business.configuration.control;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Properties;
import java.util.Set;

/**
 * Implementation of a configuration structure for the MongoDB. The configuration is hierarchical
 * in order to distinguish between several application servers running on the same database.<br>
 * If the provided key do not contain any application server specific prefix (<serverName:::<key>>)
 * then the key is globally equal for all application servers.
 *
 * <code>
 *    {
 *      "key" : null,
 *      "configuration" : {"k1" : "v1, "k2" : "v2"}
 *    }
 *    {
 *      "key" : "appServer1",
 *      "configuration" : {"k1" : "v1, "k2" : "v2"}
 *    }
 * </code>
 *
 * @author Ulrich Cech
 */
class MongoDatabaseConfiguration {

    private static final String CONFIGURATION_COLLECTION_NAME = "configs";
    private static final String APP_SERVER_NAME_PREFIX_DELIMITER = ":::";
    private static final String CONFIGURATION_KEY = "configuration";
    private static final String CKEY_KEY = "key";
    static final String CKEY_VALUE_BASE_CONFIGURATION = null;

    private MongoDatabase mongoDatabase;


    MongoDatabaseConfiguration(final MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
        getConfigurationCollection().createIndex(new Document("key", 1));
    }


    Properties prepareInitialBaseConfiguration() {
        Document configDocument = new Document();
        configDocument.put("server_base_uri", "http://localhost:8080");
        configDocument.put("mail_smtp_host", "sslout.df.eu");
        configDocument.put("mail_smtp_auth", "true");
        configDocument.put("mail_smtp_port", "465");
        configDocument.put("mail_smtp_ssl_enable", "true");
        configDocument.put("mail_smtp_connectiontimeout", "10000");
        configDocument.put("mail_smtp_timeout", "10000");
        configDocument.put("login_valid_time_in_ms", "1800000");
        configDocument.put("primaryPSP", "NO_OP");
        Document base = new Document();
        base.put(MongoDatabaseConfiguration.CKEY_KEY, MongoDatabaseConfiguration.CKEY_VALUE_BASE_CONFIGURATION);
        base.put(MongoDatabaseConfiguration.CONFIGURATION_KEY, configDocument);
        getConfigurationCollection().insertOne(base);
        return getKeysForConfigurationKey(MongoDatabaseConfiguration.CKEY_VALUE_BASE_CONFIGURATION);
    }

    private Document getConfigurationForKey(final String key) {
        return getConfigurationCollection().find(new Document(CKEY_KEY, key)).projection(new Document("_id", 0)).first();
    }

    Properties getKeysForConfigurationKey(final String configKey) {
        Properties props = new Properties();
        Document document = getConfigurationForKey(configKey);
        if (document == null) {
            return props;
        }
        Document configDocument = ((Document) document.get(CONFIGURATION_KEY));
        Set<String> keySet = configDocument.keySet();
        for (String key : keySet) {
            props.setProperty(key, (String) configDocument.get(key));
        }
        return props;
    }

    private MongoCollection<Document> getConfigurationCollection() {
        return mongoDatabase.getCollection(CONFIGURATION_COLLECTION_NAME);
    }
}
