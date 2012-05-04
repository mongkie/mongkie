/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.ui.datatable;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.*;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.datatable.DataTableController;
import org.mongkie.datatable.spi.DataTable;
import static org.mongkie.kopath.viz.Config.ROLE_PATHWAY;
import org.mongkie.lib.widgets.BusyLabel;
import org.mongkie.util.AccumulativeEventsProcessor;
import static org.mongkie.visualization.Config.MODE_DATATABLE;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import prefuse.data.Graph;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.datatable//DataTable//EN",
autostore = false)
@TopComponent.Description(preferredID = DataTableTopComponent.PREFERRED_ID,
iconBase = "org/mongkie/ui/datatable/resources/tables.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_DATATABLE, openAtStartup = true, roles = {ROLE_NETWORK, ROLE_PATHWAY}, position = 100)
@ActionID(category = "Window", id = "org.mongkie.ui.datatable.DataTableTopComponent")
@ActionReference(path = "Menu/Window", position = 220)
@TopComponent.OpenActionRegistration(displayName = "#CTL_DataTableAction",
preferredID = DataTableTopComponent.PREFERRED_ID)
@Messages({
    "CTL_DataTableAction=Data Table",
    "CTL_DataTableTopComponent=Data Table",
    "HINT_DataTableTopComponent=Nodes and Edges Table"
})
public final class DataTableTopComponent extends TopComponent implements DisplayListener<MongkieDisplay>, ExplorerManager.Provider {

    static final String PREFERRED_ID = "DataTableTopComponent";
    private BusyLabel refreshing;

    public DataTableTopComponent() {
        initComponents();
        setName(Bundle.CTL_DataTableTopComponent());
        setToolTipText(Bundle.HINT_DataTableTopComponent());

        addTableButtons();
        associateExplorerLookups();

        viewScrollPane.setBorder(BorderFactory.createEmptyBorder());
        viewScrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
        refreshing = new BusyLabel(null, 24, 24, viewScrollPane, null);
        refreshing.setBusy(false);

        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void displaySelected(MongkieDisplay display) {
                refreshTables(display);
                display.addDisplayListener(DataTableTopComponent.this);
            }

            @Override
            public void displayDeselected(MongkieDisplay display) {
                display.removeDisplayListener(DataTableTopComponent.this);
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                refreshing.setBusy(false);
            }
        });
        final MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                @Override
                public void run() {
                    refreshTables(d);
                }
            });
        }
    }

    private void addTableButtons() {
        for (final DataTable table : Lookup.getDefault().lookupAll(DataTable.class)) {
            JToggleButton toggle = new JToggleButton(table.getName(), table.getIcon());
            toggle.setFocusable(false);
            toggle.setActionCommand(table.getName());
            tableButtonGroup.add(toggle);
            int i = topToolbar.getComponentIndex(separatorAfterTableButtons);
            if (i == 0) {
                toggle.setSelected(true);
            }
            toggle.addItemListener(new ItemListener() {

                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.DESELECTED) {
                        removeTools();
                        table.deselected();
                    } else if (e.getStateChange() == ItemEvent.SELECTED) {
                        if (!refreshing.isBusy()) {
                            JComponent view = table.getView();
                            configureViewScrollPane(view);
                            viewScrollPane.setViewportView(view);
                        }
                        table.selected();
                        addTools(table);
                    }
                }
            });
            topToolbar.add(toggle, i);
            if (i == 0) {
                table.selected();
                addTools(table);
            }
        }
    }

    private void removeTools() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (int i = topToolbar.getComponentCount() - 1;
                        i > topToolbar.getComponentIndex(separatorAfterTableButtons);) {
                    topToolbar.remove(i--);
                }
                topToolbar.updateUI();
            }
        });
    }

    private void addTools(final DataTable table) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                int i = topToolbar.getComponentIndex(separatorAfterTableButtons) + 1;
                for (JComponent tool : table.getTools()) {
                    topToolbar.add(tool, i++);
                }
                topToolbar.updateUI();
            }
        });
    }

    private void associateExplorerLookups() {
        List<Lookup> lookups = new ArrayList<Lookup>();
        for (final DataTable table : Lookup.getDefault().lookupAll(DataTable.class)) {
            if (table instanceof ExplorerManager.Provider) {
                ExplorerManager em = ((ExplorerManager.Provider) table).getExplorerManager();
                lookups.add(ExplorerUtils.createLookup(em, getActionMap()));
            }
        }
        associateLookup(new ProxyLookup(lookups.toArray(new Lookup[]{})));
    }

    DataTable selectTable(String name) {
        for (Enumeration<AbstractButton> toggles = tableButtonGroup.getElements(); toggles.hasMoreElements();) {
            AbstractButton toggle = toggles.nextElement();
            if (toggle.getActionCommand().equals(name)) {
                toggle.setSelected(true);
                return Lookup.getDefault().lookup(DataTableController.class).getDataTable(name);
            }
        }
        return null;
    }

    private void clearTables() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (DataTable table : Lookup.getDefault().lookupAll(DataTable.class)) {
                    table.clear();
                }
            }
        });
    }

    private void refreshTables(final MongkieDisplay d) {
        if (!refreshing.isBusy()) {
            refreshing.setBusy(true);
        }
        Runnable refresh = new Runnable() {

            @Override
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            for (DataTable table : Lookup.getDefault().lookupAll(DataTable.class)) {
                                table.refreshModel(d);
                            }
                        }
                    });
                    JComponent view = Lookup.getDefault().lookup(DataTableController.class).getDataTable(tableButtonGroup.getSelection().getActionCommand()).getView();
                    configureViewScrollPane(view);
                    refreshing.setBusy(false, view);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        if (refreshProcessor != null && refreshProcessor.isAccumulating()) {
            refreshProcessor.eventAttended(refresh);
        } else {
            refreshProcessor = new AccumulativeEventsProcessor(refresh);
            refreshProcessor.start();
        }
    }
    private AccumulativeEventsProcessor refreshProcessor;

    private void configureViewScrollPane(JComponent view) {
        if (view instanceof JScrollPane) {
            viewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            view.setPreferredSize(viewScrollPane.getViewport().getViewSize());
        } else {
            viewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        }
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        clearTables();
        if (d.isLoading() && !refreshing.isBusy()) {
            refreshing.setBusy(true);
        }
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        refreshTables(d);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableButtonGroup = new javax.swing.ButtonGroup();
        topToolbar = new javax.swing.JToolBar();
        separatorAfterTableButtons = new javax.swing.JToolBar.Separator();
        viewScrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        topToolbar.setBorder((javax.swing.border.Border) javax.swing.UIManager.get("Nb.Editor.Toolbar.border"));
        topToolbar.setFloatable(false);
        topToolbar.setRollover(true);
        topToolbar.add(separatorAfterTableButtons);

        add(topToolbar, java.awt.BorderLayout.NORTH);
        add(viewScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar.Separator separatorAfterTableButtons;
    private javax.swing.ButtonGroup tableButtonGroup;
    private javax.swing.JToolBar topToolbar;
    private javax.swing.JScrollPane viewScrollPane;
    // End of variables declaration//GEN-END:variables

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

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    public static DataTableTopComponent findInstance() {
        return (DataTableTopComponent) WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        DataTable currentTable = getSelectedTable();
        if (currentTable instanceof ExplorerManager.Provider) {
            return ((ExplorerManager.Provider) currentTable).getExplorerManager();
        }
        throw new AssertionError("The selected data table is not providing a explorer manager");
    }

    DataTable getSelectedTable() {
        return Lookup.getDefault().lookup(DataTableController.class).getDataTable(tableButtonGroup.getSelection().getActionCommand());
    }
}
