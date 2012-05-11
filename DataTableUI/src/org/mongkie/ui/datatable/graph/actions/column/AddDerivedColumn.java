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
package org.mongkie.ui.datatable.graph.actions.column;

import java.awt.Image;
import org.mongkie.datatable.spi.GraphAddColumnAction;
import org.mongkie.datatable.spi.GraphDataTable;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = GraphAddColumnAction.class, position = 1)
public class AddDerivedColumn extends GraphAddColumnAction {

    @Override
    public String getName() {
        return "Derived from...";
    }

    @Override
    public String getDescription() {
        return "Create a new column derived from the existing one";
    }

    @Override
    public Image getIcon() {
        return null;
    }

    @Override
    public void execute(GraphDataTable table) {
        System.out.println(getDescription());
    }

    @Override
    public boolean isEnabled(GraphDataTable table) {
        return true;
    }

    @Override
    public SettingUI<GraphDataTable> getSettingUI(GraphDataTable table) {
        return null;
    }
}
