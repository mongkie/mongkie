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
package org.mongkie.clustering.spi;

import java.awt.Color;
import java.util.Collection;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface Cluster {

    public Graph getGraph();

    public Collection<Node> getNodes();

    public int getNodesCount();

    public String getName();

    public void setName(String name);

    public Color getColor();

    public void setColor(Color color);

    public int getRank();
}
