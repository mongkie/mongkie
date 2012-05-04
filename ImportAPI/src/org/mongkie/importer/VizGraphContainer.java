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
package org.mongkie.importer;

import java.util.List;
import java.util.Map;
import prefuse.data.Table;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface VizGraphContainer extends GraphContainer {

    public void setNodeVisualProperties(Table table);

    public Table getNodeVisualProperties();

    public void setEdgeVisualProperties(Table table);

    public Table getEdgeVisualProperties();

    public void setAggregateVisualProperties(Table table);

    public Table getAggregateVisualProperties();

    public void setAggregateId2NodeItemRows(Map<Integer, List<Integer>> rows);

    public Map<Integer, List<Integer>> getAggregateId2NodeRows();
}
