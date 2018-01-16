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
package de.lottoritter.platform.persistence.migration.entity;

import de.lottoritter.platform.persistence.PersistentEntity;
import org.mongodb.morphia.annotations.Entity;

/**
 * @author Ulrich Cech
 */
@Entity(value = "migrations", noClassnameStored = true)
public class Migration extends PersistentEntity {

    private String migrationVersion;

    private boolean success;

    private String errorStacktrace;


    public Migration() {
    }

    public Migration(String migrationVersion) {
        this.migrationVersion = migrationVersion;
    }


    public String getMigrationVersion() {
        return migrationVersion;
    }

    public void setMigrationVersion(String migrationVersion) {
        this.migrationVersion = migrationVersion;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorStacktrace() {
        return errorStacktrace;
    }

    public void setErrorStacktrace(String errorStacktrace) {
        this.errorStacktrace = errorStacktrace;
    }
}
