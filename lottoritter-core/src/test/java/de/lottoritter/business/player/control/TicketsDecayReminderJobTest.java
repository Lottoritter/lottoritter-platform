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
package de.lottoritter.business.player.control;

import de.lottoritter.business.activitylog.control.ActivityLogController;
import de.lottoritter.business.activitylog.entity.ActivityLog;
import de.lottoritter.business.lotteries.LotteryManager;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Lottery;
import de.lottoritter.business.lotteries.german6aus49.German6aus49Ticket;
import de.lottoritter.business.mailing.control.MailController;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.business.shoppingcart.control.ShoppingCartRepository;
import de.lottoritter.business.shoppingcart.entity.ShoppingCart;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Iterator;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Christopher Schmidt
 */
public class TicketsDecayReminderJobTest {

    @Mock
    private MailController mailControllerMock;

    @Mock
    private ActivityLogController activityLogController;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void thatSendMailWorks() {
        //given
        ZonedDateTime start = ZonedDateTime.now().plusMinutes(5);

        TicketsDecayReminderJob classUnderTest = new TicketsDecayReminderJob();
        Whitebox.setInternalState(classUnderTest, "runJobs", createDummyInstance(true));
        LotteryManager lotteryManagerMock = mock(LotteryManager.class);

        German6aus49Lottery german6aus49LotteryMock = mock(German6aus49Lottery.class);
        when(german6aus49LotteryMock.getNextDrawing(null, null)).thenReturn(start);
        when(lotteryManagerMock.getLotteries()).thenReturn(Collections.singletonList(german6aus49LotteryMock));
        ShoppingCartRepository shoppingCartRepositoryMock = mock(ShoppingCartRepository.class);
        ShoppingCart shoppingCart = mock(ShoppingCart.class);

        when(shoppingCart.getPlayerId()).thenReturn(new ObjectId());
        German6aus49Ticket ticketMock = mock(German6aus49Ticket.class);
        when(ticketMock.getLottery()).thenReturn(german6aus49LotteryMock);
        when(ticketMock.getStartingDate()).thenReturn(start);
        when(shoppingCart.getTicketList()).thenReturn(Collections.singletonList(ticketMock));
        when(shoppingCartRepositoryMock.getAllShoppingsCartWithTickets()).thenReturn(Collections.singletonList(shoppingCart));

        UserRepository userRepositoryMock = mock(UserRepository.class);
        when(userRepositoryMock.findById(any(ObjectId.class))).thenReturn(mock(Player.class));
        Whitebox.setInternalState(classUnderTest, "lotteryManager", lotteryManagerMock);
        Whitebox.setInternalState(classUnderTest, "shoppingCartRepository", shoppingCartRepositoryMock);
        Whitebox.setInternalState(classUnderTest, "userRepository", userRepositoryMock);
        Whitebox.setInternalState(classUnderTest, "mailController", mailControllerMock);
        Whitebox.setInternalState(classUnderTest, "activityLogController", activityLogController);

        //when
        classUnderTest.execute();

        //then
        verify(mailControllerMock, times(1)).sendTicketsDecayReminderMail(any(Player.class));
    }

    private Instance<Boolean> createDummyInstance(boolean val) {
        return new Instance<Boolean>() {
            @Override
            public Iterator<Boolean> iterator() {
                return null;
            }

            @Override
            public Instance<Boolean> select(Annotation... qualifiers) {
                return null;
            }

            @Override
            public boolean isUnsatisfied() {
                return false;
            }

            @Override
            public boolean isAmbiguous() {
                return false;
            }

            @Override
            public void destroy(Boolean instance) {

            }

            @Override
            public <U extends Boolean> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public <U extends Boolean> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
                return null;
            }

            @Override
            public Boolean get() {
                return val;
            }
        };
    }
}