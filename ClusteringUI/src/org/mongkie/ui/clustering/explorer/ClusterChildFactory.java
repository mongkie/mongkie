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
package org.mongkie.ui.clustering.explorer;

import java.util.List;
import org.mongkie.clustering.spi.Cluster;
import org.mongkie.clustering.spi.Clustering;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ClusterChildFactory extends ChildFactory<Cluster> {

    private Clustering cl;

    public ClusterChildFactory(Clustering cl) {
        this.cl = cl;
    }

    @Override
    protected boolean createKeys(List<Cluster> toPopulate) {
        for (Cluster c : cl.getClusters()) {
            toPopulate.add(c);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(Cluster key) {
        return new ClusterNode(key);
    }
}
