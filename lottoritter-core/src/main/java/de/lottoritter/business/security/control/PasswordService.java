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

import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.entity.Player;
import org.apache.commons.lang3.StringUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Creates and validates passwords.
 *
 * @author Ulrich Cech
 */
@Stateless
public class PasswordService {

    public static final String SALT = "8a72f5ca87fc752141fdba2e80d5d430";

    @Inject
    UserRepository userRepository;


    public PasswordService() {}


    public void setupPasswordToUser(final Player player, final String password) {
        player.setPwHash(createMD5(password)); // could be later encrypted with external key
        player.setPwRenewHash(null);
    }

    public boolean isPasswordValid(final Player player, final String pwHashToCheck) {
        if (pwHashToCheck == null || StringUtils.isBlank(pwHashToCheck)) {
            return false;
        }
        return player.getPwHash().equals(pwHashToCheck);
    }

    public String createMD5(String password) {
        if (password == null) {
            return null;
        }
        MessageDigest md;
        String result = null;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(fromHex(PasswordService.SALT));
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            result = sb.toString();
        } catch (NoSuchAlgorithmException ignore) {}
        return result;
    }

    String toHex(byte[] array)  {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }

    byte[] fromHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
