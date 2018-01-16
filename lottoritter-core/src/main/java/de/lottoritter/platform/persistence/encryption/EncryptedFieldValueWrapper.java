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
package de.lottoritter.platform.persistence.encryption;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * This class is used for MongoDB-queries, when searching for encrypted fields.
 * Then, the encrypted field is converted through the TypeConverter to the decrypted field value,
 * which then can be used in the normal query-execution.
 *
 * @author Ulrich Cech
 */
public class EncryptedFieldValueWrapper {

    private static final Charset ENCODING = Charset.forName("UTF-8");

    private final byte[] value;


    /**
     * Use this for the encrypted field value.
     */
    public EncryptedFieldValueWrapper(byte[] value) {
        this.value = value;
    }

    /**
     * Use this for the unencrypted field value
     */
    public EncryptedFieldValueWrapper(Object value) {
        Objects.nonNull(value);
        String stringValue = value.toString();
        this.value = stringValue.getBytes(ENCODING);
    }

    public byte[] getValue() {
        return value;
    }

}
