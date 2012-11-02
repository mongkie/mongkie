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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.im.QueryEvent;
import org.mongkie.im.QueryEvent.Type;
import org.mongkie.im.SourceModel;
import org.mongkie.im.SourceModelListener;
import org.mongkie.im.spi.Interaction;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.data.Edge;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class SourceModelImpl implements SourceModel {

    private final InteractionSource is;
    private final LongTaskExecutor link, expand;
    private final List<SourceModelListener> listeners = new ArrayList<SourceModelListener>();
    private transient boolean linked = false, annotated = false;
    private final MongkieDisplay d;
    private String key;

    SourceModelImpl(MongkieDisplay d, final InteractionSource is) {
        this.is = is;
        this.d = d;
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
        if (key.equals(this.key)) {
            return false;
        }
        this.key = key;
        return true;
    }

    @Override
    public String getKeyField() {
        return key;
    }

    boolean addInteraction(Interaction i, Edge e) {
        List<Edge> edges = interactionsMap.get(i);
        if (edges == null) {
            edges = new ArrayList<Edge>();
            interactionsMap.put(i, edges);
        }
        return edges.add(e);
    }

    List<Edge> removeInteraction(Interaction i) {
        return interactionsMap.remove(i);
    }

    List<Edge> clearInteractions() {
        List<Edge> allEdges = new ArrayList<Edge>();
        for (List<Edge> edges : interactionsMap.values()) {
            allEdges.addAll(edges);
            edges.clear();
        }
        interactionsMap.clear();
        return allEdges;
    }

    Set<Interaction> getInteractions() {
        return interactionsMap.keySet();
    }
    private final Map<Interaction, List<Edge>> interactionsMap = new HashMap<Interaction, List<Edge>>();
}
