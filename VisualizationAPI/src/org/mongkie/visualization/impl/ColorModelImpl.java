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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import static kobic.prefuse.Config.*;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.color.ColorModel;
import org.mongkie.visualization.color.ColorProvider;
import org.mongkie.visualization.color.EdgeColorProvider;
import org.mongkie.visualization.color.NodeColorProvider;
import prefuse.Visualization;
import prefuse.util.ColorLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ColorModelImpl implements ColorModel {

    private final MongkieDisplay d;
    private final List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
    private final List<ColorProvider<NodeItem>> nodeColorizerChain = new ArrayList<ColorProvider<NodeItem>>();
    private final List<ColorProvider<EdgeItem>> edgeColorizerChain = new ArrayList<ColorProvider<EdgeItem>>();
    private static final ColorProvider<NodeItem> DEFAULT_NODECOLORPROVIDER =
            new NodeColorProvider() {

                @Override
                protected Color getItemTextColor(NodeItem item) {
                    return ColorLib.getColor(COLOR_DEFAULT_NODE_TEXT);
                }

                @Override
                protected Color getItemStrokeColor(NodeItem item) {
                    return ColorLib.getColor(COLOR_DEFAULT_NODE_STROKE);
                }

                @Override
                protected Color getItemFillColor(NodeItem item) {
                    return ColorLib.getColor(COLOR_DEFAULT_NODE_FILL);
                }
            };
    private static final ColorProvider<EdgeItem> DEFAULT_EDGECOLORPROVIDER =
            new EdgeColorProvider() {

                @Override
                protected Color getItemTextColor(EdgeItem item) {
                    return ColorLib.getColor(COLOR_DEFAULT_EDGE_TEXT);
                }

                @Override
                protected Color getItemStrokeColor(EdgeItem item) {
                    return ColorLib.getColor(COLOR_DEFAULT_EDGE_STROKE);
                }

                @Override
                protected Color getItemFillColor(EdgeItem item) {
                    return ColorLib.getColor(COLOR_DEFAULT_EDGE_FILL);
                }
            };

    public ColorModelImpl(MongkieDisplay d) {
        this.d = d;
        nodeColorizerChain.add(DEFAULT_NODECOLORPROVIDER);
        edgeColorizerChain.add(DEFAULT_EDGECOLORPROVIDER);
    }

    @Override
    public ColorProvider<NodeItem> getNodeColorProvider() {
        return nodeColorizerChain.get(0);
    }

    @Override
    public ColorProvider<EdgeItem> getEdgeColorProvider() {
        return edgeColorizerChain.get(0);
    }

    @Override
    public ColorProvider<VisualItem> getGroupColorProvider() {
        return ColorProvider.ForGroup.getInstance();
    }

    void setNodeColorProvider(ColorProvider<NodeItem> ncp) {
        ColorProvider<NodeItem> parent = nodeColorizerChain.get(0);
        if (ncp == parent) {
            return;
        }
        unsetNodeColorProvider(ncp, false);
        ncp.setParent(parent);
        nodeColorizerChain.add(0, ncp);
        d.getVisualization().rerun(Visualization.DRAW);
        firePropertyChangeEvent(NODE_COLORPROVIDER, parent, ncp);
    }

    void unsetNodeColorProvider(ColorProvider<NodeItem> ncp) {
        unsetNodeColorProvider(ncp, true);
    }

    private void unsetNodeColorProvider(ColorProvider<NodeItem> ncp, boolean fire) {
        if (!nodeColorizerChain.contains(ncp) || ncp == DEFAULT_NODECOLORPROVIDER) {
            return;
        }
        int chainIdx = nodeColorizerChain.indexOf(ncp);
        nodeColorizerChain.remove(chainIdx);
        ColorProvider<NodeItem> parent = nodeColorizerChain.get(chainIdx--);
        ColorProvider<NodeItem> child = chainIdx < 0 ? null : nodeColorizerChain.get(chainIdx);
        if (child != null) {
            child.setParent(parent);
        } else if (fire) {
            d.getVisualization().rerun(Visualization.DRAW);
            firePropertyChangeEvent(NODE_COLORPROVIDER, ncp, parent);
        }
    }

    void setEdgeColorProvider(ColorProvider<EdgeItem> ecp) {
        ColorProvider<EdgeItem> parent = edgeColorizerChain.get(0);
        if (ecp == parent) {
            return;
        }
        unsetEdgeColorizer(ecp, false);
        ecp.setParent(parent);
        edgeColorizerChain.add(0, ecp);
        d.getVisualization().rerun(Visualization.DRAW);
        firePropertyChangeEvent(EDGE_COLORPROVIDER, parent, ecp);
    }

    void unsetEdgeColorizer(ColorProvider<EdgeItem> ecp) {
        unsetEdgeColorizer(ecp, true);
    }

    private void unsetEdgeColorizer(ColorProvider<EdgeItem> ecp, boolean fire) {
        if (!edgeColorizerChain.contains(ecp) || ecp == DEFAULT_EDGECOLORPROVIDER) {
            return;
        }
        int chainIdx = edgeColorizerChain.indexOf(ecp);
        edgeColorizerChain.remove(chainIdx);
        ColorProvider<EdgeItem> parent = edgeColorizerChain.get(chainIdx--);
        ColorProvider<EdgeItem> child = chainIdx < 0 ? null : edgeColorizerChain.get(chainIdx);
        if (child != null) {
            child.setParent(parent);
        } else if (fire) {
            d.getVisualization().rerun(Visualization.DRAW);
            firePropertyChangeEvent(EDGE_COLORPROVIDER, ecp, parent);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(propertyChangeEvent);
        }
    }

    public MongkieDisplay getDisplay() {
        return d;
    }
}
