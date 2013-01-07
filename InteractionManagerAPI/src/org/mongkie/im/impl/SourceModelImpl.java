/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.im.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.im.QueryEvent;
import org.mongkie.im.QueryEvent.Type;
import org.mongkie.im.SourceModel;
import org.mongkie.im.SourceModelListener;
import static org.mongkie.im.impl.InteractionControllerImpl.FIELD_INTERACTION_SOURCE;
import org.mongkie.im.spi.Interaction;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.VisualStyle;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.util.DataLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class SourceModelImpl implements SourceModel, DisplayListener<MongkieDisplay> {

    private final InteractionSource is;
    private final LongTaskExecutor link, expand;
    private final List<SourceModelListener> listeners = new ArrayList<SourceModelListener>();
    private transient boolean linked = false, annotated = false;
    private final MongkieDisplay d;
    private String key;
    private final VisualStyle<NodeItem> nodeVisualStyle;
    private final VisualStyle<EdgeItem> edgeVisualStyle;

    SourceModelImpl(MongkieDisplay d, final InteractionSource is) {
        this.is = is;
        this.d = d;
        nodeVisualStyle = VisualStyle.createNodeStyle();
        edgeVisualStyle = VisualStyle.createEdgeStyle();
        d.addDisplayListener(SourceModelImpl.this);
        link = new LongTaskExecutor(true, is.getName() + " Link");
        link.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskStarted(LongTask task) {
                for (SourceModelListener l : listeners) {
                    l.processQueryEvent(new QueryEvent(Type.LINK_STARTED));
                }
            }

            @Override
            public void taskFinished(LongTask task) {
                for (SourceModelListener l : listeners) {
                    l.processQueryEvent(new QueryEvent(Type.LINK_FINISHED));
                }
            }
        });
        link.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        });
        expand = new LongTaskExecutor(true, is.getName() + " Expand");
        expand.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskStarted(LongTask task) {
                for (SourceModelListener l : listeners) {
                    l.processQueryEvent(new QueryEvent(Type.EXPAND_STARTED));
                }
            }

            @Override
            public void taskFinished(LongTask task) {
                for (SourceModelListener l : listeners) {
                    l.processQueryEvent(new QueryEvent(Type.EXPAND_FINISHED));
                }
            }
        });
        expand.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        });
    }

    VisualStyle<NodeItem> getNodeVisualStyle() {
        return nodeVisualStyle;
    }

    VisualStyle<EdgeItem> getEdgeVisualStyle() {
        return edgeVisualStyle;
    }

    @Override
    public InteractionSource getInteractionSource() {
        return is;
    }

    @Override
    public MongkieDisplay getDisplay() {
        return d;
    }

    void destroy() {
        if (isLinking()) {
            link.cancel();
        }
        listeners.clear();
        d.removeDisplayListener(this);
    }

    LongTaskExecutor getLinkExecutor() {
        return link;
    }

    LongTaskExecutor getExpandExecutor() {
        return expand;
    }

    @Override
    public boolean isLinking() {
        return link.isRunning();
    }

    @Override
    public boolean isExpanding() {
        return expand.isRunning();
    }

    @Override
    public boolean isLinked() {
        return linked;
    }

    @Override
    public boolean isPartiallyLinked() {
        return !linked
                && d.getGraph().getEdgeTable().getColumn(FIELD_INTERACTION_SOURCE) != null
                && DataLib.get(d.getGraph().getEdgeTable(), FIELD_INTERACTION_SOURCE, is.getName()) >= 0;
    }

    void fireUnlinkedEvent() {
        setLinked(false);
        setAnnotated(false);
        for (SourceModelListener l : listeners) {
            l.processQueryEvent(new QueryEvent(Type.UNLINKED));
        }
    }

    @Override
    public boolean isAnnotated() {
        return annotated;
    }

    void setLinked(boolean linked) {
        this.linked = linked;
    }

    void setAnnotated(boolean annotated) {
        this.annotated = annotated;
    }

    @Override
    public boolean addModelListener(SourceModelListener l) {
        return !listeners.contains(l) && listeners.add(l);
    }

    @Override
    public boolean removeModelListener(SourceModelListener l) {
        return listeners.remove(l);
    }

    boolean setKeyField(String key) {
        if (key != null && key.equals(this.key)) {
            return false;
        }
        this.key = key;
        return true;
    }

    @Override
    public String getKeyField() {
        return key;
    }

    void addInteraction(Interaction i, Edge e) {
        Set<Edge> edges = interaction2Edges.get(i);
        if (edges == null) {
            edges = new HashSet<Edge>();
            interaction2Edges.put(i, edges);
        }
        edges.add(e);
        edge2Interaction.put(e, i);
    }

    Set<Edge> removeInteraction(Interaction i) {
        Set<Edge> edges = interaction2Edges.remove(i);
        if (edges != null) {
            for (Edge e : edges) {
                edge2Interaction.remove(e);
            }
        }
        return edges;
    }

    Set<Edge> clearInteractions() {
        Set<Edge> edges = new HashSet<Edge>();
        for (Set<Edge> _edges : interaction2Edges.values()) {
            edges.addAll(_edges);
            _edges.clear();
        }
        interaction2Edges.clear();
        edge2Interaction.clear();
        return edges;
    }

    Set<Interaction> getInteractions() {
        return interaction2Edges.keySet();
    }

    Set<Edge> getEdges(Interaction i) {
        return interaction2Edges.get(i);
    }

    Set<Edge> getEdges() {
        return edge2Interaction.keySet();
    }

    Interaction getInteraction(Edge e) {
        return edge2Interaction.get(e);
    }
    private final Map<Interaction, Set<Edge>> interaction2Edges = new HashMap<Interaction, Set<Edge>>();
    private final Map<Edge, Interaction> edge2Interaction = new HashMap<Edge, Interaction>();

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        setLinked(false);
        setAnnotated(false);
        setKeyField(null);
        clearInteractions();
        for (SourceModelListener l : listeners) {
            l.graphDisposing(g);
        }
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        for (SourceModelListener l : listeners) {
            l.graphChanged(g);
        }
    }
}
