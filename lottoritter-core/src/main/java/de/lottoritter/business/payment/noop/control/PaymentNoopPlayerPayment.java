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
package de.lottoritter.business.payment.noop.control;

import de.lottoritter.business.payment.control.PaymentControllerProxy;
import de.lottoritter.business.payment.entity.CardType;
import de.lottoritter.business.payment.entity.PaymentServiceProvider;
import de.lottoritter.business.payment.entity.PaymentType;
import de.lottoritter.business.payment.entity.PlayerPayment;
import de.lottoritter.business.payment.entity.PspCode;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.player.entity.PlayerPostCreationEvent;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Ulrich Cech
 */
@ApplicationScoped
public class PaymentNoopPlayerPayment {

    @Inject
    Datastore datastore;

    @Inject
    PaymentControllerProxy paymentControllerProxy;



    public void createNoopPlayerPayment(@Observes @PlayerPostCreationEvent Player player) {
        if (paymentControllerProxy.getController().getPspCode() == PspCode.NO_OP) {
            final PaymentServiceProvider newPaymentServiceProvider = new PaymentServiceProvider(PspCode.NO_OP,
                    new ObjectId().toString());
            PlayerPayment newPlayerPayment = new PlayerPayment();
            newPlayerPayment.setExternalId(new ObjectId().toString());
            newPlayerPayment.setType(PaymentType.CREDITCARD);
            newPlayerPayment.setClientId(newPaymentServiceProvider.getExternalClientId());
            newPlayerPayment.setCardType(CardType.VISA);
            newPlayerPayment.setCardOrAccountHolder("Max Mustermann Testcard");
            newPlayerPayment.setExpireMonth(12);
            newPlayerPayment.setExpireYear(3000);
            newPlayerPayment.setLast4("1234");
            newPaymentServiceProvider.addPlayerPayment(newPlayerPayment);
            player.addPaymentServiceProvider(newPaymentServiceProvider);
            datastore.update(
                    datastore.createQuery(Player.class).field("_id").equal(player.getId()),
                    datastore.createUpdateOperations(Player.class).addToSet("paymentServiceProviderList", newPaymentServiceProvider)
            );
        }
    }

}
