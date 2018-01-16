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
package de.lottoritter.business.player.entity;

/**
 * Enum for all possible states of the user account.
 *
 * @author Ulrich Cech
 */
public enum UserState {

    REGISTERED,
    PENDING, // after registration and before activation
    ACTIVATED, // after activation is confirmed
    CONFIRMED, // after the name and age is verified by AVS
    SUSPENDED, // user account is suspended by user
    BLOCKED, // user account is blocked by LBB
    DELETED // user account is deleted by user/LBB

}
