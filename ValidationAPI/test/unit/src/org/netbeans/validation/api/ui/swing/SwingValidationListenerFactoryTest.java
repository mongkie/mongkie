/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.validation.api.ui.swing;

import java.awt.Color;
import javax.swing.ComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert.*;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.conversion.Converter;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationListenerFactory;
import org.netbeans.validation.api.ui.ValidationStrategy;
import org.netbeans.validation.api.ui.ValidationUI;

/**
 *
 * @author Administrator
 */
public class SwingValidationListenerFactoryTest {

    public SwingValidationListenerFactoryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testStandardValidatorListeners() {
//        ValidationListener<JComboBox> boxStringL = ValidationListenerFactory.createValidationListener(new JComboBox(), ValidationStrategy.DEFAULT, ValidationUI.NO_OP, StringValidators.NO_WHITESPACE);
//        ValidationListener<JTextField> fieldModelS = ValidationListenerFactory.createValidationListener(new JTextField(), ValidationStrategy.DEFAULT, ValidationUI.NO_OP, StringValidators.NO_WHITESPACE);
        ColorV cv = new ColorV();
        ValidationListener<JColorChooser> colorChooserL = ValidationListenerFactory.createValidationListener(new JColorChooser(), ValidationStrategy.DEFAULT, ValidationUI.NO_OP, cv);
        assert colorChooserL instanceof ColorChooserValidationListenerFactory.ColorChooserListener;
        cv.validated = false;

        CBV cbv = new CBV();
//        ValidationListener<JComboBox> boxModelL = ValidationListenerFactory.createValidationListener(new JComboBox(), ValidationStrategy.DEFAULT, ValidationUI.NO_OP, cbv);
        cbv.validated = false;
//        ValidationListener<JTextField> fieldModelD = ValidationListenerFactory.createValidationListener(new JTextField(), ValidationStrategy.DEFAULT, ValidationUI.NO_OP, Converter.find(String.class, Document.class).convert(StringValidators.NO_WHITESPACE));

        colorChooserL.performValidation();
        assert cv.validated;
        assert !cbv.validated;
//        boxModelL.performValidation();
//        assert cbv.validated;
    }
    
    private static class CBV implements Validator<ComboBoxModel> {
        private boolean validated;
        public void validate(Problems problems, String compName, ComboBoxModel model) {
            validated = true;
        }

        public Class<ComboBoxModel> modelType() {
            return ComboBoxModel.class;
        }
    }
    
    private static class ColorV extends AbstractValidator<Color> {
        boolean validated;
        ColorV() {
            super (Color.class);
        }

        public void validate(Problems problems, String compName, Color model) {
            if (Color.BLACK.equals(model)) {
                problems.add ("BLACK!");
            }
            validated = true;
        }
    }
}