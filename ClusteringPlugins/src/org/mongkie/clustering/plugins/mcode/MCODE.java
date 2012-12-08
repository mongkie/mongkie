/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKIE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.clustering.plugins.mcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MCODE implements Clustering<MCODECluster> {

    private final MCODEBuilder builder;
    private final MCODEAlgorithm algo;

    MCODE(MCODEBuilder builder) {
        this.builder = builder;
        algo = new MCODEAlgorithm();
    }

    @Override
    public Collection<MCODECluster> execute(Graph g) {
        algo.setCancelled(false);
        algo.scoreGraph(g, builder.getName());
        if (algo.isCancelled()) {
            return null;
        }
        List<MCODECluster> clusters = new ArrayList<MCODECluster>();
        clusters.addAll(Arrays.asList(algo.findClusters(g, builder.getName())));
        return clusters;
    }

    @Override
    public boolean cancel() {
        algo.setCancelled(true);
        return false;
    }

    @Override
    public ClusteringBuilder getBuilder() {
        return builder;
    }
}
