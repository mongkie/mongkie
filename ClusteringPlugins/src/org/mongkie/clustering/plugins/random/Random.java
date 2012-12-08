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
package org.mongkie.clustering.plugins.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.mongkie.clustering.spi.Cluster;
import org.mongkie.clustering.DefaultClusterImpl;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import org.openide.util.Exceptions;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class Random implements Clustering {

    private final RandomBuilder builder;
    private int clusterSize;
    static final int MIN_CLUSTER_SIZE = 3;
    private List<Cluster> clusters = new ArrayList<Cluster>();

    Random(RandomBuilder builder) {
        this.builder = builder;
        this.clusterSize = MIN_CLUSTER_SIZE;
    }

    int getClusterSize() {
        return clusterSize;
    }

    void setClusterSize(int clusterSize) {
        this.clusterSize = clusterSize < MIN_CLUSTER_SIZE ? MIN_CLUSTER_SIZE : clusterSize;
    }

    @Override
    public Collection<Cluster> execute(Graph g) {
        List<Node> nodes = new ArrayList<Node>(g.getNodeCount());
        Iterator<Node> nodesIter = g.nodes();
        while (nodesIter.hasNext()) {
            Node n = nodesIter.next();
            nodes.add(n);
        }
        Collections.shuffle(nodes);
        int i = 1, j = 1;
        DefaultClusterImpl c = new DefaultClusterImpl(g, "Random " + j);
        c.setRank(j - 1);
        clusters.clear();
        for (Node n : nodes) {
            c.addNode(n);
            if (i >= clusterSize) {
                clusters.add(c);
                c = new DefaultClusterImpl(g, "Random " + ++j);
                c.setRank(j - 1);
                i = 1;
            } else {
                i++;
            }
        }
        if (c.getNodesCount() > 0) {
            clusters.add(c);
        }

        try {
            synchronized (this) {
                wait(1000);
            }
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        return clusters;
    }

    @Override
    public boolean cancel() {
        synchronized (this) {
            clusters.clear();
            notifyAll();
        }
        return true;
    }

    @Override
    public ClusteringBuilder getBuilder() {
        return builder;
    }
}
