/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.filter.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static kobic.prefuse.Constants.*;
import org.mongkie.filter.FilterModel;
import org.mongkie.filter.FilterModelListener;
import org.mongkie.filter.spi.Filter;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.filter.VisibilityFilter;
import prefuse.activity.Activity;
import prefuse.activity.ActivityAdapter;
import prefuse.data.Tuple;
import prefuse.data.event.ExpressionListener;
import prefuse.data.expression.CompositePredicate;
import prefuse.data.expression.Expression;
import prefuse.data.expression.Predicate;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.expression.VisiblePredicate;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
final class FilterModelImpl implements FilterModel, ExpressionListener {

    private static final String FILTER = "filter";
    private final MongkieDisplay display;
    private final ActionList filter;
    private final VisiblePredicates nodeVisiblePredicates;
    private final VisiblePredicates edgeVisiblePredicates;
    protected final List<FilterModelListener> listeners = Collections.synchronizedList(new ArrayList<FilterModelListener>());

    FilterModelImpl(MongkieDisplay display) {
        this.display = display;
        filter = new ActionList(display.getVisualization());
        Visualization v = display.getVisualization();
        filter.add(new VisibilityFilter(v, EDGES, edgeVisiblePredicates = new VisiblePredicates()));
        edgeVisiblePredicates.addExpressionListener(FilterModelImpl.this);
        filter.add(new VisibilityFilter(v, NODES, nodeVisiblePredicates = new VisiblePredicates()) {
            @Override
            public void run(double frac) {
                super.run(frac);
                // All edges connected to invisbile nodes also must be invisible
                for (Iterator<NodeItem> invisibleNodes = m_vis.items(m_group, VisiblePredicate.FALSE); invisibleNodes.hasNext();) {
                    NodeItem n = invisibleNodes.next();
                    for (Iterator<EdgeItem> edges = n.edges(); edges.hasNext();) {
                        EdgeItem e = edges.next();
                        PrefuseLib.updateVisible(e, false);
                    }
                }
            }
        });
        nodeVisiblePredicates.addExpressionListener(FilterModelImpl.this);
        filter.add(new RepaintAction(v));
        filter.addActivityListener(new ActivityAdapter() {
            @Override
            public void activityFinished(Activity a) {
                fireFiltersApplied();
            }
        });
        putFilterAction();
    }

    void putFilterAction() {
        display.getNodeDataViewSupport().addFilter(nodeVisiblePredicates);
        display.getEdgeDataViewSupport().addFilter(edgeVisiblePredicates);
        display.getVisualization().putAction(FILTER, filter);
    }

    void clearFilterAction() {
        nodeVisiblePredicates.clear();
        display.getNodeDataViewSupport().removeFilter(nodeVisiblePredicates);
        edgeVisiblePredicates.clear();
        display.getEdgeDataViewSupport().removeFilter(edgeVisiblePredicates);
        display.getVisualization().removeAction(FILTER);
    }

    VisiblePredicates getNodeVisiblePredicates() {
        return nodeVisiblePredicates;
    }

    VisiblePredicates getEdgeVisiblePredicates() {
        return edgeVisiblePredicates;
    }

    @Override
    public void expressionChanged(Expression expr) {
        //TODO accumulative event processing?
        if (display.getVisualization().getAction(FILTER) != null) {
            display.getVisualization().rerun(FILTER);
        }
    }

    @Override
    public void addModelListener(FilterModelListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelListener(FilterModelListener l) {
        listeners.remove(l);
    }

    private void fireFiltersApplied() {
        synchronized (listeners) {
            for (Iterator<FilterModelListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                listenerIter.next().fitersApplied(
                        new HashSet<Filter>(nodeVisiblePredicates.filters.values()),
                        new HashSet<Filter>(edgeVisiblePredicates.filters.values()));
            }
        }
        System.out.println("fireFiltersApplied");
    }

    @Override
    public MongkieDisplay getDisplay() {
        return display;
    }

    @Override
    public Predicate getNodeVisiblePredicate() {
        return nodeVisiblePredicates;
    }

    @Override
    public Predicate getEdgeVisiblePredicate() {
        return edgeVisiblePredicates;
    }

    @Override
    public Filter getNodeFilter(String name) {
        return nodeVisiblePredicates.getFilter(name);
    }

    @Override
    public Filter getEdgeFilter(String name) {
        return edgeVisiblePredicates.getFilter(name);
    }

    @Override
    public Filter getFilter(String group, String name) {
        if (NODES.equals(group)) {
            return getNodeFilter(name);
        } else if (EDGES.equals(group)) {
            return getEdgeFilter(name);
        }
        throw new IllegalArgumentException("Unknown data group: " + group);
    }

    static class VisiblePredicates extends CompositePredicate {

        private final Map<String, Filter> filters = new HashMap<String, Filter>();
        private Connective connective = Connective.AND;

        Connective getConnective() {
            return connective;
        }

        void setConnective(Connective connective) {
            this.connective = connective;
        }

        @Override
        public boolean getBoolean(Tuple t) {
            if (size() == 0) {
                return true;
            }
            switch (connective) {
                case AND:
                    for (Predicate p : m_clauses) {
                        if (!p.getBoolean(t)) {
                            return false;
                        }
                    }
                    return true;
                case OR:
                    for (Predicate p : m_clauses) {
                        if (p.getBoolean(t)) {
                            return true;
                        }
                    }
                    return false;
                default:
                    throw new UnsupportedOperationException("Not supported operation yet: " + connective.name());
            }

        }

        Filter addFilter(Filter filter) {
            super.add(filter);
            return filters.put(filter.getName(), filter);
        }

        boolean removeFilter(Filter filter) {
            if (super.remove(filter)) {
                filters.remove(filter.getName());
                return true;
            }
            return false;
        }

        Filter getFilter(String name) {
            return filters.get(name);
        }

        @Override
        public void clear() {
            super.clear();
            filters.clear();
        }

        boolean isEmpty() {
            return m_clauses.isEmpty() && filters.isEmpty();
        }

        @Override
        public void add(Predicate p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Predicate p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Predicate p) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Predicate[] p) {
            throw new UnsupportedOperationException();
        }

        static enum Connective {

            AND, OR
        }
    }
}
