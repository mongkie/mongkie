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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.group.GroupManager;
import org.mongkie.visualization.selection.SelectionManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import prefuse.Visualization;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualizationController.class)
public final class VisualizationControllerImpl implements VisualizationController {

    private MongkieDisplay currentDisplay;
    private final Lookup.Result<MongkieDisplay> result;
    private final List<WorkspaceListener> listeners;
    private final SelectionManager selectionManager;
    private final GroupManager groupManager;

    public VisualizationControllerImpl() {
        result = Utilities.actionsGlobalContext().lookupResult(MongkieDisplay.class);
        result.addLookupListener(VisualizationControllerImpl.this);
        listeners = Collections.synchronizedList(new ArrayList<WorkspaceListener>());
        addWorkspaceListener(selectionManager = new SelectionManager());
        addWorkspaceListener(groupManager = new GroupManager());
    }

    @Override
    public Visualization getVisualization() {
        MongkieDisplay d = getDisplay();
        if (d == null) {
            throw new IllegalStateException("Current display does not exist.");
        }
        return d.getVisualization();
    }

    @Override
    public MongkieDisplay getDisplay() {
        return currentDisplay;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Iterator<? extends MongkieDisplay> displayIter = result.allInstances().iterator();
        MongkieDisplay d;
        if (!displayIter.hasNext() || (d = displayIter.next()) == currentDisplay) {
            return;
        }
        setDisplay(d);
    }

    @Override
    public void displayClosed(MongkieDisplay d) {
        if (currentDisplay == d) {
            currentDisplay = null;
            fireDisplayChange(d, null);
        }
        synchronized (listeners) {
            for (Iterator<WorkspaceListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                listenerIter.next().displayClosed(d);
            }
        }
    }

    @Override
    public void addWorkspaceListener(WorkspaceListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    @Override
    public void removeWorkspaceListener(WorkspaceListener l) {
        listeners.remove(l);
    }

    private void fireDisplayChange(MongkieDisplay oldDisplay, MongkieDisplay newDisplay) {
        synchronized (listeners) {
            for (Iterator<WorkspaceListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                WorkspaceListener l = listenerIter.next();
                if (oldDisplay != null) {
                    l.displayDeselected(oldDisplay);
                }
                if (newDisplay == null) {
                    l.displayClosedAll();
                } else {
                    l.displaySelected(newDisplay);
                }
            }
        }
    }

    private void setDisplay(MongkieDisplay d) {
        MongkieDisplay prevDisplay = currentDisplay;
        currentDisplay = d;
        fireDisplayChange(prevDisplay, currentDisplay);
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public GroupManager getGroupManager() {
        return groupManager;
    }
}
