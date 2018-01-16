package de.lottoritter.business.player.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.junit.Test;

import de.lottoritter.business.validation.control.ValidationController;

/**
 * @author Ulrich Cech
 */
public class PlayerTest {

    @Test
    public void testEmailValidation() {
        Player player = new Player();
        try {
            ValidationController.get(Player.class).processBeanValidationForProperty(player,"email");
            fail("Invalid email not recognized");
        } catch (ConstraintViolationException ex) {
            List<ConstraintViolation> errors = new ArrayList<>(ex.getConstraintViolations());
            errors.sort(Comparator.comparing(ConstraintViolation::getMessageTemplate));
            assertThat(errors.get(0).getMessageTemplate(), is("{email_not_empty}"));
        }
        player.setEmail("");
        try {
            ValidationController.get(Player.class).processBeanValidationForProperty(player,"email");
            fail("Invalid email not recognized");
        } catch (ConstraintViolationException ex) {
            List<ConstraintViolation> errors = new ArrayList<>(ex.getConstraintViolations());
            errors.sort(Comparator.comparing(ConstraintViolation::getMessageTemplate));
            assertThat(errors.get(0).getMessageTemplate(), is("{email_not_empty}"));
            assertThat(errors.get(1).getMessageTemplate(), is("{email_not_valid}"));
        }
        player.setEmail("invalid@email");
        try {
            ValidationController.get(Player.class).processBeanValidationForProperty(player,"email");
            fail("Invalid email not recognized");
        } catch (ConstraintViolationException ex) {
            List<ConstraintViolation> l = new ArrayList<>(ex.getConstraintViolations());
            assertThat(l.get(0).getMessageTemplate(), is("{email_not_valid}"));
        }
        player.setEmail("correct@example.com");
        try {
            ValidationController.get(Player.class).processBeanValidationForProperty(player,"email");
        } catch (ConstraintViolationException ex) {
            fail("Correct email not recognized");
        }
    }
}