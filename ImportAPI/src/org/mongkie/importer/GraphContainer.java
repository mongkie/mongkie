/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.importer;

import org.mongkie.importer.spi.Importer;
import org.mongkie.importer.spi.Processor;
import prefuse.data.Graph;

/**
 * A container is created each time data are imported by <b>importers</b>. Its role is to host all data
 * collected by importers during import process. After pushing data in the container, its content can be
 * analyzed to verify its validity and then be processed by <b>processors</b>. Thus containers are
 * <b>loaded</b> by importers and <b>processed</b> by processors.
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @see Importer
 * @see Processor
 */
public interface GraphContainer extends Container<Graph> {

    public Graph getGraph();

    public void setDirected(boolean directed);

    public boolean isDirected();

    public void setAutoScale(boolean autoScale);

    public boolean isAutoScale();

    public boolean isAllowSelfLoop();

    public void setAllowSelfLoop(boolean allowSelfLoop);

    public boolean isAllowParallelEdge();

    public void setAllowParallelEdge(boolean allowParallelEdge);
    
    public void setNodeIdColumn(String col);
    
    public String getNodeIdColumn();
}
