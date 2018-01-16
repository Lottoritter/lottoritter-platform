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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import de.lottoritter.platform.persistence.FongoDbPersistenceTest;
import de.lottoritter.platform.persistence.migration.entity.Migration;
import de.lottoritter.platform.persistence.migration.test_versions.V_000000;

/**
 * @author Ulrich Cech
 */
public class MigrationControllerTest extends FongoDbPersistenceTest {

    @Test
    public void testMigration() {
        MigrationController migrationController = new MigrationController();
        migrationController.datastore = getDatastore();
        final MigrationController spyMigrationController = Mockito.spy(migrationController);
        when(spyMigrationController.getBasePackageName()).thenReturn(V_000000.class.getPackage().getName());
        spyMigrationController.startupAutomatically();
        final List<Migration> migrations = spyMigrationController.datastore.createQuery(Migration.class).asList();
        assertThat(migrations.size(), is(3));
        assertThat(migrations.get(0).isSuccess(), is(true));
        assertThat(migrations.get(1).isSuccess(), is(true));
        assertThat(migrations.get(2).isSuccess(), is(false));
        assertThat(migrations.get(2).getErrorStacktrace().startsWith("org.mongodb.morphia.query.UpdateException: Can not persist a null entity"), is(true));
    }

}