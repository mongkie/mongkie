/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.ui.visualization;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import static kobic.prefuse.Constants.EDGES;
import static kobic.prefuse.Constants.NODES;
import kobic.prefuse.controls.PopupControl;
import org.mongkie.datatable.DataNode;
import org.mongkie.datatable.DataTableController;
import org.mongkie.lib.widgets.CollapsiblePanel;
import org.mongkie.lib.widgets.WidgetUtilities;
import org.mongkie.perspective.NonSingletonTopComponent;
import org.mongkie.perspective.spi.Perspective;
import org.mongkie.ui.visualization.options.OptionsSettingPanel;
import org.mongkie.ui.visualization.options.OptionsToolbar;
import org.mongkie.ui.visualization.popup.spi.NodeMenuItemFactory;
import org.mongkie.ui.visualization.tools.AddonPopupDialog;
import org.mongkie.ui.visualization.tools.AddonsBar;
import org.mongkie.ui.visualization.tools.PropertiesBar;
import org.mongkie.ui.visualization.tools.spi.AddonUI;
import static org.mongkie.visualization.Config.MODE_DISPLAY;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.VisualizationControllerUI;
import org.mongkie.visualization.selection.SelectionListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.visualization//MongkieDisplay//EN", autostore = false)
@TopComponent.Description(preferredID = "DisplayTopComponent",
iconBase = "org/mongkie/ui/visualization/resources/network.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED) // Need to open an initial display
@TopComponent.Registration(mode = MODE_DISPLAY, openAtStartup = true, roles = ROLE_NETWORK)
public final class DisplayTopComponent extends TopComponent
        implements org.mongkie.visualization.DisplayTopComponent, NonSingletonTopComponent {

    private MongkieDisplay display;

    public DisplayTopComponent() {
        this(null);
    }

    public DisplayTopComponent(Graph g) {
        this(g, false);
    }

    public DisplayTopComponent(final Graph g, final boolean loading) {
        initComponents();

        setName(NbBundle.getMessage(DisplayTopComponent.class, "CTL_DisplayTopComponent"));
        setToolTipText(NbBundle.getMessage(DisplayTopComponent.class, "HINT_DisplayTopComponent"));

        final InstanceContent content = new InstanceContent();
        content.add(getActionMap());
        associateLookup(new AbstractLookup(content));
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                display = new MongkieDisplay(g);
                if (loading) {
                    display.setLoading(true);
                }

                content.add(display);
                Lookup.getDefault().lookup(VisualizationController.class).resultChanged(null);

                Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().addSelectionListener(
                        new SelectionListener() {
                            @Override
                            public MongkieDisplay getDisplay() {
                                return display;
                            }

                            @Override
                            public void selected(Set<VisualItem> members, VisualItem... items) {
                                setActivatedNodesOf(display, members);
                            }

                            @Override
                            public void unselected(Set<VisualItem> members, VisualItem... items) {
                                setActivatedNodesOf(display, members);
                            }

                            private void setActivatedNodesOf(MongkieDisplay display, Set<VisualItem> items) {
                                for (DataNode n : tupleNodes) {
                                    try {
                                        n.destroy();
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                                tupleNodes.clear();
                                for (VisualItem item : items) {
                                    if (item instanceof NodeItem) {
                                        tupleNodes.add(Lookup.getDefault().lookup(DataTableController.class).createDataNode(item.getSourceTuple(), display.getGraph().getNodeLabelField()));
                                    } else if (item instanceof EdgeItem) {
                                        tupleNodes.add(Lookup.getDefault().lookup(DataTableController.class).createDataNode(item.getSourceTuple(), display.getGraph().getEdgeLabelField()));
                                    }
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setActivatedNodes(tupleNodes.toArray(new DataNode[]{}));
                                    }
                                });
                            }
                            private final List<DataNode> tupleNodes = new ArrayList<DataNode>();
                        });

                display.addControlListener(new PopupControl<MongkieDisplay>(display) {
                    @Override
                    protected String getUrlField() {
                        return null;
                    }

                    @Override
                    protected void addNodePopupMenuItems(JPopupMenu popup) {
                        super.addNodePopupMenuItems(popup);
                        for (NodeMenuItemFactory f : Lookup.getDefault().lookupAll(NodeMenuItemFactory.class)) {
                            for (JMenuItem mi : f.createMenuItems(this)) {
                                popup.add(mi);
                            }
                        }
                    }

                    @Override
                    protected void addAggregatePopupMenuItems(JPopupMenu popup) {
                        popup.add(new AbstractAction("New Graph", ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/tab_new.png", false)) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
//                                Set<Node> nodes = new HashSet<Node>();
//                                for (Iterator<NodeItem> nodeItemIter = ((AggregateItem) clickedItem).items(); nodeItemIter.hasNext();) {
//                                    nodes.add((Node) nodeItemIter.next().getSourceTuple());
//                                }
//                                Lookup.getDefault().lookup(VisualizationControllerUI.class).openNewDisplayTopComponent(display.getGraph().createPartial(nodes));
                                final Map<NodeItem, Tuple> itemToNode = new HashMap<NodeItem, Tuple>();
                                final Map<EdgeItem, Tuple> itemToEdge = new HashMap<EdgeItem, Tuple>();
                                final org.mongkie.visualization.DisplayTopComponent dtc =
                                        Lookup.getDefault().lookup(VisualizationControllerUI.class).openNewDisplayTopComponent(
                                        display.getVisualGraph().createPartialGraph(((AggregateItem) clickedItem).items(), itemToNode, itemToEdge));
                                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                                    @Override
                                    public void run() {
                                        Visualization v = dtc.getDisplay().getVisualization();
                                        //TODO: normalize codes...
                                        for (NodeItem old : itemToNode.keySet()) {
                                            Tuple node = itemToNode.get(old);
                                            NodeItem item = (NodeItem) v.getVisualItem(NODES, node);
                                            item.setFont(old.getFont());
                                            item.setShape(old.getShape());
                                            item.setSize(old.getSize());
                                        }
                                        for (EdgeItem old : itemToEdge.keySet()) {
                                            Tuple edge = itemToEdge.get(old);
                                            EdgeItem item = (EdgeItem) v.getVisualItem(EDGES, edge);
                                            item.setFont(old.getFont());
                                            item.setShape(old.getShape());
                                            item.setSize(old.getSize());
                                            item.setStroke(old.getStroke());
                                        }
                                        v.repaint();
                                    }
                                });
                            }
                        });
                        super.addAggregatePopupMenuItems(popup);
                    }
                });

                JPanel propertiesPanel = new JPanel(new BorderLayout());
                if (WidgetUtilities.isAquaLookAndFeel()) {
                    propertiesPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
                }
                propertiesPanel.add(new PropertiesBar(), BorderLayout.CENTER);
                AddonsBar addonsBar = new AddonsBar();
                for (AddonUI addon : Lookup.getDefault().lookupAll(AddonUI.class)) {
                    final JButton b = addon.buildActionButton(display);
                    final AddonUI.ContentPanel c = addon.buildContentPanel(display);
                    if (c != null) {
                        final AddonPopupDialog d = new AddonPopupDialog(b, false);
                        d.setContentPane(c);
                        b.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                c.refresh();
                                d.pack();
                                d.showPopup();
                            }
                        });
                    }
                    addonsBar.add(b);
                }
                propertiesPanel.add(addonsBar, BorderLayout.EAST);
                add(propertiesPanel, BorderLayout.NORTH);

                CollapsiblePanel optionsPanel = CollapsiblePanel.createPanel(new OptionsToolbar(display), new OptionsSettingPanel(display),
                        ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/arrow_up.png", false), ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/arrow_up_hover.png", false), "Show Display Options",
                        ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/arrow_down.png", false), ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/arrow_down_hover.png", false), "Hide Dispaly Options",
                        false);
                add(optionsPanel, BorderLayout.PAGE_END);
                optionsPanel.addCollapseListener(new CollapsiblePanel.CollapseListener() {
                    @Override
                    public void collapsed() {
                        display.getOverview().repaint();
                    }

                    @Override
                    public void expanded() {
                        display.getOverview().repaint();
                    }
                });

                remove(initializingLabel);
                add(display, BorderLayout.CENTER);
                int width = getWidth();
                int height = getHeight();
                display.setSize(width, height);
                display.pan(width / 2, height / 2);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        initializingLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        initializingLabel.setBackground(new java.awt.Color(255, 255, 255));
        initializingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        initializingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/wait.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(initializingLabel, org.openide.util.NbBundle.getMessage(DisplayTopComponent.class, "DisplayTopComponent.initializingLabel.text")); // NOI18N
        add(initializingLabel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel initializingLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                display.getVisualization().rerun(Visualization.DRAW, Visualization.ANIMATE);
            }
        });
    }

    @Override
    public void componentClosed() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                display.getVisualization().cancel();
                Lookup.getDefault().lookup(VisualizationController.class).displayClosed(display);
            }
        });
    }

    @Override
    public MongkieDisplay getDisplay() {
        return display;
    }

    @Override
    public boolean belongsTo(Perspective p) {
        return p.getRole().equals(ROLE_NETWORK);
    }

    @Override
    public String toString() {
        return WindowManager.getDefault().findTopComponentID(this);
    }

    Object readProperties(java.util.Properties p) {
        return this;
    }

    void writeProperties(java.util.Properties p) {
    }
}
