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
package de.lottoritter.business.validation.control;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.time.ZonedDateTime;

import javax.validation.ConstraintViolationException;
import javax.validation.groups.Default;

import org.junit.BeforeClass;
import org.junit.Test;

import de.lottoritter.business.player.entity.PasswordGroup;

/**
 * @author Ulrich Cech
 */
public class ValidationControllerTest {

    @BeforeClass
    public static void setup() {
        WeldManager.releaseWeld();
    }

    @Test
    public void processBeanValidation() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.futureDate = ZonedDateTime.now().minusDays(1);
        try {
            ValidationController.get(TestEntity.class).processBeanValidation(testEntity);
            fail("Wrong values are not recognized");
        } catch (Exception ex) {
            ex.printStackTrace();
            assertThat(ex, instanceOf(ConstraintViolationException.class));
            assertThat(((ConstraintViolationException)ex).getConstraintViolations().size(), is(3));
        }
    }

    @Test
    public void processBeanValidationForProperty() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.emptyOrSize = "abc";
        try {
            ValidationController.get(TestEntity.class).processBeanValidationForProperty(testEntity, "emptyOrSize");
        } catch (Exception ex) {
            fail("Correct values for the specified group are not recognized.");
        }
        testEntity.emptyOrSize = "abcdef";
        try {
            ValidationController.get(TestEntity.class).processBeanValidationForProperty(testEntity, "emptyOrSize");
            fail("Wrong value in 'emptyOrSize' is not recognized");
        } catch (Exception ex) {
            assertThat(ex, instanceOf(ConstraintViolationException.class));
            assertThat(((ConstraintViolationException)ex).getConstraintViolations().size(), is(1));
        }
    }

    @Test
    public void processBeanValidationForGroup() throws Exception {
        TestEntity testEntity = new TestEntity();
        testEntity.emptyOrSize = "abc";
        testEntity.noSpace = "abc";
        testEntity.notEmpty = "abcd";
        try {
            ValidationController.get(TestEntity.class).processBeanValidationForGroup(testEntity, PasswordGroup.class);
        } catch (Exception ex) {
            fail("Correct values for the specified group are not recognized.");
        }
        testEntity.noSpace = "ab c";
        try {
            ValidationController.get(TestEntity.class).processBeanValidationForGroup(testEntity, PasswordGroup.class);
            fail("Wrong value in 'noSpace' is not recognized");
        } catch (Exception ex) {
            assertThat(ex, instanceOf(ConstraintViolationException.class));
            assertThat(((ConstraintViolationException)ex).getConstraintViolations().size(), is(1));
        }
    }


    static class TestEntity {

        @EmptyOrSize(min=2, max=4, groups = {Default.class, PasswordGroup.class})
        private String emptyOrSize;

        @NoSpace(groups = {Default.class, PasswordGroup.class})
        private String noSpace;

        @NotEmpty(groups = {Default.class, PasswordGroup.class})
        private String notEmpty;

        @FutureDate
        private ZonedDateTime futureDate;

        @BooleanTrue
        private boolean booleanTrue;
    }
}