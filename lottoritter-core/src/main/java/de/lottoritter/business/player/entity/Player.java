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
package de.lottoritter.business.player.entity;

import de.lottoritter.business.oauth.entity.OAuthData;
import de.lottoritter.business.payment.entity.PaymentLimits;
import de.lottoritter.business.payment.entity.PaymentServiceProvider;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.validation.control.EmailUnique;
import de.lottoritter.business.validation.control.LocaleCodeValid;
import de.lottoritter.business.validation.control.NoSpace;
import de.lottoritter.business.validation.control.NotEmpty;
import de.lottoritter.business.validation.control.ValidationController;
import de.lottoritter.platform.persistence.PersistentEntity;
import de.lottoritter.platform.persistence.encryption.Encrypted;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Transient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.validation.groups.Default;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The user/player of the platform.
 *
 * @author Ulrich Cech
 */
@Entity(value = "players", noClassnameStored = true)
@Indexes({
        @Index(fields = @Field("email"),
               options = @IndexOptions(name = "users_email_1", background = true, unique = true))
})
@EmailUnique
@ValidPassword(message = "{password_passwordAgain_not_match}", groups = {PasswordGroup.class})
@ValidOldPassword(message = "{password_oldPassword_not_match}", groups = {PasswordChangeGroup.class})
public class Player extends PersistentEntity {

    private static final long serialVersionUID = 149723085606875670L;

    private boolean blocked = false;

    @NotNull(message = "{pwHash_not_empty}")
    @DiffIgnore
    private String pwHash;

    @DiffIgnore
    private String pwRenewHash;

    @NotEmpty(message = "{email_not_empty}", groups = {Default.class, LoginGroup.class, ProfileChangeGroup.class})
    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "{email_not_valid}", groups = {Default.class, LoginGroup.class})
    @Encrypted
    private String email;

    @LocaleCodeValid
    @DiffIgnore
    private String localeCode = "de";

    @Embedded(concreteClass = HashSet.class)
    @DiffIgnore
    private Set<RoleId> roles = new HashSet<>();

    @Embedded
    @DiffIgnore
    private UserActivation activation;

    @Embedded
    @DiffIgnore
    private OAuthData oAuthData;

    @DiffIgnore
    private UserState state;

    @DiffIgnore
    private String registrationIpAddress;

    @Embedded
    @NotNull
    @Valid
    @DiffIgnore
    private PaymentLimits paymentLimits;

    @Embedded
    @DiffIgnore
    private List<PaymentServiceProvider> paymentServiceProviderList;

    @Embedded
    @DiffIgnore
    private List<Token> tokenList;

    @Transient
    @DiffIgnore
    private String oldPassword;

    @Transient
    @NotEmpty(message = "{password_not_empty}", groups = {PasswordGroup.class, LoginGroup.class})
    @Size(min = 6, max = 30, message = "{password_size}", groups = {PasswordGroup.class, LoginGroup.class})
    @NoSpace(message = "{password_no_space}", groups = {PasswordGroup.class, LoginGroup.class})
    @DiffIgnore
    private String password;

    @Transient
    @NotEmpty(message = "{passwordAgain_not_empty}", groups = {PasswordGroup.class})
    @Size(min = 6, max = 30, message = "{passwordAgain_size}", groups = {PasswordGroup.class})
    @NoSpace(message = "{passwordAgain_no_space}", groups = {PasswordGroup.class})
    @DiffIgnore
    private String passwordAgain;

    @Transient
    @DiffIgnore
    private boolean rememberMe;


    public Player() {
        this.paymentLimits = new PaymentLimits();
        this.paymentServiceProviderList = new ArrayList<>();
    }


    @PostLoad
    public void postLoad() {
        if (paymentLimits == null) {
            paymentLimits = new PaymentLimits();
        }
        if (paymentServiceProviderList == null) {
            paymentServiceProviderList = new ArrayList<>();
        }
        if (! paymentServiceProviderList.isEmpty()) {
            for (PaymentServiceProvider paymentServiceProvider : paymentServiceProviderList) {
                if (paymentServiceProvider.getPlayerPaymentList() == null) {
                    paymentServiceProvider.setPlayerPaymentList(new ArrayList<>());
                }
            }
        }
        if (activation != null) {
            activation.setOwner(this);
        }
        if (tokenList == null) {
            tokenList = new ArrayList<>();
        }
    }

    public PaymentServiceProvider getPaymentServiceProviderWithCode(PspCode pspCode) {
        for (PaymentServiceProvider paymentServiceProvider : getPaymentServiceProviderList()) {
            if (paymentServiceProvider.getPspCode() == pspCode) {
                return paymentServiceProvider;
            }
        }
        return null;
    }

    public PaymentServiceProvider getPaymentServiceProviderWithCode(String pspCodeString) {
        return getPaymentServiceProviderWithCode(PspCode.valueOf(pspCodeString));
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isNew() {
        return state == null;
    }

    @Override
    public void validate() {
        ValidationController.get(Player.class).processBeanValidation(this);
    }

    public void validateRegistrationFields() {
        ValidationController.get(Player.class).processBeanValidationForGroup(this, Default.class, PasswordGroup.class);
    }

    public void validateLoginFields() {
        ValidationController.get(Player.class).processBeanValidationForGroup(this, LoginGroup.class);
    }

    public String getPwHash() {
        return pwHash;
    }

    public void setPwHash(final String pwHash) {
        this.pwHash = pwHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addRole(RoleId roleId) {
        roles.add(roleId);
    }

    public Set<RoleId> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(RoleId role) {
        return roles.contains(role);
    }

    public UserActivation getActivation() {
        return activation;
    }

    public void setActivation(UserActivation activation) {
        this.activation = activation;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    public String getPwRenewHash() {
        return pwRenewHash;
    }

    public void setPwRenewHash(String pwRenewHash) {
        this.pwRenewHash = pwRenewHash;
    }

    public OAuthData getoAuthData() {
        return oAuthData;
    }

    public void setoAuthData(OAuthData oAuthData) {
        this.oAuthData = oAuthData;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordAgain() {
        return passwordAgain;
    }

    public void setPasswordAgain(String passwordAgain) {
        this.passwordAgain = passwordAgain;
    }

    public String getRegistrationIpAddress() {
        return registrationIpAddress;
    }

    public void setRegistrationIpAddress(String registrationIpAddress) {
        this.registrationIpAddress = registrationIpAddress;
    }

    public void setPaymentLimits(PaymentLimits paymentLimits) {
        this.paymentLimits = paymentLimits;
    }

    public PaymentLimits getPaymentLimits() {
        if (this.paymentLimits == null) {
            this.paymentLimits = new PaymentLimits();
        }
        return paymentLimits;
    }

    public List<PaymentServiceProvider> getPaymentServiceProviderList() {
        return Collections.unmodifiableList(paymentServiceProviderList);
    }

    public void setPaymentServiceProviderList(List<PaymentServiceProvider> paymentServiceProviderList) {
        this.paymentServiceProviderList = paymentServiceProviderList;
    }

    public void addPaymentServiceProvider(PaymentServiceProvider newPaymentServiceProvider) {
        this.paymentServiceProviderList.add(newPaymentServiceProvider);
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void addToken(Token newToken) {
        this.tokenList.add(newToken);
    }

    public void removeToken(Token token) {
        this.tokenList.remove(token);
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        if (!super.equals(o)) return false;
        Player player = (Player) o;
        return Objects.equals(email, player.email) &&
                state == player.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), email, state);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("email", email)
                .append("localeCode", localeCode)
                .append("state", state)
                .toString();
    }
}
