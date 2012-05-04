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

import java.util.logging.Logger;
import static org.mongkie.kopath.viz.Config.ROLE_PATHWAY;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.longtask.progress.Progress;
import org.mongkie.longtask.progress.ProgressTask;
import org.mongkie.longtask.progress.ProgressTicket;
import org.mongkie.ui.kopath.explorer.ExplorerDatabaseChildFactory;
import org.mongkie.ui.kopath.explorer.ExplorerDatabaseNode;
import org.mongkie.ui.kopath.explorer.ExplorerRootNode;
import static org.mongkie.visualization.Config.MODE_CONTEXT;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.kopath//PathwayExplorer//EN",
autostore = false)
@TopComponent.Description(preferredID = PathwayExplorerTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/kopath/resources/databaseNetwork.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_CONTEXT, openAtStartup = true, roles = ROLE_PATHWAY, position = 90)
public final class PathwayExplorerTopComponent extends TopComponent implements ExplorerManager.Provider {

    private static PathwayExplorerTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String PREFERRED_ID = "PathwayExplorerTopComponent";
    private transient ExplorerManager explorerManager = new ExplorerManager();
    private ExplorerRootNode rootForTotalPathways;
    private final LongTaskExecutor executor;
    private static final ExplorerRootNode EMPTY = new ExplorerRootNode(Children.LEAF);

    public PathwayExplorerTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(PathwayExplorerTopComponent.class, "CTL_PathwayExplorerTopComponent"));
        setToolTipText(NbBundle.getMessage(PathwayExplorerTopComponent.class, "HINT_PathwayExplorerTopComponent"));

        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        explorerManager.setRootContext(EMPTY);
        explorerManager.getRootContext().setDisplayName("Initializing...");
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                explorerManager.setRootContext(rootForTotalPathways =
                        new ExplorerRootNode(Children.create(new ExplorerDatabaseChildFactory(), true)));
                explorerManager.getRootContext().setDisplayName(
                        NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.rootNode.displayName"));
            }
        });

        executor = new LongTaskExecutor(true, "ExplorerSearch");
        executor.setLongTaskListener(new LongTaskListener() {

            @Override
            public void taskStarted(LongTask task) {
                explorerManager.setRootContext(EMPTY);
                explorerManager.getRootContext().setDisplayName("Searching...");
                searchButton.setEnabled(false);
                resetButton.setEnabled(false);
                searchField.setEnabled(false);
            }

            @Override
            public void taskFinished(LongTask task) {
                searchButton.setEnabled(true);
                resetButton.setEnabled(true);
                searchField.setEnabled(true);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                ErrorManager.getDefault().notify(t);
            }
        });
    }

    private void search() {
        final String pathway = searchField.getText();
        if (pathway == null || pathway.isEmpty() || executor.isRunning()) {
            return;
        }
        final ProgressTask task = new ProgressTask() {

            @Override
            public boolean cancel() {
                return false;
            }
        };
        executor.execute(task, new Runnable() {

            @Override
            public void run() {
                ProgressTicket ticket = task.getProgressTicket();
                Progress.setDisplayName(ticket, "Searching Pathway");
                Progress.start(ticket);
                try {
                    Node root = new ExplorerRootNode(Children.create(new ExplorerDatabaseChildFactory(pathway), false));
                    int pathwayCount = 0;
                    for (Node n : root.getChildren().getNodes()) {
                        pathwayCount += ((ExplorerDatabaseNode) n).getPathwayCount();
                    }
                    root.setDisplayName(NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.searchResult.rootNode.displayName", pathwayCount));
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.searchFinished.statusText", pathwayCount));
                    explorerManager.setRootContext(root);
                } finally {
                    Progress.finish(ticket);
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        explorerToolbar = new javax.swing.JToolBar();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        explorerTree = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());

        explorerToolbar.setFloatable(false);
        explorerToolbar.setRollover(true);

        searchField.setText(org.openide.util.NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.searchField.text")); // NOI18N
        searchField.setToolTipText(org.openide.util.NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.searchField.toolTipText")); // NOI18N
        searchField.setMaximumSize(new java.awt.Dimension(1000, 30));
        searchField.setPreferredSize(new java.awt.Dimension(250, 25));
        searchField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchFieldActionPerformed(evt);
            }
        });
        explorerToolbar.add(searchField);

        searchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/kopath/resources/find.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(searchButton, org.openide.util.NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.searchButton.text")); // NOI18N
        searchButton.setToolTipText(org.openide.util.NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.searchButton.toolTipText")); // NOI18N
        searchButton.setFocusable(false);
        searchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        searchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });
        explorerToolbar.add(searchButton);

        resetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/kopath/resources/reset.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resetButton, org.openide.util.NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.resetButton.text")); // NOI18N
        resetButton.setToolTipText(org.openide.util.NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.resetButton.toolTipText")); // NOI18N
        resetButton.setFocusable(false);
        resetButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        resetButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        explorerToolbar.add(resetButton);

        add(explorerToolbar, java.awt.BorderLayout.PAGE_START);
        add(explorerTree, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        explorerManager.setRootContext(rootForTotalPathways);
        explorerManager.getRootContext().setDisplayName(
                NbBundle.getMessage(PathwayExplorerTopComponent.class, "PathwayExplorerTopComponent.rootNode.displayName"));
        searchField.setText(null);
//        explorerTree.requestFocusInWindow();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        search();
    }//GEN-LAST:event_searchButtonActionPerformed

    private void searchFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchFieldActionPerformed
        search();
    }//GEN-LAST:event_searchFieldActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar explorerToolbar;
    private javax.swing.JScrollPane explorerTree;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized PathwayExplorerTopComponent getDefault() {
        if (instance == null) {
            instance = new PathwayExplorerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the PathwayExplorerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized PathwayExplorerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PathwayExplorerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof PathwayExplorerTopComponent) {
            return (PathwayExplorerTopComponent) win;
        }
        Logger.getLogger(PathwayExplorerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
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

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
}
