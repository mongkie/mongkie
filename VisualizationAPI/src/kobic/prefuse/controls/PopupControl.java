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
package kobic.prefuse.controls;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import static kobic.prefuse.Constants.*;
import kobic.prefuse.display.NetworkDisplay;
import prefuse.Visualization;
import static prefuse.Visualization.AGGR_ITEMS;
import static prefuse.Visualization.FOCUS_ITEMS;
import prefuse.controls.ControlAdapter;
import prefuse.data.Tuple;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.util.io.IOLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PopupControl<D extends NetworkDisplay> extends ControlAdapter {

    private final JPopupMenu nodePopup = new JPopupMenu();
    private final JPopupMenu edgePopup = new JPopupMenu();
    private final JPopupMenu aggregatePopup = new JPopupMenu();
    private final JPopupMenu globalPopup = new JPopupMenu();
    protected final D display;
    protected final Visualization v;
    protected VisualItem clickedItem = null;

    public PopupControl(final D display) {

        this.display = display;
        this.v = display.getVisualization();

        nodePopup.setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        edgePopup.setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        aggregatePopup.setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        globalPopup.setBorder(BorderFactory.createLineBorder(ColorLib.getColor(0, 0, 0, 100)));
        addPopupMenuItems(nodePopup, edgePopup, aggregatePopup, globalPopup);
    }

    private void addPopupMenuItems(JPopupMenu node, JPopupMenu edge, JPopupMenu aggregate, JPopupMenu global) {
        addNodePopupMenuItems(node);
        addEdgePopupMenuItems(edge);
        addAggregatePopupMenuItems(aggregate);
        addGlobalPopupMenuItems(global);
    }

    protected void addNodePopupMenuItems(JPopupMenu popup) {
        final String urlField = getUrlField();
        if (urlField != null) {
            popup.add(new AbstractAction("Open URL", IOLib.getIcon(PopupControl.class, IMAGE_PATH + "openUrl.png")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openUrl(clickedItem, urlField);
                }
            });
        }
        final Action deleteAction = new AbstractAction("Delete", IOLib.getIcon(PopupControl.class, IMAGE_PATH + "delete.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TupleSet focusedTupleSet = display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS);
                final NodeItem clickedItem = (NodeItem) getClickedItem();
                if (focusedTupleSet.containsTuple(clickedItem)) {
                    final Tuple[] selectedNodeItems = focusedTupleSet.toArray();
                    focusedTupleSet.clear();
                    display.getVisualization().rerun(new Runnable() {
                        @Override
                        public void run() {
                            for (Tuple n : selectedNodeItems) {
                                display.getGraph().removeNode(((NodeItem) n).getSourceTuple().getRow());
                            }
                        }
                    }, new String[]{});
                } else {
                    display.getVisualization().rerun(new Runnable() {
                        @Override
                        public void run() {
                            display.getGraph().removeNode(clickedItem.getSourceTuple().getRow());
                        }
                    }, new String[]{});
                }
                display.getVisualization().repaint();
            }
        };
        popup.add(deleteAction);
        final Action groupingAction = new AbstractAction("Group", IOLib.getIcon(PopupControl.class, IMAGE_PATH + "group.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.aggregateItems(v.getFocusGroup(FOCUS_ITEMS), true);
            }
        };
        popup.add(groupingAction);
        popup.addPopupMenuListener(new PopupMenuListener() {
            TupleSet focusedTupleSet = v.getFocusGroup(FOCUS_ITEMS);

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                groupingAction.setEnabled(focusedTupleSet.getTupleCount() > 1 && focusedTupleSet.containsTuple(clickedItem));
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
    }

    protected abstract String getUrlField();

    protected void addEdgePopupMenuItems(JPopupMenu popup) {
    }

    protected void addAggregatePopupMenuItems(JPopupMenu popup) {
        popup.add(new AbstractAction("Select Nodes", IOLib.getIcon(PopupControl.class, IMAGE_PATH + "select.png")) {
            private void clearFocusItemsAreNotNodeItem() {
                List<VisualItem> notNodeItems = new ArrayList<VisualItem>();
                for (Iterator<VisualItem> focusIter = v.getFocusGroup(FOCUS_ITEMS).tuples(); focusIter.hasNext();) {
                    VisualItem item = focusIter.next();
                    if (!(item instanceof NodeItem)) {
                        notNodeItems.add(item);
                    }
                }
                for (VisualItem notNode : notNodeItems) {
                    v.getFocusGroup(FOCUS_ITEMS).removeTuple(notNode);
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                AggregateItem aggregate = (AggregateItem) clickedItem;
                clearFocusItemsAreNotNodeItem();
                for (Iterator<NodeItem> nodesIter = aggregate.items(); nodesIter.hasNext();) {
                    v.getFocusGroup(FOCUS_ITEMS).addTuple(nodesIter.next());
                }
                display.unaggregateItems(aggregate);
            }
        });
        popup.add(new AbstractAction("Ungroup", IOLib.getIcon(PopupControl.class, IMAGE_PATH + "ungroup.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                display.unaggregateItems((AggregateItem) clickedItem);
            }
        });
    }

    protected void addGlobalPopupMenuItems(JPopupMenu popup) {
    }

    protected final void openUrl(VisualItem clicked, String urlField) {
        try {
            Desktop.getDesktop().browse(URI.create(clicked.getString(urlField)));
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void itemClicked(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isRightMouseButton(e) || !item.isInteractive()) {
            clickedItem = null;
            return;
        }
        clickedItem = item;
        if (clickedItem.isInGroup(NODES) && nodePopup.getComponentCount() > 0) {
            nodePopup.show(e.getComponent(), e.getX(), e.getY());
        } else if (clickedItem.isInGroup(EDGES) && edgePopup.getComponentCount() > 0) {
            edgePopup.show(e.getComponent(), e.getX(), e.getY());
        } else if (clickedItem.isInGroup(AGGR_ITEMS) && aggregatePopup.getComponentCount() > 0) {
            aggregatePopup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!SwingUtilities.isRightMouseButton(e)) {
            return;
        }
        clickedItem = null;
        globalPopup.show(e.getComponent(), e.getX(), e.getY());
    }

    public D getDisplay() {
        return display;
    }

    public VisualItem getClickedItem() {
        return clickedItem;
    }
}
