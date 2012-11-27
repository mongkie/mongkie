/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.lib.widgets.pe;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import org.openide.util.Exceptions;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class JSliderInplaceEditor<N extends Number> extends JComponent implements InplaceEditor {

    private PropertyEditor editor = null;
    private PropertyModel model = null;
    protected final JSlider slider;
    private final JLabel valueLabel;
    protected N min, max;
    private final String format;

    static {
        UIManager.put("Slider.paintValue", Boolean.FALSE);
    }

    protected JSliderInplaceEditor(N min, N max, final String format, final SliderListener<N> setter) {
        this.min = min;
        this.max = max;
        this.format = format;

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

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
                valueLabel.setText(String.format(format, getSliderValue()));
                Object newValue = getValue();
                if (setter != null) {
                    try {
                        if (!model.getValue().equals(newValue)) {
                            setter.valueChanged(model, (N) newValue);
                        }
                    } catch (InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        add(Box.createRigidArea(new Dimension(2, 0)));

        valueLabel = new JLabel("NaN");
        valueLabel.setFont(new java.awt.Font("Tahoma", 0, 11));
        valueLabel.setVerticalAlignment(SwingConstants.BOTTOM);
        valueLabel.setPreferredSize(new Dimension(45, 20));
        add(valueLabel);
    }

    protected abstract N getSliderValue();

    protected abstract void setSliderValue(N value);

    protected PropertyModel getModel() {
        return model;
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
    }

    @Override
    public Object getValue() {
        return valueOf(String.format(format, getSliderValue()));
    }

    protected abstract N valueOf(String s);

    @Override
    public void setValue(Object o) {
        if (!o.equals(getValue())) {
            setSliderValue((N) o);
        }
    }

    @Override
    public boolean supportsTextEntry() {
        return false;
    }

    @Override
    public void reset() {
        setValue(editor.getValue());
    }

    @Override
    public void addActionListener(ActionListener al) {
    }

    @Override
    public void removeActionListener(ActionListener al) {
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

    public static interface SliderListener<T extends Number> {

        public void valueChanged(PropertyModel model, T value);
    }

    public static class Float extends JSliderInplaceEditor<java.lang.Float> {

        public Float(float min, float max) {
            this(min, max, (SliderListener<java.lang.Float>) null);
        }

        public Float(float min, float max, SliderListener<java.lang.Float> setter) {
            this(min, max, "%- 1.2f", setter);
        }

        public Float(float min, float max, String format) {
            this(min, max, format, null);
        }

        public Float(float min, float max, String format, SliderListener<java.lang.Float> setter) {
            super(min, max, format, setter);
        }

        @Override
        protected java.lang.Float getSliderValue() {
            int smin = slider.getMinimum();
            int smax = slider.getMaximum();
            int srange = smax - smin;
            float f = (slider.getValue() - smin) / (float) srange;
            float val = min + f * (max - min);
            return val;
        }

        @Override
        protected void setSliderValue(java.lang.Float value) {
            int smin = slider.getMinimum();
            int smax = slider.getMaximum();
            int srange = smax - smin;
            int val = smin + Math.round(srange * ((value - min) / (max - min)));
            slider.setValue(val);
        }

        @Override
        protected java.lang.Float valueOf(String s) {
            return java.lang.Float.valueOf(s);
        }
    }

    public static class Double extends JSliderInplaceEditor<java.lang.Double> {

        public Double(double min, double max) {
            this(min, max, (SliderListener<java.lang.Double>) null);
        }

        public Double(double min, double max, SliderListener<java.lang.Double> setter) {
            this(min, max, "%- 1.2f", setter);
        }

        public Double(double min, double max, String format) {
            this(min, max, format, null);
        }

        public Double(double min, double max, String format, SliderListener<java.lang.Double> setter) {
            super(min, max, format, setter);
        }

        @Override
        protected java.lang.Double getSliderValue() {
            int smin = slider.getMinimum();
            int smax = slider.getMaximum();
            int srange = smax - smin;
            double f = (slider.getValue() - smin) / srange;
            double val = min + f * (max - min);
            return val;
        }

        @Override
        protected void setSliderValue(java.lang.Double value) {
            int smin = slider.getMinimum();
            int smax = slider.getMaximum();
            int srange = smax - smin;
            int val = (int) (smin + Math.round(srange * ((value - min) / (max - min))));
            slider.setValue(val);
        }

        @Override
        protected java.lang.Double valueOf(String s) {
            return java.lang.Double.valueOf(s);
        }
    }

    public static class Int extends JSliderInplaceEditor<Integer> {

        public Int(int min, int max, SliderListener<Integer> setter) {
            this(min, max, "%- 1.0f", setter);
        }

        public Int(int min, int max, String format, SliderListener<Integer> setter) {
            super(min, max, format, setter);
        }

        public Int(int min, int max, String format) {
            this(min, max, format, null);
        }

        @Override
        protected Integer getSliderValue() {
            int smin = slider.getMinimum();
            int smax = slider.getMaximum();
            int srange = smax - smin;
            float f = (slider.getValue() - smin) / (float) srange;
            int val = Math.round(min + f * (max - min));
            return val;
        }

        @Override
        protected void setSliderValue(Integer value) {
            int smin = slider.getMinimum();
            int smax = slider.getMaximum();
            int srange = smax - smin;
            int val = smin + Math.round(srange * ((value - min) / (max - min)));
            slider.setValue(val);
        }

        @Override
        protected Integer valueOf(String s) {
            return Integer.valueOf(s);
        }
    }
}
