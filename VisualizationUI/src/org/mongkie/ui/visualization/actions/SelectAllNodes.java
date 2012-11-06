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
package org.mongkie.ui.visualization.actions;

import javax.swing.Action;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.DisplayAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import prefuse.Visualization;
import prefuse.data.tuple.DefaultTupleSet;

@ActionID(
    category = "Display",
id = "org.mongkie.ui.visualization.actions.SelectAllNodes")
@ActionRegistration(
    iconBase = "org/mongkie/ui/visualization/resources/select_all.png",
displayName = "#CTL_SelectAllNodes")
@ActionReference(path = "Shortcuts", name = "D-A")
@Messages("CTL_SelectAllNodes=Select All")
public final class SelectAllNodes extends DisplayAction {

    public SelectAllNodes() {
    }

    public SelectAllNodes(Lookup lookup) {
        super(lookup);
    }

    @Override
    protected boolean isEnabled(MongkieDisplay d) {
        return d.getGraph().getNodeCount() > 0;
    }

    @Override
    protected void performAction(MongkieDisplay d) {
        Visualization v = d.getVisualization();
        ((DefaultTupleSet) v.getFocusGroup(Visualization.FOCUS_ITEMS)).set(d.getVisualGraph().getNodeTable());
        v.rerun(Visualization.DRAW);
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new SelectAllNodes(lookup);
    }
}
