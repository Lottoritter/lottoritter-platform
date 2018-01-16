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

import de.lottoritter.business.lotteries.Field;
import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Ticket;
import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotField;
import de.lottoritter.business.payment.boundary.PriceFormatter;
import de.lottoritter.platform.ResourceBundleRepository;
import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christopher Schmidt
 */
@ApplicationScoped
public class PartialHtmlMailInjector {

    @Inject
    private MailLocalizer mailLocalizer;

    @Inject
    private PriceFormatter priceFormatter;

    @Inject
    private ResourceBundleRepository resourceBundleRepository;

    private static final Logger logger = Logger.getLogger(PartialHtmlMailInjector.class.getName());

    public String injectTicketList(List<MainTicket> tickets, Locale playerLocale) {
        final InputStream partial = this.getClass().getResourceAsStream("/template/mail/partials/ticket-header-line.html");
        StringWriter stringWriter = new StringWriter();
        try {
            mailLocalizer.copyStreamToWriter(partial, stringWriter, StandardCharsets.UTF_8);
            String rawMail = stringWriter.getBuffer().toString();

            String localizedPartial = mailLocalizer.localizePartial(MailType.PurchaseConfirmation.getPartialMailType(), rawMail, playerLocale);
            StringBuilder result = new StringBuilder();
            String current = localizedPartial;

            for (MainTicket ticket : tickets) {
                String lottery = resourceBundleRepository.getLocalized("MailMessages", "mail.purchase.confirmation.lottery." + ticket.getLotteryIdentifier(), playerLocale);
                current = StringUtils.replace(current, "{{lottery}}", lottery);
                current = StringUtils.replace(current, "{{number}}", Arrays.toString(ticket.getNumber()));
                current = StringUtils.replace(current, "{{fields}}", getTicketFields(ticket));
                current = StringUtils.replace(current, "{{price}}", priceFormatter.getPriceFormatted(ticket.getTotalTicketPrice()));
                result.append(current);
                current = localizedPartial;
            }

            return result.toString();
        } catch (Exception ex) {
            // TODO  alert for administrator, highest priority!!!
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return "";
    }

    private String getTicketFields(Ticket ticket) {
        StringBuilder builder = new StringBuilder();
        for (Object o : ticket.getFields()) {
            Field field = (Field) o;
            if (field.isValidFilled()) {
                final List<Integer> selectedNumbers = field.getSelectedNumbers();
                builder.append("<span style=\"font-size:12px\">");

                String ball = "<span style=\"border: 1px solid;" +
                        "border-radius: 100%;" +
                        "width: 20px;" +
                        "height: 20px;" +
                        "display: block;" +
                        "text-align: center;" +
                        "line-height: 20px;" +
                        "float: left;" +
                        "margin-right: 5px;\">";


                for (Integer selectedNumber : selectedNumbers) {
                    builder.append(ball);
                    builder.append(selectedNumber);
                    builder.append("</span>");
                }

                if (field instanceof EuroJackpotField) {
                    EuroJackpotField euroJackpotField = (EuroJackpotField) field;

                    builder.append("<span style=\"line-height: 20px; width: 10px; display: block; float: left;\">+</span>");

                    for (Integer additionalNumber : euroJackpotField.getSelectedAdditionalNumbers()) {
                        builder.append(ball);
                        builder.append(additionalNumber);
                        builder.append("</span>");
                    }
                }
                builder.append("</span><br/><br/>");
            }
        }
        return builder.toString();
    }
}
