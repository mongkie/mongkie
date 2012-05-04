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
package org.mongkie.kopath.viz;

import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import static kobic.prefuse.Constants.*;
import kobic.prefuse.EdgeStroke;
import kobic.prefuse.action.assignment.EdgeDataColorAction;
import kobic.prefuse.action.assignment.NodeDataColorAction;
import kobic.prefuse.action.layout.DecoratorLayout;
import kobic.prefuse.controls.AggregateDragControl;
import kobic.prefuse.display.DataEditSupport;
import kobic.prefuse.display.DataViewSupport;
import kobic.prefuse.render.DecoratorLabelRenderer;
import static org.mongkie.kopath.Config.*;
import org.mongkie.kopath.ControlType;
import org.mongkie.kopath.EntityFeature;
import org.mongkie.kopath.NodeType;
import org.mongkie.kopath.spi.PathwayDatabase;
import org.mongkie.kopath.util.Utilities;
import static org.mongkie.kopath.viz.Config.*;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.Constants;
import prefuse.Visualization;
import static prefuse.Visualization.AGGR_ITEMS;
import static prefuse.Visualization.DRAW;
import prefuse.action.ActionList;
import prefuse.action.assignment.*;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
import prefuse.controls.Control;
import prefuse.data.*;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;
import prefuse.data.event.TableListener;
import prefuse.data.event.TupleSetListener;
import prefuse.data.expression.AbstractPredicate;
import prefuse.data.expression.ColumnExpression;
import prefuse.data.expression.NotPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.tuple.TupleSet;
import prefuse.data.util.NamedColumnProjection;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.ImageFactory;
import prefuse.render.Renderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.util.force.*;
import prefuse.visual.*;
import prefuse.visual.expression.InGroupPredicate;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PathwayDisplay extends MongkieDisplay implements GraphListener {

    protected static final String FIELD_CORESPONDING_NODE = "CorespondingNode";
    protected final Predicate COMPLEXES_INAGGREGATES = new AbstractPredicate() {

        @Override
        public boolean getBoolean(Tuple t) {
            NodeItem coresNode = (NodeItem) t.get(FIELD_CORESPONDING_NODE);
            return (coresNode != null && NodeType.valueOf(coresNode.getString(FIELD_ITYPE)) == NodeType.COMPLEX);
        }
    };
    protected Predicate MOLECULES_INCOMPLEX, IS_ACTIVATION_CONTROL, IS_INHIBITION_CONTROL;
    private static final Logger logger = Logger.getLogger(PathwayDisplay.class.getName());
    private boolean integratedPathway = false;

    public PathwayDisplay() {
        super(createEmptyEachGraph());
    }

    public PathwayDisplay(Visualization v) {
        super(v);
    }

    public boolean isIntegratedPathway() {
        return integratedPathway;
    }

    public void setIntegratedPathway(boolean integratedPathway) {
        this.integratedPathway = integratedPathway;
    }

    @Override
    protected Image getBackgroundImage() {
        return new ImageIcon(PathwayDisplay.class.getResource("resources/logo.gif")).getImage();
    }

    @Override
    protected int adjustZorderScore(VisualItem item, int score) {
        int adjustive = 0;
        if (item instanceof NodeItem) {
            switch (NodeType.valueOf(item.getString(FIELD_ITYPE))) {
                case MOLECULE:
                case COMPOUND:
                case BIOLOGICAL_PROCESS:
                    adjustive += 2;
                    break;
                case COMPLEX:
                    adjustive += 1;
                    break;
                case SUPEREX:
                    if (item.isHover()) {
                        adjustive -= (1 << 25);
                    }
                    break;
                default:
                    break;
            }
        } else if (item instanceof EdgeItem) {
            if (item.isHover() && item.isHighlighted()) {
                adjustive -= (1 << 25);
                adjustive -= (1 << 24);
                adjustive--;
            }
        }
        return score += adjustive;
    }

    @Override
    protected void addDecorators(Visualization v, DefaultRendererFactory rendererFactory) {
        super.addDecorators(v, rendererFactory);

        getVisualGraph().getNodeTable().addColumn(FIELD_ISEXPANDING, boolean.class, false);
        VisualTable expandingIcons = v.addDecorators(NODE_EXPANDING_DECORATORS, NODES,
                ExpressionParser.predicate(FIELD_ISEXPANDING), (Schema) DEFAULT_NODE_DECORATOR_SCHEMA.clone());
        DecoratorLabelRenderer expandingIconRenderer = new DecoratorLabelRenderer.Icon(expandingIcons, null) {

            @Override
            public void render(Graphics2D g, VisualItem item) {
                super.render(g, item);
                // invalidate the icon manually to animate on repainting
                item.setValidated(false);
            }

            @Override
            protected Image getImage(VisualItem item) {
                return expandingGif;
            }
            private final Image expandingGif =
                    new ImageIcon(PathwayDisplay.class.getResource("resources/expanding.gif")).getImage();
        };
        expandingIconRenderer.setVerticalAlignment(Constants.CENTER);
        expandingIconRenderer.setHorizontalPadding(0);
        expandingIconRenderer.setVerticalPadding(0);
//        expandingIconRenderer.setMaxImageDimensions(100, 100);
        rendererFactory.add(new InGroupPredicate(NODE_EXPANDING_DECORATORS), expandingIconRenderer);
        final DecoratorLayout expandingIconLayout = new DecoratorLayout.Center(expandingIcons);
        getNodeDecoratorLayouts().addDecoratorRendererLayout(expandingIconRenderer, expandingIconLayout);
        // When expanding field set to true, it needs to layout the newly added expanding icon(decorator)
        expandingIcons.addTupleSetListener(new TupleSetListener() {

            @Override
            public void tupleSetChanged(TupleSet tupleSet, Tuple[] added, Tuple[] removed) {
                for (Tuple tuple : added) {
                    DecoratorItem decorator = (DecoratorItem) tuple;
                    expandingIconLayout.run(decorator, decorator.getDecoratedItem());
                }
            }
        });

        VisualTable molecularEventIcons = v.addDecorators(EDGE_MOLECULAREVENT_DECORATORS, EDGES, new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                return t.getString(FIELD_MOLECULAREVENT) != null;
            }
        }, (Schema) DEFAULT_EDGE_DECORATOR_SCHEMA.clone());
        DecoratorLabelRenderer molecularEventIconRenderer = new DecoratorLabelRenderer.Icon(molecularEventIcons, FIELD_MOLECULAREVENT);
        addMolecularEventIcons(molecularEventIconRenderer.getImageFactory());
        molecularEventIconRenderer.setVerticalAlignment(Constants.CENTER);
        molecularEventIconRenderer.setHorizontalPadding(0);
        molecularEventIconRenderer.setVerticalPadding(0);
