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
package de.lottoritter.business.mailing.control;

import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.business.payment.boundary.PriceFormatter;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.UserState;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;

import javax.ejb.Singleton;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christopher Schmidt
 */
@Singleton
public class MailController {

    private static final Logger logger = Logger.getLogger(MailController.class.getName());

    @Inject
    private MailLocalizer mailLocalizer;

    @Inject
    private PartialHtmlMailInjector partialHtmlMailInjector;

    @Inject
    private MailService mailService;

    @Inject
    private Datastore datastore;

    @Inject
    private PriceFormatter priceFormatter;

    @Inject
    @Configurable(value = "server_base_uri", defaultValue = "http://localhost:8080")
    private Instance<String> configServerBaseUri;


    public void sendActivationRequestMail(final Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/activation-request.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());
            String localizedMail = mailLocalizer.localizeMail(MailType.ActivationRequest, rawMail, playerLocale);

            String activationLink = configServerBaseUri.get() + "/api/player/activation?code="+ player.getActivation().getCode();
            localizedMail = StringUtils.replace(localizedMail,"{{activationLink}}", activationLink);

            String localizedSubject = mailLocalizer.localizeSubject(MailType.ActivationRequest, playerLocale);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);

            UpdateOperations<Player> updateOperations = datastore.createUpdateOperations(Player.class).set("state", UserState.PENDING);
            datastore.update(datastore.createQuery(Player.class).filter("_id", player.getId()), updateOperations);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendResetPasswordRequestMail(final Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/password-reset-request.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String token = Base64.getEncoder().encodeToString(player.getPwRenewHash().getBytes());
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());
            String resetLink = configServerBaseUri.get() + "/resetpw.xhtml?cp=true&code=" + token;

            String localizedMail = mailLocalizer.localizeMail(MailType.ResetPasswordRequest, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.ResetPasswordRequest, playerLocale);
            localizedMail = StringUtils.replace(localizedMail,"{{resetLink}}", resetLink);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendActivationConfirmedMail(final Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/activation-confirmation.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());

            String localizedMail = mailLocalizer.localizeMail(MailType.ActivationConfirmation, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.ActivationConfirmation, playerLocale);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);

            UpdateOperations<Player> updateOperations = datastore.createUpdateOperations(Player.class).set("state", UserState.PENDING);
            datastore.update(datastore.createQuery(Player.class).filter("_id", player.getId()), updateOperations);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendLimitsChangedMail(final Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/limits-changed.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());

            String localizedMail = mailLocalizer.localizeMail(MailType.LimitsChanged, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.LimitsChanged, playerLocale);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendResetPasswordConfirmationMail(Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/password-reset-confirmation.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());

            String localizedMail = mailLocalizer.localizeMail(MailType.ResetPasswordConfirmation, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.ResetPasswordConfirmation, playerLocale);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendPurchaseConfirmationMail(ShoppingCart shoppingCart, Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/purchase-confirmation.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());

            String localizedMail = mailLocalizer.localizeMail(MailType.PurchaseConfirmation, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.PurchaseConfirmation, playerLocale);

            localizedMail = StringUtils.replace(localizedMail, "((ticketlist))", partialHtmlMailInjector.injectTicketList(shoppingCart.getTicketList(), playerLocale));
            localizedMail = StringUtils.replace(localizedMail, "{{total}}", priceFormatter.getPriceFormatted(shoppingCart.getTotalPrice()));

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendSelfExclusionConfirmationMail(Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/self-exclusion-confirmation.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());

            String localizedMail = mailLocalizer.localizeMail(MailType.SelfExclusion, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.SelfExclusion, playerLocale);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendTicketsDecayReminderMail(Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/tickets-decay-reminder.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());

            String cartLink = configServerBaseUri.get() + "/shoppingcart";
            String localizedMail = mailLocalizer.localizeMail(MailType.TicketsDecayInCartReminder, rawMail, playerLocale);
            String localizedSubject = mailLocalizer.localizeSubject(MailType.TicketsDecayInCartReminder, playerLocale);
            localizedMail = StringUtils.replace(localizedMail,"{{cartLink}}", cartLink);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void sendEmailChangedMail(Player player) {
        final InputStream mailTemplateStream = this.getClass().getResourceAsStream("/template/mail/change-email.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(mailTemplateStream, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();
            Locale playerLocale = new Locale(player.getLocaleCode());
            String localizedMail = mailLocalizer.localizeMail(MailType.EmailChanged, rawMail, playerLocale);

            String activationLink = configServerBaseUri.get() + "/api/player/emailapproved?code="+ player.getActivation().getCode();
            localizedMail = StringUtils.replace(localizedMail,"{{activationLink}}", activationLink);

            String localizedSubject = mailLocalizer.localizeSubject(MailType.EmailChanged, playerLocale);

            mailService.sendMail(localizedSubject, player.getEmail(), localizedMail);
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
