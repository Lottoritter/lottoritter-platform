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
package de.lottoritter.platform.persistence.migration.control;

import de.lottoritter.platform.persistence.migration.entity.Migration;
import de.lottoritter.platform.persistence.migration.versions.V_000000;
import org.mongodb.morphia.Datastore;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * The migration controller is automatically started when application starts up (triggered with the
 * {@link Startup}-annotation) after initialisation of the DBConnection-Bean.
 * <br/><br/>
 * The versions are implemented in the 'versions'-package and have to implement the Migrateable-interface. The naming
 * of the version-classes is important, because the version number is persisted in the migrations-collection, so that
 * we know, which migration was last successful.
 *
 * @author Ulrich Cech
 */
@Singleton
@Startup
@DependsOn("DBConnection")
@SuppressWarnings({"unused", "WeakerAccess"})
public class MigrationController {

    @Inject
    Datastore datastore;


    @PostConstruct
    void startupAutomatically() {
        int counter = 0;
        boolean migrationExists = true;
        while (migrationExists) {
            final String migrationVersion = "V_" + String.format("%06d", counter);
            final String className = getBasePackageName() + "." + migrationVersion;
            try {
                final Class<?> migrationClass = Class.forName(className);
                final Migrateable migratable = (Migrateable) migrationClass.newInstance();
                final Migration persistentMigration = getExistingPersistentMigration(migrationVersion);
                if ((persistentMigration != null) && persistentMigration.isSuccess()) {
                    ++counter;
                } else {
                    if (persistentMigration != null) {
                        executeMigration(migratable, persistentMigration);
                    } else {
                        Migration migration = new Migration(migrationVersion);
                        executeMigration(migratable, migration);
                    }
                    ++counter;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                migrationExists = false;
            }
        }
    }

    String getBasePackageName() {
        return V_000000.class.getPackage().getName();
    }

    private Migration getExistingPersistentMigration(String migrationVersion) {
        return datastore.createQuery(Migration.class).field("migrationVersion").equal(migrationVersion).get();
    }

    private void executeMigration(Migrateable migratable, Migration migration) {
        try {
            migratable.executeMigration(datastore);
            migration.setSuccess(true);
            migration.setErrorStacktrace(null);
        } catch (Exception ex) {
            migration.setSuccess(false);
            migration.setErrorStacktrace(getStacktraceAsString(ex));
        }
        datastore.save(migration);
    }

    private String getStacktraceAsString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
