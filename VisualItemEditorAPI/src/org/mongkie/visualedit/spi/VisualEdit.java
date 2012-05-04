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
package org.mongkie.visualedit.spi;

import java.awt.Image;
import java.beans.PropertyEditor;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface VisualEdit<T> {

    public String getName();

    public String getDisplayName();

    public Image getIcon();

    public Class<? extends VisualItem> getItemType();

    public Class<T> getValueType();

    public T getValue(VisualItem item);

    public void setValue(VisualItem item, T value);

    public boolean canEditAsText();

    public boolean supportsPropertyEditor();

    public Class<? extends PropertyEditor> getPropertyEditorClass();
}
