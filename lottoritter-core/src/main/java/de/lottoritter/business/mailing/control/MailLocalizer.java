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

import de.lottoritter.platform.ResourceBundleRepository;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Christopher Schmidt
 */
@Singleton
public class MailLocalizer {

    private static final Pattern LOCALIZED_PATTERN = Pattern.compile("(?<=\\[\\[)(.*?)(?=\\]\\])");
    private static final String MAIL_RESOURCE_BUNDLE_NAME = "MailMessages";

    private Map<Locale, Map<MailType, String>> cachedLocalizedMails;
    private Map<Locale, Map<MailType.PartialMailType, String>> cachedLocalizedPartials;
    private Map<Locale, Map<MailType, String>> cachedLocalizedSubjects;

    @Inject
    private ResourceBundleRepository resourceBundleRepository;

    @PostConstruct
    private void init() {
        cachedLocalizedMails = new HashMap<>();
        cachedLocalizedSubjects = new HashMap<>();
        cachedLocalizedPartials = new HashMap<>();
    }

    public String localizePartial(MailType.PartialMailType partialMailType, String partial, Locale locale) {
        if (!cachedLocalizedPartials.containsKey(locale)) {
            HashMap<MailType.PartialMailType, String> translatedPartials = new HashMap<>();
            String localizedPartial = localize(partial, locale);
            translatedPartials.put(partialMailType, localizedPartial);
            cachedLocalizedPartials.put(locale, translatedPartials);
            return localizedPartial;
        } else {
            Map<MailType.PartialMailType, String> translatedPartials = cachedLocalizedPartials.get(locale);

            if (!translatedPartials.containsKey(partialMailType)) {
                String localizedPartial = localize(partial, locale);
                translatedPartials.put(partialMailType, localizedPartial);
                return localizedPartial;
            } else {
                return translatedPartials.get(partialMailType);
            }
        }
    }

    public String localizeMail(MailType mailType, String rawMail, Locale locale) {
        if (!cachedLocalizedMails.containsKey(locale)) {
            HashMap<MailType, String> translatedMails = new HashMap<>();
            String localizedMail = localize(rawMail, locale);
            translatedMails.put(mailType, localizedMail);
            cachedLocalizedMails.put(locale, translatedMails);
            return localizedMail;
        } else {
            Map<MailType, String> translatedMails = cachedLocalizedMails.get(locale);

            if (!translatedMails.containsKey(mailType)) {
                String localizedMail = localize(rawMail, locale);
                translatedMails.put(mailType, localizedMail);
                return localizedMail;
            } else {
                return translatedMails.get(mailType);
            }
        }
    }

    public String localizeSubject(MailType mailType, Locale locale) {
        if (!cachedLocalizedSubjects.containsKey(locale)) {
            HashMap<MailType, String> translatedSubjects = new HashMap<>();
            String localizedSubject = localize(mailType.getSubjectKey(), locale);
            translatedSubjects.put(mailType, localizedSubject);
            cachedLocalizedSubjects.put(locale, translatedSubjects);
            return localizedSubject;
        } else {
            Map<MailType, String> translatedSubjects = cachedLocalizedSubjects.get(locale);

            if (!translatedSubjects.containsKey(mailType)) {
                String localizedSubject = localize(mailType.getSubjectKey(), locale);
                translatedSubjects.put(mailType, localizedSubject);
                return localizedSubject;
            } else {
                return translatedSubjects.get(mailType);
            }
        }
    }

    public void copyStreamToWriter(InputStream source, Writer target, Charset encoding) throws IOException {
        char[] buffer = new char[4096];
        final InputStreamReader sourceReader = new InputStreamReader(source, Charsets.toCharset(encoding));
        int counter = 0;
        while (-1 != (counter = sourceReader.read(buffer))) {
            target.write(buffer, 0, counter);
        }
    }


    private String localize(final String text, final Locale locale) {
        String localizedText = text;
        Matcher matcher = LOCALIZED_PATTERN.matcher(text);
        int start = 0;

        while (matcher.find(start)) {
            String currentKey = matcher.group(0);
            start = matcher.end(0);
            String localized = resourceBundleRepository.getLocalized(MAIL_RESOURCE_BUNDLE_NAME, currentKey, locale);
            localizedText = StringUtils.replace(localizedText, "[[" + currentKey + "]]", localized);
        }

        return localizedText;
    }
}
