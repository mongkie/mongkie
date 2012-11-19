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
package org.mongkie.importer.plugins.processor;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.importer.GraphContainer;
import org.mongkie.importer.VizGraphContainer;
import org.mongkie.importer.spi.Processor;
import org.mongkie.visualization.DisplayTopComponent;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationControllerUI;
import org.mongkie.visualization.color.ColorController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import static prefuse.Visualization.*;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = Processor.class, position = 100)
public class DefaultProcessor implements Processor<GraphContainer> {

    private MongkieDisplay display;
    private GraphContainer container;

    @Override
    public void process() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (container instanceof VizGraphContainer) {
                    processVisualGraph((VizGraphContainer) container);
                } else {
                    DisplayTopComponent tc =
                            Lookup.getDefault().lookup(VisualizationControllerUI.class).openNewDisplayTopComponent(
                            container.getSource(), container.getGraph());
                    setDisplay(tc.getDisplay());
                }
            }
        });
    }

    protected final void processVisualGraph(final VizGraphContainer vgc) {
        VisualizationControllerUI vizUI = Lookup.getDefault().lookup(VisualizationControllerUI.class);
        final DisplayTopComponent tc = vizUI.openEmptyDisplayTopComponent(vgc.getSource(), true);
        vizUI.invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                final MongkieDisplay d = tc.getDisplay();
                setDisplay(d);
                d.getVisualization().cancel(LAYOUT);
                d.setLoading(false);
                d.resetGraph(vgc.getGraph(), new DisplayListener() {
                    @Override
                    public void graphDisposing(NetworkDisplay d, Graph g) {
                    }

                    @Override
                    public void graphChanged(NetworkDisplay d, Graph g) {
                        restoreVisualProperties(d.getVisualGraph().getNodeTable().tuples(), vgc.getNodeVisualProperties());
                        restoreVisualProperties(d.getVisualGraph().getEdgeTable().tuples(), vgc.getEdgeVisualProperties());
                        Map<Integer, List<Integer>> aggrId2NodeRows = vgc.getAggregateId2NodeRows();
                        Table aggrVizProperties = vgc.getAggregateVisualProperties();
                        DefaultTupleSet nodeItems = new DefaultTupleSet();
                        for (Iterator<Tuple> propsIter = aggrVizProperties.tuples(); propsIter.hasNext();) {
                            Tuple prop = propsIter.next();
                            for (int nrow : aggrId2NodeRows.get(prop.getInt(AggregateItem.AGGR_ID))) {
                                NodeItem nitem = (NodeItem) d.getVisualGraph().getNode(nrow);
                                nodeItems.addTuple(nitem);
                                Lookup.getDefault().lookup(ColorController.class).unsetStrokeColor(nitem);
                            }
                            AggregateItem aggregate = d.aggregateItems(nodeItems, true, prop.getString(AggregateItem.AGGR_NAME), new String[]{});
                            for (int col = 0; col < prop.getColumnCount(); col++) {
                                String field = prop.getColumnName(col);
//                                        if (field.equals(AggregateItem.AGGR_ID)) {
//                                            continue;
//                                        }
                                Object val = prop.get(field);
                                aggregate.set(field, val);
                                restoreColorProperties(field, aggregate, val);
                            }
                        }
                    }

                    private <I extends VisualItem> void restoreVisualProperties(Iterator<I> items, Table properties) {
                        while (items.hasNext()) {
                            I item = items.next();
                            for (int col = 0; col < properties.getColumnCount(); col++) {
                                String field = properties.getColumnName(col);
                                Object val = properties.get(item.getRow(), field);
                                item.set(field, val);
                                restoreColorProperties(field, item, val);
                            }
                        }
                    }

                    private void restoreColorProperties(String field, VisualItem item, Object val) {
                        if (field.equals(VisualItem.FILLCOLOR)) {
                            Lookup.getDefault().lookup(ColorController.class).setFillColor(item, ColorLib.getColor((Integer) val));
                        } else if (field.equals(VisualItem.STROKECOLOR)) {
                            Lookup.getDefault().lookup(ColorController.class).setStrokeColor(item, ColorLib.getColor((Integer) val));
                        } else if (field.equals(VisualItem.TEXTCOLOR)) {
                            Lookup.getDefault().lookup(ColorController.class).setTextColor(item, ColorLib.getColor((Integer) val));
                        }
                    }
                }, DRAW, ANIMATE);
            }
        });
    }

    @Override
    public void setContainer(GraphContainer container) {
        this.container = container;
    }

    @Override
    public void setDisplay(MongkieDisplay display) {
        this.display = display;
    }

    @Override
    public MongkieDisplay getDisplay() {
        return display;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(DefaultProcessor.class, "DefaultProcessor.displayName");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
