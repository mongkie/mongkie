/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualedit.spi.impl;

import java.beans.PropertyEditor;
import kobic.prefuse.EdgeStroke;
import org.mongkie.visualedit.editors.EdgeLineStyleEditor;
import org.mongkie.visualedit.spi.EdgeItemEdit;
import org.mongkie.visualedit.spi.VisualEdit;
import org.openide.util.lookup.ServiceProvider;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualEdit.class, position = 100)
public class EdgeLineStyleEdit extends EdgeItemEdit<EdgeStroke> {

    @Override
    public String getName() {
        return "Line Style";
    }

    @Override
    public Class<EdgeStroke> getValueType() {
        return EdgeStroke.class;
    }

    @Override
    public EdgeStroke getValue(VisualItem item) {
        return EdgeStroke.get(item.getStroke());
    }

    @Override
    public void setValue(VisualItem item, EdgeStroke value) {
        item.setStroke(value.getStroke());
    }

    @Override
    public Class<? extends PropertyEditor> getPropertyEditorClass() {
        return EdgeLineStyleEditor.class;
    }

    @Override
    public boolean supportsPropertyEditor() {
        return true;
    }
}