//        molecularEventIconRenderer.setMaxImageDimensions(32, 32);
        rendererFactory.add(new InGroupPredicate(EDGE_MOLECULAREVENT_DECORATORS), molecularEventIconRenderer);
        getEdgeDecoratorLayouts().addDecoratorRendererLayout(molecularEventIconRenderer,
                new DecoratorLayout(molecularEventIcons) {

                    private final QuadCurve2D subCurve = new QuadCurve2D.Float();

                    private void subdivide(QuadCurve2D src, QuadCurve2D result, boolean left, int count) {
                        result.setCurve(src);
                        for (int i = 0; i < count; i++) {
                            if (left) {
                                result.subdivide(result, null);
                            } else {
                                result.subdivide(null, result);
                            }
                        }
                    }

                    @Override
                    public void run(DecoratorItem decorator, VisualItem item) {
                        double x, y;
                        Shape edgeShape = ((EdgeRenderer) item.getRenderer()).getShape(item);
                        if (edgeShape instanceof QuadCurve2D) {
                            QuadCurve2D edgeCurve = (QuadCurve2D) edgeShape;
                            subdivide(edgeCurve, subCurve, false, 3);
                            x = subCurve.getX1();
                            y = subCurve.getY1();
                        } else {
                            Rectangle2D edgeBounds = item.getBounds();
                            VisualItem dest = ((EdgeItem) item).getTargetItem();
                            x = (edgeBounds.getCenterX() + dest.getX()) / 2;
                            y = (edgeBounds.getCenterY() + dest.getY()) / 2;
                        }
                        setX(decorator, item, x);
                        setY(decorator, item, y);
                    }
                });

        VisualTable featureIcons = v.addDecorators(NODE_FEATURE_DECORATORS, NODES, new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                return t.getString(FIELD_FEATURE) != null;
            }
        }, (Schema) DEFAULT_NODE_DECORATOR_SCHEMA.clone());
        DecoratorLabelRenderer nodeFeatureRenderer = new DecoratorLabelRenderer.Icon(featureIcons, FIELD_FEATURE);
        addFeatureIcons(nodeFeatureRenderer.getImageFactory());
        nodeFeatureRenderer.setVerticalAlignment(Constants.CENTER);
        nodeFeatureRenderer.setHorizontalPadding(0);
        nodeFeatureRenderer.setVerticalPadding(0);
//        nodeFeatureRenderer.setMaxImageDimensions(32, 32);
        rendererFactory.add(new InGroupPredicate(NODE_FEATURE_DECORATORS), nodeFeatureRenderer);
        getNodeDecoratorLayouts().addDecoratorRendererLayout(nodeFeatureRenderer,
                new DecoratorLayout(featureIcons) {

                    @Override
                    public void run(DecoratorItem decorator, VisualItem item) {
                        Rectangle2D bounds = item.getBounds();
                        double x = bounds.getX();
                        double y = bounds.getY();
                        setX(decorator, item, x);
                        setY(decorator, item, y);
                    }
                });

        VisualTable locationIcons = v.addDecorators(NODE_LOCATION_DECORATORS, NODES, new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple n) {
                return n.getString(FIELD_LOCATION) != null;
            }
        }, (Schema) DEFAULT_NODE_DECORATOR_SCHEMA.clone());
        DecoratorLabelRenderer nodeLocationRenderer = new DecoratorLabelRenderer.Icon(locationIcons, FIELD_LOCATION, null) {

            private final Image locImage = new ImageIcon(PathwayDisplay.class.getResource("resources/marker.png")).getImage();

            @Override
            protected Image getImage(VisualItem item) {
                return locImage;
            }
        };
        nodeLocationRenderer.setVerticalAlignment(Constants.TOP);
        nodeLocationRenderer.setHorizontalPadding(0);
        nodeLocationRenderer.setVerticalPadding(0);
//        nodeLocationRenderer.setMaxImageDimensions(16, 16);
        rendererFactory.add(new InGroupPredicate(NODE_LOCATION_DECORATORS), nodeLocationRenderer);
        v.setVisible(NODE_LOCATION_DECORATORS, new NotPredicate(ExpressionParser.predicate(FIELD_LOCATIONCHANGED)), false);
        getNodeDecoratorLayouts().addDecoratorRendererLayout(nodeLocationRenderer,
                new DecoratorLayout(locationIcons) {

                    @Override
                    public void run(DecoratorItem decorator, VisualItem item) {
                        Rectangle2D bounds = item.getBounds();
                        double x = bounds.getMaxX() + (decorator.getBounds().getWidth() / 2);
                        double y = bounds.getY();
                        setX(decorator, item, x);
                        setY(decorator, item, y);
                    }
                });

        VisualTable familyIcons = v.addDecorators(NODE_FAMILY_DECORATORS, NODES, ExpressionParser.predicate(FIELD_ISFAMILY), (Schema) DEFAULT_NODE_DECORATOR_SCHEMA.clone());
        DecoratorLabelRenderer familyIconRenderer = new DecoratorLabelRenderer.Icon(familyIcons, null) {

            private final Image familyIcon = new ImageIcon(PathwayDisplay.class.getResource("resources/family_24.png")).getImage();

            @Override
            protected Image getImage(VisualItem item) {
                return familyIcon;
            }
        };
        familyIconRenderer.setVerticalAlignment(Constants.CENTER);
        familyIconRenderer.setHorizontalPadding(0);
        familyIconRenderer.setVerticalPadding(0);
//        familyIconRenderer.setMaxImageDimensions(24, 24);
        rendererFactory.add(new InGroupPredicate(NODE_FAMILY_DECORATORS), familyIconRenderer);
        getNodeDecoratorLayouts().addDecoratorRendererLayout(familyIconRenderer,
                new DecoratorLayout(familyIcons) {

                    @Override
                    public void run(DecoratorItem decorator, VisualItem item) {
                        Rectangle2D bounds = item.getBounds();
                        double x = bounds.getMaxX() - 4;
                        double y = bounds.getMaxY() - 4;
                        setX(decorator, item, x);
                        setY(decorator, item, y);
                    }
                });

        VisualTable dimerIcons = v.addDecorators(NODE_DIMER_DECORATORS, NODES, ExpressionParser.predicate(FIELD_ISDIMER), (Schema) DEFAULT_NODE_DECORATOR_SCHEMA.clone());
        DecoratorLabelRenderer dimerIconRenderer = new DecoratorLabelRenderer.Icon(dimerIcons, null) {

            private final Image dimerIcon = new ImageIcon(PathwayDisplay.class.getResource("resources/dimer_24.png")).getImage();

            @Override
            protected Image getImage(VisualItem item) {
                return dimerIcon;
            }
        };
        dimerIconRenderer.setVerticalAlignment(Constants.CENTER);
        dimerIconRenderer.setHorizontalPadding(0);
        dimerIconRenderer.setVerticalPadding(0);
