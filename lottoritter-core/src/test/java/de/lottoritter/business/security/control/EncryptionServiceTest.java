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

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.List;

import static org.junit.Assert.assertThat;

/**
 * @author Ulrich Cech
 */
public class EncryptionServiceTest extends FongoDbPersistenceTest {

    @Test
    public void encryptData() {
        final String base64Encoded = "4EGUAy2YZUIPrOAgciWyxkMn6q7a0JlTsu8zI8kdM+s=";
        EncryptionService cut = new EncryptionService();
        final Charset charset = Charset.forName("UTF-8");
        final byte[] bytes = cut.encryptData("test@example.com".getBytes(charset));
        assertThat(new String(Base64.getEncoder().encode(bytes)), CoreMatchers.is(base64Encoded));

        TestPlayer testPlayer = new TestPlayer();
        testPlayer.setEmail("test@example.com");
        getDatastore().save(testPlayer);

        final List<Player> foundPlayerList = getDatastore().find(Player.class).field("email").equal(new EncryptedFieldValueWrapper("test@example.com")).asList();
        assertThat(foundPlayerList.size(), CoreMatchers.is(1));
    }

    @Test
    public void decryptData() throws Exception {
        final String base64Encoded = "4EGUAy2YZUIPrOAgciWyxkMn6q7a0JlTsu8zI8kdM+s=";
        EncryptionService cut = new EncryptionService();
        assertThat(new String(cut.decryptData(Base64.getDecoder().decode(base64Encoded))), Is.is("test@example.com"));
    }


    static class TestPlayer extends Player {
        @Override
        public void validate() {
        }
    }

}