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
package org.mongkie.kopath.viz.worker;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static kobic.prefuse.Constants.NODES;
import static org.mongkie.kopath.Config.*;
import org.mongkie.kopath.spi.PathwayDatabase;
import static org.mongkie.kopath.viz.Config.FIELD_ISEXPANDING;
import org.mongkie.kopath.viz.PathwayDisplay;
import prefuse.Visualization;
import static prefuse.Visualization.*;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.util.DataLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ExpansionWorker extends PathwayWorker {

    private final VisualItem seedItem;

    public ExpansionWorker(PathwayDisplay display, int dbId, VisualItem seedItem) {
        super(display, dbId, seedItem.getString(FIELD_LOCALID));
        this.seedItem = seedItem;
    }

    @Override
    public void precess() {
        display.setLoading(true);
        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "Expanding the pathway: {0} from {1}", new Object[]{pathwayIds[0], PathwayDatabase.Lookup.valueOf(dbId).getName()});
        display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).clear();
        synchronized (display.getVisualization()) {
            seedItem.setBoolean(FIELD_ISEXPANDING, true);
        }
    }

    @Override
    public void process(final Graph g) {
        final Graph graph = display.getGraph();
        display.getVisualization().process(new Runnable() {

            @Override
            public void run() {
                if (display.isIntegratedPathway() && expand(graph, g)) {
                    seedItem.setExpandable(false);
                    // Graph changed
                    display.fireGraphChangedEvent();
                }
            }
        }, DRAW, LAYOUT, ANIMATE);
    }

    private Node getNodeUsingAndPredicate(Graph g, final String f1, final Object v1, final String f2, final Object v2) {
        Iterator<Tuple> tupleIter = g.getNodeTable().tuples(new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                Object v;
                return (v = t.get(f1)) != null && v.equals(v1) && (v = t.get(f2)) != null && v.equals(v2);
            }
        });
        return tupleIter.hasNext() ? g.getNode(tupleIter.next().getRow()) : null;
    }

    private boolean expand(Graph graph, Graph from) {
        // Adjust UID values of nodes
        int nodeMaxUid = DataLib.max(graph.nodes(), FIELD_UID).getInt(FIELD_UID);
        for (Iterator<Node> nodeIter = from.nodes(); nodeIter.hasNext();) {
            Node n = nodeIter.next();
            n.setInt(FIELD_UID, n.getInt(FIELD_UID) + nodeMaxUid + 1);
            int[] subes = (int[]) n.get(FIELD_SUBNODES);
            if (subes != null) {
                subes = Arrays.copyOf(subes, subes.length);
                for (int i = 0; i < subes.length; i++) {
                    subes[i] = subes[i] + nodeMaxUid + 1;
                }
                n.set(FIELD_SUBNODES, subes);
            }
        }
        // Find duplicates with NodeId or PublicId+PublicIdDbName
        Set<Node> duplicates = new HashSet<Node>();
        Map<Node, Node> addedNodes = new HashMap<Node, Node>();
        for (Iterator<Node> nodeIter = from.nodes(); nodeIter.hasNext();) {
            Node n = nodeIter.next();
            String nodeId = n.getString(FIELD_NODEID);
            String publicId = n.getString(FIELD_PUBLICID);
            String publicIdDb = n.getString(FIELD_PUBLICIDDBNAME);
            Node duplicate;
            if ((nodeId != null && (duplicate = graph.getNodeFrom(FIELD_NODEID, nodeId)) != null)
                    || (publicId != null && (duplicate = getNodeUsingAndPredicate(graph, FIELD_PUBLICID, publicId, FIELD_PUBLICIDDBNAME, publicIdDb)) != null)) {
                duplicates.add(n);
                addedNodes.put(n, duplicate);
            }
        }
        // Add nodes except duplicates
        for (Iterator<Node> nodeIter = from.nodes(); nodeIter.hasNext();) {
            Node n = nodeIter.next();
            if (duplicates.contains(n)) {
                continue;
            }
            Node added = graph.getNode(graph.getNodes().addTuple(n).getRow());
            addedNodes.put(n, added);
            VisualItem addedItem = display.getVisualization().getVisualItem(NODES, added);
            PrefuseLib.setX(addedItem, seedItem, seedItem.getX());
            PrefuseLib.setY(addedItem, seedItem, seedItem.getY());
        }
        // Add edges
        addingEdges:
        for (Iterator<Edge> edgeIter = from.edges(); edgeIter.hasNext();) {
            Edge e = edgeIter.next();
            Node source = e.getSourceNode();
            Node target = e.getTargetNode();
            String pathwayId = e.getString(FIELD_PATHWAYID);
            if (duplicates.contains(source) && duplicates.contains(target)) {
                for (Edge ee : graph.getEdges(addedNodes.get(source), addedNodes.get(target))) {
                    if (e.getBoolean(FIELD_ISINCLUDE) && ee.getBoolean(FIELD_ISINCLUDE)) {
                        continue addingEdges;
                    }
                    String ei = ee.getString(FIELD_PATHWAYID);
                    if ((pathwayId == null && ei == null) || (pathwayId != null && pathwayId.equals(ei)) || (ei != null && ei.equals(pathwayId))) {
                        continue addingEdges;
                    }
                }
                for (Edge ee : graph.getEdges(addedNodes.get(target), addedNodes.get(source))) {
                    if (e.getBoolean(FIELD_ISINCLUDE) && ee.getBoolean(FIELD_ISINCLUDE)) {
                        continue addingEdges;
                    }
                }
            }
            Edge added = graph.getEdge(graph.getEdges().addTuple(e).getRow());
            added.setInt(FIELD_SOURCEKEY, addedNodes.get(source).getRow());
            added.setInt(FIELD_TARGETKEY, addedNodes.get(target).getRow());
        }
        return true;
    }

    @Override
    public void lastly() {
//        display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).addTuple(seedItem);
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Expanding completed.");
        display.setLoading(false);
        synchronized (display.getVisualization()) {
            seedItem.setBoolean(FIELD_ISEXPANDING, false);
        }
    }
}
