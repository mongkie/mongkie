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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import kobic.prefuse.data.GraphFactory;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.CSVTableReader;
import prefuse.data.io.DataIOException;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MCODEAlgorithmTest {

    static MCODEAlgorithm algo;
    static Graph smallGraph, sox2Graph;

    public MCODEAlgorithmTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        algo = new MCODEAlgorithm();
        smallGraph = readSmallGraph();
        System.out.println("Small graph > Node count:" + smallGraph.getNodeCount() + ", Edge count:" + smallGraph.getEdgeCount());
        sox2Graph = readSox2Graph();
        System.out.println("Sox2 graph > Node count:" + sox2Graph.getNodeCount() + ", Edge count:" + sox2Graph.getEdgeCount());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        smallGraph.dispose();
        smallGraph.clear();
        sox2Graph.dispose();
        sox2Graph.clear();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSmallNetwork() {
        algo.scoreGraph(smallGraph, "Results 1");
        MCODECluster[] clusters = algo.findClusters(smallGraph, "Results 1");

        assertEquals(2, clusters.length);

        assertEquals((double) 1.5, algo.scoreCluster(clusters[0]), 0);
        assertEquals((double) 1.5, clusters[0].getClusterScore(), 0);
        assertEquals(4, clusters[0].getNodesCount());
        assertEquals(4, clusters[0].getGraphPerspective().getNodeCount());
        assertEquals(7, clusters[0].getGraphPerspective().getEdgeCount());

        assertEquals((double) 1.0, algo.scoreCluster(clusters[1]), 0);
        assertEquals((double) 1.0, clusters[1].getClusterScore(), 0);
        assertEquals(3, clusters[1].getNodesCount());
        assertEquals(3, clusters[1].getGraphPerspective().getNodeCount());
        assertEquals(4, clusters[1].getGraphPerspective().getEdgeCount());
    }

    @Test
    public void testSox2Network() {
        algo.scoreGraph(sox2Graph, "Results 2");
        MCODECluster[] clusters = algo.findClusters(sox2Graph, "Results 2");

        assertEquals(11, clusters.length);

        assertEquals(0, clusters[0].getRank());
        assertEquals((double) 1.1, algo.scoreCluster(clusters[0]), 0);
        assertEquals((double) 1.1, clusters[0].getClusterScore(), 0);
        assertEquals(10, clusters[0].getNodesCount());
        assertEquals(10, clusters[0].getGraphPerspective().getNodeCount());
        assertEquals(21, clusters[0].getGraphPerspective().getEdgeCount());

        assertEquals(1, clusters[1].getRank());
        assertEquals((double) 1.0, algo.scoreCluster(clusters[1]), 0);
        assertEquals((double) 1.0, clusters[1].getClusterScore(), 0);
        assertEquals(5, clusters[1].getNodesCount());
        assertEquals(5, clusters[1].getGraphPerspective().getNodeCount());
        assertEquals(9, clusters[1].getGraphPerspective().getEdgeCount());

        assertEquals(2, clusters[2].getRank());
        assertEquals((double) 1.0, algo.scoreCluster(clusters[2]), 0);
        assertEquals((double) 1.0, clusters[2].getClusterScore(), 0);
        assertEquals(4, clusters[2].getNodesCount());
        assertEquals(4, clusters[2].getGraphPerspective().getNodeCount());
        assertEquals(8, clusters[2].getGraphPerspective().getEdgeCount());

        assertEquals(3, clusters[3].getRank());
        assertEquals((double) 1.0, algo.scoreCluster(clusters[3]), 0);
        assertEquals((double) 1.0, clusters[3].getClusterScore(), 0);
        assertEquals(3, clusters[3].getNodesCount());
        assertEquals(3, clusters[3].getGraphPerspective().getNodeCount());
        assertEquals(5, clusters[3].getGraphPerspective().getEdgeCount());

        assertEquals(4, clusters[4].getRank());
        assertEquals((double) 1.0, algo.scoreCluster(clusters[4]), 0);
        assertEquals((double) 1.0, clusters[4].getClusterScore(), 0);
        assertEquals(3, clusters[4].getNodesCount());
        assertEquals(3, clusters[4].getGraphPerspective().getNodeCount());
        assertEquals(3, clusters[4].getGraphPerspective().getEdgeCount());

        assertEquals(5, clusters[5].getRank());
        assertEquals((double) 1.0, algo.scoreCluster(clusters[5]), 0);
        assertEquals((double) 1.0, clusters[5].getClusterScore(), 0);
        assertEquals(3, clusters[5].getGraphPerspective().getNodeCount());
        assertEquals(3, clusters[5].getNodesCount());
        assertEquals(3, clusters[5].getGraphPerspective().getEdgeCount());

        assertEquals(6, clusters[6].getRank());
        assertEquals((double) 0.75, algo.scoreCluster(clusters[6]), 0);
        assertEquals((double) 0.75, clusters[6].getClusterScore(), 0);
        assertEquals(4, clusters[6].getNodesCount());
        assertEquals(4, clusters[6].getGraphPerspective().getNodeCount());
        assertEquals(6, clusters[6].getGraphPerspective().getEdgeCount());

        assertEquals(7, clusters[7].getRank());
        assertEquals((double) 0.667, algo.scoreCluster(clusters[7]), 0.001);
        assertEquals((double) 0.667, clusters[7].getClusterScore(), 0.001);
        assertEquals(3, clusters[7].getNodesCount());
        assertEquals(3, clusters[7].getGraphPerspective().getNodeCount());
        assertEquals(5, clusters[7].getGraphPerspective().getEdgeCount());

        assertEquals(8, clusters[8].getRank());
        assertEquals((double) 0.5, algo.scoreCluster(clusters[8]), 0);
        assertEquals((double) 0.5, clusters[8].getClusterScore(), 0);
        assertEquals(2, clusters[8].getNodesCount());
        assertEquals(2, clusters[8].getGraphPerspective().getNodeCount());
        assertEquals(3, clusters[8].getGraphPerspective().getEdgeCount());

        assertEquals(9, clusters[9].getRank());
        assertEquals((double) 0.5, algo.scoreCluster(clusters[9]), 0);
        assertEquals((double) 0.5, clusters[9].getClusterScore(), 0);
        assertEquals(2, clusters[9].getNodesCount());
        assertEquals(2, clusters[9].getGraphPerspective().getNodeCount());
        assertEquals(3, clusters[9].getGraphPerspective().getEdgeCount());

        assertEquals(10, clusters[10].getRank());
        assertEquals((double) 0.5, algo.scoreCluster(clusters[10]), 0);
        assertEquals((double) 0.5, clusters[10].getClusterScore(), 0);
        assertEquals(2, clusters[10].getNodesCount());
        assertEquals(2, clusters[10].getGraphPerspective().getNodeCount());
        assertEquals(3, clusters[10].getGraphPerspective().getEdgeCount());
    }

    private static Graph readSox2Graph() throws DataIOException, IOException {
        Graph g = GraphFactory.createDefault();
        Table ppiTable = new CSVTableReader().readTable(MCODEAlgorithmTest.class.getResourceAsStream("resources/sox2.csv"));
        Iterator<Tuple> ppiIter = ppiTable.tuples();
        Map<String, Integer> id2Key = new HashMap<String, Integer>();
        while (ppiIter.hasNext()) {
            Tuple ppi = ppiIter.next();
            String A = ppi.getString("GENE_A");
            Node source = id2Key.containsKey(A) ? g.getNode(id2Key.get(A)) : null;
            if (source == null) {
                source = g.addNode();
                id2Key.put(A, source.getRow());
            }
            String B = ppi.getString("GENE_B");
            Node target = id2Key.containsKey(B) ? g.getNode(id2Key.get(B)) : null;
            if (target == null) {
                target = g.addNode();
                id2Key.put(B, target.getRow());
            }
//            if (source == target) {
//                continue;
//            }
            if (g.getEdge(source, target) == null && g.getEdge(target, source) == null) {
                g.addEdge(source, target);
            }
        }
        return g;
    }

    private static Graph readSmallGraph() throws DataIOException, IOException {
        Graph g = GraphFactory.createDefault();
        Table ppiTable = new CSVTableReader().readTable(MCODEAlgorithmTest.class.getResourceAsStream("resources/small2.csv"));
        Iterator<Tuple> ppiIter = ppiTable.tuples();
        Map<String, Integer> id2Key = new HashMap<String, Integer>();
        while (ppiIter.hasNext()) {
            Tuple ppi = ppiIter.next();
            String A = ppi.getString("A");
            Node source = id2Key.containsKey(A) ? g.getNode(id2Key.get(A)) : null;
            if (source == null) {
                source = g.addNode();
                id2Key.put(A, source.getRow());
            }
            String B = ppi.getString("B");
            Node target = id2Key.containsKey(B) ? g.getNode(id2Key.get(B)) : null;
            if (target == null) {
                target = g.addNode();
                id2Key.put(B, target.getRow());
            }
//            if (source == target) {
//                continue;
//            }
            if (g.getEdge(source, target) == null && g.getEdge(target, source) == null) {
                g.addEdge(source, target);
            }
        }
        return g;
    }
}
