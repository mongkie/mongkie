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
import java.util.List;
import static kobic.prefuse.Constants.*;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.filter.FilterController;
import org.mongkie.filter.FilterModel;
import org.mongkie.filter.FilterModelChangeListener;
import org.mongkie.filter.spi.Filter;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = FilterController.class)
public class FilterControllerImpl implements FilterController, DisplayListener<MongkieDisplay> {

    private List<FilterModelChangeListener> listeners;
    private FilterModelImpl model;

    public FilterControllerImpl() {
        listeners = new ArrayList<FilterModelChangeListener>();
        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(new WorkspaceListener() {
            @Override
            public void displaySelected(MongkieDisplay display) {
                display.addDisplayListener(FilterControllerImpl.this);
                FilterModelImpl old = model;
                model = display.getLookup().lookup(FilterModelImpl.class);
                if (model == null) {
                    model = new FilterModelImpl(display);
                    display.add(model);
                }
                fireModelChangeEvent(old, model);
            }

            @Override
            public void displayDeselected(MongkieDisplay display) {
                display.removeDisplayListener(FilterControllerImpl.this);
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                FilterModelImpl old = model;
                model = null;
                fireModelChangeEvent(old, model);
            }
        });
        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            model = d.getLookup().lookup(FilterModelImpl.class);
            if (model == null) {
                model = new FilterModelImpl(d);
                d.add(model);
            }
        }
    }

    @Override
    public void addModelChangeListener(FilterModelChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelChangeListener(FilterModelChangeListener l) {
        listeners.remove(l);
    }

    private void fireModelChangeEvent(FilterModel oldModel, FilterModel newModel) {
        for (FilterModelChangeListener l : listeners) {
            l.modelChanged(oldModel, newModel);
        }
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        model.clearFilterAction();
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        if (d.isFired()) {
            fireModelChangeEvent(null, model);
        } else {
            fireModelChangeEvent(model, null);
        }
        model.putFilterAction();
    }

    @Override
    public FilterModel getModel() {
        return model;
    }

    @Override
    public FilterModel getModel(MongkieDisplay d) {
        FilterModel m = d.getLookup().lookup(FilterModelImpl.class);
        if (m == null) {
            m = new FilterModelImpl(d);
            d.add(m);
        }
        return m;
    }

    @Override
    public void addFilter(String group, Filter filter) {
        if (NODES.equals(group)) {
            addNodeFilter(filter);
        } else if (EDGES.equals(group)) {
            addEdgeFilter(filter);
        } else {
            throw new IllegalArgumentException("Unknown data group: " + group);
        }
    }

    @Override
    public boolean removeFilter(String group, Filter filter) {
        if (NODES.equals(group)) {
            return removeNodeFilter(filter);
        } else if (EDGES.equals(group)) {
            return removeEdgeFilter(filter);
        }
        throw new IllegalArgumentException("Unknown data group: " + group);
    }

    @Override
    public void addNodeFilter(Filter filter) {
        model.getNodeVisiblePredicates().addFilter(filter);
    }

    @Override
    public boolean removeNodeFilter(Filter filter) {
        return model.getNodeVisiblePredicates().removeFilter(filter);
    }

    @Override
    public void clearNodeFilters() {
        model.getNodeVisiblePredicates().clear();
    }

    @Override
    public void addEdgeFilter(Filter filter) {
        model.getEdgeVisiblePredicates().addFilter(filter);
    }

    @Override
    public boolean removeEdgeFilter(Filter filter) {
        return model.getEdgeVisiblePredicates().removeFilter(filter);
    }

    @Override
    public void clearEdgeFilters() {
        model.getEdgeVisiblePredicates().clear();
    }
}
