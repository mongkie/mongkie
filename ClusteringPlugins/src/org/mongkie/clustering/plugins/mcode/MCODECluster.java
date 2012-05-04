/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * User: Vuk Pavlovic
 * Date: Nov 29, 2006
 * Time: 5:34:46 PM
 * Description: Stores various cluster information for simple get/set purposes
 */
package org.mongkie.clustering.plugins.mcode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.mongkie.clustering.DefaultClusterImpl;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * Stores various cluster information for simple get/set purposes.
 */
public class MCODECluster extends DefaultClusterImpl {

    private List<Integer> nodeIndices = null;
    private int seedNode;
    private Map<Integer, Boolean> nodeSeenHashMap; // stores the nodes that have already been included in higher ranking clusters
    private double clusterScore;
    private String resultTitle;
    private GraphPerspective perspective;

    public MCODECluster(Graph g) {
        super(g, "Not ranked");
    }

    String getResultTitle() {
        return resultTitle;
    }

    void setResultTitle(String resultTitle) {
        this.resultTitle = resultTitle;
    }

    double getClusterScore() {
        return clusterScore;
    }

    void setClusterScore(double clusterScore) {
        this.clusterScore = clusterScore;
    }

    GraphPerspective getGraphPerspective() {
        return perspective;
    }

    void setGraphPerspective(GraphPerspective perspective) {
        clearNodes();
        this.perspective = perspective;
        for (Iterator<Node> nodeIter = perspective.nodesIterator(); nodeIter.hasNext();) {
            super.addNode(nodeIter.next());
        }
    }

    List<Integer> getNodeIndices() {
        return nodeIndices;
    }

    void setNodeIndices(List<Integer> nodeIndices) {
        this.nodeIndices = nodeIndices;
    }

    Integer getSeedNode() {
        return seedNode;
    }

    void setSeedNode(int seedNode) {
        this.seedNode = seedNode;
    }

    Map<Integer, Boolean> getNodeSeenHashMap() {
        return nodeSeenHashMap;
    }

    void setNodeSeenHashMap(Map<Integer, Boolean> nodeSeenHashMap) {
        this.nodeSeenHashMap = nodeSeenHashMap;
    }

    @Override
    public void setRank(int rank) {
        super.setRank(rank);
        setName("Cluster " + (rank + 1));
    }

    @Override
    public boolean addNode(Node n) {
        throw new UnsupportedOperationException("Not used");
    }

    @Override
    public void clearNodes() {
        super.clearNodes();
        if (perspective != null) {
            perspective.clear();
        }
    }

    @Override
    public boolean removeNode(Node n) {
        return super.removeNode(n) && perspective.removeNode(n);
    }
}
