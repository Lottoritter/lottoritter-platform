package de.lottoritter.business.payment.noop.control;

import de.lottoritter.business.payment.control.AbstractPaymentSubscriptionSynchronizer;
import de.lottoritter.business.player.entity.Player;

import javax.ejb.Singleton;

/**
 * @author Ulrich Cech
 */
@Singleton
public class PaymentNoopSynchronizer extends AbstractPaymentSubscriptionSynchronizer {

    @Override
    public void checkForNewSubscriptionTransactions(Player player) {}

}
