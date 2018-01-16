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

import de.lottoritter.business.payment.entity.Bank;
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
 * Imports the Bundesbank-file with BLZ, BIC and Bankname.
 *
 * @author Ulrich Cech
 */
@SuppressWarnings("unused")
public class V_000001 implements Migrateable {

//    private int[] fixedLengthFields = {8, 1, 58, 5, 35, 27, 5, 11, 2, 6, 10};

    @Override
    public void executeMigration(Datastore datastore) throws IOException {
        final BufferedReader in = getBufferedReaderForFile();
        String line;
        String lastBic = "";
        List<Bank> bankList = new LinkedList<>();
        while ((line = in.readLine()) != null) {
            String bic = line.substring(139, 150).trim();
            if (bic.length() == 0) {
                bic = lastBic;
            } else {
                lastBic = bic;
            }
            Bank bank = new Bank(line.substring(0, 8),
                    line.substring(9, 67).trim(),
                    line.substring(67, 72).trim(),
                    line.substring(72, 107).trim(),
                    line.substring(107, 134).trim(),
                    bic);
            bankList.add(bank);
        }
        datastore.save(bankList);
    }

    BufferedReader getBufferedReaderForFile() throws IOException {
        final InputStream resourceAsStream = getInputStream();
        ByteArrayOutputStream downloadableFileStream = new ByteArrayOutputStream();
        int index;
        byte[] buffer = new byte[4096];
        while ( (index = resourceAsStream.read(buffer)) != -1 ) {
            downloadableFileStream.write(buffer, 0, index);
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(downloadableFileStream.toByteArray());
        return new BufferedReader(new InputStreamReader(bis, StandardCharsets.ISO_8859_1));
    }

    InputStream getInputStream() {
        return V_000001.class.getResourceAsStream("blz_2017_03_06_txt.txt");
    }

}
