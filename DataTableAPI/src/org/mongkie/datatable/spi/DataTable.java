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
package org.mongkie.datatable.spi;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.mongkie.visualization.MongkieDisplay;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface DataTable {

    public static final String NODES = "Nodes";
    public static final String EDGES = "Edges";

    public String getName();

    public Icon getIcon();

    public JComponent getView();

    public Tool[] getTools();

    public void refreshModel(MongkieDisplay d);

    public void selected();

    public void deselected();

    public interface Tool<T extends DataTable> {

        public T getDataTable();

        public void refresh(boolean clear);

        public JComponent getComponent();
    }
}
