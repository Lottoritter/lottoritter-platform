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
package de.lottoritter.business.player.boundary;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.lottoritter.business.lotteries.MainTicket;
import de.lottoritter.business.lotteries.Price;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Field;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Ticket;
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleField;
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleTicket;
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.player.control.UserRepository;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.RoleId;
import de.lottoritter.business.player.entity.UserActivation;
import de.lottoritter.business.player.entity.UserState;
import de.lottoritter.business.shoppingcart.control.ShoppingCartRepository;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import de.lottoritter.business.mailing.control.MailType;
import de.lottoritter.platform.rest.AbstractRestResource;

/**
 * REST-Resource to test mail types
 *
 * @author Christopher Schmidt
 */
@Path("mailing")
@Produces({AbstractRestResource.MEDIA_TYPE_JSON_STRING, AbstractRestResource.MEDIA_TYPE_XML_STRING})
@Consumes({AbstractRestResource.MEDIA_TYPE_JSON_STRING, AbstractRestResource.MEDIA_TYPE_XML_STRING})
@RequestScoped
public class MailTestResource extends AbstractRestResource {

    @Inject
    MailController mailController;

    @Inject
    UserRepository userRepository;

    @Inject
    private ShoppingCartRepository shoppingCartRepository;

    @GET
    @Path("{mail}/{mailType}")
    @RolesAllowed({RoleId.ADMIN_STRING})
    public Response activateAccount(@PathParam("mail") String email, @PathParam("mailType") MailType mailType) {
        Player player = userRepository.findUserByEmail(email);

        if (player != null) {
            switch (mailType) {
                case ActivationRequest:
                    player.setActivation(new UserActivation(player));
                    player.setState(UserState.REGISTERED);
                    mailController.sendActivationRequestMail(player);
                    break;
                case ActivationConfirmation:
                    mailController.sendActivationConfirmedMail(player);
                    break;
                case ResetPasswordRequest:
                    userRepository.resetPasswordStart(player);
                    mailController.sendResetPasswordRequestMail(player);
                    break;
                case ResetPasswordConfirmation:
                    mailController.sendResetPasswordConfirmationMail(player);
                    break;
                case PurchaseConfirmation:
                    ShoppingCart shoppingCart = createTempShoppingCart(player);
                    mailController.sendPurchaseConfirmationMail(shoppingCart, player);
                    break;
                case LimitsChanged:
                    mailController.sendLimitsChangedMail(player);
                    break;
                case SelfExclusion:
                    mailController.sendSelfExclusionConfirmationMail(player);
                    break;
                case TicketsDecayInCartReminder:
                    mailController.sendTicketsDecayReminderMail(player);
                    break;
                case EmailChanged:
                    player.setActivation(new UserActivation(player));
                    mailController.sendEmailChangedMail(player);
                    break;
            }
        }

        return Response.ok().build();
    }

    private ShoppingCart createTempShoppingCart(Player player) {
        ShoppingCart shoppingCart = new ShoppingCart(player);

        GluecksspiraleTicket gluecksspiraleTicket = new GluecksspiraleTicket();
        gluecksspiraleTicket.setNumber(new Integer[]{9, 8, 7, 6, 5, 4, 3});
        gluecksspiraleTicket.setTicketFee(new Price(60));
        gluecksspiraleTicket.setTicketPrice(new Price(1200));

        German6aus49Ticket german6aus49Ticket = new German6aus49Ticket();
        german6aus49Ticket.setTicketFee(new Price(60));
        german6aus49Ticket.setTicketPrice(new Price(1200));
        german6aus49Ticket.setNumber(new Integer[]{9, 8, 7, 6, 5, 4, 3});
        Integer[] numbers = new Integer[6];
        numbers[0] = 1;
        numbers[1] = 2;
        numbers[2] = 3;
        numbers[3] = 4;
        numbers[4] = 5;
        numbers[5] = 6;
        German6aus49Field field = new German6aus49Field(1, numbers);
        GluecksspiraleField gluecksspiraleField = new GluecksspiraleField();
        gluecksspiraleField.setSelectedNumbers(Arrays.asList(9, 8, 7, 6, 5, 4, 3));
        gluecksspiraleField.setParentTicket(gluecksspiraleTicket);
        gluecksspiraleTicket.setFields(Collections.singletonList(gluecksspiraleField));

        german6aus49Ticket.setFields(Arrays.asList(field, field));
        List<MainTicket> ticketList = Arrays.asList(german6aus49Ticket, german6aus49Ticket, gluecksspiraleTicket);
        shoppingCart.setTicketList(ticketList);
        return shoppingCart;
    }
}
