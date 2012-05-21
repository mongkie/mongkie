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
package kobic.prefuse.display;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.AggregateShape;
import static kobic.prefuse.Constants.*;
import kobic.prefuse.action.assignment.NodeSizeAction;
import kobic.prefuse.action.layout.AggregateShapeLayout;
import kobic.prefuse.action.layout.AggregateShapeLayout.NodeItemsProjectionTable;
import kobic.prefuse.action.layout.DecoratorLayout;
import kobic.prefuse.action.layout.DecoratorLayout.DecoratedTableListener;
import kobic.prefuse.controls.AggregateDragControl;
import kobic.prefuse.controls.HighlightControl;
import kobic.prefuse.controls.MultipleSelectionControl;
import kobic.prefuse.controls.SingleSelectionControl;
import kobic.prefuse.data.GraphFactory;
import kobic.prefuse.data.io.ReaderFactory;
import kobic.prefuse.render.AggregateShapeRenderer;
import kobic.prefuse.render.DecoratorLabelRenderer;
import kobic.prefuse.render.ExtendedNodeRenderer;
import prefuse.Display;
import prefuse.Visualization;
import static prefuse.Visualization.*;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.ItemAction;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.action.layout.Layout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
import prefuse.controls.*;
import prefuse.data.*;
import prefuse.data.event.TableListener;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.io.DataIOException;
import prefuse.data.search.RegexSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.DataLib;
import prefuse.util.PrefuseLib;
import prefuse.util.StrokeLib;
import prefuse.util.display.PaintListener;
import prefuse.util.io.IOLib;
import prefuse.visual.*;
import prefuse.visual.expression.InGroupPredicate;
import prefuse.visual.expression.VisiblePredicate;
import prefuse.visual.sort.ItemSorter;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class NetworkDisplay extends Display {

    private Graph g;
    private final Visualization v;
    private VisualGraph vg;
    private ActionList draw, animate, layout;
    private transient Layout networkLayout;
    private transient AggregateShape aggregateShape;
    private final OverviewDisplay overview;
    protected static final Predicate INGROUP_FOCUS_ITEMS = new InGroupPredicate(FOCUS_ITEMS);
    protected static final Predicate INGROUP_SEARCH_ITEMS = new InGroupPredicate(SEARCH_ITEMS);
    private final SearchTupleSet nodeSearchEngine = new RegexSearchTupleSet();
    private final SearchTupleSet edgeSearchEngine = new RegexSearchTupleSet();
    private HoverTooltipControl nodeTooltipControl, edgeTooltipControl;
    private HighlightControl highlightControl;
    public static final String PROP_KEY = "NetworkDisplay";
    private boolean fired;

    public NetworkDisplay() {
        this((Graph) null);
    }

    public NetworkDisplay(Object... fields) {
        this((Graph) null, fields);
    }

    public NetworkDisplay(String graphML, Object... fields) throws DataIOException {
        this(ReaderFactory.getDefaultGraphMLReader().readGraph(new ByteArrayInputStream(graphML.getBytes())), fields);
    }

    public NetworkDisplay(Graph g, Object... fields) {
        super(new Visualization());
        NetworkDisplay.this.initializeClass(fields);
        fired = (g != null);

        v = getVisualization();
        this.g = g == null ? GraphFactory.createDefault() : g;
        vg = addGraph(v, this.g);
        setupVisualization(v, createRendererFactory(v));
        registerActions();

        overview = new OverviewDisplay(this);
        overview.setSize(300, 400);
        m_transact.addActivityListener(new ActivityAdapter() {

            @Override
            public void activityCancelled(Activity a) {
                activityFinished(a);
            }

            @Override
            public void activityFinished(Activity a) {
                overview.repaint();
            }

            @Override
            public void activityStepped(Activity a) {
                overview.repaint();
            }
        });

        initializeDisplay();
        indexSearchFields(nodeSearchEngine, edgeSearchEngine);

        v.run(DRAW);
        if (g != null && g.getNodeCount() > 1) {
            v.run(LAYOUT);
        }
        v.run(ANIMATE);
    }

    public boolean isFired() {
        return fired;
    }

    public NetworkDisplay(Visualization v, Object... fields) {
        super(v);
        NetworkDisplay.this.initializeClass(fields);

        this.v = getVisualization();
        this.g = (Graph) v.getSourceData(GRAPH);
        this.vg = (VisualGraph) v.getVisualGroup(GRAPH);
        this.draw = (ActionList) v.getAction(DRAW);
        this.animate = (ActionList) v.getAction(ANIMATE);
        this.layout = (ActionList) v.getAction(LAYOUT);
        this.networkLayout = (Layout) layout.get(0);

        overview = new OverviewDisplay(this);
        overview.setSize(300, 400);
        m_transact.addActivityListener(new ActivityAdapter() {

            @Override
            public void activityCancelled(Activity a) {
                activityFinished(a);
            }

            @Override
            public void activityFinished(Activity a) {
                overview.repaint();
            }

            @Override
            public void activityStepped(Activity a) {
                overview.repaint();
            }
        });

        initializeDisplay();
        indexSearchFields(nodeSearchEngine, edgeSearchEngine);
    }

    protected void initializeClass(Object... fields) {
    }

    private void indexSearchFields(SearchTupleSet nodeSearch, SearchTupleSet edgeSearch) {
        indexNodeSearchFields(nodeSearch);
        indexEdgeSearchFields(edgeSearch);
    }

    protected void indexNodeSearchFields(SearchTupleSet nodeSearch) {
        nodeSearch.clear();
        nodeSearch.index(v.visibleItems(NODES), getNodeLabelField());
    }

    protected void indexEdgeSearchFields(SearchTupleSet edgeSearch) {
        edgeSearch.clear();
        edgeSearch.index(v.visibleItems(EDGES), getEdgeLabelField());
    }

    private void search(final SearchTupleSet searchEngine, final String query) {
        v.rerun(new Runnable() {

            @Override
            public void run() {
                TupleSet searchedTupleSet = v.getFocusGroup(SEARCH_ITEMS);
                searchedTupleSet.clear();
                if (query == null || query.length() == 0) {
                    return;
                }
                searchEngine.search(".*" + query + ".*");
                Iterator<VisualItem> resultsIter = searchEngine.tuples();
                while (resultsIter.hasNext()) {
                    searchedTupleSet.addTuple(resultsIter.next());
                }
            }
        }, DRAW);
    }

    public void searchNodes(String query) {
        search(nodeSearchEngine, query);
    }

    public void searchEdges(String query) {
        search(edgeSearchEngine, query);
    }

    private void initializeDisplay() {
        registerControls();

        setItemSorter(new ZorderSorter());

        setHighQuality(true);
//        setSize(700, 700);
//        pan(350, 350);
        setForeground(Color.GRAY);
        setBackground(Color.WHITE);
//        setDamageRedraw(false);

        addPaintListener(new BackgroundImagePainter(
                backgroundImage == null ? backgroundImage = getBackgroundImage() : backgroundImage,
                loadingImage == null ? loadingImage = getLoadingImage() : loadingImage));
    }
    private Image backgroundImage = null, loadingImage = null;
    private transient boolean loading = false;

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        setLoading(loading, false);
    }

    public void setLoading(boolean loading, boolean disposeGraph) {
        boolean alreadyLoading = this.loading;
        this.loading = loading;
        if (disposeGraph && !alreadyLoading && loading) {
            v.reset();
            disposeGraph();
        }
        setRepainting(loading);
    }

    protected final void setRepainting(final boolean repainting) {
        v.rerun(new Runnable() {

            @Override
            public void run() {
                animate.remove(repaintingAction);
                if (repaintingAction != null) {
                    repaintingAction.cancel();
                    repaintingAction.setVisualization(null);
                }
                if (repainting) {
                    animate.add(repaintingAction = new RepaintAction(v));
                } else {
                    repaintingAction = null;
                }
//                Logger.getLogger(getClass().getName()).log(Level.INFO, "Animated repainting {0}", repainting ? "started." : "finished.");
            }
        }, ANIMATE);
    }
    private RepaintAction repaintingAction;

    protected void disposeGraph() {
        fireGraphDisposingEvent();
        g.clear();
        g.removeAllGraphModelListeners();
        g.dispose();
    }

    private class BackgroundImagePainter implements PaintListener {

        private final Image bgImg, loadingImg;

        public BackgroundImagePainter(Image bgImg, Image loadingImg) {
            this.bgImg = bgImg;
            this.loadingImg = loadingImg;
        }

        @Override
        public void prePaint(Display d, Graphics2D g) {
        }

        @Override
        public void postPaint(Display d, Graphics2D g) {
            if (!getGraph().isEmpty()) {
                return;
            }
            Rectangle2D displayBounds = d.getBounds();
            Image img = isLoading() ? loadingImg : bgImg;
            g.drawImage(img, (int) displayBounds.getCenterX() - img.getWidth(null) / 2, (int) displayBounds.getCenterY() - img.getHeight(null) / 2, null);
        }
    }

    protected Image getBackgroundImage() {
        Image img = IOLib.getImage(NetworkDisplay.class, IMAGE_PATH + "logo.png");
        waitForImage(img);
        return img;
    }

    protected Image getLoadingImage() {
        Image img = IOLib.getImage(NetworkDisplay.class, IMAGE_PATH + "loading.png");
        waitForImage(img);
        return img;
    }

    private void waitForImage(Image img) {
        MediaTracker mt = new MediaTracker(new Container());
        mt.addImage(img, 0);
        try {
            mt.waitForID(0);
        } catch (InterruptedException ex) {
            Logger.getLogger(NetworkDisplay.class.getName()).log(Level.SEVERE, null, ex);
        }
        mt.removeImage(img, 0);
    }

    protected int adjustZorderScore(VisualItem item, int score) {
        return score;
    }

    public Graph getGraph() {
        return g;
    }

    public VisualGraph getVisualGraph() {
        return vg;
    }

    public OverviewDisplay getOverview() {
        return overview;
    }

    public Layout getNetworkLayout() {
        return networkLayout;
    }

    public void rerunNetworkLayout() {
        v.rerun(LAYOUT);
    }

    protected VisualGraph addGraph(Visualization v, Graph g) {
        this.g = g;
        g.getNodeTable().putClientProperty(PROP_KEY, this);
        g.getNodeTable().putClientProperty(PROPKEY_DATAGROUP, NODES);
        g.getNodeTable().putClientProperty(DataViewSupport.PROP_KEY, createNodeDataViewSupport(g.getNodeTable()));
        g.getNodeTable().putClientProperty(DataEditSupport.PROP_KEY, createNodeDataEditSupport(g.getNodeTable()));
        g.getEdgeTable().putClientProperty(PROP_KEY, this);
        g.getEdgeTable().putClientProperty(PROPKEY_DATAGROUP, EDGES);
        g.getEdgeTable().putClientProperty(DataViewSupport.PROP_KEY, createEdgeDataViewSupport(g.getEdgeTable()));
        g.getEdgeTable().putClientProperty(DataEditSupport.PROP_KEY, createEdgeDataEditSupport(g.getEdgeTable()));
        vg = v.addGraph(GRAPH, g);
        return vg;
    }

    protected DataEditSupport createNodeDataEditSupport(Table table) {
        return new DataEditSupport(table) {

            @Override
            public boolean isEditable(String field) {
                return true;
            }

            @Override
            public boolean isAddColumnSupported() {
                return true;
            }

            @Override
            public boolean isRemoveColumnSupported() {
                return true;
            }

            @Override
            public boolean isAddDataSupported() {
                return true;
            }

            @Override
            public boolean isRemoveDataSupported() {
                return true;
            }
        };
    }

    protected DataEditSupport createEdgeDataEditSupport(Table table) {
        return new DataEditSupport(table) {

            @Override
            public boolean isEditable(String field) {
                return true;
            }

            @Override
            public boolean isAddColumnSupported() {
                return true;
            }

            @Override
            public boolean isRemoveColumnSupported() {
                return true;
            }

            @Override
            public boolean isAddDataSupported() {
                return true;
            }

            @Override
            public boolean isRemoveDataSupported() {
                return true;
            }
        };
    }

    public DataEditSupport getNodeDataEditSupport() {
        return (DataEditSupport) getGraph().getNodeTable().getClientProperty(DataEditSupport.PROP_KEY);
    }

    public DataEditSupport getEdgeDataEditSupport() {
        return (DataEditSupport) getGraph().getEdgeTable().getClientProperty(DataEditSupport.PROP_KEY);
    }

    public DataEditSupport getDataEditSupport(String dataGroup) {
        return (DataEditSupport) ((Table) getVisualization().getSourceData(dataGroup)).getClientProperty(DataEditSupport.PROP_KEY);
    }

    public final void resetGraph(Graph g) {
        resetGraph(g, (DisplayListener) null, DRAW, LAYOUT, ANIMATE);
    }

    public final void resetGraph(Graph g, final DisplayListener processor, String... activities) {
        final Graph ng = (g == null) ? GraphFactory.createDefault() : g;
        v.rerun(new Runnable() {

            @Override
            public void run() {
                unregisterActions();
                unregisterControls();
                v.reset();
                disposeGraph();
                addGraph(v, ng);
                setupVisualization(v, createRendererFactory(v));
                registerActions();
                registerControls();
                if (processor != null) {
                    processor.graphChanged(NetworkDisplay.this, ng);
                }
                fireGraphChangedEvent(ng);
            }
        }, activities);
    }

    private void fireGraphDisposingEvent() {
        synchronized (listeners) {
            for (Iterator<DisplayListener> iter = listeners.iterator(); iter.hasNext();) {
                iter.next().graphDisposing(this, getGraph());
            }
        }
    }

    private void fireGraphChangedEvent(Graph g) {
        if (this.g != g) {
            throw new IllegalStateException("Graph is not changed to new one?");
        }
        fired = (g != null);
        indexSearchFields(nodeSearchEngine, edgeSearchEngine);
        synchronized (listeners) {
            for (Iterator<DisplayListener> iter = listeners.iterator(); iter.hasNext();) {
                iter.next().graphChanged(this, g);
            }
        }
    }
    private final List<DisplayListener> listeners = Collections.synchronizedList(new ArrayList<DisplayListener>());

    public final void fireGraphChangedEvent() {
        fireGraphChangedEvent(g);
    }

    public void addDisplayListener(DisplayListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeDisplayListener(DisplayListener l) {
        listeners.remove(l);
    }

    protected final int getNextAggregateId() {
        int aggregateId = 0;
        for (Object idx : DataLib.ordinalArray(v.getVisualGroup(AGGR_ITEMS).tuples(new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                return t.getInt(AggregateItem.AGGR_ID) >= 0;
            }
        }), AggregateItem.AGGR_ID)) {
            if (aggregateId != ((Integer) idx).intValue()) {
                return aggregateId;
            }
            aggregateId++;
        }
        return aggregateId;
    }

    public AggregateItem aggregateItems(final TupleSet items, final boolean clearItems) {
        return aggregateItems(items, clearItems, null);
    }

    public AggregateItem aggregateItems(final TupleSet items, final boolean clearItems, final String label) {
        return aggregateItems(items, clearItems, label, DRAW, ANIMATE);
    }

    public AggregateItem aggregateItems(final TupleSet items, final boolean clearItems, final String label, String... activities) {
        final AggregateTable aggregates = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        final int aggregateId = getNextAggregateId();
        v.rerun(new Runnable() {

            @Override
            public void run() {
                AggregateItem aggregateItem = (label == null)
                        ? aggregates.addItem(new Object[]{aggregateId, String.valueOf(aggregateId)})
                        : aggregates.addItem(new Object[]{aggregateId, label});
                AggregateShape s = getAggregateShape();
                aggregateItem.setShape(s.getCode());
                aggregateItem.addItems(items.tuples(new InGroupPredicate(NODES)));
//                Iterator<VisualItem> itemIter = items.tuples(new InGroupPredicate(NODES));
//                while (itemIter.hasNext()) {
//                    aggregateItem.addItem(itemIter.next());
//                }
                s.layout(aggregateItem);
                if (clearItems) {
                    items.clear();
                }
            }
        }, activities);
        return (AggregateItem) aggregates.getTuple(AggregateItem.AGGR_ID, aggregateId);
    }

    public AggregateItem aggregateNodes(final Collection<Node> nodes, final String label) {
        final AggregateTable aggregates = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        final int aggregateId = getNextAggregateId();
        v.rerun(new Runnable() {

            @Override
            public void run() {
                AggregateItem aggrItem = (label == null)
                        ? aggregates.addItem(new Object[]{aggregateId, String.valueOf(aggregateId)})
                        : aggregates.addItem(new Object[]{aggregateId, label});
                AggregateShape s = getAggregateShape();
                aggrItem.setShape(s.getCode());
                aggrItem.addItems(nodes, NODES);
//                for (Node n : nodes) {
//                    aggrItem.addItem(v.getVisualItem(NODES, n));
//                }
                s.layout(aggrItem);
            }
        }, DRAW, ANIMATE);
        return (AggregateItem) aggregates.getTuple(AggregateItem.AGGR_ID, aggregateId);
    }

    public void unaggregateItems(final AggregateItem aggrItem) {
        final AggregateTable aggregates = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        v.rerun(new Runnable() {

            @Override
            public void run() {
                v.getFocusGroup(FOCUS_ITEMS).removeTuple(aggrItem);
                aggregates.removeTuple(aggrItem);
            }
        }, DRAW, ANIMATE);
    }

    private DefaultRendererFactory createRendererFactory(Visualization v) {
        DefaultRendererFactory rendererFactory =
                new DefaultRendererFactory(createNodeRenderer(getNodeSizeBase()), createEdgeRenderer(), new AggregateShapeRenderer());
        return rendererFactory;
    }

    protected Renderer createNodeRenderer(int sizeBase) {
        return new ExtendedNodeRenderer(sizeBase);
    }

    protected Renderer createEdgeRenderer() {
        return new EdgeRenderer(true, false);
    }

    protected AggregateShape getInitialAggregateShape() {
        return AggregateShape.CONVEX_HULL_CURVE;
    }

    public AggregateShape getAggregateShape() {
        return aggregateShape;
    }

    public void setAggregateShape(final AggregateShape shape) {
        /*
         * AggregateShapeLayout is not in the ANIMATE actions anymore
         */
//        v.rerun(new Runnable() {
//            
//            @Override
//            public void run() {
//                final DefaultRendererFactory drf = (DefaultRendererFactory) v.getRendererFactory();
//                final Renderer aggrR = drf.getAggregateRenderer();
//                drf.setAggregateRenderer(v, null);
//                final Layout l = (Layout) animate.remove(0);
//                l.cancel();
//                v.setValue(AGGR_ITEMS, null, VisualItem.SHAPE, shape.getCode());
//                aggregateShape = shape;
//                l.addActivityListener(new ActivityAdapter() {
//                    
//                    @Override
//                    public void activityFinished(Activity a) {
//                        drf.setAggregateRenderer(v, aggrR);
//                        l.removeActivityListener(this);
//                    }
//                });
//                l.run();
//                animate.add(0, l);
//            }
//        }, ANIMATE);
        final int code = shape.getCode();
        final AggregateShape s = AggregateShape.get(code);
        if (s == null) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unknown aggregate shape code: {0}", code);
            return;
        }
        v.rerun(new Runnable() {

            @Override
            public void run() {
                AggregateTable aggregateTable = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
                for (Iterator<AggregateItem> aggregates = aggregateTable.tuples(VisiblePredicate.TRUE); aggregates.hasNext();) {
                    AggregateItem aggregate = aggregates.next();
                    if (aggregate.getShape() != code) {
                        aggregate.setShape(code);
                        if (s.layout(aggregate)) {
                            aggregate.validateBounds();
                        }
                    }
                }
            }
        }, DRAW);
        this.aggregateShape = shape;
    }

    protected void setupVisualization(Visualization v, DefaultRendererFactory renderFactory) {
        v.setValue(EDGES, null, VisualItem.INTERACTIVE, Boolean.valueOf(isEdgeInteractive()));
        aggregateShape = getInitialAggregateShape();
        v.setValue(AGGR_ITEMS, null, VisualItem.SHAPE, aggregateShape.getCode());
        addDecorators(v, renderFactory);
        v.setRendererFactory(renderFactory);
    }

    protected void addDecorators(Visualization v, DefaultRendererFactory rendererFactory) {
        // Node decorator for displaying labels
        nodeDecoratorLayouts = new DecoratedTableListener();
        VisualTable nodeLabels = v.addDecorators(NODE_LABEL, NODES, DEFAULT_NODE_DECORATOR_SCHEMA);
        DecoratorLabelRenderer nodeLabelRenderer = makeupNodeLabelRenderer(new DecoratorLabelRenderer.Text(nodeLabels, getNodeLabelField()));
        rendererFactory.add(new InGroupPredicate(NODE_LABEL), nodeLabelRenderer);
        nodeDecoratorLayouts.addDecoratorRendererLayout(nodeLabelRenderer, new DecoratorLayout.Center(nodeLabels));
        getVisualGraph().getNodeTable().addTableListener(nodeDecoratorLayouts);
        // Edge decorator for displaying labels
        edgeDecoratorLayouts = new DecoratedTableListener();
        String edgeLabelField = getEdgeLabelField();
        if (edgeLabelField != null && g.getEdgeTable().canGetString(edgeLabelField)) {
            VisualTable edgeLabels = v.addDecorators(EDGE_LABEL, EDGES, DEFAULT_EDGE_DECORATOR_SCHEMA);
            DecoratorLabelRenderer edgeLabelRenderer = makeupEdgeLabelRenderer(new DecoratorLabelRenderer.Text(edgeLabels, edgeLabelField));
            rendererFactory.add(new InGroupPredicate(EDGE_LABEL), edgeLabelRenderer);
            edgeDecoratorLayouts.addDecoratorRendererLayout(edgeLabelRenderer, new DecoratorLayout.Center(edgeLabels));
            getVisualGraph().getEdgeTable().addTableListener(edgeDecoratorLayouts);
        }
        // Aggregate decorator for displaying labels
        aggregateDecoratorLayouts = new DecoratedTableListener();
        VisualTable aggregateLabels = v.addDecorators(AGGREGATE_LABEL, AGGR_ITEMS, DEFAULT_AGGR_DECORATOR_SCHEMA);
        DecoratorLabelRenderer aggregateLabelRenderer = makeupAggregateLabelRenderer(new DecoratorLabelRenderer.Text(aggregateLabels, AggregateItem.AGGR_NAME));
        rendererFactory.add(new InGroupPredicate(AGGREGATE_LABEL), aggregateLabelRenderer);
        aggregateDecoratorLayouts.addDecoratorRendererLayout(aggregateLabelRenderer, new DecoratorLayout.Center(aggregateLabels));
        ((VisualTable) v.getVisualGroup(AGGR_ITEMS)).addTableListener(aggregateDecoratorLayouts);
    }
    private DecoratedTableListener nodeDecoratorLayouts;
    private DecoratedTableListener edgeDecoratorLayouts;
    private DecoratedTableListener aggregateDecoratorLayouts;

    protected DecoratedTableListener getNodeDecoratorLayouts() {
        return nodeDecoratorLayouts;
    }

    protected DecoratedTableListener getEdgeDecoratorLayouts() {
        return edgeDecoratorLayouts;
    }

    protected DecoratedTableListener getAggregateDecoratorLayouts() {
        return aggregateDecoratorLayouts;
    }

    protected DecoratorLabelRenderer makeupNodeLabelRenderer(DecoratorLabelRenderer r) {
        return r;
    }

    protected DecoratorLabelRenderer makeupEdgeLabelRenderer(DecoratorLabelRenderer r) {
        return r;
    }

    protected DecoratorLabelRenderer makeupAggregateLabelRenderer(DecoratorLabelRenderer r) {
        return r;
    }

    public abstract boolean isEdgeInteractive();

    protected String getNodeLabelField() {
        return g.getNodeLabelField();
    }

    protected String getEdgeLabelField() {
        return g.getEdgeLabelField();
    }

    protected int getNodeSizeBase() {
        return NODESIZE_BASE;
    }

    protected void unregisterActions() {
        nodeItemsInAggregateTable.removeAllTableListeners();
        draw.removeAll();
        animate.removeAll();
        layout.removeAll();
        v.removeAction(DRAW);
        v.removeAction(ANIMATE);
        v.removeAction(LAYOUT);
    }

    private void registerActions() {
        draw = new ActionList();
        addNodeSizeAction(draw, getNodeSizeField());
        addNodeShapeAction(draw);
        addNodeStrokeAction(draw);
        addNodeColorActions(draw);
        addEdgeStrokeAction(draw);
        addEdgeColorActions(draw);
        addAggregateColorActions(draw);
        addColorMappingRules();
        addDrawActions(draw);
        draw.add(new RepaintAction(v));

        layout = new ActionList(getLayoutDuration());
        layout.add(networkLayout = createLayout());
        addLayoutActions(layout);
        layout.add(new RepaintAction(v));

        animate = new ActionList(Activity.INFINITY);
        addAnimateActions(animate);

        v.putAction(DRAW, draw);
        v.putAction(LAYOUT, layout);
        v.putAction(ANIMATE, animate);
    }

    public boolean isLayoutRunning() {
        return layout.isRunning();
    }

    protected long getLayoutDuration() {
        return 8000;
    }

    public void setLayout(Layout l, long duration) {
        synchronized (v) {
            v.cancel(LAYOUT);
            if (networkLayout != l) {
                layout.remove(networkLayout);
                layout.add(0, networkLayout = l);
            }
            layout.setDuration(duration);
            v.run(LAYOUT);
        }
    }

    public void cancelLayout() {
        synchronized (v) {
            v.cancel(LAYOUT);
        }
    }

    public ActionList getLayoutAction() {
        return getActionList(LAYOUT);
    }

    protected abstract Layout createLayout();

    protected abstract String getNodeSizeField();

    public final ActionList getActionList(String name) {
        if (name.equals(DRAW)) {
            return draw;
        } else if (name.equals(ANIMATE)) {
            return animate;
        } else if (name.equals(LAYOUT)) {
            return layout;
        } else {
            return null;
        }
    }

    protected final <T extends ItemAction> T getAction(String actionListName, Class<T> clazz, String group) {
        return getAction(getActionList(actionListName), clazz, group);
    }

    private <T extends ItemAction> T getAction(ActionList actionList, Class<T> clazz, String group) {
        int size = actionList.size();
        for (int i = 0; i < size; i++) {
            Action a = actionList.get(i);
            if (a.getClass() == clazz && ((T) a).getGroup().equals(group)) {
                return (T) a;
            }
        }
        return null;
    }

    protected final ColorAction getColorAction(String group, String colorField) {
        int size = draw.size();
        for (int i = 0; i < size; i++) {
            Action a = draw.get(i);
            if (a instanceof ColorAction
                    && ((ColorAction) a).getGroup().equals(group)
                    && ((ColorAction) a).getField().equals(colorField)) {
                return (ColorAction) a;
            }
        }
        return null;
    }

    protected SizeAction addNodeSizeAction(ActionList draw, String nodeSizeField) {
        if (g.getNodeTable().canGetInt(nodeSizeField)) {
            SizeAction sizer = new NodeSizeAction(nodeSizeField);
            draw.add(sizer);
            return sizer;
        }
        return null;
    }

    protected void addNodeColorActions(ActionList draw) {
        draw.add(new ColorAction(NODES, VisualItem.STROKECOLOR, getNodeStrokeColor()));
        draw.add(new ColorAction(NODES, VisualItem.TEXTCOLOR, getNodeTextColor()));
        draw.add(new ColorAction(NODES, VisualItem.FILLCOLOR, getNodeFillColor()));
    }

    protected void addColorMappingRules() {
        ColorAction nodeStroke = getColorAction(NODES, VisualItem.STROKECOLOR);
        if (nodeStroke != null) {
            nodeStroke.add(INGROUP_FOCUS_ITEMS, getItemFocusColor());
            nodeStroke.add(INGROUP_SEARCH_ITEMS, ColorLib.color(Color.red));
        }
        ColorAction nodeFill = getColorAction(NODES, VisualItem.FILLCOLOR);
        if (nodeFill != null) {
            nodeFill.add(VisualItem.HOVER, getNodeHoverColor());
            nodeFill.add(VisualItem.HIGHLIGHT, getNodeHighlightColor());
//            nodeFill.add(INGROUP_FOCUS_ITEMS, ColorLib.setAlpha(getNodeFocusColor(), ALPHA_FILLCOLOR));
//            nodeFill.add(VisualItem.FIXED, getNodeFixedColor());
        }
        ColorAction edgeStroke = getColorAction(EDGES, VisualItem.STROKECOLOR);
        if (edgeStroke != null) {
            edgeStroke.add(INGROUP_FOCUS_ITEMS, getItemFocusColor());
            edgeStroke.add(VisualItem.AGGREGATING, COLOR_TRANSPARENT);
            edgeStroke.add(VisualItem.HIGHLIGHT, getEdgeHighlightColor());
        }
        ColorAction edgeFill = getColorAction(EDGES, VisualItem.FILLCOLOR);
        if (edgeFill != null) {
            edgeFill.add(INGROUP_FOCUS_ITEMS, getItemFocusColor());
            edgeFill.add(VisualItem.AGGREGATING, COLOR_TRANSPARENT);
            edgeFill.add(VisualItem.HIGHLIGHT, getEdgeHighlightColor());
        }
        ColorAction aggrStroke = getColorAction(AGGR_ITEMS, VisualItem.STROKECOLOR);
        if (aggrStroke != null) {
            aggrStroke.add(INGROUP_FOCUS_ITEMS, getItemFocusColor());
            aggrStroke.add(VisualItem.HOVER, COLOR_DEFAULT_AGGR_HOVER_STROKE);
        }
    }

    protected int getNodeStrokeColor() {
        return COLOR_DEFAULT_NODE_STROKE;
    }

    protected int getNodeTextColor() {
        return COLOR_DEFAULT_NODE_TEXT;
    }

    protected int getNodeFillColor() {
        return COLOR_DEFAULT_NODE_FILL;
    }

    protected int getNodeHoverColor() {
        return COLOR_DEFAULT_NODE_HOVER;
    }

    protected int getNodeHighlightColor() {
        return COLOR_DEFAULT_NODE_HIGHLIGHT;
    }

    protected int getNodeFixedColor() {
        return COLOR_DEFAULT_NODE_FIXED;
    }

    protected int getItemFocusColor() {
        return COLOR_DEFAULT_ITEM_FOCUS;
    }

    protected ShapeAction addNodeShapeAction(ActionList draw) {
        return null;
    }

    protected StrokeAction addNodeStrokeAction(ActionList draw) {
        return addNodeStrokeDefaultAction(draw);
    }

    protected final StrokeAction addNodeStrokeDefaultAction(ActionList draw) {
        StrokeAction nodeStroke = new StrokeAction(NODES, StrokeLib.getStroke(2.0F));
        nodeStroke.add(INGROUP_FOCUS_ITEMS, StrokeLib.getStroke(4.0F));
        nodeStroke.add(INGROUP_SEARCH_ITEMS, StrokeLib.getStroke(4.0F));
        draw.add(nodeStroke);
        return nodeStroke;
    }

    protected void addEdgeColorActions(ActionList draw) {
        draw.add(new ColorAction(EDGES, VisualItem.STROKECOLOR, getEdgeStrokeColor()));
        draw.add(new ColorAction(EDGES, VisualItem.FILLCOLOR, getEdgeFillColor()));
        draw.add(new ColorAction(EDGES, VisualItem.TEXTCOLOR, getEdgeTextColor()));
    }

    protected int getEdgeStrokeColor() {
        return COLOR_DEFAULT_EDGE_STROKE;
    }

    protected int getEdgeFillColor() {
        return COLOR_DEFAULT_EDGE_FILL;
    }

    protected int getEdgeTextColor() {
        return COLOR_DEFAULT_EDGE_TEXT;
    }

    protected int getEdgeHighlightColor() {
        return COLOR_DEFAULT_EDGE_HIGHLIGHT;
    }

    protected StrokeAction addEdgeStrokeAction(ActionList draw) {
        return null;
    }

    protected final void addDefaultAggregateColorActions(ActionList draw) {
        draw.add(new ColorAction(AGGR_ITEMS, VisualItem.FILLCOLOR, ColorLib.setAlpha(COLOR_DEFAULT_AGGR_FILL, COLOR_AGGRFILL_ALPHA)));
        ColorAction aggrStroke = new ColorAction(AGGR_ITEMS, VisualItem.STROKECOLOR, COLOR_DEFAULT_AGGR_STROKE);
        draw.add(aggrStroke);
    }

    protected void addAggregateColorActions(ActionList draw) {
        addDefaultAggregateColorActions(draw);
    }

    protected void addDrawActions(ActionList draw) {
        // draw.add(new FontAction(NODES, FONT_NODETEXT));
    }

    protected void addAnimateActions(ActionList animate) {
    }

    protected void addLayoutActions(ActionList layout) {
        for (DecoratorLayout l : nodeDecoratorLayouts.getLayouts()) {
            layout.add(l);
        }
        for (DecoratorLayout l : edgeDecoratorLayouts.getLayouts()) {
            layout.add(l);
        }
        for (DecoratorLayout l : aggregateDecoratorLayouts.getLayouts()) {
            layout.add(l);
        }
        layout.add(new AggregateShapeLayout(v));
        nodeItemsInAggregateTable = AggregateShapeLayout.createNodeItemsProjectionTable(v);
        final TableListener aggregateShapeLayout = nodeItemsInAggregateTable.createNodeItemsProjectionListener();
        nodeItemsInAggregateTable.addTableListener(aggregateShapeLayout);
        layout.addActivityListener(new ActivityAdapter() {

            @Override
            public void activityCancelled(Activity a) {
                activityFinished(a);
            }

            @Override
            public void activityFinished(Activity a) {
                getVisualGraph().getNodeTable().addTableListener(nodeDecoratorLayouts);
                getVisualGraph().getEdgeTable().addTableListener(edgeDecoratorLayouts);
                ((VisualTable) v.getVisualGroup(AGGR_ITEMS)).addTableListener(aggregateDecoratorLayouts);
                nodeItemsInAggregateTable.addTableListener(aggregateShapeLayout);
            }

            @Override
            public void activityStarted(Activity a) {
                getVisualGraph().getNodeTable().removeTableListener(nodeDecoratorLayouts);
                getVisualGraph().getEdgeTable().removeTableListener(edgeDecoratorLayouts);
                ((VisualTable) v.getVisualGroup(AGGR_ITEMS)).removeTableListener(aggregateDecoratorLayouts);
                nodeItemsInAggregateTable.removeTableListener(aggregateShapeLayout);
            }
        });
    }
    private NodeItemsProjectionTable nodeItemsInAggregateTable;

    private void unregisterControls() {
        for (Control cl : controls) {
            removeControlListener(cl);
        }
        controls.clear();
    }
    private final List<Control> controls = new ArrayList<Control>();

    private void registerControls() {
        Schema nodeTooltipSchema = getNodeDataViewSupport().getTooltipSchema();
        if (nodeTooltipSchema != null) {
            addControlListener(nodeTooltipControl = new HoverTooltipControl(NODES, nodeTooltipSchema) {

                @Override
                protected String getString(Tuple data, String field) {
                    return getNodeDataViewSupport().getStringAt(data, field);
                }

                @Override
                protected String getTitle(String field) {
                    return getNodeDataViewSupport().getColumnTitle(field);
                }
            });
            controls.add(nodeTooltipControl);
        }
        Schema edgeTooltipSchema = getEdgeDataViewSupport().getTooltipSchema();
        if (edgeTooltipSchema != null && isEdgeInteractive()) {
            addControlListener(edgeTooltipControl = new HoverTooltipControl(EDGES, edgeTooltipSchema) {

                @Override
                protected String getString(Tuple data, String field) {
                    return getEdgeDataViewSupport().getStringAt(data, field);
                }

                @Override
                protected String getTitle(String field) {
                    return getEdgeDataViewSupport().getColumnTitle(field);
                }
            });
            controls.add(edgeTooltipControl);
        }
        controls.add(addControlListener(new MultipleSelectionControl(v, NODES, DRAW)));
        controls.add(addControlListener(new MultipleSelectionControl(v, EDGES)));
        controls.add(addControlListener(new SingleSelectionControl(v, AGGR_ITEMS)));
        controls.add(addControlListener(createDragControl()));
        controls.add(addControlListener(new DragSelectionControl(this, NODES, DRAW)));
        controls.add(addControlListener(new PanControl(overview)));
        controls.add(addControlListener(new WheelZoomControl(overview)));
        controls.add(addControlListener(new ZoomToFitControl(Control.MIDDLE_MOUSE_BUTTON)));
        highlightControl = createHighlightControl();
        if (highlightControl != null) {
            controls.add(addControlListener(highlightControl));
        }
//        controls.addAll(addControls(v));
    }

    protected List<Control> addControls(Visualization v) {
        return new ArrayList<Control>();
    }

    protected Control createDragControl() {
        return new AggregateDragControl();
    }

    protected HighlightControl createHighlightControl() {
        return new HighlightControl(DRAW);
    }

    public DataViewSupport getNodeDataViewSupport() {
        return (DataViewSupport) getGraph().getNodeTable().getClientProperty(DataViewSupport.PROP_KEY);
    }

    public DataViewSupport getEdgeDataViewSupport() {
        return (DataViewSupport) getGraph().getEdgeTable().getClientProperty(DataViewSupport.PROP_KEY);
    }

    public DataViewSupport getDataViewSupport(String dataGroup) {
        return (DataViewSupport) ((Table) getVisualization().getSourceData(dataGroup)).getClientProperty(DataViewSupport.PROP_KEY);
    }

    protected DataViewSupport createNodeDataViewSupport(final Table table) {
        return new DataViewSupport(table) {

            @Override
            public Schema getOutlineSchema() {
                return table.getSchema();
            }
        };
    }

    protected DataViewSupport createEdgeDataViewSupport(final Table table) {
        return new DataViewSupport(table) {

            @Override
            public Schema getOutlineSchema() {
                return table.getSchema();
            }
        };
    }

    public void setTooltipEnabled(boolean on) {
        if (nodeTooltipControl != null) {
            nodeTooltipControl.setEnabled(on);
        }
        if (edgeTooltipControl != null) {
            edgeTooltipControl.setEnabled(on);
        }
    }

    public boolean setEdgeLabelVisible(boolean visible) {
        Iterator labels = getVisualization().items(EDGE_LABEL);
        while (labels.hasNext()) {
            VisualItem label = (VisualItem) labels.next();
            PrefuseLib.updateVisible(label, visible);
        }
        return visible;
    }

    @Deprecated
    protected final boolean inSameAggregate(NodeItem n1, NodeItem n2) {
        AggregateTable aggregates = (AggregateTable) getVisualization().getVisualGroup(AGGR_ITEMS);
        Set<AggregateItem> aggregateItems1 = aggregates.getAggregateSet(n1);
        Set<AggregateItem> aggregateItems2 = aggregates.getAggregateSet(n2);
        aggregateItems1.retainAll(aggregateItems2);
        return !aggregateItems1.isEmpty();
    }

    protected class ZorderSorter extends ItemSorter {

        @Override
        public int score(VisualItem item) {
            int score = super.score(item);
            return adjustZorderScore(item, score);
        }
    }
}
