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

import org.mongkie.visualedit.spi.AggregateItemEdit;
import org.mongkie.visualedit.spi.VisualEdit;
import org.openide.util.lookup.ServiceProvider;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualEdit.class, position = 500)
public class AggregateNameEdit extends AggregateItemEdit<String> {

    @Override
    public String getName() {
        return "Name";
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public String getValue(VisualItem group) {
        return group.getString(AggregateItem.AGGR_NAME);
    }

    @Override
    public void setValue(VisualItem group, String name) {
        group.setString(AggregateItem.AGGR_NAME, name);
    }
}
