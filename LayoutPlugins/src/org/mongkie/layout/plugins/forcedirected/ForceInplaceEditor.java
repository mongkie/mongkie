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

import java.lang.reflect.InvocationTargetException;
import org.mongkie.lib.widgets.pe.JSliderInplaceEditor;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.Exceptions;
import prefuse.util.force.Force;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class ForceInplaceEditor extends JSliderInplaceEditor.Float {

    ForceInplaceEditor(final Force force, final int paramIdx) {
        this(force, paramIdx, "%- 1.1f");
    }

    ForceInplaceEditor(final Force force, final int paramIdx, String format) {
        super(force.getMinValue(paramIdx), force.getMaxValue(paramIdx), format,
                new JSliderInplaceEditor.SliderListener<java.lang.Float>() {
                    @Override
                    public void valueChanged(PropertyModel model, java.lang.Float value) {
                        try {
                            model.setValue(value);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        force.setParameter(paramIdx, value);
                    }
                });
    }
}
