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
package de.lottoritter.presentation.admin.configuration.control;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import de.lottoritter.platform.persistence.DBConnection;
import org.bson.Document;
import org.mongodb.morphia.Datastore;
import org.primefaces.event.RowEditEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Ulrich Cech
 */
@Named
@ViewScoped
public class MngmConfigurationViewController implements Serializable {

    private static final long serialVersionUID = -7216869077618801109L;

    private static final Logger logger = Logger.getLogger(MngmConfigurationViewController.class.getName());

    @Inject
    Datastore datastore;

    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    private MongoCollection mongoCollection;

    private MailSettings mailSettings;

    private Map<String, String> globalSettings;

    private NewSettingEntry newGlobalEntry = new NewSettingEntry();

    private List<InstanceSettings> allInstanceSettings;

    private InstanceSettings newInstanceSettingsEntry = new InstanceSettings();


    public MngmConfigurationViewController() {
    }


    @PostConstruct
    private void init() {
        mongoClient = datastore.getMongo();
        mongoDatabase = mongoClient.getDatabase(DBConnection.DB_NAME);
        mongoCollection = mongoDatabase.getCollection("configs");
        mailSettings = new MailSettings(mongoCollection);
        globalSettings = readGlobalSettings();
        allInstanceSettings = readAllInstanceSettings();
    }


    private Map<String, String> readGlobalSettings() {
        Map<String, String> globalSettings = new TreeMap<>();
        final Document defaultConfigurationDocument =
                ((Document) mongoCollection.find(new Document("key", null)).first()).get("configuration", Document.class);
        for (Map.Entry<String, Object> stringObjectEntry : defaultConfigurationDocument.entrySet()) {
            if (! stringObjectEntry.getKey().startsWith("mail")) {
                globalSettings.put(stringObjectEntry.getKey(), (String) stringObjectEntry.getValue());
            }
        }
        return globalSettings;
    }

    private List<InstanceSettings> readAllInstanceSettings() {
        List<InstanceSettings> allInstanceSettings = new ArrayList<>();
        final FindIterable<Document> instanceDocuments = mongoCollection.find(Filters.ne("key", null));
        MongoCursor<Document> cursor = instanceDocuments.iterator();
        while (cursor.hasNext()) {
            Document document = cursor.next();
            final Document configuration = (Document) document.get("configuration");
            for (Map.Entry<String, Object> configEntry : configuration.entrySet()) {
                String instanceName = (String) document.get("key");
                allInstanceSettings.add(new InstanceSettings(instanceName, configEntry.getKey(), (String) configEntry.getValue()));
            }
        }
        return allInstanceSettings;
    }



