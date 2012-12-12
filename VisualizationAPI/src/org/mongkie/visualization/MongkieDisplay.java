/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualization;

import java.awt.Color;
import java.util.*;
import kobic.prefuse.AggregateShape;
import static kobic.prefuse.Constants.*;
import kobic.prefuse.display.NetworkDisplay;
import kobic.prefuse.render.ExtendedEdgeRenderer;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualization.workspace.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import prefuse.Visualization;
import static prefuse.Visualization.*;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.tuple.TupleSet;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.StrokeLib;
import prefuse.util.force.*;
import prefuse.visual.*;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MongkieDisplay extends NetworkDisplay implements Workspace {
    
    private transient InstanceContent instanceContent;
    private transient Lookup lookup;
    
    public MongkieDisplay() {
        this((Graph) null);
    }
    
    public MongkieDisplay(Graph g) {
        super(g);
        
        setEdgeLabelVisible(true);
        
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
    }
    
    public MongkieDisplay(Visualization v) {
        super(v);
        
        setEdgeLabelVisible(true);
        
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
    }
    
    @Override
    public void add(Object instance) {
        instanceContent.add(instance);
    }
    
    @Override
    public void remove(Object instance) {
        instanceContent.remove(instance);
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    @Override
    protected Renderer createEdgeRenderer() {
        return new ExtendedEdgeRenderer();
    }

//    @Override
//    protected Renderer createEdgeRenderer() {
//        EdgeRenderer r = new EdgeRenderer(true, false);
//        r.setEdgeType(prefuse.Constants.EDGE_TYPE_CURVE);
//        return r;
//    }
//
    @Override
    protected AggregateShape getInitialAggregateShape() {
        return AggregateShape.CIRCLE;
    }
    
    @Override
    protected void addNodeColorActions(ActionList draw) {
        draw.add(new ColorAction(NODES, VisualItem.STROKECOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getNodeColorProvider().getStrokeColor((NodeItem) item);
                setDefaultColor(c == null ? getNodeStrokeColor() : ColorLib.color(c));
                super.process(item, frac);
            }
        });
        draw.add(new ColorAction(NODES, VisualItem.TEXTCOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getNodeColorProvider().getTextColor((NodeItem) item);
                setDefaultColor(c == null ? getNodeTextColor() : ColorLib.color(c));
                super.process(item, frac);
            }
        });
        draw.add(new ColorAction(NODES, VisualItem.FILLCOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getNodeColorProvider().getFillColor((NodeItem) item);
                setDefaultColor(c == null ? getNodeFillColor() : ColorLib.color(c));
                super.process(item, frac);
            }
        });
    }
    
    @Override
    protected void addEdgeColorActions(ActionList draw) {
        draw.add(new ColorAction(EDGES, VisualItem.STROKECOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getEdgeColorProvider().getStrokeColor((EdgeItem) item);
                setDefaultColor(c == null ? getEdgeStrokeColor() : ColorLib.color(c));
                super.process(item, frac);
            }
        });
        draw.add(new ColorAction(EDGES, VisualItem.TEXTCOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getEdgeColorProvider().getTextColor((EdgeItem) item);
                setDefaultColor(c == null ? getEdgeTextColor() : ColorLib.color(c)); // fix
                super.process(item, frac);
            }
        });
        draw.add(new ColorAction(EDGES, VisualItem.FILLCOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getEdgeColorProvider().getFillColor((EdgeItem) item);
                setDefaultColor(c == null ? getEdgeFillColor() : ColorLib.color(c));
                super.process(item, frac);
            }
        });
    }
    
    @Override
    protected void addAggregateColorActions(ActionList draw) {
        draw.add(new ColorAction(Visualization.AGGR_ITEMS, VisualItem.FILLCOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getGroupColorProvider().getFillColor((AggregateItem) item);
                setDefaultColor(c == null ? ColorLib.setAlpha(COLOR_DEFAULT_AGGR_FILL, COLOR_AGGRFILL_ALPHA) : ColorLib.setAlpha(ColorLib.color(c), COLOR_AGGRFILL_ALPHA));
                super.process(item, frac);
            }
        });
        draw.add(new ColorAction(Visualization.AGGR_ITEMS, VisualItem.TEXTCOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getGroupColorProvider().getTextColor((AggregateItem) item);
                setDefaultColor(c == null ? COLOR_DEFAULT_AGGR_TEXT : ColorLib.color(c));
                super.process(item, frac);
            }
        });
        draw.add(new ColorAction(Visualization.AGGR_ITEMS, VisualItem.STROKECOLOR) {
            
            @Override
            public void process(VisualItem item, double frac) {
                Color c = Lookup.getDefault().lookup(ColorController.class).getModel(MongkieDisplay.this).getGroupColorProvider().getFillColor((AggregateItem) item);
                setDefaultColor(c == null ? COLOR_DEFAULT_AGGR_STROKE : ColorLib.color(c));
                super.process(item, frac);
            }
        });
    }
    
    @Override
    protected StrokeAction addNodeStrokeAction(ActionList draw) {
        StrokeAction strokeAction = super.addNodeStrokeAction(draw);
        strokeAction.add(new AbstractPredicate() {
            
            @Override
            public boolean getBoolean(Tuple t) {
                return ((AggregateTable) getVisualization().getVisualGroup(Visualization.AGGR_ITEMS)).containsItem((NodeItem) t);
            }
        }, StrokeLib.getStroke(4.0F));
        return strokeAction;
    }
    
    @Override
    public boolean isEdgeInteractive() {
        return true;
    }
    
    @Override
    public AggregateItem aggregateItems(final TupleSet items, final boolean clearItems, final String label, String... activities) {
        final Visualization v = getVisualization();
        final AggregateTable aggregates = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        final int aggregateId = getNextAggregateId();
        v.process(new Runnable() {
            
            @Override
            public void run() {
                AggregateItem aggregateItem = (label == null)
                        ? aggregates.addItem(new Object[]{aggregateId, String.valueOf(aggregateId)})
                        : aggregates.addItem(new Object[]{aggregateId, label});
                AggregateShape s = getAggregateShape();
                aggregateItem.setShape(s.getCode());
                aggregateItem.addItems(items.tuples());
                Tuple[] itemArr = items.toArray();
                int sparsity = (itemArr.length < 8) ? 1 : (itemArr.length / 8) + 1;
                for (int i = 0; i < itemArr.length; i++) {
//                    aggregateItem.addItem((VisualItem) itemArr[i]);
                    if (i % sparsity == 0) {
                        for (int j = 0; j < itemArr.length; j++) {
                            if (i == j) {
                                continue;
                            }
                            Edge e = getGraph().addEdge((Node) v.getSourceTuple((VisualItem) itemArr[i]), (Node) v.getSourceTuple((VisualItem) itemArr[j]));
                            VisualItem eitem = v.getVisualItem(EDGES, e);
                            eitem.setAggregating(true);
                            eitem.setInteractive(false);
                        }
                    }
                }
                s.layout(aggregateItem);
                if (clearItems) {
                    items.clear();
                }
            }
        }, activities);
        return (AggregateItem) aggregates.getTuple(AggregateItem.AGGR_ID, aggregateId);
    }

    // Not yet applied. Above is another option?
    private List<Tuple> topDegreeItems(Tuple[] itemArr) {
        if (itemArr.length < 8) {
            return Arrays.asList(itemArr);
        }
        List<Tuple> hubs = new ArrayList<Tuple>(Arrays.asList(itemArr));
        Collections.sort(hubs, new Comparator<Tuple>() {
            
            @Override
            public int compare(Tuple o1, Tuple o2) {
                return ((Node) ((VisualItem) o2).getSourceTuple()).getDegree() - ((Node) ((VisualItem) o1).getSourceTuple()).getDegree();
            }
        });
        return hubs.subList(0, 4);
    }
    
    private List<Node> topDegreeNodes(Node[] nodes) {
        if (nodes.length < 8) {
            return Arrays.asList(nodes);
        }
        List<Node> hubs = new ArrayList<Node>(Arrays.asList(nodes));
        Collections.sort(hubs, new Comparator<Node>() {
            
            @Override
            public int compare(Node o1, Node o2) {
                return o2.getDegree() - o1.getDegree();
            }
        });
        return hubs.subList(0, 4);
    }
    
    @Override
    public AggregateItem aggregateNodes(final Collection<Node> nodes, final String label) {
        final Visualization v = getVisualization();
        final AggregateTable aggregates = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        final int aggregateId = getNextAggregateId();
        v.process(new Runnable() {
            
            @Override
            public void run() {
                AggregateItem aggrItem = (label == null)
                        ? aggregates.addItem(new Object[]{aggregateId, String.valueOf(aggregateId)})
                        : aggregates.addItem(new Object[]{aggregateId, label});
                AggregateShape s = getAggregateShape();
                aggrItem.setShape(s.getCode());
                aggrItem.addItems(nodes, NODES);
                Node[] nodeArr = nodes.toArray(new Node[nodes.size()]);
                List<Node> hubs = topDegreeNodes(nodeArr);
                for (int i = 0; i < nodeArr.length; i++) {
//                    aggrItem.addItem(v.getVisualItem(NODES, nodeArr[i]));
                    if (hubs.contains(nodeArr[i])) {
                        for (int j = 0; j < nodeArr.length; j++) {
                            if (i == j) {
                                continue;
                            }
                            Edge e = getGraph().addEdge(nodeArr[i], nodeArr[j]);
                            VisualItem eitem = v.getVisualItem(EDGES, e);
                            eitem.setAggregating(true);
                            eitem.setInteractive(false);
                        }
                    }
                }
                s.layout(aggrItem);
            }
        }, DRAW);
        return (AggregateItem) aggregates.getTuple(AggregateItem.AGGR_ID, aggregateId);
    }
    
    @Override
    public void unaggregateItems(final AggregateItem aggrItem) {
        final Visualization v = getVisualization();
        final Graph G = getGraph();
        
        final AggregateTable aggregates = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        v.process(new Runnable() {
            
            @Override
            public void run() {
                VisualItem[] itemArr = aggrItem.toArray();
                for (int i = 0; i < itemArr.length; i++) {
                    for (int j = 0; j < itemArr.length; j++) {
                        if (i == j) {
                            continue;
                        }
                        Iterator<Edge> outEdges = G.getNode(itemArr[i].getRow()).outEdges();
                        while (outEdges.hasNext()) {
                            Edge e = outEdges.next();
                            if (v.getVisualItem(EDGES, e).isAggregating() && e.getTargetNode().getRow() == itemArr[j].getRow()) {
                                G.removeEdge(e);
                                break;
                            }
                        }
                    }
                }
                v.getFocusGroup(FOCUS_ITEMS).removeTuple(aggrItem);
                aggrItem.removeAllItems();
                aggregates.removeRow(aggrItem.getRow());
            }
        }, DRAW);
    }
    
    @Override
    protected Layout createGraphLayout() {
//        ForceSimulator forceSimulator = new ForceSimulator(new RungeKuttaIntegrator());
//        forceSimulator.addForce(new NBodyForce(
//                NBodyForce.DEFAULT_MIN_GRAV_CONSTANT,
//                NBodyForce.DEFAULT_MIN_DISTANCE,
//                NBodyForce.DEFAULT_THETA));
//        forceSimulator.addForce(new DragForce(DragForce.DEFAULT_DRAG_COEFF));
//        forceSimulator.addForce(new SpringForce(
//                SpringForce.DEFAULT_SPRING_COEFF / 10,
//                SpringForce.DEFAULT_SPRING_LENGTH * 4));
//        return new ForceDirectedLayout(GRAPH, forceSimulator, false);
        ForceSimulator forceSimulator = new ForceSimulator(new RungeKuttaIntegrator());
        forceSimulator.addForce(new NBodyForce(
                NBodyForce.DEFAULT_MIN_GRAV_CONSTANT,
//                NBodyForce.DEFAULT_MAX_DISTANCE,
                NBodyForce.DEFAULT_MIN_DISTANCE,
                NBodyForce.DEFAULT_THETA));
        forceSimulator.addForce(new DragForce(DragForce.DEFAULT_DRAG_COEFF));
        forceSimulator.addForce(new SpringForce(
                SpringForce.DEFAULT_SPRING_COEFF / 10 * 4,
                SpringForce.DEFAULT_SPRING_LENGTH * 4));
        return new ForceDirectedLayout(GRAPH, forceSimulator, false) {
            
            @Override
            protected float getSpringLength(EdgeItem e) {
                if (e.isAggregating()) {
                    return SpringForce.DEFAULT_MIN_SPRING_LENGTH;
                } else {
                    return SpringForce.DEFAULT_MAX_SPRING_LENGTH;
                }
            }
//            @Override
//            protected boolean isEnabled(VisualItem item) {
//                if (item instanceof EdgeItem) {
//                    if (item.getBoolean(FIELD_ISAGGREGATING)) {
//                        return true;
//                    }
//                    EdgeItem e = (EdgeItem) item;
//                    Node sourceNode = (Node) getVisualization().getSourceTuple(e.getSourceItem());
//                    Node targetNode = (Node) getVisualization().getSourceTuple(e.getTargetItem());
//                    Iterator<Edge> inEdges = getGraph().inEdges(sourceNode);
//                    while (inEdges.hasNext()) {
//                        Edge in = inEdges.next();
//                        if (in.getSourceNode().equals(targetNode) && in.getBoolean(FIELD_ISAGGREGATING)) {
//                            return false;
//                        }
//                    }
//                    Iterator<Edge> outEdges = getGraph().outEdges(sourceNode);
//                    while (outEdges.hasNext()) {
//                        Edge out = outEdges.next();
//                        if (out.getTargetNode().equals(targetNode) && out.getBoolean(FIELD_ISAGGREGATING)) {
//                            return false;
//                        }
//                    }
//                    return true;
//                } else {
//                    return super.isEnabled(item);
//                }
//            }
        };
    }
    
    @Override
    protected String getNodeSizeField() {
        return null;
    }
}
