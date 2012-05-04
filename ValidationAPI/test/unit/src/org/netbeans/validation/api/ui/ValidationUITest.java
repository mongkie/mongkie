/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.validation.api.ui;

import org.junit.Test;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.Severity;
import static org.junit.Assert.*;

/**
 *
 * @author Administrator
 */
public class ValidationUITest {

    @Test
    public void testFoo() {
        MulticastValidationUI main = new MulticastValidationUI();
        Problem p = new Problem ("fail", Severity.FATAL);

        UI ui1 = new UI();
        UI ui2 = new UI();
        UI ui3 = new UI();

        main.add(ui1);
        main.add(ui2);
        main.add(ui3);

        main.showProblem(null);
        ui1.assertCleared();
        ui2.assertCleared();
        ui3.assertCleared();

        main.showProblem (p);
        ui1.assertProblem("fail");
        ui1.assertNotCleared();
        ui2.assertProblem("fail");
        ui2.assertNotCleared();
        ui3.assertProblem("fail");
        ui3.assertNotCleared();
    }

    private class UI implements ValidationUI {
        Problem p;
        boolean cleared;

        public void assertCleared() {
            boolean old = cleared;
            cleared = false;
            assertTrue(old);
        }

        public void assertNotCleared() {
            assertFalse(cleared);
        }

        public void assertProblem(String msg) {
            assertNotNull(p);
            assertEquals(msg, p.getMessage());
            cleared = false;
        }

        public void assertNotProblem() {
            assertNull(p);
        }

        @Override
        public void showProblem(Problem problem) {
            p = problem;
            if (problem == null) {
                cleared = true;
            }
        }

        public void clearProblem() {
            showProblem(null);
        }
    }
}