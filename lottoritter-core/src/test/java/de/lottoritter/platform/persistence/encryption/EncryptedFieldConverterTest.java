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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.mongodb.morphia.mapping.MappedField;

/**
 * @author Ulrich Cech
 */
public class EncryptedFieldConverterTest {

    private final ZonedDateTime TEST_ZONEDDATETIME = ZonedDateTime.of(2017, 6, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private final String ENCODED_ZONEDDATETIME = "6c18OG+2etLzw5GEbtXYMA==";
    private static final String TEST_STRING = "This string should be encrypted";
    private static final String ENCODED_STRING = "qkaFVjU0jYOzw3ENcE1mtXvi726hPOjCXQBMoJW6T2M=";
    private static final Integer TEST_INTEGER = 135987665;
    private static final String ENCODED_INTEGER = "GsdrdPVEjdBRY2uQdiM5Hw==";
    private static final byte[] TEST_BYTE_ARRAY = new byte[] {1,-112,44,48,-99,65,66,-12,-11};
    private static final String ENCODED_BYTE_ARRAY = "6ZMM+YPLNdSiFgEfI4WTww==";

    @Test
    public void encodeZonedDateTime() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(EncryptedZonedDateTime.class);
        EncryptedZonedDateTime encryptedDateTime = new EncryptedZonedDateTime(TEST_ZONEDDATETIME);
        final Object encode = cut.encode(encryptedDateTime, mockedMappedField);
        assertThat(Base64.encodeBase64String((byte[]) encode), is(ENCODED_ZONEDDATETIME));
    }

    @Test
    public void decodeZonedDateTime() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(EncryptedZonedDateTime.class);
        final Object decode = cut.decode(ZonedDateTime.class, Base64.decodeBase64(ENCODED_ZONEDDATETIME), mockedMappedField);
        assertThat(decode, instanceOf(EncryptedZonedDateTime.class));
        assertThat(((EncryptedZonedDateTime) decode).getZonedDateTime(), is(TEST_ZONEDDATETIME));
    }

    @Test
    public void encodeString() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(String.class);
        final Object encode = cut.encode(TEST_STRING, mockedMappedField);
        assertThat(encode, instanceOf(byte[].class));
        assertThat(Base64.encodeBase64String((byte[]) encode), is(ENCODED_STRING));
    }

    @Test
    public void decodeString() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(String.class);
        final Object decode = cut.decode(String.class, Base64.decodeBase64(ENCODED_STRING), mockedMappedField);
        assertThat(decode, instanceOf(String.class));
        assertThat(decode, is(TEST_STRING));
    }

    @Test
    public void encodeInteger() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(Integer.class);
        final Object encode = cut.encode(TEST_INTEGER, mockedMappedField);
        assertThat(encode, instanceOf(byte[].class));
        assertThat(Base64.encodeBase64String((byte[]) encode), is(ENCODED_INTEGER));
    }

    @Test
    public void decodeInteger() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(Integer.class);
        final Object decode = cut.decode(Integer.class, Base64.decodeBase64(ENCODED_INTEGER), mockedMappedField);
        assertThat(decode, instanceOf(Integer.class));
        assertThat(decode, is(TEST_INTEGER));
    }

    @Test
    public void encodeByteArray() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(byte[].class);
        final Object encode = cut.encode(TEST_BYTE_ARRAY, mockedMappedField);
        assertThat(encode, instanceOf(byte[].class));
        assertThat(Base64.encodeBase64String((byte[]) encode), is(ENCODED_BYTE_ARRAY));

    }

    @Test
    public void decodeByteArray() throws Exception {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(byte[].class);
        final Object decode = cut.decode(byte[].class, Base64.decodeBase64(ENCODED_BYTE_ARRAY), mockedMappedField);
        assertThat(decode, instanceOf(byte[].class));
        assertThat(decode, is(TEST_BYTE_ARRAY));
    }
}