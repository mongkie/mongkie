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
package org.mongkie.visualization.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.workspace.WorkspaceListener;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.DefaultTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SelectionManager implements WorkspaceListener, TupleSetListener, DisplayListener {

    private final List<SelectionListener> listeners;
    private MongkieDisplay display;

    public SelectionManager() {
        listeners = new ArrayList<SelectionListener>();
    }

    @Override
    public void displaySelected(MongkieDisplay display) {
        this.display = display;
        TupleSet focusedTupleSet = display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
        focusedTupleSet.addTupleSetListener(this);
        tupleSetChanged(focusedTupleSet, ((DefaultTupleSet) focusedTupleSet).toArray(), new Tuple[]{});
    }

    @Override
    public void displayDeselected(MongkieDisplay display) {
        TupleSet focusedTupleSet = display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
        focusedTupleSet.removeTupleSetListener(this);
        tupleSetChanged(new DefaultTupleSet(), new Tuple[]{}, ((DefaultTupleSet) focusedTupleSet).toArray());
        this.display = null;
    }

    @Override
    public void displayClosed(MongkieDisplay display) {
    }

    @Override
    public void displayClosedAll() {
    }

    public void addSelectionListener(SelectionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeSelectionListener(SelectionListener l) {
        listeners.remove(l);
    }

    @Override
    public void tupleSetChanged(TupleSet tupleSet, Tuple[] added, Tuple[] removed) {
        DefaultTupleSet focusedTupleSet = (DefaultTupleSet) tupleSet;
        VisualItem[] selectedItems = toVisualItems(added);
        VisualItem[] unselectedItems = toVisualItems(removed);
        for (SelectionListener l : listeners) {
            MongkieDisplay d = l.getDisplay();
            if (d != null && display != d) {
                continue;
            }
            if (selectedItems.length > 0) {
                l.selected(focusedTupleSet.asSet(VisualItem.class), selectedItems);
            }
            if (unselectedItems.length > 0) {
                l.unselected(focusedTupleSet.asSet(VisualItem.class), unselectedItems);
            }
        }
    }

    private VisualItem[] toVisualItems(Tuple[] tuples) {
        VisualItem[] items = new VisualItem[tuples.length];
        for (int i = 0; i < tuples.length; i++) {
            items[i] = (VisualItem) tuples[i];
        }
        return items;
    }

    public Set<VisualItem> getSelectedItems() {
        return getSelectedItems(display);
    }

    public Set<VisualItem> getSelectedItems(NetworkDisplay display) {
        assert display != null;
        return ((DefaultTupleSet) display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS)).asSet(VisualItem.class);
    }

    @Override
    public void graphDisposing(NetworkDisplay d, Graph g) {
        displayDeselected((MongkieDisplay) d);
    }

    @Override
    public void graphChanged(NetworkDisplay d, Graph g) {
        displaySelected((MongkieDisplay) d);
    }
}
