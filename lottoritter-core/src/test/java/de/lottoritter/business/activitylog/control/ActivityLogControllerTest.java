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
package de.lottoritter.business.activitylog.control;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.List;

import de.lottoritter.business.temporal.control.DateTimeService;
import org.bson.types.ObjectId;
import org.junit.Test;

import de.lottoritter.business.activitylog.entity.ActivityFamily;
import de.lottoritter.business.activitylog.entity.ActivityLog;
import de.lottoritter.business.activitylog.entity.ActivityType;
import de.lottoritter.business.player.entity.Player;
import de.lottoritter.platform.persistence.FongoDbPersistenceTest;

/**
 * @author Ulrich Cech
 */
public class ActivityLogControllerTest extends FongoDbPersistenceTest {

    private static final ObjectId TEST_PLAYER_ID = new ObjectId();

    DateTimeService dateTimeService = new DateTimeService();

    @Test
    public void saveActivityLog() throws Exception {
        ActivityLogController cut = new ActivityLogController();
        cut.datastore = getDatastore();
        cut.dateTimeService = dateTimeService;
        cut.saveActivityLog(getPlayer(), ActivityType.LOGIN_SUCCESS, "data1", "testdata1", "data2", "testdata2");
        final List<ActivityLog> activityLogList = getDatastore().createQuery(ActivityLog.class).asList();
        assertThat(activityLogList, notNullValue());
        assertThat(activityLogList.size(), is(1));
        assertThat(activityLogList.get(0).getActivityType(), is(ActivityType.LOGIN_SUCCESS));
        assertThat(activityLogList.get(0).getActivityFamily(), is(ActivityFamily.LOGIN));
        assertThat(activityLogList.get(0).getPlayerId(), is(TEST_PLAYER_ID));
        assertThat(activityLogList.get(0).getData().containsKey("data1"), is(true));
        assertThat(activityLogList.get(0).getData().containsValue("testdata1"), is(true));
        assertThat(activityLogList.get(0).getData().containsKey("data2"), is(true));
        assertThat(activityLogList.get(0).getData().containsValue("testdata2"), is(true));
    }

    private Player getPlayer() throws IllegalAccessException, NoSuchFieldException {
        Player player = new Player();
        final Field field = player.getClass().getSuperclass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(player, TEST_PLAYER_ID);
        return player;
    }

}