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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import java.util.HashSet;
import java.util.Set;

import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;

/**
 * @author Ulrich Cech
 */
@ApplicationScoped
public class PlayerIdentityStore implements IdentityStore {

    @Inject
    UserRepository userRepository;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        // check if the credential was UsernamePasswordCredential
        if (credential instanceof UsernamePasswordCredential) {
            String username = ((UsernamePasswordCredential) credential).getCaller();
            String pwHash = ((UsernamePasswordCredential) credential).getPasswordAsString();
            final Player dbPlayer = userRepository.findUserByUsernameAndPwHash(username, pwHash);
            if (dbPlayer != null) {
                Set<String> roleNames = new HashSet<>();
                dbPlayer.getRoles().forEach(r -> roleNames.add(r.name()));
                return new CredentialValidationResult(dbPlayer.getEmail(), roleNames);
            } else {
                return INVALID_RESULT;
            }
        }
        return NOT_VALIDATED_RESULT;
    }
}
