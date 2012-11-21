/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.layout.plugins.forcedirected;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import prefuse.util.force.Force;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ForceInplaceEditor extends JComponent implements InplaceEditor {

    private ActionListener listener = null;
    private PropertyEditor editor = null;
    private PropertyModel model = null;
    private final JSlider slider;
    private final JLabel valueLabel;
    private float min, max;

    static {
        UIManager.put("Slider.paintValue", Boolean.FALSE);
    }

    public ForceInplaceEditor(final Force force, final int paramIdx) {

        min = force.getMinValue(paramIdx);
        max = force.getMaxValue(paramIdx);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        slider = new JSlider();
        slider.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        slider.setFocusable(false);
        add(slider);
        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // Enable slider after the first mouse click is consumed
                if (!slider.isEnabled()) {
                    slider.setEnabled(true);
                }
            }
        });
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float val = getSliderValue();
                valueLabel.setText(String.format("%- 2.1f", val));
                force.setParameter(paramIdx, val);
            }
        });

        valueLabel = new JLabel("NaN");
        valueLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        valueLabel.setPreferredSize(new Dimension(40, 20));
        add(valueLabel);

    }

    private float getSliderValue() {
        int smin = slider.getMinimum();
        int smax = slider.getMaximum();
        int srange = smax - smin;
        float f = (slider.getValue() - smin) / (float) srange;
        float val = min + f * (max - min);
        return val;
    }

    private void setSliderValue(float value) {
        int smin = slider.getMinimum();
        int smax = slider.getMaximum();
        int srange = smax - smin;
        int val = smin + Math.round(srange * ((value - min) / (max - min)));
        slider.setValue(val);
    }

    @Override
    public void connect(PropertyEditor pe, PropertyEnv env) {
        editor = pe;
        reset();
        // Disable slider until the first mouse clic is finished.
        slider.setEnabled(false);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void clear() {
        editor = null;
        model = null;
        listener = null;
    }

    @Override
    public boolean supportsTextEntry() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        setValue(editor.getValue());
    }

    @Override
    public void addActionListener(ActionListener l) {
        listener = l;
    }

    @Override
    public void removeActionListener(ActionListener l) {
        if (listener == l) {
            listener = null;
        }
    }

    @Override
    public KeyStroke[] getKeyStrokes() {
        return new KeyStroke[0];
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    @Override
    public PropertyModel getPropertyModel() {
        return model;
    }

    @Override
    public void setPropertyModel(PropertyModel pm) {
        this.model = pm;
    }

    @Override
    public boolean isKnownComponent(Component c) {
        return c == slider || slider.isAncestorOf(c);
    }

    @Override
    public void setValue(Object o) {
        if (!o.equals(getValue())) {
            setSliderValue((Float) o);
        }
    }

    @Override
    public Object getValue() {
        return getSliderValue();
    }
}
