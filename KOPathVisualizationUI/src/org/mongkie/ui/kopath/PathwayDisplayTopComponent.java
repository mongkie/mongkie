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
package org.mongkie.ui.kopath;

import java.awt.BorderLayout;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import kobic.prefuse.controls.PopupControl;
import static org.mongkie.kopath.Config.*;
import org.mongkie.kopath.Pathway;
import static org.mongkie.kopath.viz.Config.ROLE_PATHWAY;
import org.mongkie.kopath.viz.PathwayDataNode;
import org.mongkie.kopath.viz.PathwayDisplay;
import org.mongkie.kopath.viz.worker.ExpansionWorker;
import org.mongkie.kopath.viz.worker.RetrievalWorker;
import static org.mongkie.visualization.Config.MODE_DISPLAY;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.selection.SelectionListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import prefuse.Visualization;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.kopath//PathwayDisplay//EN",
autostore = false)
@TopComponent.Description(preferredID = PathwayDisplayTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/kopath/resources/pathways.png",
persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED) // Need to open an initial display
@TopComponent.Registration(mode = MODE_DISPLAY, openAtStartup = true, roles = ROLE_PATHWAY)
public final class PathwayDisplayTopComponent extends TopComponent {

    private static PathwayDisplayTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
    static final String PREFERRED_ID = "PathwayDisplayTopComponent";
    private PathwayDisplay display;

    public PathwayDisplayTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(PathwayDisplayTopComponent.class, "CTL_PathwayDisplayTopComponent"));
        setToolTipText(NbBundle.getMessage(PathwayDisplayTopComponent.class, "HINT_PathwayDisplayTopComponent"));

        final InstanceContent content = new InstanceContent();
        content.add(getActionMap());
        associateLookup(new AbstractLookup(content));

        DropTarget dropTarget = new DropTarget(this, new PathwayDropListener());
        dropTarget.setDefaultActions(DnDConstants.ACTION_COPY);
        dropTarget.setActive(true);

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                display = new PathwayDisplay();
                Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().addSelectionListener(
                        new SelectionListener() {
                            @Override
                            public MongkieDisplay getDisplay() {
                                return display;
                            }

                            @Override
                            public void selected(Set<VisualItem> members, VisualItem... items) {
                                setActivatedNodesOf(members);
                            }

                            @Override
                            public void unselected(Set<VisualItem> members, VisualItem... items) {
                                setActivatedNodesOf(members);
                            }

                            private void setActivatedNodesOf(Set<VisualItem> items) {
                                for (PathwayDataNode n : tupleNodes) {
                                    try {
                                        n.destroy();
                                    } catch (IOException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                                tupleNodes.clear();
                                for (VisualItem item : items) {
                                    if (item instanceof NodeItem) {
                                        tupleNodes.add(new PathwayDataNode(item, FIELD_NAME));
                                    } else if (item instanceof EdgeItem) {
                                        tupleNodes.add(new PathwayDataNode(item, FIELD_INTERACTIONID));
                                    }
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        setActivatedNodes(tupleNodes.toArray(new PathwayDataNode[]{}));
                                    }
                                });
                            }
                            List<PathwayDataNode> tupleNodes = new ArrayList<PathwayDataNode>();
                        });
                display.addControlListener(new PopupControl<PathwayDisplay>(display) {
                    @Override
                    protected void addNodePopupMenuItems(JPopupMenu popup) {
                        super.addNodePopupMenuItems(popup);
                        final Action expandAction = new AbstractAction("Expand",
                                ImageUtilities.loadImageIcon("org/mongkie/ui/kopath/resources/expand.png", false)) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                new ExpansionWorker(display, 0, clickedItem).execute();
                            }
                        };
                        popup.add(expandAction);
                        popup.addPopupMenuListener(new PopupMenuListener() {
                            @Override
                            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                String localId;
                                expandAction.setEnabled(display.isIntegratedPathway() && clickedItem.isExpandable()
                                        && clickedItem.canGetString(FIELD_LOCALID) && (localId = clickedItem.getString(FIELD_LOCALID)) != null && !localId.equals("-"));
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                            }

                            @Override
                            public void popupMenuCanceled(PopupMenuEvent e) {
                            }
                        });
                    }

                    @Override
                    protected String getUrlField() {
                        return null;
                    }
                });
                content.add(display);
                Lookup.getDefault().lookup(VisualizationController.class).resultChanged(null);
                remove(initializingLabel);
                add(display, BorderLayout.CENTER);
                int width = getWidth();
                int height = getHeight();
                display.setSize(width, height);
                display.pan(width / 2, height / 2);
            }
        });
    }

    private class PathwayDropListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(DropTargetEvent dtde) {
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }

        @Override
        public void drop(DropTargetDropEvent evt) {
            if (evt.isDataFlavorSupported(Pathway.DATA_FLAVOR)) {
                try {
                    Object transData = evt.getTransferable().getTransferData(Pathway.DATA_FLAVOR);
                    if (transData instanceof Pathway) {
                        evt.acceptDrop(DnDConstants.ACTION_COPY);
                        Pathway p = (Pathway) evt.getTransferable().getTransferData(Pathway.DATA_FLAVOR);
                        StatusDisplayer.getDefault().setStatusText(p.getName());
                        pathwayDropped(p);
                    }
                } catch (UnsupportedFlavorException ufe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ufe);
                    evt.rejectDrop();
                    evt.dropComplete(true);
                } catch (IOException ioe) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ioe);
                    evt.rejectDrop();
                    evt.dropComplete(false);
                }
            } else {
                evt.rejectDrop();
                evt.dropComplete(false);
            }
        }
    }

    private void pathwayDropped(Pathway p) {
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Pathway dropped: {0}@{1}", new Object[]{p.getName(), p.getDatabase().getName()});
        new RetrievalWorker(display, p.getDatabase().getCode(), p.getId()).execute();
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

        initializingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        initializingLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/kopath/resources/wait.gif"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(initializingLabel, org.openide.util.NbBundle.getMessage(PathwayDisplayTopComponent.class, "PathwayDisplayTopComponent.initializingLabel.text")); // NOI18N
        add(initializingLabel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel initializingLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized PathwayDisplayTopComponent getDefault() {
        if (instance == null) {
            instance = new PathwayDisplayTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the PathwayDisplayTopComponent instance. Never call
     * {@link #getDefault} directly!
     */
    public static synchronized PathwayDisplayTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PathwayDisplayTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof PathwayDisplayTopComponent) {
            return (PathwayDisplayTopComponent) win;
        }
        Logger.getLogger(PathwayDisplayTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

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

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
