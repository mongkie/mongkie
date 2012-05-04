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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GraphPerspective {

    private final Graph G, g;
    private final Set<Node> nodes;
    private static final String NODE_INDEX = "NodeRootIndex";

    private GraphPerspective(Graph G) {
        this.G = G;
        g = createInternalGraph();
        nodes = new LinkedHashSet<Node>();
    }

    private Graph createInternalGraph() {
        Table nodeTable = new Table();
        nodeTable.addColumn(NODE_INDEX, int.class, -1);
        return new Graph(nodeTable, G.isDirected(), NODE_INDEX, Graph.DEFAULT_SOURCE_KEY, Graph.DEFAULT_TARGET_KEY);
    }

    private void resetInternalGraph() {
        g.clear();
        for (Node N : nodes) {
            g.addNode().setInt(NODE_INDEX, N.getRow());
        }
        for (Iterator<Edge> edgeIter = G.edges(); edgeIter.hasNext();) {
            Edge E = edgeIter.next();
            Node source = E.getSourceNode();
            Node target = E.getTargetNode();
            if (nodes.contains(source) && nodes.contains(target)) {
                g.addEdge(g.getNodeIndex(source.getRow()), g.getNodeIndex(target.getRow()));
            }
        }
    }

    private void setNodes(List<Integer> nodeIndices) {
        nodes.clear();
        for (int row : nodeIndices) {
            nodes.add(G.getNode(row));
        }
        resetInternalGraph();
    }

    static GraphPerspective create(List<Integer> nodeIndices, Graph G) {
        GraphPerspective perspective = new GraphPerspective(G);
        perspective.setNodes(nodeIndices);
        return perspective;
    }

    Iterator<Node> nodesIterator() {
        return nodes.iterator();
    }

    int getDegree(Node N) {
        return (nodes.contains(N)) ? g.getDistinctDegree(g.getNodeFromKey(N.getRow())) : 0;
    }

    int getNodeCount() {
        return g.getNodeCount();
    }

    int getEdgeCount() {
        return g.getEdgeCount();
    }

    GraphPerspective createPerspective(List<Integer> nodeIndices) {
        GraphPerspective child = new GraphPerspective(G);
        for (int row : nodeIndices) {
            Node N = G.getNode(row);
            if (nodes.contains(N)) {
                child.nodes.add(N);
            }
        }
        child.resetInternalGraph();
        return child;
    }

    int[] getNodeIndicesArray() {
        int[] indices = new int[nodes.size()];
        int i = 0;
        for (Node N : nodes) {
            indices[i++] = N.getRow();
        }
        return indices;
    }

    void clear() {
        nodes.clear();
        g.clear();
    }

    boolean removeNode(Node N) {
        boolean removed = nodes.remove(N);
        if (removed) {
            resetInternalGraph();
        }
        return removed;
    }

    boolean isNeighbor(Node N1, Node N2) {
        if (!nodes.contains(N1) || !nodes.contains(N2)) {
            return false;
        }
        Node n1 = g.getNodeFromKey(N1.getRow());
        Node n2 = g.getNodeFromKey(N2.getRow());
        return (g.getEdge(n1, n2) != null || g.getEdge(n2, n1) != null) ? true : false;
    }
}
