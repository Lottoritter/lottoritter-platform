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

import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.platform.persistence.DBConnection;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles the configuration of the application.
 *
 * @author Ulrich Cech
 */
@Singleton
@Startup
@DependsOn("DBConnection")
@Named
public class ConfigurationController implements Serializable {

    private static final long serialVersionUID = 6962217699259540044L;

    private static final Logger logger = Logger.getLogger(ConfigurationController.class.getName());

    @Inject
    DBConnection dbConnection;

    private Map<String, String> configCache = new HashMap<>();


    public ConfigurationController() {
    }


    @PostConstruct
    public void initializeConfiguration() {
        MongoDatabaseConfiguration config =
                new MongoDatabaseConfiguration(dbConnection.getMongoDatabase());

        // initialize cache
        String appserverConfigName = System.getenv("lottoritter_jvmRoute");
        Properties baseConfig = config.getKeysForConfigurationKey(MongoDatabaseConfiguration.CKEY_VALUE_BASE_CONFIGURATION);
        if (baseConfig.isEmpty()) {
            baseConfig = config.prepareInitialBaseConfiguration();
        }
        for (Map.Entry<Object, Object> objectObjectEntry : baseConfig.entrySet()) {
            configCache.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue().toString());
        }

        Properties appServerConfig = config.getKeysForConfigurationKey(appserverConfigName);
        for (Map.Entry<Object, Object> objectObjectEntry : appServerConfig.entrySet()) {
            configCache.put(objectObjectEntry.getKey().toString(), objectObjectEntry.getValue().toString());
        }
        StringBuilder builder = new StringBuilder("Starting with configuration:\n");
        for (Map.Entry<String, String> entry : getConfigCache().entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info(builder.toString());
        }
    }

    public void refreshConfiguration() {
        getConfigCache().clear();
        initializeConfiguration();
    }


    @Produces @Configurable(value = "", defaultValue = "")
    public String getString(InjectionPoint ip) {
        String className = ip.getMember().getDeclaringClass().getName();
        String key = className + "." + ip.getMember().getName();
        String fieldName = computeKeyName(ip.getAnnotated(), key);
        String value = this.configCache.get(fieldName);
        if (value == null) {
            value = ip.getAnnotated().getAnnotation(Configurable.class).defaultValue();
        }
        return value;
    }

    @Produces @Configurable(value = "", defaultValue = "0")
    public long getLong(InjectionPoint ip) {
        String stringValue = getString(ip);
        if (stringValue == null) {
            return 0;
        }
        return Long.parseLong(stringValue);
    }

    @Produces @Configurable(value = "", defaultValue = "false")
    public Boolean getBoolean(InjectionPoint ip) {
        String stringValue = getString(ip);
        if (stringValue == null) {
            return false;
        }
        return Boolean.valueOf(stringValue);
    }

    @Produces @Configurable(value = "mail_settings", defaultValue = "")
    public Properties getMailSmtpSettings() {
        Properties props = new Properties();
        getConfigCache().entrySet()
                .stream()
                .filter(
                        p -> p.getKey().startsWith("mail_smtp"))
                .collect(Collectors.toSet())
                .forEach(p -> props.setProperty(p.getKey().replaceAll("_", ".") , String.valueOf(p.getValue())));
        return props;
    }

    @Produces @Configurable(value = "primaryPSP", defaultValue = "NO_OP")
    public PspCode getPrimaryPaymentServiceProvider(InjectionPoint ip) {
        try {
            return PspCode.valueOf(getString(ip));
        } catch (IllegalArgumentException ex) {
            logger.severe("PSP-code not recognized. Using NO_OP as fallback.");
        }
        return PspCode.NO_OP;
    }

    private String computeKeyName(Annotated annotated, String key) {
        Configurable annotation = annotated.getAnnotation(Configurable.class);
        return annotation == null ? key : annotation.value();
    }


    private Map<String, String> getConfigCache() {
        return configCache;
    }

}
