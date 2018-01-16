package de.lottoritter.business.payment.control;

import de.lottoritter.business.player.entity.Player;

/**
 * @author Ulrich Cech
 */
public abstract class AbstractPaymentSubscriptionSynchronizer {

    public abstract void checkForNewSubscriptionTransactions(final Player player);

}
