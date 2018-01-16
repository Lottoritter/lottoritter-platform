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
package de.lottoritter.business.activitylog.entity;

/**
 * @author Ulrich Cech
 */
public enum ActivityType {

    LOGIN_SUCCESS(ActivityFamily.LOGIN),
    LOGIN_FAILED_PASSWORD(ActivityFamily.LOGIN),
    LOGIN_FAILED_USERNAME(ActivityFamily.LOGIN),
    LOGOUT(ActivityFamily.LOGIN),

    CHANGE_PASSWORD(ActivityFamily.CHANGE_PASSWORD),
    CHANGE_PASSWORD_START_FAILED(ActivityFamily.CHANGE_PASSWORD),
    CHANGE_PASSWORD_START_SUCCESS(ActivityFamily.CHANGE_PASSWORD),
    CHANGE_PASSWORD_ACTIVATIONCODE_FAILED(ActivityFamily.CHANGE_PASSWORD),
    CHANGE_PASSWORD_ACTIVATIONCODE_SUCCESS(ActivityFamily.CHANGE_PASSWORD),

    SELF_SUSPEND(ActivityFamily.BLOCKING),

    REGISTRATION_SUCCESS(ActivityFamily.REGISTRATION),
    REGISTRATION_ACTIVATION_SUCCESS(ActivityFamily.REGISTRATION),
    REGISTRATION_WELCOME_MAIL_JOB(ActivityFamily.JOB),

    SHOPPINGCART_ADD_TICKET(ActivityFamily.SHOPPINGCART),
    SHOPPINGCART_ATTACH_PLAYER(ActivityFamily.SHOPPINGCART),
    SHOPPINGCART_REMOVE_TICKET(ActivityFamily.SHOPPINGCART),
    SHOPPINGCART_ADD_UNTRACKED_SESSION_TICKETS(ActivityFamily.SHOPPINGCART),
    SHOPPINGCART_ADJUST_TICKET(ActivityFamily.SHOPPINGCART),

    JOB_TICKET_CLOSED(ActivityFamily.JOB),
    JOB_TICKET_DECAY(ActivityFamily.JOB),
    JOB_TICKET_CLOSING(ActivityFamily.JOB),

    CHANGE_PROFILE(ActivityFamily.PROFILE),
    CHANGE_LIMITS(ActivityFamily.PROFILE),
    ADJUST_LIMITS(ActivityFamily.PROFILE),

    DRAWING_CRAWLER_SUPER6(ActivityFamily.JOB),
    DRAWING_CRAWLER_SPIEL77(ActivityFamily.JOB),
    DRAWING_CRAWLER_KENO(ActivityFamily.JOB),
    DRAWING_CRAWLER_GERMAN6AUS49(ActivityFamily.JOB),
    DRAWING_CRAWLER_EUROJACKPOT(ActivityFamily.JOB),

    PAYMENT_SUBSCRIPTION_SYNCHRONIZE(ActivityFamily.JOB);


    private ActivityFamily activityFamily;


    ActivityType(ActivityFamily activityFamily) {
        this.activityFamily = activityFamily;
    }


    public ActivityFamily getActivityFamily() {
        return activityFamily;
    }

}
