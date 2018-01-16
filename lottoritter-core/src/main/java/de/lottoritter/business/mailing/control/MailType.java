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

/**
 * @author Christopher Schmidt
 */
public enum MailType {

    ActivationRequest("[[mail.activation.request.subject]]", null),
    ActivationConfirmation("[[mail.activation.confirmation.subject]]", null),
    ResetPasswordRequest("[[mail.resetpw.request.subject]]", null),
    ResetPasswordConfirmation("[[mail.resetpw.confirmation.subject]]", null),
    LimitsChanged("[[mail.limits.changed.subject]]", null),
    SelfExclusion("[[mail.self-exclusion.confirmation.subject]]", null),
    PurchaseConfirmation("[[mail.purchase.confirmation.subject]]", PartialMailType.PurchaseConfirmationPartial),
    EmailChanged("[[mail.change.email.subject]]", null),
    TicketsDecayInCartReminder("[[mail.tickets.decay.reminder.subject]]", null);

    private String subjectKey;
    private PartialMailType partialMailType;

    MailType(String subjectKey, PartialMailType partialMailType) {
        this.subjectKey = subjectKey;
        this.partialMailType = partialMailType;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public PartialMailType getPartialMailType() {
        return partialMailType;
    }

    enum PartialMailType {
        PurchaseConfirmationPartial
    }
}
