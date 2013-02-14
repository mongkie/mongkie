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
import java.util.logging.Logger;
import javax.swing.JScrollPane;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import static org.mongkie.kopath.viz.Config.ROLE_PATHWAY;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.longtask.progress.Progress;
import org.mongkie.longtask.progress.ProgressTask;
import org.mongkie.longtask.progress.ProgressTicket;
import org.mongkie.ui.kopath.search.*;
import static org.mongkie.visualization.Config.MODE_ACTION;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.ErrorManager;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.kopath//PathwaySearch//EN",
autostore = false)
@TopComponent.Description(preferredID = PathwaySearchTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/kopath/resources/search.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_ACTION, openAtStartup = true, roles = ROLE_PATHWAY, position = 90)
public final class PathwaySearchTopComponent extends TopComponent implements ExplorerManager.Provider {

    private static PathwaySearchTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
    static final String PREFERRED_ID = "PathwaySearchTopComponent";
    private JXTaskPane geneIdSearchPane;
    private JXTaskPane pathwayNameSearchPane;
    private transient ExplorerManager explorerManager = new ExplorerManager();
    private final LongTaskExecutor executor;
    private GeneIDSearchPanel geneIdSearchPanel;
    private PathwayNameSearchPanel pathwayNameSearchPanel;
    private SearchedRootNode noResultRoot, searchingRoot;

    public PathwaySearchTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(PathwaySearchTopComponent.class, "CTL_PathwaySearchTopComponent"));
        setToolTipText(NbBundle.getMessage(PathwaySearchTopComponent.class, "HINT_PathwaySearchTopComponent"));

        executor = new LongTaskExecutor(true, "PathwaySearch");
        executor.setLongTaskListener(new LongTaskListener() {
            @Override
            public void taskStarted(LongTask task) {
                searchStarted();
            }

            @Override
            public void taskFinished(LongTask task) {
                searchFinished();
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                ErrorManager.getDefault().notify(t);
            }
        });

        geneIdSearchPane = new JXTaskPane("Physical Entity",
                ImageUtilities.loadImageIcon("org/mongkie/ui/kopath/resources/search.png", false));
        geneIdSearchPane.add(geneIdSearchPanel = new GeneIDSearchPanel());
        pathwayNameSearchPane = new JXTaskPane("Pathway Name",
                ImageUtilities.loadImageIcon("org/mongkie/ui/kopath/resources/search.png", false));
        pathwayNameSearchPane.add(pathwayNameSearchPanel = new PathwayNameSearchPanel());

        JXTaskPaneContainer tpc = new JXTaskPaneContainer();
        tpc.add(geneIdSearchPane);
        tpc.add(pathwayNameSearchPane);

        JScrollPane jsp = new JScrollPane(tpc);
        jsp.setBorder(null);
        jsp.setViewportBorder(null);
        searchInputPanel.add(jsp, BorderLayout.CENTER);

        noResultRoot = new SearchedRootNode(ImageUtilities.loadImage("org/mongkie/ui/kopath/resources/searchOpen.png"));
        noResultRoot.setDisplayName("No result");
        searchingRoot = new SearchedRootNode(ImageUtilities.loadImage("org/mongkie/ui/kopath/resources/wait.gif"));
        searchingRoot.setDisplayName("Searching...");

        associateLookup(ExplorerUtils.createLookup(explorerManager, getActionMap()));
        explorerManager.setRootContext(noResultRoot);

        bannerPanel.setVisible(false);
    }

    public void search(final String query, final boolean geneIdSearch) {
        if (query == null || query.isEmpty() || executor.isRunning()) {
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
                    Node root = new SearchedRootNode(geneIdSearch
                            ? new SearchedDatabaseChildren(query.split("[,;\\s\n]+"))
                            : new SearchedDatabaseChildren(query));
                    int pathwayCount = 0;
                    for (Node n : root.getChildren().getNodes()) {
                        pathwayCount += ((SearchedDatabaseNode) n).getPathwayCount();
                    }
                    root.setDisplayName(NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.searchResult.rootNode.displayName", pathwayCount));
                    StatusDisplayer.getDefault().setStatusText(
                            NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.searchFinished.statusText", pathwayCount));
                    explorerManager.setRootContext(root);
                    if (geneIdSearch) {
                        geneIdSearchPanel.requestFocusInWindow();
                    } else {
                        pathwayNameSearchPanel.requestFocusInWindow();
                    }
                } finally {
                    Progress.finish(ticket);
                }
            }
        });
    }

    public void searchStarted() {
        explorerManager.setRootContext(searchingRoot);

        geneIdSearchPanel.setEnabled(false);
        pathwayNameSearchPanel.setEnabled(false);

        bannerPanel.setVisible(false);
    }

    public void searchFinished() {
        geneIdSearchPanel.setEnabled(true);
        pathwayNameSearchPanel.setEnabled(true);

        if (explorerManager.getRootContext() == searchingRoot) { // search failed?
            explorerManager.setRootContext(noResultRoot);
        } else {
            displayBanner();
        }
    }

    public void displayBanner() {
        int results = 0;
        for (Node n : explorerManager.getRootContext().getChildren().getNodes()) {
            results += ((SearchedDatabaseNode) n).getPathwayCount();
        }
        bannerLabel.setText(NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.bannerLabel.text", results));
        bannerPanel.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        splitPane = new javax.swing.JSplitPane();
        searchResultPanel = new javax.swing.JPanel();
        bannerPanel = new javax.swing.JPanel();
        bannerLabel = new javax.swing.JLabel();
        clearButton = new javax.swing.JButton();
        resultTree = new BeanTreeView();
        searchInputPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        splitPane.setBorder(null);
        splitPane.setDividerLocation(430);
        splitPane.setDividerSize(10);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        searchResultPanel.setLayout(new java.awt.BorderLayout());

        bannerPanel.setBackground(new java.awt.Color(178, 223, 240));
        bannerPanel.setLayout(new java.awt.GridBagLayout());

        bannerLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/kopath/resources/information.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(bannerLabel, org.openide.util.NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.bannerLabel.text", new Object[] {17})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 0);
        bannerPanel.add(bannerLabel, gridBagConstraints);

        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/kopath/resources/clear.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(clearButton, org.openide.util.NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.clearButton.text")); // NOI18N
        clearButton.setToolTipText(org.openide.util.NbBundle.getMessage(PathwaySearchTopComponent.class, "PathwaySearchTopComponent.clearButton.toolTipText")); // NOI18N
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 1);
        bannerPanel.add(clearButton, gridBagConstraints);

        searchResultPanel.add(bannerPanel, java.awt.BorderLayout.SOUTH);

        resultTree.setBorder(null);
        searchResultPanel.add(resultTree, java.awt.BorderLayout.CENTER);

        splitPane.setBottomComponent(searchResultPanel);

        searchInputPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setLeftComponent(searchInputPanel);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        explorerManager.setRootContext(noResultRoot);
        bannerPanel.setVisible(false);
    }//GEN-LAST:event_clearButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerLabel;
    private javax.swing.JPanel bannerPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JScrollPane resultTree;
    private javax.swing.JPanel searchInputPanel;
    private javax.swing.JPanel searchResultPanel;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized PathwaySearchTopComponent getDefault() {
        if (instance == null) {
            instance = new PathwaySearchTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the PathwaySearchTopComponent instance. Never call
     * {@link #getDefault} directly!
     */
    public static synchronized PathwaySearchTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PathwaySearchTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof PathwaySearchTopComponent) {
            return (PathwaySearchTopComponent) win;
        }
        Logger.getLogger(PathwaySearchTopComponent.class.getName()).warning(
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
