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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;

import de.lottoritter.business.security.control.EncryptionService;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.mongodb.morphia.mapping.MappedField;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

/**
 * @author Ulrich Cech
 */
public class EncryptedFieldConverterTest {

    private final ZonedDateTime TEST_ZONEDDATETIME = ZonedDateTime.of(2017, 6, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private final String ENCODED_ZONEDDATETIME = "j7gvQX1JegiayE1P6U0D9Q==";
    private static final String TEST_STRING = "This string should be encrypted";
    private static final String ENCODED_STRING = "iFm7IEhDq41K6xcjqYFo1jjNU+7KITZ+ZOHVr0gRlqQ=";
    private static final Integer TEST_INTEGER = 135987665;
    private static final String ENCODED_INTEGER = "5BN7P1DNXhZ+CVv+I0K7Fw==";
    private static final byte[] TEST_BYTE_ARRAY = new byte[] {1,-112,44,48,-99,65,66,-12,-11};
    private static final String ENCODED_BYTE_ARRAY = "q9jx4ZKPjhN1l8hU0/IB4g==";

    @Test
    public void encodeZonedDateTime() throws Exception {
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
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
        EncryptedFieldConverter cut = createCut();
        final Field mockedField = mock(Field.class);
        when(mockedField.isAnnotationPresent(Encrypted.class)).thenReturn(true);
        final MappedField mockedMappedField = mock(MappedField.class);
        when(mockedMappedField.getField()).thenReturn(mockedField);
        when(mockedMappedField.getType()).thenReturn(byte[].class);
        final Object decode = cut.decode(byte[].class, Base64.decodeBase64(ENCODED_BYTE_ARRAY), mockedMappedField);
        assertThat(decode, instanceOf(byte[].class));
        assertThat(decode, is(TEST_BYTE_ARRAY));
    }


    private EncryptedFieldConverter createCut() {
        EncryptedFieldConverter cut = new EncryptedFieldConverter();
        try {
            final EncryptionService encryptionService = cut.getEncryptionService();
            final Field encryptionKeyField = encryptionService.getClass().getDeclaredField("encryptionKey");
            encryptionKeyField.setAccessible(true);
                encryptionKeyField.set(encryptionService, createEncryptionKey("01234567890123456789012345678912"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail(e.getMessage());
        }
        return cut;
    }

    private Instance<String> createEncryptionKey(String val) {
        return new Instance<String>() {
            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @Override
            public Instance<String> select(Annotation... qualifiers) {
                return null;
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(String instance) {

            }

            @Override
            public <U extends String> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public <U extends String> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public String get() {
                return val;
            }
        };
    }

}