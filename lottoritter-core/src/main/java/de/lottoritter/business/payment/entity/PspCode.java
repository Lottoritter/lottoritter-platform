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
package de.lottoritter.business.payment.entity;

import de.lottoritter.business.payment.control.AbstractPaymentController;
import de.lottoritter.business.payment.control.AbstractPaymentSubscriptionSynchronizer;
import de.lottoritter.business.payment.noop.control.PaymentNoopController;
import de.lottoritter.business.payment.noop.control.PaymentNoopSynchronizer;
import de.lottoritter.business.payment.paymill.control.PaymentPaymillController;
import de.lottoritter.business.payment.paymill.control.PaymillSubscriptionSynchronizer;
import de.lottoritter.platform.cdi.CDIBeanService;

/**
 * @author Ulrich Cech
 */
public enum PspCode {

    PAYMILL(PaymentPaymillController.class, PaymillSubscriptionSynchronizer.class),
    NO_OP(PaymentNoopController.class, PaymentNoopSynchronizer.class);


    private Class<? extends AbstractPaymentController> paymentController;
    private Class<? extends AbstractPaymentSubscriptionSynchronizer> paymentSubscriptionSynchronizer;

    PspCode(Class<? extends AbstractPaymentController> newPaymentController,
            Class<? extends AbstractPaymentSubscriptionSynchronizer> newPaymentSubscriptionSynchronizer) {
        this.paymentController = newPaymentController;
        this.paymentSubscriptionSynchronizer = newPaymentSubscriptionSynchronizer;
    }

    public AbstractPaymentController getPaymentController() {
        return CDIBeanService.getInstance().getCDIBean(this.paymentController);
    }

    public AbstractPaymentSubscriptionSynchronizer getPaymentSubscriptionSynchronizer() {
        return CDIBeanService.getInstance().getCDIBean(this.paymentSubscriptionSynchronizer);
    }
}
