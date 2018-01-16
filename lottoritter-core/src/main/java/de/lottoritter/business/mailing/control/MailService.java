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

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.lottoritter.business.configuration.control.Configurable;
import de.lottoritter.platform.PlatformException;

/**
 * Provides mail functionality.
 *
 * @author Ulrich Cech
 */
@Stateless
public class MailService {

    private static final Logger logger = Logger.getLogger(MailService.class.getName());


    @Inject @Configurable(value = "mail_user", defaultValue = "")
    Instance<String> configMailUser;

    @Inject @Configurable(value = "mail_password", defaultValue = "")
    Instance<String> configMailUserPassword;

    @Inject @Configurable(value = "mail_settings", defaultValue = "")
    Instance<Properties> configMailSettings;


    public void sendMail(final String subject, final String recipient, final String text) {
        Message msg = new MimeMessage(createSmtpSession());
        try {
            msg.setSubject(subject);
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            msg.setFrom(new InternetAddress(configMailUser.get()));
            msg.setContent(text, "text/html; charset=utf-8");
            Transport.send(msg);
        } catch(MessagingException mex) {
            logger.log(Level.SEVERE, "Could not send mail.", mex);
            //TODO  alert for administrator, highest priority!!!
            throw new PlatformException(mex);
        }
    }


    Session createSmtpSession() {
        final Properties props = configMailSettings.get();

        return Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(configMailUser.get(), configMailUserPassword.get());
            }
        });
    }

}
