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

import de.lottoritter.business.player.entity.PlzCityState;
import de.lottoritter.platform.persistence.migration.control.Migrateable;
import org.mongodb.morphia.Datastore;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

/**
 * Imports CityCode, Cityname and Province reference file into database.
 *
 * @author Ulrich Cech
 */
@SuppressWarnings("unused")
public class V_000002 implements Migrateable {

    @Override
    public void executeMigration(Datastore datastore) throws IOException {
        BufferedReader in = getBufferedReader();
        String line;
        List<PlzCityState> plzCityStateList = new LinkedList<>();
        while ((line = in.readLine()) != null) {
            final String[] strings = line.split("\\t");
            PlzCityState plzCityState = new PlzCityState(strings[1], strings[2], strings[0]);
            plzCityStateList.add(plzCityState);
        }
        datastore.save(plzCityStateList);
    }

    BufferedReader getBufferedReader() throws IOException {
        final InputStream resourceAsStream = getInputStream();
        ByteArrayOutputStream downloadableFileStream = new ByteArrayOutputStream();
        int index;
        byte[] buffer = new byte[4096];
        while ( (index = resourceAsStream.read(buffer)) != -1 ) {
            downloadableFileStream.write(buffer, 0, index);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(downloadableFileStream.toByteArray());
        return new BufferedReader(new InputStreamReader(bis, StandardCharsets.UTF_8));
    }

    InputStream getInputStream() {
        return V_000002.class.getResourceAsStream("OpenGeoDB_bundesland_plz_ort_de.csv");
    }


}
