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
package de.lottoritter.business.security.control;

import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.persistence.FongoDbPersistenceTest;
import de.lottoritter.platform.persistence.encryption.EncryptedFieldValueWrapper;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * @author Ulrich Cech
 */
public class EncryptionServiceTest extends FongoDbPersistenceTest {

    @Test
    public void encryptData() throws NoSuchFieldException, IllegalAccessException {
        final String base64Encoded = "hfZcGLKmy9Q9iPRR+3XfmV7TzHoYU+6NKvJk4gSZfI0=";
        EncryptionService cut = new EncryptionService();
        cut.encryptionKey = createEncryptionKey("01234567890123456789012345678912");
        final Charset charset = Charset.forName("UTF-8");
        final byte[] bytes = cut.encryptData("test@example.com".getBytes(charset));
        assertThat(new String(Base64.getEncoder().encode(bytes)), CoreMatchers.is(base64Encoded));

        final EncryptionService encryptionService = getEncryptedFieldConverter().getEncryptionService();
        final Field encryptionKeyField = encryptionService.getClass().getDeclaredField("encryptionKey");
        encryptionKeyField.setAccessible(true);
        encryptionKeyField.set(encryptionService, cut.encryptionKey);

        TestPlayer testPlayer = new TestPlayer();
        testPlayer.setEmail("test@example.com");
        getDatastore().save(testPlayer);

        final List<Player> foundPlayerList = getDatastore().find(Player.class).field("email").equal(new EncryptedFieldValueWrapper("test@example.com")).asList();
        assertThat(foundPlayerList.size(), CoreMatchers.is(1));
    }

    @Test
    public void decryptData() throws Exception {
        final String base64Encoded = "hfZcGLKmy9Q9iPRR+3XfmV7TzHoYU+6NKvJk4gSZfI0=";
        EncryptionService cut = new EncryptionService();
        cut.encryptionKey = createEncryptionKey("01234567890123456789012345678912");
        assertThat(new String(cut.decryptData(Base64.getDecoder().decode(base64Encoded))), Is.is("test@example.com"));
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


    static class TestPlayer extends Player {
        @Override
        public void validate() {
        }
    }

}