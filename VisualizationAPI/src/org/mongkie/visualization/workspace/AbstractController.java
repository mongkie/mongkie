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
package org.mongkie.visualization.workspace;

import java.util.ArrayList;
import java.util.List;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.openide.util.Lookup;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @param <M>
 * @param <V> 
 */
public abstract class AbstractController<M extends AbstractModel, V> implements Controller<M, V>, WorkspaceListener, DisplayListener {

    private final List<ModelChangeListener<M>> listeners;
    protected M model;

    public AbstractController() {
        listeners = new ArrayList<ModelChangeListener<M>>();

        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(AbstractController.this);

        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            model = d.getLookup().lookup(getModelClass());
            if (model == null) {
                model = createModel(d);
                d.add(model);
            }
        }
    }

    @Override
    public void displaySelected(MongkieDisplay display) {
        display.addDisplayListener(this);
        M old = model;
        model = display.getLookup().lookup(getModelClass());
        if (model == null) {
            model = createModel(display);
            display.add(model);
        } else if (model.get() != null) {
            model.load((V) model.get());
        }
        fireModelChangeEvent(old, model);
    }

    @Override
    public void displayDeselected(MongkieDisplay display) {
        display.removeDisplayListener(this);
        M old = display.getLookup().lookup(getModelClass());
        if (old.get() != null) {
            old.unload((V) old.get());
        }
    }

    @Override
    public void graphDisposing(NetworkDisplay d, Graph g) {
    }

    // TODO: Must be considered with Graph.graphChanged() when graph edited(expansion, deletion)
    @Override
    public void graphChanged(NetworkDisplay d, Graph g) {
        if (d.isFired()) {
            fireModelChangeEvent(null, model);
        } else {
            fireModelChangeEvent(model, null);
        }
    }

    @Override
    public void displayClosed(MongkieDisplay display) {
    }

    @Override
    public void displayClosedAll() {
        M old = model;
        model = null;
        fireModelChangeEvent(old, model);
    }

    public void fireModelChangeEvent() {
        fireModelChangeEvent(null, model);
    }

    protected abstract Class<? extends M> getModelClass();

    protected abstract M createModel(MongkieDisplay d);

    @Override
    public final void setModelData(V data) {
        model.set(data);
    }

    @Override
    public M getModel() {
        return model;
    }

    @Override
    public synchronized M getModel(MongkieDisplay d) {
        M m = d.getLookup().lookup(getModelClass());
        if (m == null) {
            m = createModel(d);
            d.add(m);
        }
        return m;
    }

    @Override
    public void addModelChangeListener(ModelChangeListener<M> l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeModelChangeListener(ModelChangeListener<M> l) {
        listeners.remove(l);
    }

    protected void fireModelChangeEvent(M o, M n) {
        for (ModelChangeListener<M> l : listeners) {
            l.modelChanged(o, n);
        }
    }
}
