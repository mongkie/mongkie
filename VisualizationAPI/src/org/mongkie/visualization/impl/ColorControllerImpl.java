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
package org.mongkie.visualization.impl;

import java.awt.Color;
import java.util.Iterator;
import org.mongkie.visualization.Config;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualization.color.ColorModel;
import org.mongkie.visualization.color.ColorProvider;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import static prefuse.Visualization.DRAW;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ColorController.class)
public class ColorControllerImpl implements ColorController {

    private ColorModelImpl model;

    public ColorControllerImpl() {
        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void displaySelected(MongkieDisplay display) {
                model = display.getLookup().lookup(ColorModelImpl.class);
                if (model == null) {
                    model = new ColorModelImpl(display);
                    display.add(model);
                }
            }

            @Override
            public void displayDeselected(MongkieDisplay display) {
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                model = null;
            }
        });
        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            model = d.getLookup().lookup(ColorModelImpl.class);
            if (model == null) {
                model = new ColorModelImpl(d);
                d.add(model);
            }
        }
    }

    @Override
    public Color getTextColor(VisualItem item) {
        if (item instanceof NodeItem) {
            return model.getNodeColorProvider().getTextColor((NodeItem) item);
        } else if (item instanceof EdgeItem) {
            return model.getEdgeColorProvider().getTextColor((EdgeItem) item);
        } else if (item instanceof AggregateItem) {
            return model.getGroupColorProvider().getTextColor(item);
        }
        return null;
    }

    @Override
    public Color setTextColor(VisualItem item, Color c) {
        if (item instanceof NodeItem) {
            model.getNodeColorProvider().addTextColor((NodeItem) item, c);
        } else if (item instanceof EdgeItem) {
            model.getEdgeColorProvider().addTextColor((EdgeItem) item, c);
        } else if (item instanceof AggregateItem) {
            AggregateItem aggrItem = (AggregateItem) item;
            model.getGroupColorProvider().addTextColor(aggrItem, c);
//            for (Iterator<NodeItem> nodeItemIter = aggrItem.items(); nodeItemIter.hasNext();) {
//                NodeItem nodeItem = nodeItemIter.next();
//                model.getGroupColorProvider().addTextColor(nodeItem, c);
//                PrefuseLib.update(nodeItem, VisualItem.TEXTCOLOR, ColorLib.color(c));
//            }
        }
        item.getVisualization().rerun(DRAW);
        //TODO: already redraw?
        return setColor(item, VisualItem.TEXTCOLOR, c);
    }

    @Override
    public Color getStrokeColor(VisualItem item) {
        if (item instanceof NodeItem) {
            return model.getNodeColorProvider().getStrokeColor((NodeItem) item);
        } else if (item instanceof EdgeItem) {
            return model.getEdgeColorProvider().getStrokeColor((EdgeItem) item);
        } else if (item instanceof AggregateItem) {
            return model.getGroupColorProvider().getStrokeColor(item);
        }
        return null;
    }

    @Override
    public Color setStrokeColor(VisualItem item, Color c) {
        if (item instanceof NodeItem) {
            model.getNodeColorProvider().addStrokeColor((NodeItem) item, c);
        } else if (item instanceof EdgeItem) {
            model.getEdgeColorProvider().addStrokeColor((EdgeItem) item, c);
        } else if (item instanceof AggregateItem) {
            AggregateItem aggrItem = (AggregateItem) item;
            model.getGroupColorProvider().addStrokeColor(aggrItem, c);
//            for (Iterator<NodeItem> nodeItemIter = aggrItem.items(); nodeItemIter.hasNext();) {
//                NodeItem nodeItem = nodeItemIter.next();
//                model.getGroupColorProvider().addStrokeColor(nodeItem, c);
//                PrefuseLib.update(nodeItem, VisualItem.STROKECOLOR, ColorLib.color(c));
//            }
        }
        item.getVisualization().rerun(DRAW);
        return setColor(item, VisualItem.STROKECOLOR, c);
    }

    @Override
    public Color getFillColor(VisualItem item) {
        if (item instanceof NodeItem) {
            return model.getNodeColorProvider().getFillColor((NodeItem) item);
        } else if (item instanceof EdgeItem) {
            return model.getEdgeColorProvider().getFillColor((EdgeItem) item);
        } else if (item instanceof AggregateItem) {
            return model.getGroupColorProvider().getFillColor(item);
        }
        return null;
    }

    @Override
    public Color setFillColor(VisualItem item, Color c) {
        if (item instanceof NodeItem) {
            model.getNodeColorProvider().addFillColor((NodeItem) item, c);
        } else if (item instanceof EdgeItem) {
            model.getEdgeColorProvider().addFillColor((EdgeItem) item, c);
        } else if (item instanceof AggregateItem) {
            AggregateItem aggrItem = (AggregateItem) item;
            model.getGroupColorProvider().addFillColor(aggrItem, c);
            setStrokeColorForNodesIn(aggrItem, c);
            c = ColorLib.getColor(c.getRed(), c.getGreen(), c.getBlue(), Config.COLOR_AGGRFILL_ALPHA);
        }
        item.getVisualization().rerun(DRAW);
        return setColor(item, VisualItem.FILLCOLOR, c);
    }

    private void setStrokeColorForNodesIn(AggregateItem group, Color c) {
        for (Iterator<NodeItem> nodeItemIter = group.items(); nodeItemIter.hasNext();) {
            NodeItem nodeItem = nodeItemIter.next();
            Color ic = getInterpolatedColor(nodeItem, c);
            model.getGroupColorProvider().addStrokeColor(nodeItem, ic);
            PrefuseLib.update(nodeItem, VisualItem.STROKECOLOR, ColorLib.color(ic));
        }
    }

    private Color getInterpolatedColor(NodeItem nodeItem, Color nc) {
        return nc;
        //TODO: nodes in multiple groups
//        Color oc = model.getGroupColorProvider().getStrokeColor(nodeItem);
//        if (oc == null) {
//            return nc;
//        }
//        return ColorLib.getColor(ColorLib.interp(oc.getRGB(), nc.getRGB(), 0.5f));
    }

    private Color setColor(VisualItem item, String field, Color c) {
        Color old = ColorLib.getColor(item.getTextColor());
        PrefuseLib.update(item, field, ColorLib.color(c));
        return old;
    }

    @Override
    public Color unsetTextColor(VisualItem item) {
        Color c = null;
        if (item instanceof NodeItem) {
            c = model.getNodeColorProvider().removeTextColor((NodeItem) item);
            PrefuseLib.update(item, VisualItem.TEXTCOLOR, ColorLib.color(c));
        } else if (item instanceof EdgeItem) {
            c = model.getEdgeColorProvider().removeTextColor((EdgeItem) item);
            PrefuseLib.update(item, VisualItem.TEXTCOLOR, ColorLib.color(c));
        } else if (item instanceof AggregateItem) {
            AggregateItem aggrItem = (AggregateItem) item;
            c = model.getGroupColorProvider().removeTextColor(aggrItem);
            PrefuseLib.update(item, VisualItem.TEXTCOLOR, ColorLib.color(c));
            for (Iterator<NodeItem> nodeItemIter = aggrItem.items(); nodeItemIter.hasNext();) {
                NodeItem nodeItem = nodeItemIter.next();
                model.getGroupColorProvider().removeTextColor(nodeItem);
                PrefuseLib.update(nodeItem, VisualItem.TEXTCOLOR, ColorLib.color(model.getNodeColorProvider().getTextColor(nodeItem)));
            }
        }
        item.getVisualization().rerun(DRAW);
        return c;
    }

    @Override
    public Color unsetStrokeColor(VisualItem item) {
        Color c = null;
        if (item instanceof NodeItem) {
            c = model.getNodeColorProvider().removeStrokeColor((NodeItem) item);
            PrefuseLib.update(item, VisualItem.STROKECOLOR, ColorLib.color(c));
        } else if (item instanceof EdgeItem) {
            c = model.getEdgeColorProvider().removeStrokeColor((EdgeItem) item);
            PrefuseLib.update(item, VisualItem.STROKECOLOR, ColorLib.color(c));
        } else if (item instanceof AggregateItem) {
            AggregateItem aggrItem = (AggregateItem) item;
            c = model.getGroupColorProvider().removeStrokeColor(aggrItem);
            PrefuseLib.update(item, VisualItem.STROKECOLOR, ColorLib.color(c));
            for (Iterator<NodeItem> nodeItemIter = aggrItem.items(); nodeItemIter.hasNext();) {
                NodeItem nodeItem = nodeItemIter.next();
                model.getGroupColorProvider().removeStrokeColor(nodeItem);
                PrefuseLib.update(nodeItem, VisualItem.STROKECOLOR, ColorLib.color(model.getNodeColorProvider().getStrokeColor(nodeItem)));
            }
        }
        item.getVisualization().rerun(DRAW);
        return c;
    }

    @Override
    public Color unsetFillColor(VisualItem item) {
        Color c = null;
        if (item instanceof NodeItem) {
            c = model.getNodeColorProvider().removeFillColor((NodeItem) item);
            PrefuseLib.update(item, VisualItem.FILLCOLOR, ColorLib.color(c));
        } else if (item instanceof EdgeItem) {
            c = model.getEdgeColorProvider().removeFillColor((EdgeItem) item);
            PrefuseLib.update(item, VisualItem.FILLCOLOR, ColorLib.color(c));
        } else if (item instanceof AggregateItem) {
            AggregateItem aggrItem = (AggregateItem) item;
            c = model.getGroupColorProvider().removeFillColor(aggrItem);
            PrefuseLib.update(aggrItem, VisualItem.FILLCOLOR, ColorLib.rgba(c.getRed(), c.getGreen(), c.getBlue(), Config.COLOR_AGGRFILL_ALPHA));
            for (Iterator<NodeItem> nodeItemIter = aggrItem.items(); nodeItemIter.hasNext();) {
                NodeItem nodeItem = nodeItemIter.next();
                model.getGroupColorProvider().removeStrokeColor(nodeItem);
                PrefuseLib.update(nodeItem, VisualItem.STROKECOLOR, ColorLib.color(model.getNodeColorProvider().getStrokeColor(nodeItem)));
            }
        }
        item.getVisualization().rerun(DRAW);
        return c;
    }

    @Override
    public ColorModel getModel() {
        return model;
    }

    @Override
    public synchronized ColorModel getModel(MongkieDisplay d) {
        ColorModelImpl m = d.getLookup().lookup(ColorModelImpl.class);
        if (m == null) {
            m = new ColorModelImpl(d);
            d.add(m);
        }
        return m;
    }

    @Override
    public void setNodeColorProvider(ColorProvider<NodeItem> ncp) {
        if (model != null) {
            model.setNodeColorProvider(ncp);
            model.getDisplay().getVisualization().rerun(DRAW);
        }
    }

    @Override
    public void unsetNodeColorProvider(ColorProvider<NodeItem> ncp) {
        if (model != null) {
            model.unsetNodeColorProvider(ncp);
            model.getDisplay().getVisualization().rerun(DRAW);
        }
    }

    @Override
    public void setEdgeColorProvider(ColorProvider<EdgeItem> ecp) {
        if (model != null) {
            model.setEdgeColorProvider(ecp);
            model.getDisplay().getVisualization().rerun(DRAW);
        }
    }

    @Override
    public void unsetEdgeColorProvider(ColorProvider<EdgeItem> ecp) {
        if (model != null) {
            model.unsetEdgeColorizer(ecp);
            model.getDisplay().getVisualization().rerun(DRAW);
        }
    }
}
