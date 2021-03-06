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
package org.mongkie.clustering;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.mongkie.clustering.spi.Cluster;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class DefaultClusterImpl implements Cluster {

    private final Graph g;
    private String name;
    private final Set<Node> nodes;
    private Color color = Color.BLACK;
    private int rank = -1;

    public DefaultClusterImpl(Graph g) {
        this(g, "No name");
    }

    public DefaultClusterImpl(Graph g, String name) {
        this.g = g;
        this.name = name;
        nodes = new LinkedHashSet<Node>();
    }

    @Override
    public Graph getGraph() {
        return g;
    }

    public boolean addNode(Node n) {
        if (n.getGraph() != g) {
            throw new IllegalArgumentException("A Node must be in the same graph with other nodes.");
        }
        return nodes.add(n);
    }

    public boolean removeNode(Node n) {
        return nodes.remove(n);
    }

    public void clearNodes() {
        nodes.clear();
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes;
    }

    @Override
    public int getNodesCount() {
        return nodes.size();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
