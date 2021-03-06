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

import java.awt.Font;
import kobic.prefuse.Constants;
import org.mongkie.visualedit.spi.AggregateItemEdit;
import org.mongkie.visualedit.spi.VisualEdit;
import org.openide.util.lookup.ServiceProvider;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualEdit.class, position = 200)
public class AggregateFontEdit extends AggregateItemEdit<Font> {

    @Override
    public String getName() {
        return "Font";
    }

    @Override
    public Class<Font> getValueType() {
        return Font.class;
    }

    @Override
    public Font getDefaultValue() {
        return Constants.FONT_DEFAULT_AGGRTEXT;
    }

    @Override
    public Font getValue(VisualItem group) {
        return group.getFont();
    }

    @Override
    public void setValue(VisualItem group, Font f) {
        PrefuseLib.update(group, VisualItem.FONT, f);
    }

    @Override
    public boolean canEditAsText() {
        return false;
    }
}
