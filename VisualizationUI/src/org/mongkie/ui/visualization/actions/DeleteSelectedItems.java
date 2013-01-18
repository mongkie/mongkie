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

import java.util.Set;
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
import org.openide.util.Utilities;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

@ActionID(category = "Display",
id = "org.mongkie.ui.visualization.actions.DeleteSelectedItems")
@ActionRegistration(iconBase = "org/mongkie/ui/visualization/resources/delete.png",
displayName = "#CTL_DeleteSelectedItems", lazy = true)
@ActionReference(path = "Shortcuts", name = "DELETE")
@Messages("CTL_DeleteSelectedItems=Delete Selected Items")
public final class DeleteSelectedItems extends DisplayAction.Focus<VisualItem> {

    private final DisplayAction deleteNodesAction, deleteEdgesAction;

    public DeleteSelectedItems() {
        this(Utilities.actionsGlobalContext());
    }

    public DeleteSelectedItems(Lookup lookup) {
        super(lookup);
        deleteNodesAction = new DisplayAction.Focus<NodeItem>(lookup) {
            @Override
            protected Class<NodeItem> getItemType() {
                return NodeItem.class;
            }

            @Override
            protected void performAction(final MongkieDisplay display, final Set<NodeItem> nodes) {
                if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation("Are you sure you want to delete selected " + nodes.size() + " nodes?", NotifyDescriptor.YES_NO_OPTION))
                        == NotifyDescriptor.YES_OPTION) {
                    display.getVisualization().process(new Runnable() {
                        @Override
                        public void run() {
                            clearFocusItems();
                            for (NodeItem n : nodes) {
                                display.getGraph().removeNode(n.getSourceTuple().getRow());
                            }
                        }
                    });
                    display.getVisualization().repaint();
                }
            }

            @Override
            public Action createContextAwareInstance(Lookup lookup) {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
        deleteEdgesAction = new DisplayAction.Focus<EdgeItem>(lookup) {
            @Override
            protected Class<EdgeItem> getItemType() {
                return EdgeItem.class;
            }

            @Override
            protected void performAction(final MongkieDisplay display, final Set<EdgeItem> edges) {
                if (DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation("Are you sure you want to delete selected " + edges.size() + " edges?", NotifyDescriptor.YES_NO_OPTION))
                        == NotifyDescriptor.YES_OPTION) {
                    display.getVisualization().process(new Runnable() {
                        @Override
                        public void run() {
                            clearFocusItems();
                            for (EdgeItem e : edges) {
                                display.getGraph().removeEdge(e.getSourceTuple().getRow());
                            }
                        }
                    });
                    display.getVisualization().repaint();
                }
            }

            @Override
            public Action createContextAwareInstance(Lookup lookup) {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }

    @Override
    protected Class<VisualItem> getItemType() {
        return VisualItem.class;
    }

    @Override
    protected void performAction(MongkieDisplay display, Set<VisualItem> items) {
        assert deleteNodesAction.isEnabled() != deleteEdgesAction.isEnabled();
        if (deleteNodesAction.isEnabled()) {
            deleteNodesAction.actionPerformed(null);
        }
        if (deleteEdgesAction.isEnabled()) {
            deleteEdgesAction.actionPerformed(null);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup lookup) {
        return new DeleteSelectedItems(lookup);
    }
}
