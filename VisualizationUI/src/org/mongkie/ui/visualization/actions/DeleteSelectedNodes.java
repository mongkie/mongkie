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

import java.util.List;
import javax.swing.Action;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.DisplayAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import prefuse.visual.NodeItem;

@ActionID(category = "Display",
id = "org.mongkie.ui.visualization.actions.DeleteSelectedNodes")
@ActionRegistration(iconBase = "org/mongkie/ui/visualization/resources/delete.png",
displayName = "#CTL_DeleteSelectedNodes", lazy = true)
@ActionReference(path = "Shortcuts", name = "DELETE")
@Messages("CTL_DeleteSelectedNodes=Delete")
public final class DeleteSelectedNodes extends DisplayAction.Focus<NodeItem> {

    public DeleteSelectedNodes() {
    }

    public DeleteSelectedNodes(Lookup lookup) {
        super(lookup);
    }

    @Override
    protected Class<NodeItem> getItemType() {
        return NodeItem.class;
    }

    @Override
    protected void performAction(final MongkieDisplay display, final List<NodeItem> nodes) {
        if (DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation("Are you sure you want to delete selected " + nodes.size() + " nodes?", NotifyDescriptor.YES_NO_OPTION))
                == NotifyDescriptor.YES_OPTION) {
            display.getVisualization().rerun(new Runnable() {
                @Override
                public void run() {
                    clearFocusedItems();
                    for (NodeItem n : nodes) {
                        display.getGraph().removeNode(n.getSourceTuple().getRow());
                    }
                }
            }, new String[]{});
            display.getVisualization().repaint();
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new DeleteSelectedNodes(lookup);
    }
}
