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
package de.lottoritter.business.lotteries;

import de.lottoritter.business.lotteries.eurojackpot.EuroJackpotLottery;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Lottery;
import de.lottoritter.business.lotteries.gluecksspirale.GluecksspiraleLottery;
import de.lottoritter.business.lotteries.plus5.Plus5Lottery;
import de.lottoritter.business.lotteries.spiel77.Spiel77Lottery;
import de.lottoritter.business.lotteries.super6.Super6Lottery;

/**
 * @author Ulrich Cech
 */
public class WinningRank {


    public enum Rank {
        G6AUS49_GK1(German6aus49Lottery.IDENTIFIER,6,1), G6AUS49_GK2(German6aus49Lottery.IDENTIFIER,6,0),
        G6AUS49_GK3(German6aus49Lottery.IDENTIFIER,5,1), G6AUS49_GK4(German6aus49Lottery.IDENTIFIER,5,0),
        G6AUS49_GK5(German6aus49Lottery.IDENTIFIER,4,1), G6AUS49_GK6(German6aus49Lottery.IDENTIFIER,4,0),
        G6AUS49_GK7(German6aus49Lottery.IDENTIFIER,3,1), G6AUS49_GK8(German6aus49Lottery.IDENTIFIER,3,0),
        G6AUS49_GK9(German6aus49Lottery.IDENTIFIER,2,1),

        EJ_GK1(EuroJackpotLottery.IDENTIFIER,5,2), EJ_GK2(EuroJackpotLottery.IDENTIFIER,5,1),
        EJ_GK3(EuroJackpotLottery.IDENTIFIER,5,0), EJ_GK4(EuroJackpotLottery.IDENTIFIER,4,2),
        EJ_GK5(EuroJackpotLottery.IDENTIFIER,4,1), EJ_GK6(EuroJackpotLottery.IDENTIFIER,4,0),
        EJ_GK7(EuroJackpotLottery.IDENTIFIER,3,2), EJ_GK8(EuroJackpotLottery.IDENTIFIER,2,2),
        EJ_GK9(EuroJackpotLottery.IDENTIFIER,3,1), EJ_GK10(EuroJackpotLottery.IDENTIFIER,3,0),
        EJ_GK11(EuroJackpotLottery.IDENTIFIER,1,2), EJ_GK12(EuroJackpotLottery.IDENTIFIER,2,1),

        GS_GK1(GluecksspiraleLottery.IDENTIFIER,1,0), GS_GK2(GluecksspiraleLottery.IDENTIFIER,2,0),
        GS_GK3(GluecksspiraleLottery.IDENTIFIER,3,0), GS_GK4(GluecksspiraleLottery.IDENTIFIER,4,0),
        GS_GK5(GluecksspiraleLottery.IDENTIFIER,5,0), GS_GK6(GluecksspiraleLottery.IDENTIFIER,6,0),
        GS_GK7(GluecksspiraleLottery.IDENTIFIER,7,0),

        S77_GK1(Spiel77Lottery.IDENTIFIER,1,0), S77_GK2(Spiel77Lottery.IDENTIFIER,2,0),
        S77_GK3(Spiel77Lottery.IDENTIFIER,3,0), S77_GK4(Spiel77Lottery.IDENTIFIER,4,0),
        S77_GK5(Spiel77Lottery.IDENTIFIER,5,0), S77_GK6(Spiel77Lottery.IDENTIFIER,6,0),
        S77_GK7(Spiel77Lottery.IDENTIFIER,7,0),

        S6_GK1(Super6Lottery.IDENTIFIER,1,0), S6_GK2(Super6Lottery.IDENTIFIER,2,0),
        S6_GK3(Super6Lottery.IDENTIFIER,3,0), S6_GK4(Super6Lottery.IDENTIFIER,4,0),
        S6_GK5(Super6Lottery.IDENTIFIER,5,0), S6_GK6(Super6Lottery.IDENTIFIER,6,0),

        PLUS5_GK1(Plus5Lottery.IDENTIFIER,1,0), PLUS5_GK2(Plus5Lottery.IDENTIFIER,2,0),
        PLUS5_GK3(Plus5Lottery.IDENTIFIER,3,0), PLUS5_GK4(Plus5Lottery.IDENTIFIER,4,0),
        PLUS5_GK5(Plus5Lottery.IDENTIFIER,5,0);

        private String lotteryIdentifier;

        private int numbersHit;

        private int additionalNumbersHit;

        Rank(String lotteryIdentifier, int numbersHit, int additionalNumbersHit) {
            this.lotteryIdentifier = lotteryIdentifier;
            this.numbersHit = numbersHit;
            this.additionalNumbersHit = additionalNumbersHit;
        }
    }

    private String rankName;


    public WinningRank(String rankName) {
        this.rankName = rankName;
    }


    public String getRankName() {
        return rankName;
    }


    public static WinningRank findForAllNumbersHit(String lotteryIdentifier, int numberHit, int additionalNumbersHit) {
        for (Rank rank : Rank.values()) {
            if (rank.lotteryIdentifier.equals(lotteryIdentifier) && (rank.numbersHit == numberHit)
                    && (rank.additionalNumbersHit == additionalNumbersHit)) {
                return new WinningRank(rank.name());
            }
        }
        return null;
    }
}