//        dimerIconRenderer.setMaxImageDimensions(24, 24);
        rendererFactory.add(new InGroupPredicate(NODE_DIMER_DECORATORS), dimerIconRenderer);
        getNodeDecoratorLayouts().addDecoratorRendererLayout(dimerIconRenderer,
                new DecoratorLayout(dimerIcons) {

                    @Override
                    public void run(DecoratorItem decorator, VisualItem item) {
                        Rectangle2D bounds = item.getBounds();
                        double x = bounds.getX() - (decorator.getBounds().getWidth() / 4) - 2;
                        double y = bounds.getCenterY();
                        setX(decorator, item, x);
                        setY(decorator, item, y);
                    }
                });
    }
    private static final String NODE_EXPANDING_DECORATORS = "node_expanding_decorators";
    private static final String NODE_FEATURE_DECORATORS = "node_feature_decorators";
    private static final String NODE_LOCATION_DECORATORS = "node_location_decorators";
    private static final String NODE_FAMILY_DECORATORS = "node_family_decorators";
    private static final String NODE_DIMER_DECORATORS = "node_dimer_decorators";
    private static final String EDGE_MOLECULAREVENT_DECORATORS = "edge_molecularEvent_decorators";

    public void setLocationVisible(boolean locationVisible) {
        for (Iterator<DecoratorItem> decoratorIter = getVisualization().items(NODE_LOCATION_DECORATORS); decoratorIter.hasNext();) {
            DecoratorItem decorator = decoratorIter.next();
            PrefuseLib.updateVisible(decorator, locationVisible || decorator.getBoolean(FIELD_LOCATIONCHANGED));
        }
    }

    private void addMolecularEventIcons(ImageFactory imgFactory) {
        for (ControlType event : ControlType.getMolecularEvents()) {
            imgFactory.addImage(event.getSymbol(), new ImageIcon(PathwayDisplay.class.getResource("resources/bullet32w" + event.getSymbol() + ".png")).getImage());
        }
    }

    private void addFeatureIcons(ImageFactory imgFactory) {
        for (EntityFeature feature : EntityFeature.values()) {
            String featureName = feature.toString();
            imgFactory.addImage(featureName, new ImageIcon(PathwayDisplay.class.getResource("resources/bullet32b_" + featureName + ".png")).getImage());
        }
    }

    @Override
    protected void setupVisualization(Visualization v, DefaultRendererFactory renderFactory) {
        super.setupVisualization(v, renderFactory);

        AggregateTable aggregateTable = (AggregateTable) v.getVisualGroup(AGGR_ITEMS);
        aggregateTable.addColumn(FIELD_CORESPONDING_NODE, NodeItem.class, null);
        int _aggregateId = -1;

        Graph g = getGraph();
        Table nodeTable = g.getNodeTable();

        Set<Tuple> complexNodes = nodeTable.getTuples(FIELD_ITYPE, NodeType.COMPLEX.toString());
        for (Tuple complexNode : complexNodes) {
            String referenceId = complexNode.getString(FIELD_LOCALID);
            AggregateItem complexAggregate = aggregateTable.addItem(new Object[]{_aggregateId--, referenceId == null ? "" : referenceId});
            complexAggregate.set(FIELD_CORESPONDING_NODE, v.getVisualItem(NODES, complexNode));
            int[] suids = (int[]) complexNode.get(FIELD_SUBNODES);
            for (int suid : suids) {
                Tuple sub = nodeTable.getTuple(FIELD_UID, suid);
                if (sub == null) {
                    logger.log(Level.WARNING, "Can not find a node for the uid : {0}", suid);
                    continue;
                }
                complexAggregate.addItem(v.getVisualItem(NODES, sub));
            }
            PrefuseLib.updateVisible(complexAggregate, false);
        }

        Set<Tuple> superexNodes = nodeTable.getTuples(FIELD_ITYPE, NodeType.SUPEREX.toString());
        for (Tuple superexNode : superexNodes) {
            String referenceId = superexNode.getColumnIndex(FIELD_LOCALID) < 0 ? null : superexNode.getString(FIELD_LOCALID);
            AggregateItem superexAggregate = aggregateTable.addItem(new Object[]{_aggregateId--, referenceId == null ? "" : referenceId});
            superexAggregate.set(FIELD_CORESPONDING_NODE, v.getVisualItem(NODES, superexNode));
            int[] suids = (int[]) superexNode.get(FIELD_SUBNODES);
            for (int suid : suids) {
                Tuple sub = nodeTable.getTuple(FIELD_UID, suid);
                if (sub == null) {
                    logger.log(Level.WARNING, "Can not find a node for the uid : {0}", suid);
                    continue;
                }
                superexAggregate.addItem(v.getVisualItem(NODES, sub));
            }
            PrefuseLib.updateVisible(superexAggregate, false);
        }

        v.setValue(NODES, ExpressionParser.predicate(FIELD_ITYPE + " = '" + NodeType.CONTROLLIE + "'"), VisualItem.INTERACTIVE, false);
        v.setValue(EDGES, ExpressionParser.predicate(FIELD_ISVIRTUAL), VisualItem.INTERACTIVE, false);
        v.setValue(EDGES, ExpressionParser.predicate(g.getEdgeSourceField() + " = " + g.getEdgeTargetField()), VisualItem.SHAPE, Constants.SHAPE_ELLIPSE);
    }

    @Override
    protected DecoratorLabelRenderer makeupNodeLabelRenderer(DecoratorLabelRenderer r) {
        r = super.makeupNodeLabelRenderer(r);
        r.addInvisibility(FIELD_ITYPE + " = '" + NodeType.COMPLEX + "'");
        return r;
    }

    @Override
    protected Renderer createEdgeRenderer() {
        EdgeRenderer edgeR = new EdgeRenderer(true, true) {

            private Ellipse2D m_ellipse = new Ellipse2D.Float();
            private Rectangle2D m_intersect = new Rectangle2D.Float();
            private Arc2D m_arc = new Arc2D.Float();
            private final Polygon m_inhibitArrow = new Polygon();

            @Override
            protected Shape getRawShape(VisualItem item) {
                EdgeItem edge = (EdgeItem) item;
                VisualItem source = edge.getSourceItem();
                VisualItem target = edge.getTargetItem();
                if (source == target) {
                    m_curWidth = (float) Math.round(m_width * getLineWidth(item));
                    Rectangle2D itemBounds = source.getBounds();
                    getAlignedPoint(m_tmpPoints[0], itemBounds, m_xAlign1, m_yAlign1);
                    double thetaFix = 0.00D;
                    double eyFix = 0.00D;
                    switch (NodeType.valueOf(source.getString(FIELD_ITYPE))) {
                        case SUPEREX:
                            m_ellipse.setFrame(m_tmpPoints[0].getX() - itemBounds.getWidth() / 6, m_tmpPoints[0].getY() - itemBounds.getHeight() / 6, itemBounds.getWidth() * 2, itemBounds.getHeight() * 2);
                            thetaFix = 0.82D;
                            break;
                        case COMPLEX:
                            m_ellipse.setFrame(m_tmpPoints[0].getX() - itemBounds.getWidth() * 3 / 4, m_tmpPoints[0].getY() - itemBounds.getHeight() * 3 / 4, itemBounds.getWidth() * 5.5, itemBounds.getHeight() * 5.5);
                            thetaFix = 0.30D;
                            eyFix = 3;
                            break;
                        case MOLECULE:
                        case ENZYME:
                        case COMPOUND:
                        case BIOLOGICAL_PROCESS:
                            m_ellipse.setFrame(m_tmpPoints[0].getX(), m_tmpPoints[0].getY(), itemBounds.getWidth() * 3 / 2, itemBounds.getHeight() * 3 / 2);
                            thetaFix = 0.77D;
                            break;
                        default:
                            m_curArrow = null;
                            return null;
                    }
                    Rectangle2D.intersect(itemBounds, m_ellipse.getFrame(), m_intersect);
                    double sx = m_intersect.getMinX(), sy = m_intersect.getMaxY();
                    double ex = m_intersect.getMaxX(), ey = m_intersect.getMinY();
                    m_arc.setArc(m_ellipse.getBounds2D(), 0, 0, Arc2D.OPEN);
                    m_arc.setAngles(sx, sy, ex + getArrowHeadHeight(), ey);
                    AffineTransform selfArrowTrans = getSelfArrowTrans(sx, sy, ex, ey + eyFix, m_curWidth, thetaFix);
                    m_curArrow = selfArrowTrans.createTransformedShape(getArrowHead(edge));
                    return m_arc;
                }
                return super.getRawShape(item);
            }

            @Override
            protected boolean isDirected(EdgeItem e) {
                return e.getBoolean(FIELD_ISINCLUDE) && isIntegratedPathway() ? false : super.isDirected(e);
            }

            private AffineTransform getSelfArrowTrans(double sx, double sy, double ex, double ey, double width, double thetaFix) {
                m_arrowTrans.setToTranslation(ex, ey);

                m_arrowTrans.rotate(HALF_PI + thetaFix
                        + Math.atan2(ey - sy, ex - sx));
                if (width > 1) {
                    double scalar = width / 4;
                    m_arrowTrans.scale(scalar, scalar);
                }
                return m_arrowTrans;
            }

            @Override
            protected VisualItem getTargetItem(EdgeItem e, boolean forward) {
                VisualItem target = forward ? e.getTargetItem() : e.getSourceItem();
                if (NodeType.valueOf(target.getString(FIELD_ITYPE)) == NodeType.CONTROLLIE) {
                    EdgeItem controllieEdge = (EdgeItem) getVisualGraph().getEdgeTable().getTuple(target.getInt(FIELD_CONTROLLIE_EDGEROW));
                    if (controllieEdge.getSourceItem() == controllieEdge.getTargetItem()) {
                        return controllieEdge;
                    }
                }
                return super.getTargetItem(e, forward);
            }
            private final int INHIBIT_ARROW_GAP = 3;
            private int m_inhibitArrowWidth = getArrowHeadWidth();
            private int m_inhibitArrowHeight = m_inhibitArrowWidth / 2;

            @Override
            protected void setArrowHeadSize(int width, int height) {
                super.setArrowHeadSize(width, height);
                m_inhibitArrowWidth = width;
                m_inhibitArrowHeight = width / 2;
            }

            @Override
            protected Polygon getArrowHead(EdgeItem e) {
                ControlType type;
                if (e.getBoolean(FIELD_ISCONTROL) && (type = ControlType.fromName(e.getString(FIELD_CONTROLTYPE))) != null) {
                    switch (type) {
                        case INHIBITION:
                        case REPRESSION:
                            m_inhibitArrow.reset();
                            m_inhibitArrow.addPoint(0, -INHIBIT_ARROW_GAP);
                            m_inhibitArrow.addPoint(-m_inhibitArrowWidth, -INHIBIT_ARROW_GAP);
                            m_inhibitArrow.addPoint(-m_inhibitArrowWidth, -m_inhibitArrowHeight - INHIBIT_ARROW_GAP);
                            m_inhibitArrow.addPoint(m_inhibitArrowWidth, -m_inhibitArrowHeight - INHIBIT_ARROW_GAP);
                            m_inhibitArrow.addPoint(m_inhibitArrowWidth, -INHIBIT_ARROW_GAP);
                            m_inhibitArrow.addPoint(0, -INHIBIT_ARROW_GAP);
                            return m_inhibitArrow;
                        default:
                            break;
                    }
                }
                return super.getArrowHead(e);
            }

            @Override
            protected void adjustLineEndByArrowHead(EdgeItem e, Point2D lineEnd) {
                ControlType type;
                if (e.getBoolean(FIELD_ISCONTROL) && (type = ControlType.fromName(e.getString(FIELD_CONTROLTYPE))) != null) {
                    switch (type) {
                        case INHIBITION:
                        case REPRESSION:
                            lineEnd.setLocation(0, -INHIBIT_ARROW_GAP - m_inhibitArrowHeight);
                            return;
                        default:
                            break;
                    }
                }
                super.adjustLineEndByArrowHead(e, lineEnd);
            }
        };
//        edgeR.setLineForSingleEdge(false);

        return edgeR;
    }

    @Override
    protected void addDrawActions(ActionList draw) {
        super.addDrawActions(draw);
        FontAction nodeFont = new FontAction(NODES);
        nodeFont.add(FIELD_ITYPE + " = '" + NodeType.COMPOUND + "'", FontLib.getFont("SansSerif", Font.PLAIN, 14));
        draw.add(nodeFont);
    }

    @Override
    protected void addNodeColorActions(ActionList draw) {
        String[] ordinalMaps = NodeType.names();
        draw.add(new NodeDataColorAction(FIELD_ITYPE, VisualItem.STROKECOLOR, ordinalMaps, NodeType.paletteOf(VisualItem.STROKECOLOR)));
        draw.add(new NodeDataColorAction(FIELD_ITYPE, VisualItem.FILLCOLOR, ordinalMaps, NodeType.paletteOf(VisualItem.FILLCOLOR)));
        draw.add(new NodeDataColorAction(FIELD_ITYPE, VisualItem.TEXTCOLOR, ordinalMaps, NodeType.paletteOf(VisualItem.TEXTCOLOR)));
    }

    @Override
    protected void addAggregateColorActions(ActionList draw) {
        super.addDefaultAggregateColorActions(draw);
    }

    @Override
    protected void addColorMappingRules() {
        super.addColorMappingRules();
        MOLECULES_INCOMPLEX = new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                if (NodeType.valueOf(t.getString(FIELD_ITYPE)) != NodeType.MOLECULE) {
                    return false;
                }
                Iterator<AggregateItem> aggrIter = ((AggregateTable) getVisualization().getVisualGroup(AGGR_ITEMS)).getAggregates(t);
                while (aggrIter.hasNext()) {
                    if (COMPLEXES_INAGGREGATES.getBoolean(aggrIter.next())) {
                        return true;
                    }
                }
                return false;
            }
        };
        ColorAction nodeStroke = getColorAction(NODES, VisualItem.STROKECOLOR);
        if (nodeStroke != null) {
            nodeStroke.add(MOLECULES_INCOMPLEX, ColorLib.setAlpha(ColorLib.color(Color.blue), 60));
        }
        ColorAction nodeFill = getColorAction(NODES, VisualItem.FILLCOLOR);
        if (nodeFill != null) {
            nodeFill.add(MOLECULES_INCOMPLEX, ColorLib.setAlpha(ColorLib.rgb(130, 130, 255), 40));
        }
        IS_ACTIVATION_CONTROL = new IsControlPredicate(ControlType.ACTIVATION.getName());
        IS_INHIBITION_CONTROL = new IsControlPredicate(ControlType.INHIBITION.getName());
        ColorAction edgeStroke = getColorAction(EDGES, VisualItem.STROKECOLOR);
        if (edgeStroke != null) {
            edgeStroke.add(new AbstractPredicate() {

                @Override
                public boolean getBoolean(Tuple t) {
                    return isIntegratedPathway() && t.getString(FIELD_DATABASE) != null;
                }
            }, new EdgeDataColorAction(FIELD_DATABASE, VisualItem.STROKECOLOR, PathwayDatabase.Lookup.names(), PATHWAYDB_EDGECOLORS));
            edgeStroke.add(IS_ACTIVATION_CONTROL, ControlType.ACTIVATION.getColor());
            edgeStroke.add(IS_INHIBITION_CONTROL, ControlType.INHIBITION.getColor());
        }
        ColorAction edgeFill = getColorAction(EDGES, VisualItem.FILLCOLOR);
        if (edgeFill != null) {
            edgeFill.add(new AbstractPredicate() {

                @Override
                public boolean getBoolean(Tuple t) {
                    return isIntegratedPathway() && t.getString(FIELD_DATABASE) != null;
                }
            }, new EdgeDataColorAction(FIELD_DATABASE, VisualItem.FILLCOLOR, PathwayDatabase.Lookup.names(), PATHWAYDB_EDGECOLORS));
            edgeFill.add(IS_ACTIVATION_CONTROL, ControlType.ACTIVATION.getColor());
            edgeFill.add(IS_INHIBITION_CONTROL, ControlType.INHIBITION.getColor());
        }
    }

    private final class IsControlPredicate extends AbstractPredicate {

        private final String controlType;

        public IsControlPredicate(String controlType) {
            this.controlType = controlType;
        }

        @Override
        public boolean getBoolean(Tuple t) {
            EdgeItem e = (EdgeItem) t;
//            if (!e.getBoolean(FIELD_ISCONTROL) || e.isAggregating() || e.getBoolean(FIELD_ISINCLUDE) || e.getBoolean(FIELD_ISVIRTUAL)) {
//                return false;
//            }
//            if (!t.getString(FIELD_DATABASE).equals("KEGG")
//                    && NodeType.valueOf(e.getTargetItem().getString(FIELD_ITYPE)) != NodeType.CONTROLLIE) {
//                return false;
//            }
            return e.getBoolean(FIELD_ISCONTROL) && e.getString(FIELD_CONTROLTYPE).equalsIgnoreCase(controlType);
        }
    }

    @Override
    protected SizeAction addNodeSizeAction(ActionList draw, String nodeSizeField) {
        SizeAction sizer = new SizeAction(NODES);
        sizer.add(FIELD_ITYPE + " = '" + NodeType.BIOLOGICAL_PROCESS + "'", 1.1D);
        sizer.add(FIELD_ITYPE + " = '" + NodeType.ENZYME + "'", 0.8D);
        sizer.add(FIELD_ITYPE + " = '" + NodeType.COMPOUND + "'", 0.8D);
        sizer.add(FIELD_ITYPE + " = '" + NodeType.SUPEREX + "'", 0.5D);
        sizer.add(FIELD_ITYPE + " = '" + NodeType.COMPLEX + "'", 0.5D);
        sizer.add(FIELD_ITYPE + " = '" + NodeType.CONTROLLIE + "'", 0.0D);
        draw.add(sizer);
        return sizer;
    }

    @Override
    protected StrokeAction addNodeStrokeAction(ActionList draw) {
        return addNodeStrokeDefaultAction(draw);
    }

    @Override
    protected StrokeAction addEdgeStrokeAction(ActionList draw) {
        StrokeAction edgeStroke = super.addEdgeStrokeAction(draw);
        if (edgeStroke == null) {
            edgeStroke = new StrokeAction(EDGES);
            draw.add(edgeStroke);
        }
//        edgeStroke.add(FIELD_ISINCLUDE, new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10F, new float[]{4F}, 0F));
        edgeStroke.add(FIELD_ISINCLUDE, EdgeStroke.DASH_SHORT.getStroke());
        return edgeStroke;
    }

    @Override
    protected void addEdgeColorActions(ActionList draw) {
        ColorAction edgeStroke = new ColorAction(EDGES, VisualItem.STROKECOLOR, getEdgeStrokeColor());
//        edgeStroke.add(FIELD_ISVIRTUAL, ColorLib.color(Color.red));
        edgeStroke.add(FIELD_ISVIRTUAL, COLOR_TRANSPARENT);
        draw.add(edgeStroke);
        ColorAction edgeFill = new ColorAction(EDGES, VisualItem.FILLCOLOR, getEdgeFillColor());
//        edgeFill.add(FIELD_ISVIRTUAL, ColorLib.color(Color.red));
        edgeFill.add(FIELD_ISVIRTUAL, COLOR_TRANSPARENT);
        draw.add(edgeFill);
    }

    @Override
    protected int getEdgeFillColor() {
        return ColorLib.gray(140);
    }

    @Override
    protected int getEdgeStrokeColor() {
        return getEdgeFillColor();
    }

    @Override
    public boolean isEdgeInteractive() {
        return true;
    }

    @Override
    protected Layout createLayout() {
        ForceSimulator forceSimulator = new ForceSimulator(new RungeKuttaIntegrator());
        forceSimulator.addForce(new NBodyForce(
                NBodyForce.DEFAULT_GRAV_CONSTANT,
                NBodyForce.DEFAULT_MAX_DISTANCE,
                NBodyForce.DEFAULT_THETA));
        forceSimulator.addForce(new DragForce(DragForce.DEFAULT_DRAG_COEFF));
        forceSimulator.addForce(new SpringForce(
                SpringForce.DEFAULT_SPRING_COEFF,
                SpringForce.DEFAULT_SPRING_LENGTH * 3));
        return new PartialForceDirectedLayout(forceSimulator, false) {

            @Override
            protected float getSpringLength(EdgeItem e) {
                if (e.getBoolean(FIELD_ISINCLUDE)) {
                    return SpringForce.DEFAULT_SPRING_LENGTH;
                } else if (e.getBoolean(FIELD_ISVIRTUAL)) {
                    return SpringForce.DEFAULT_MAX_SPRING_LENGTH - SpringForce.DEFAULT_SPRING_LENGTH;
                } else {
                    return SpringForce.DEFAULT_MAX_SPRING_LENGTH;
                }
            }

            @Override
            protected float getMassValue(VisualItem n) {
                return 4.0F;
            }

            @Override
            protected float getSpringCoefficient(EdgeItem e) {
                return -1.0F;
            }
        };
//        RadialTreeLayout l = new RadialTreeLayout(GRAPH, 150) {
//
//
//            @Override
//            protected boolean isEnabled(NodeItem n) {
//                return n.getString(FIELD_NODEID) != null;
//            }
//
//        };
//        l.setAutoScale(false);
//        return l;
//        return new NodeLinkTreeLayout(GRAPH, Constants.ORIENT_TOP_BOTTOM, 10, 10, 10);
    }

    @Override
    protected long getLayoutDuration() {
        return 10000;
    }

    @Override
    protected ShapeAction addNodeShapeAction(ActionList draw) {
        ShapeAction nodeShape = super.addNodeShapeAction(draw);
        if (nodeShape == null) {
            nodeShape = new ShapeAction(NODES);
            draw.add(nodeShape);
        }
        for (NodeType itype : NodeType.values()) {
            nodeShape.add(FIELD_ITYPE + " == '" + itype + "'", itype.getShape());
        }
        return nodeShape;
    }

    @Override
    protected void addLayoutActions(ActionList layout) {
        super.addLayoutActions(layout);
        complexLayout = new ComplexLayout(getVisualization(), getNodeSizeBase());
        final CascadedTable complexTable = complexLayout.createComplexTable();
        final TableListener complexTableListener = complexLayout.createComplexTableListener();
        complexTable.addTableListener(complexTableListener);
        layout.add(complexLayout);
        controllieLayout = new ControllieLayout(getVisualization(), FIELD_ITYPE + " = '" + NodeType.CONTROLLIE + "'");
        final CascadedTable controllieTable = controllieLayout.createControllieTable();
        final TableListener controllieTableListener = controllieLayout.createControllieTableListener();
        controllieTable.addTableListener(controllieTableListener);
        layout.add(controllieLayout);
        layout.addActivityListener(new ActivityAdapter() {

            @Override
            public void activityCancelled(Activity a) {
                activityFinished(a);
            }

            @Override
            public void activityFinished(Activity a) {
                complexTable.addTableListener(complexTableListener);
                controllieTable.addTableListener(controllieTableListener);
            }

            @Override
            public void activityStarted(Activity a) {
                complexTable.removeTableListener(complexTableListener);
                controllieTable.removeTableListener(controllieTableListener);
            }
        });
    }
    private ComplexLayout complexLayout;
    private ControllieLayout controllieLayout;

    @Override
    protected void unregisterActions() {
        getLayoutAction().remove(complexLayout);
        complexLayout.destroy();
        getLayoutAction().remove(controllieLayout);
        controllieLayout.destroy();
        super.unregisterActions();
    }

    @Override
    protected List<Control> addControls(Visualization v) {
        return super.addControls(v);
//        addControlListener(new PathwayPopupControl(this));
    }

    @Override
    protected Control createDragControl() {
        return new AggregateDragControl(new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                if (t instanceof NodeItem) {
                    return t.getBoolean(FIELD_INLAYOUT);
                }
                return true;
            }
        });
    }

    @Override
    protected VisualGraph addGraph(Visualization v, Graph g) {
        g.addGraphModelListener(this);
        return super.addGraph(v, g);
    }

    @Override
    protected DataEditSupport createNodeDataEditSupport(Table table) {
        return new DataEditSupport(table) {

            @Override
            public boolean isEditable(String field) {
                return false;
            }

            @Override
            public Class getColumnType(String field) {
                return field.equals(FIELD_SUBNODES) ? String[].class : super.getColumnType(field);
            }

            @Override
            public Object getValueAt(Tuple data, String field) {
                return field.equals(FIELD_SUBNODES)
                        ? Utilities.getEntitiesAsStringArrayFromUIDs(getGraph(), (int[]) data.get(field)) : super.getValueAt(data, field);
            }

            //TODO implement when pathway editing is enabled
            @Override
            public <T> T setValueAt(Tuple data, String field, T val) {
                return super.setValueAt(data, field, val);
            }
        };
    }

    @Override
    protected DataEditSupport createEdgeDataEditSupport(Table table) {
        return new DataEditSupport(table) {

            @Override
            public boolean isEditable(String field) {
                return false;
            }
        };
    }

    public void removeNodes(final NodeItem... nodeItems) {
        getVisualization().rerun(new Runnable() {

            @Override
            public void run() {
                for (final NodeItem nitem : nodeItems) {
                    // TODO: remove try...
                    try {
                        Node n = (Node) nitem.getSourceTuple();
                        getGraph().removeNode(n, true);
                    } catch (IllegalArgumentException ex) {
                    }
                }
            }
        }, DRAW);
    }

    private void removingNode(final NodeItem nitem) {
        final AggregateTable aggregates = (AggregateTable) getVisualization().getVisualGroup(AGGR_ITEMS);
        String itype = nitem.getString(FIELD_ITYPE);
        Set<AggregateItem> aggregatesToRemove = new HashSet<AggregateItem>();
        Set<Node> nodesToRemove = new HashSet<Node>();
        switch (NodeType.valueOf(itype)) {
            case COMPLEX:
                AggregateItem complexAggrItem = null;
                for (Iterator<AggregateItem> complexAggrItems = aggregates.tuples(
                        new AbstractPredicate() {

                            @Override
                            public boolean getBoolean(Tuple aggrItem) {
                                NodeItem complex = (NodeItem) aggrItem.get(FIELD_CORESPONDING_NODE);
                                return complex != null && complex == nitem;
                            }
                        }); complexAggrItems.hasNext();) {
                    complexAggrItem = complexAggrItems.next();
                }
                if (complexAggrItem != null) {
                    for (Iterator<NodeItem> subIterInComplex = complexAggrItem.items(); subIterInComplex.hasNext();) {
                        nodesToRemove.add((Node) subIterInComplex.next().getSourceTuple());
                    }
                    complexAggrItem.removeAllItems();
                    aggregatesToRemove.add(complexAggrItem);
                }
                break;
            case SUPEREX:
                AggregateItem superexAggrItem = null;
                for (Iterator<AggregateItem> superexAggrItems = aggregates.tuples(
                        new AbstractPredicate() {

                            @Override
                            public boolean getBoolean(Tuple aggrItem) {
                                NodeItem superex = (NodeItem) aggrItem.get(FIELD_CORESPONDING_NODE);
                                return superex != null && superex == nitem;
                            }
                        }); superexAggrItems.hasNext();) {
                    superexAggrItem = superexAggrItems.next();
                }
                if (superexAggrItem != null) {
                    superexAggrItem.removeAllItems();
                    aggregatesToRemove.add(superexAggrItem);
                }
                break;
            case MOLECULE:
            case COMPOUND:
                for (Iterator<AggregateItem> aggrIter = ((AggregateTable) getVisualization().getVisualGroup(AGGR_ITEMS)).getAggregates(nitem);
                        aggrIter.hasNext();) {
                    AggregateItem aggrItem = aggrIter.next();
                    NodeItem superItem = (NodeItem) aggrItem.get(FIELD_CORESPONDING_NODE);
                    if (superItem != null) {
                        aggrItem.removeItem(nitem);
                        //
                        // TODO: change sub uids of superItem.getSourceTuple()
                        //
                        if (aggrItem.getAggregateSize() == 0) {
                            nodesToRemove.add((Node) superItem.getSourceTuple());
                        }
                    }
                }
                break;
            case CONTROLLIE:
                break;
            default:
                break;
        }
        for (Node n : nodesToRemove) {
            getGraph().removeNode(n, true);
        }
        for (AggregateItem a : aggregatesToRemove) {
            aggregates.removeTuple(a);
        }
    }

    @Override
    public void graphChanged(Graph g, String table, int start, int end, int col, int type) {
        if (col != EventConstants.ALL_COLUMNS) {
            return;
        }
        if (table.equals(Graph.EDGES)) {
            for (int row = start; row < end + 1; row++) {
                switch (type) {
                    case EventConstants.DELETE:
                        Node controllie = getGraph().getNodeFrom(FIELD_CONTROLLIE_EDGEROW, row);
                        if (controllie != null) {
                            Edge edge = getGraph().getEdge(row);
                            for (Edge control : getGraph().getEdgesFrom(FIELD_CONTROLLIE_EDGEROW, row)) {
                                Node controller = control.getAdjacentNode(controllie);
                                for (Edge e : getGraph().getEdges(controller, edge.getSourceNode())) {
                                    if (e.getBoolean(FIELD_ISVIRTUAL)) {
                                        getGraph().removeEdge(e);
                                    }
                                }
                                for (Edge e : getGraph().getEdges(controller, edge.getTargetNode())) {
                                    if (e.getBoolean(FIELD_ISVIRTUAL)) {
                                        getGraph().removeEdge(e);
                                    }
                                }
                            }
                            getGraph().removeNode(controllie, true);
                        }
                        break;
                    default:
                        break;
                }
            }
        } else if (table.equals(Graph.NODES)) {
            for (int row = start; row < end + 1; row++) {
                switch (type) {
                    case EventConstants.DELETE:
                        removingNode((NodeItem) getVisualGraph().getNodeTable().getTuple(row));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class ComplexLayout extends Layout {

        private final double nodeSizeBase;
        private final Map<NodeItem, AggregateItem> complexAggregates = Collections.synchronizedMap(new HashMap<NodeItem, AggregateItem>());
        private final List<CascadedTable> complexTables = new ArrayList<CascadedTable>();
        private final TupleSetListener aggregatesListener;

        ComplexLayout(Visualization v, int nodeSizeBase) {
            super(AGGR_ITEMS);
            setVisualization(v);
            this.nodeSizeBase = nodeSizeBase;
            initComplexAggregates();
            ((AggregateTable) getVisualization().getVisualGroup(m_group)).addTupleSetListener(
                    aggregatesListener = new TupleSetListener() {

                @Override
                public void tupleSetChanged(TupleSet tupleSet, Tuple[] added, Tuple[] removed) {
                    boolean refiltering = false;
                    for (Tuple aggregate : added) {
                        if (COMPLEXES_INAGGREGATES.getBoolean(aggregate)) {
                            complexAggregates.put((NodeItem) aggregate.get(FIELD_CORESPONDING_NODE), (AggregateItem) aggregate);
                            refiltering = true;
                        }
                    }
                    for (Tuple aggregate : removed) {
                        if (COMPLEXES_INAGGREGATES.getBoolean(aggregate)) {
                            complexAggregates.remove((NodeItem) aggregate.get(FIELD_CORESPONDING_NODE));
                            refiltering = true;
                        }
                    }
                    if (refiltering) {
                        for (CascadedTable complexTable : complexTables) {
                            complexTable.filterRows();
                        }
                    }
                }
            });
        }

        private void initComplexAggregates() {
            complexAggregates.clear();
            for (Iterator<AggregateItem> complexAggregateIter =
                    ((AggregateTable) getVisualization().getVisualGroup(m_group)).tuples(COMPLEXES_INAGGREGATES);
                    complexAggregateIter.hasNext();) {
                AggregateItem complexAggregate = complexAggregateIter.next();
                complexAggregates.put((NodeItem) complexAggregate.get(FIELD_CORESPONDING_NODE), complexAggregate);
            }
        }

        void destroy() {
            ((AggregateTable) getVisualization().getVisualGroup(m_group)).removeTupleSetListener(aggregatesListener);
            for (CascadedTable complexTable : complexTables) {
                complexTable.removeAllTableListeners();
            }
            complexTables.clear();
            cancel();
            setVisualization(null);
            complexAggregates.clear();
        }

        /**
         * @see prefuse.action.Action#run(double)
         */
        @Override
        public void run(double frac) {
            Set<NodeItem> complexes = complexAggregates.keySet();
            synchronized (complexAggregates) {
                for (Iterator<NodeItem> complexIter = complexes.iterator(); complexIter.hasNext();) {
                    NodeItem complex = complexIter.next();
                    AggregateItem complexAggregate = complexAggregates.get(complex);
                    Rectangle2D bounds = complex.getBounds();
                    circularizeComplex(complexAggregate.items(), complexAggregate.getAggregateSize(), bounds.getCenterX(), bounds.getCenterY());
                }
            }
        }

        CascadedTable createComplexTable() {
            CascadedTable cascaded = new CascadedTable(getVisualGraph().getNodeTable(),
                    new AbstractPredicate() {

                        @Override
                        public boolean getBoolean(Tuple t) {
                            return complexAggregates.containsKey((NodeItem) t);
                        }
                    }, new NamedColumnProjection(new String[]{VisualItem.X, VisualItem.Y, VisualItem.SIZE, VisualItem.BOUNDS}, true));
            complexTables.add(cascaded);
            return cascaded;
        }

        TableListener createComplexTableListener() {
            return new TableListener() {

                @Override
                public void tableChanged(Table table, int start, int end, int col, int type) {
                    if (type == EventConstants.UPDATE && col != EventConstants.ALL_COLUMNS) {
                        VisualTable nodes = (VisualTable) ((CascadedTable) table).getParentTable();
                        String column = table.getColumnName(col);
                        for (int r = start; r <= end; ++r) {
                            NodeItem complex = (NodeItem) nodes.getItem(((CascadedTable) table).getParentRow(r));
                            if (column.equals(VisualItem.X) || column.equals(VisualItem.Y)) {
                                for (Iterator<NodeItem> molecules = complexAggregates.get(complex).items(); molecules.hasNext();) {
                                    molecules.next().setValidated(false);
                                }
                            } else if (column.equals(VisualItem.SIZE)) {
                                for (Iterator<NodeItem> molecules = complexAggregates.get(complex).items(); molecules.hasNext();) {
                                    NodeItem molecule = molecules.next();
                                    molecule.setValidated(false);
                                    molecule.validateBounds();
                                }
                            } else if (column.equals(VisualItem.BOUNDS)) {
                                AggregateItem complexAggregate = complexAggregates.get(complex);
                                Rectangle2D bounds = complex.getBounds();
                                circularizeComplex(complexAggregate.items(), complexAggregate.getAggregateSize(), bounds.getCenterX(), bounds.getCenterY());
                            }
                        }
                    }
                }
            };
        }

        private double circularizeComplex(Iterator<NodeItem> molecules, int count, double cx, double cy) {
            double radius = getRadius(count);
            for (int i = 0; molecules.hasNext(); i++) {
                VisualItem molecule = molecules.next();
                double angle = (2 * Math.PI * i) / count + 300;
                double x = Math.cos(angle) * radius + cx;
                double y = Math.sin(angle) * radius + cy;
                setX(molecule, null, x);
                setY(molecule, null, y);
            }
            return radius;
        }

        private double getRadius(int count) {
            return getRadius(count, false);
        }

        private double getRadius(int count, boolean sparse) {
            double radius;
            if (count == 0) {
                radius = 0;
            } else if (count < 4) {
                radius = nodeSizeBase * 3 / 4;
            } else {
                radius = nodeSizeBase / 2 + (count - 2) * nodeSizeBase / (sparse ? 4 : 8);
            }
            return radius;
        }
    }

    protected class ControllieLayout extends Layout {

        private final Predicate isControllie;
        private final Map<EdgeItem, List<NodeItem>> controlliesByEdge = Collections.synchronizedMap(new HashMap<EdgeItem, List<NodeItem>>());
        private final List<CascadedTable> controllieTables = new ArrayList<CascadedTable>();
        private final TupleSetListener nodesListener;

        public ControllieLayout(Visualization v, Predicate isControllie) {
            super();
            setVisualization(v);
            this.isControllie = isControllie;
            initControlliesByEdge();
            ((VisualTable) getVisualization().getVisualGroup(NODES)).addTupleSetListener(
                    nodesListener = new TupleSetListener() {

                @Override
                public void tupleSetChanged(TupleSet tupleSet, Tuple[] added, Tuple[] removed) {
                    boolean refiltering = false;
                    for (Tuple node : added) {
                        if (node.getString(FIELD_ITYPE).equals(NodeType.CONTROLLIE.toString())) {
                            NodeItem controllie = (NodeItem) node;
                            EdgeItem controllieEdge = (EdgeItem) getVisualGraph().getEdgeTable().getTuple(controllie.getInt(FIELD_CONTROLLIE_EDGEROW));
                            List<NodeItem> controllies = controlliesByEdge.get(controllieEdge);
                            if (controllies == null) {
                                controllies = new ArrayList<NodeItem>();
                                controlliesByEdge.put(controllieEdge, controllies);
                                refiltering = true;
                            }
                            controllies.add(controllie);
                        }
                    }
                    for (Tuple node : removed) {
                        if (node.getString(FIELD_ITYPE).equals(NodeType.CONTROLLIE.toString())) {
                            NodeItem controllie = (NodeItem) node;
                            Set<EdgeItem> controllieEdges = controlliesByEdge.keySet();
                            synchronized (controlliesByEdge) {
                                for (Iterator<EdgeItem> controllieEdgeIter = controllieEdges.iterator(); controllieEdgeIter.hasNext();) {
                                    EdgeItem controllieEdge = controllieEdgeIter.next();
                                    List<NodeItem> controllies = controlliesByEdge.get(controllieEdge);
                                    if (controllies.remove(controllie) && controllies.isEmpty()) {
                                        controllieEdgeIter.remove();
                                        refiltering = true;
                                    }
                                }
                            }
                        }
                    }
                    if (refiltering) {
                        for (CascadedTable controllieTable : controllieTables) {
                            controllieTable.filterRows();
                        }
                    }
                }
            });
        }

        private ControllieLayout(Visualization v, String groupExpr) {
            this(v, ExpressionParser.predicate(groupExpr));
        }

        private void initControlliesByEdge() {
            Set<EdgeItem> controllieEdges = controlliesByEdge.keySet();
            synchronized (controlliesByEdge) {
                for (Iterator<EdgeItem> controllieEdgeIter = controllieEdges.iterator(); controllieEdgeIter.hasNext();) {
                    controlliesByEdge.get(controllieEdgeIter.next()).clear();
                }
            }
            controlliesByEdge.clear();
            for (Iterator<NodeItem> controllieIter = getVisualization().items(NODES, isControllie); controllieIter.hasNext();) {
                NodeItem controllie = controllieIter.next();
                EdgeItem controllieEdge = (EdgeItem) getVisualGraph().getEdgeTable().getTuple(controllie.getInt(FIELD_CONTROLLIE_EDGEROW));
                List<NodeItem> controllies = controlliesByEdge.get(controllieEdge);
                if (controllies == null) {
                    controllies = new ArrayList<NodeItem>();
                    controlliesByEdge.put(controllieEdge, controllies);
                }
                controllies.add(controllie);
            }
        }

        void destroy() {
            VisualTable nodes = (VisualTable) getVisualization().getVisualGroup(NODES);
            if (nodes != null) {
                nodes.removeTupleSetListener(nodesListener);
            } else {
                // Visualization had been reset via NetworkDisplay.setLoading(true, true)
            }
            for (CascadedTable controllieTable : controllieTables) {
                controllieTable.removeAllTableListeners();
            }
            controllieTables.clear();
            cancel();
            setVisualization(null);
            Set<EdgeItem> controllieEdges = controlliesByEdge.keySet();
            synchronized (controlliesByEdge) {
                for (Iterator<EdgeItem> controllieEdgeIter = controllieEdges.iterator(); controllieEdgeIter.hasNext();) {
                    controlliesByEdge.get(controllieEdgeIter.next()).clear();
                }
            }
            controlliesByEdge.clear();
        }

        CascadedTable createControllieTable() {
            CascadedTable cascaded = new CascadedTable(getVisualGraph().getEdgeTable(),
                    new AbstractPredicate() {

                        @Override
                        public boolean getBoolean(Tuple t) {
                            return controlliesByEdge.containsKey((EdgeItem) t);
                        }
                    }, new NamedColumnProjection(new String[]{VisualItem.X, VisualItem.Y, VisualItem.SIZE, VisualItem.BOUNDS}, true));
            controllieTables.add(cascaded);
            return cascaded;
        }

        TableListener createControllieTableListener() {
            return new TableListener() {

                @Override
                public void tableChanged(Table table, int start, int end, int col, int type) {
                    if (type == EventConstants.UPDATE && col != EventConstants.ALL_COLUMNS) {
                        VisualTable edges = (VisualTable) ((CascadedTable) table).getParentTable();
                        String column = table.getColumnName(col);
                        for (int r = start; r <= end; ++r) {
                            EdgeItem controllieEdge = (EdgeItem) edges.getItem(((CascadedTable) table).getParentRow(r));
                            if (column.equals(VisualItem.X) || column.equals(VisualItem.Y)) {
                                for (NodeItem controllie : controlliesByEdge.get(controllieEdge)) {
                                    controllie.setValidated(false);
                                }
                            } else if (column.equals(VisualItem.SIZE)) {
                                for (NodeItem controllie : controlliesByEdge.get(controllieEdge)) {
                                    controllie.setValidated(false);
                                    controllie.validateBounds();
                                }
                            } else if (column.equals(VisualItem.BOUNDS)) {
                                for (NodeItem controllie : controlliesByEdge.get(controllieEdge)) {
                                    layout(controllie, controllieEdge);
                                }
                            }
                        }
                    }
                }
            };
        }

        @Override
        public void run(double frac) {
            Set<EdgeItem> controllieEdges = controlliesByEdge.keySet();
            synchronized (controlliesByEdge) {
                for (EdgeItem controllieEdge : controllieEdges) {
                    for (NodeItem controllie : controlliesByEdge.get(controllieEdge)) {
                        layout(controllie, controllieEdge);
                    }
                }
            }
        }

        private void layout(NodeItem controllie, EdgeItem controllieEdge) {
            Shape edgeShape = ((EdgeRenderer) controllieEdge.getRenderer()).getShape(controllieEdge);
            double x, y;
            if (edgeShape instanceof QuadCurve2D) {
                QuadCurve2D edgeCurve = (QuadCurve2D) edgeShape;
                x = edgeCurve.getCtrlX();
                y = edgeCurve.getCtrlY();
                double x1 = edgeCurve.getX1();
                double y1 = edgeCurve.getY1();
                double x2 = edgeCurve.getX2();
                double y2 = edgeCurve.getY2();
                double ctrlx1 = (x1 + x) / 2.0;
                double ctrly1 = (y1 + y) / 2.0;
                double ctrlx2 = (x2 + x) / 2.0;
                double ctrly2 = (y2 + y) / 2.0;
                x = (ctrlx1 + ctrlx2) / 2.0;
                y = (ctrly1 + ctrly2) / 2.0;
            } else {
                Rectangle2D bounds = controllieEdge.getBounds();
                x = bounds.getCenterX();
                y = bounds.getCenterY();
            }
            setX(controllie, controllieEdge, x);
            setY(controllie, controllieEdge, y);
        }
    }

    protected class PartialForceDirectedLayout extends ForceDirectedLayout {

        public PartialForceDirectedLayout() {
            this(false);
        }

        public PartialForceDirectedLayout(boolean enforceBounds) {
            super(GRAPH, enforceBounds);
        }

        public PartialForceDirectedLayout(ForceSimulator fsim) {
            this(fsim, false);
        }

        public PartialForceDirectedLayout(ForceSimulator fsim, boolean enforceBounds) {
            super(GRAPH, fsim, enforceBounds);
        }

        @Override
        protected boolean isEnabled(VisualItem item) {
            if (item instanceof NodeItem) {
                return item.getBoolean(FIELD_INLAYOUT);
            } else {
                EdgeItem e = (EdgeItem) item;
                return e.getSourceItem().getBoolean(FIELD_INLAYOUT) && e.getTargetItem().getBoolean(FIELD_INLAYOUT);
//                return true;
            }
        }
    }

    private class AlternativeColumnExpression extends ColumnExpression {

        private Object value = null;

        public AlternativeColumnExpression(String field) {
            super(field);
        }

        public Predicate setValue(Object value) {
            this.value = value;
            return this;
        }

        @Override
        public boolean getBoolean(Tuple t) {
            return t.get(m_field).equals(value);
        }
    }

    @Override
    protected DataViewSupport createEdgeDataViewSupport(Table table) {
        return new DataViewSupport(table,
                new AbstractPredicate() {

                    @Override
                    public boolean getBoolean(Tuple t) {
                        return !t.getBoolean(FIELD_ISVIRTUAL) && !t.getBoolean(FIELD_ISINCLUDE);
                    }
                }) {

            @Override
            public Schema getOutlineSchema() {
                return isIntegratedPathway() ? EDGEOUTLINE_FOR_ENTITYLEVEL : EDGEOUTLINE_FOR_PATHWAYLEVEL;
            }

            @Override
            public String getColumnTitle(String field) {
                return Config.getEdgeOutlineColumnName(field);
            }

            @Override
            public String getStringAt(Tuple edge, String field) {
                ControlType controlType = null;
                if (field.equals(FIELD_MOLECULAREVENT) && (controlType = ControlType.fromSymbol(edge.getString(field))) != null) {
                    return controlType.getName();
                }
                return super.getStringAt(edge, field);
            }
        };
    }

    @Override
    protected DataViewSupport createNodeDataViewSupport(Table table) {
        return new DataViewSupport(table,
                new AbstractPredicate() {

                    @Override
                    public boolean getBoolean(Tuple t) {
                        return !t.getString(FIELD_ITYPE).equals(NodeType.CONTROLLIE.toString());
                    }
                }) {

            @Override
            public Schema getOutlineSchema() {
                return isIntegratedPathway() ? NODEOUTLINE_FOR_ENTITYLEVEL : NODEOUTLINE_FOR_PATHWAYLEVEL;
            }

            @Override
            public Schema getPropertySchema() {
                return isIntegratedPathway() ? NODEPROPERTIES_FOR_ENTITYLEVEL : NODEPROPERTIES_FOR_PATHWAYLEVEL;
            }

            @Override
            public Schema getTooltipSchema() {
                return getPropertySchema();
            }

            @Override
            public String getColumnTitle(String field) {
                return Config.getNodeOutlineColumnName(field);
            }

            @Override
            public String getStringAt(Tuple node, String field) {
                int[] suids;
                if (field.equals(FIELD_SUBNODES) && (suids = (int[]) node.get(field)) != null) {
                    return Utilities.getEntitiesAsStringFromUIDs(getGraph(), suids);
                }
                return super.getStringAt(node, field);
            }
        };
    }
}
