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
package de.lottoritter.platform;

import javax.ejb.Singleton;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Ulrich Cech
 */
@Singleton
public class ResourceBundleRepository implements Serializable {

    private static final long serialVersionUID = -9105481104156162795L;

    public ResourceBundleRepository() {}


    public String getLocalized(final Enum<?> enumObject, final Locale locale) {
        return getLocalized(enumObject.getClass().getName(), enumObject.name(), locale);
    }


    public String getLocalized(final String resourceBundleName, final String messageKey, final Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(resourceBundleName, locale);
        if (bundle.containsKey(messageKey)) {
            return bundle.getString(messageKey);
        }
        return "";
    }


    public String getDefaultLocalized(final String messageKey, final Locale locale, Object... messageParams) {
        return getLocalized("de.lottoritter.i18n.Text", messageKey, locale, messageParams);
    }

    public String getLocalized(final String resourceBundleName, final String messageKey, final Locale locale, Object... messageParams) {
        ResourceBundle bundle = ResourceBundle.getBundle(resourceBundleName, locale);
        if (bundle.containsKey(messageKey)) {
            return MessageFormat.format(bundle.getString(messageKey), messageParams);
        }
        return "";
    }


}
