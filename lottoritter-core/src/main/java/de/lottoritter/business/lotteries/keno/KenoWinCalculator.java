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
package de.lottoritter.business.lotteries.keno;

import de.lottoritter.business.lotteries.CalculatedField;
import de.lottoritter.business.lotteries.CalculatedNumber;
import de.lottoritter.business.lotteries.WinCalculator;
import de.lottoritter.business.lotteries.WinningRank;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * @author Ulrich Cech
 */
@Stateless
@LocalBean
public class KenoWinCalculator implements WinCalculator {

    @Override
    public void calculateRank(final String lotteryIdentifier, CalculatedField fieldWrapper) {
        // TODO ulrich: implement this
//        final long countNumbersHit = fieldWrapper.getNumbers().stream().filter(CalculatedNumber::isHit).count();
//        final long countAdditionalNumbersHit = fieldWrapper.getAdditionalNumbers().stream().filter(CalculatedNumber::isHit).count();
//        fieldWrapper.setWinningRank(WinningRank.findForAllNumbersHit(lotteryIdentifier, (int) countNumbersHit, (int) countAdditionalNumbersHit));
    }

}