    public void saveMailSettings(final String formId) {
        mongoCollection.updateOne(Filters.eq("key", null), Updates.combine(
                Updates.set("configuration.mail_smtp_host", getMailSettings().getSmtpHost()),
                Updates.set("configuration.mail_smtp_port", getMailSettings().getSmtpPort()),
                Updates.set("configuration.mail_user", getMailSettings().getMailUser()),
                Updates.set("configuration.mail_password", getMailSettings().getMailPassword()),
                Updates.set("configuration.mail_smtp_auth", getMailSettings().getSmtpAuth().toString()),
                Updates.set("configuration.mail_smtp_ssl_enable", getMailSettings().getSmtpSslEnable().toString()),
                Updates.set("configuration.mail_smtp_connectiontimeout", getMailSettings().getConnectionTimeout().toString()),
                Updates.set("configuration.mail_smtp_timeout", getMailSettings().getConnectionTimeout().toString())
        ));
        FacesContext.getCurrentInstance().addMessage(formId, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Mail-settings saved successful.", ""));
    }

    public void onRowEditGlobalSettings(RowEditEvent event) {
        Map.Entry<String, String> newEntry = (Map.Entry) event.getObject();
        mongoCollection.updateOne(Filters.eq("key", null),
                Updates.set("configuration." + newEntry.getKey(), newEntry.getValue()));
    }

    public void onRowCancelSettings(RowEditEvent event) {
    }

    public void addNewGlobalSettingsEntry() {
        globalSettings.put(newGlobalEntry.key, newGlobalEntry.value);
        mongoCollection.updateOne(Filters.eq("key", null),
                Updates.set("configuration." + newGlobalEntry.key, newGlobalEntry.value));
        newGlobalEntry = new NewSettingEntry();
    }

    public void deleteGlobalSettingsEntry(Map.Entry<String, String> object) {
        globalSettings.remove(object.getKey());
        mongoCollection.updateOne(Filters.eq("key", null),
                Updates.unset("configuration." + object.getKey()));
    }

    public void onRowEditInstanceSettings(RowEditEvent event) {
        InstanceSettings settingRow = (InstanceSettings) event.getObject();
        mongoCollection.updateOne(Filters.eq("key", settingRow.getInstanceName()),
                Updates.set("configuration." + settingRow.getKey(), settingRow.getValue()));
    }

    public void addNewInstanceSettingsEntry() {
        allInstanceSettings.add(new InstanceSettings(newInstanceSettingsEntry.getInstanceName(),
                newInstanceSettingsEntry.getKey(), newInstanceSettingsEntry.getValue()));
        mongoCollection.updateOne(Filters.eq("key", newInstanceSettingsEntry.getInstanceName()),
                Updates.set("configuration." + newInstanceSettingsEntry.getKey(), newInstanceSettingsEntry.getValue()));
        newInstanceSettingsEntry = new InstanceSettings();
    }

    public void deleteInstanceSettingsEntry(InstanceSettings settingsToDelete) {
        final Iterator<InstanceSettings> iterator = allInstanceSettings.iterator();
        while (iterator.hasNext()) {
            InstanceSettings instanceSettings = iterator.next();
            if (instanceSettings.getInstanceName().equals(settingsToDelete.getInstanceName())
                    && instanceSettings.getKey().equals(settingsToDelete.getKey())
                    && instanceSettings.getValue().equals(settingsToDelete.getValue())) {
                iterator.remove();
                break;
            }
        }
        mongoCollection.updateOne(Filters.eq("key", settingsToDelete.getInstanceName()),
                Updates.unset("configuration." + settingsToDelete.getKey()));
    }




    public void sendConfiguration() {
        Client client = ClientBuilder.newClient();
        final List<InstanceSettings> baseUris = allInstanceSettings.stream()
                .filter(instanceSettings -> "server_base_uri".equalsIgnoreCase(instanceSettings.getKey()))
                .collect(Collectors.toList());
        for (InstanceSettings allInstanceSetting : baseUris) {
            String baseUri = allInstanceSetting.getValue();
            if (baseUri.endsWith("/")) {
                baseUri = baseUri.substring(0, baseUri.length() - 1);
            }
            try {
                Response result = client.target(baseUri + "/api/platform/configuration/refresh").request().get(Response.class);
                if (result.getStatus() != Response.Status.OK.getStatusCode()) {
                    throw new RuntimeException(MessageFormat.format("Refreshing of configuration returned status {0}.", result.getStatus()));
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error while refreshing the configuration.", ex);
            }
        }
    }


    public MailSettings getMailSettings() {
        return mailSettings;
    }

    public Map<String, String> getGlobalSettings() {
        return globalSettings;
    }

    public List<InstanceSettings> getAllInstanceSettings() {
        return allInstanceSettings;
    }

    public NewSettingEntry getNewGlobalEntry() {
        return newGlobalEntry;
    }

    public InstanceSettings getNewInstanceSettingsEntry() {
        return newInstanceSettingsEntry;
    }


    public static class MailSettings {
        private String smtpHost;
        private String smtpPort;
        private String mailUser;
        private String mailPassword;
        private Boolean smtpAuth;
        private Boolean smtpSslEnable;
        private Integer connectionTimeout;
        private Integer smtpTimeout;


        MailSettings(MongoCollection mongoCollection) {
            final Document defaultConfigurationDocument =
                    ((Document) mongoCollection.find(new Document("key", null)).first()).get("configuration", Document.class);
            this.smtpHost = defaultConfigurationDocument.getString("mail_smtp_host");
            this.smtpPort = defaultConfigurationDocument.getString("mail_smtp_port");
            this.mailUser = defaultConfigurationDocument.getString("mail_user");
            this.mailPassword = defaultConfigurationDocument.getString("mail_password");
            this.smtpAuth = Boolean.valueOf(defaultConfigurationDocument.getString("mail_smtp_auth"));
            this.smtpSslEnable = Boolean.valueOf(defaultConfigurationDocument.getString("mail_smtp_ssl_enable"));
            this.connectionTimeout = Integer.parseInt(defaultConfigurationDocument.getString("mail_smtp_connectiontimeout"));
            this.smtpTimeout = Integer.parseInt(defaultConfigurationDocument.getString("mail_smtp_timeout"));
        }


        public String getSmtpHost() {
            return smtpHost;
        }

        public void setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
        }

        public String getSmtpPort() {
            return smtpPort;
        }

        public void setSmtpPort(String smtpPort) {
            this.smtpPort = smtpPort;
        }

        public Boolean getSmtpAuth() {
            return smtpAuth;
        }

        public void setSmtpAuth(Boolean smtpAuth) {
            this.smtpAuth = smtpAuth;
        }

        public Boolean getSmtpSslEnable() {
            return smtpSslEnable;
        }

        public void setSmtpSslEnable(Boolean smtpSslEnable) {
            this.smtpSslEnable = smtpSslEnable;
        }

        public Integer getConnectionTimeout() {
            return connectionTimeout;
        }

        public void setConnectionTimeout(Integer connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }

        public Integer getSmtpTimeout() {
            return smtpTimeout;
        }

        public void setSmtpTimeout(Integer smtpTimeout) {
            this.smtpTimeout = smtpTimeout;
        }

        public String getMailUser() {
            return mailUser;
        }

        public void setMailUser(String mailUser) {
            this.mailUser = mailUser;
        }

        public String getMailPassword() {
            return mailPassword;
        }

        public void setMailPassword(String mailPassword) {
            this.mailPassword = mailPassword;
        }
    }


    public static class InstanceSettings {
        private String instanceName;
        private String key;
        private String value;

        public InstanceSettings() {
        }

        public InstanceSettings(String instanceName, String key, String value) {
            this.instanceName = instanceName;
            this.key = key;
            this.value = value;
        }


        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InstanceSettings)) return false;
            InstanceSettings that = (InstanceSettings) o;
            return Objects.equals(instanceName, that.instanceName) &&
                    Objects.equals(key, that.key) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(instanceName, key, value);
        }
    }


    public static class NewSettingEntry {
        private String instanceName;
        private String key;
        private String value;

        public NewSettingEntry() {
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
