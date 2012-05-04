/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.validation.api.ui.swing;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JColorChooser;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationListener;
import org.netbeans.validation.api.ui.ValidationListenerFactory;
import org.netbeans.validation.api.ui.ValidationStrategy;
import org.netbeans.validation.api.ui.ValidationUI;

/**
 *
 * @author Tim Boudreau
 */
public class ColorChooserValidationListenerFactory extends ValidationListenerFactory<JColorChooser, Color> {

    public ColorChooserValidationListenerFactory() {
        super (JColorChooser.class, Color.class);
    }

    @Override
    protected ValidationListener<JColorChooser> createListener(JColorChooser component, ValidationStrategy strategy, ValidationUI validationUI, Validator<Color> validator) {
        return new ColorChooserListener(component, validationUI, validator);
    }

    static final class ColorChooserListener
                    extends AbstractValidationListener<JColorChooser, Color>
                    implements PropertyChangeListener {
        ColorChooserListener (JColorChooser comp, ValidationUI ui, Validator<Color> color) {
            super (JColorChooser.class, comp, ui, color);
            comp.addPropertyChangeListener("color", this);
        }

        @Override
        protected Color getModelObject(JColorChooser comp) {
            return comp.getColor();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            performValidation();
        }
    }
}
