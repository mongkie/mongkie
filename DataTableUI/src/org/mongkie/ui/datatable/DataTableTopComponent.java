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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.*;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.datatable.DataTableController;
import org.mongkie.datatable.DataTableControllerUI;
import org.mongkie.datatable.spi.DataAction;
import org.mongkie.datatable.spi.DataTable;
import org.mongkie.datatable.spi.DataTable.Tool;
import org.mongkie.datatable.spi.GraphDataTable;
import org.mongkie.datatable.spi.PopupAction;
import static org.mongkie.kopath.viz.Config.ROLE_PATHWAY;
import org.mongkie.lib.widgets.BusyLabel;
import org.mongkie.ui.datatable.graph.NodeDataTable;
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
import static org.pushingpixels.flamingo.api.common.CommandButtonDisplayState.MEDIUM;
import static org.pushingpixels.flamingo.api.common.CommandButtonDisplayState.SMALL;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import static org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind.*;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
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
    private MongkieDisplay display;

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
                getSelectedTable().deselected();
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                refreshTables(null);
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
                        clearActionsAndTools();
                        table.deselected();
                    } else if (e.getStateChange() == ItemEvent.SELECTED) {
                        if (!refreshing.isBusy()) {
                            JComponent view = display != null ? table.getView() : null;
                            configureViewScrollPane(view);
                            viewScrollPane.setViewportView(view);
                        }
                        table.selected();
                        addActionsAndTools(table);
                    }
                }
            });
            topToolbar.add(toggle, i);
            if (table instanceof NodeDataTable) {
                table.selected();
                addActionsAndTools(table);
            }
        }
    }

    private void clearActionsAndTools() {
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

    private void addActionsAndTools(final DataTable table) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int i = topToolbar.getComponentIndex(separatorAfterTableButtons) + 1;
                JCommandButton command;
                for (final DataAction a : Lookup.getDefault().lookup(DataTableController.class).getDataActionsFor(table)) {
                    boolean popupOnly = false;
                    command = (a.getIcon() != null)
                            ? new JCommandButton(a.getName(), ImageWrapperResizableIcon.getIcon(a.getIcon(), new Dimension(16, 16)))
                            : new JCommandButton(a.getName());
                    command.setCommandButtonKind(a instanceof PopupAction
                            ? (popupOnly = ((PopupAction) a).isPopupOnly()) ? POPUP_ONLY : ACTION_AND_POPUP_MAIN_ACTION : ACTION_ONLY);
                    command.setDisplayState(a.hideActionText() ? SMALL : MEDIUM);
                    if (a.getDescription() != null && !a.getDescription().isEmpty()) {
                        if (a instanceof PopupAction) {
                            command.setPopupRichTooltip(new RichTooltip(a.getName(), ((PopupAction) a).getPopupDescription()));
                        }
                        command.setActionRichTooltip(new RichTooltip(a.getName(), a.getDescription()));

                    }
                    if (a instanceof PopupAction) {
                        command.setPopupCallback(new PopupPanelCallback() {
                            @Override
                            public JPopupPanel getPopupPanel(JCommandButton jcb) {
                                JCommandPopupMenu popup = new JCommandPopupMenu();
                                JCommandMenuButton menu;
                                for (final DataAction da : ((PopupAction) a).getDataActions(table)) {
                                    menu = new JCommandMenuButton(da.getName(),
                                            ImageWrapperResizableIcon.getIcon(da.getIcon() == null ? a.getIcon() : da.getIcon(), new Dimension(16, 16)));
                                    menu.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            Lookup.getDefault().lookup(DataTableControllerUI.class).executeDataAction(table, da);
                                        }
                                    });
                                    if (da.getDescription() != null && !da.getDescription().isEmpty()) {
                                        menu.setActionRichTooltip(new RichTooltip(da.getName(), da.getDescription()));
                                    }
                                    menu.setEnabled(da.isEnabled(table));
                                    popup.addMenuButton(menu);
                                }
                                return popup;
                            }
                        });
                    }
                    if (!popupOnly) {
                        command.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                Lookup.getDefault().lookup(DataTableControllerUI.class).executeDataAction(table, a);
                            }
                        });
                    }
                    command.putClientProperty(DataAction.PROP_KEY, a);
                    command.setEnabled(a.isEnabled(table));
                    topToolbar.add(command, i++);
                }
                Tool[] tools = table.getTools();
                if (tools != null && tools.length > 0) {
                    topToolbar.add(glueAfterTableActions, i++);
                    for (Tool tool : tools) {
                        JComponent c = tool.getComponent();
                        c.putClientProperty(Tool.class, tool);
                        topToolbar.add(c, i++);
                    }
                }
                topToolbar.updateUI();
                refreshActionsAndTools(table);
            }
        });
    }

    private void refreshActionsAndTools(DataTable table) {
        for (Component c : topToolbar.getComponents()) {
            if (c instanceof JCommandButton) {
                JCommandButton command = (JCommandButton) c;
                command.setEnabled(table != null && ((DataAction) command.getClientProperty(DataAction.PROP_KEY)).isEnabled(table));
            } else if (c instanceof JComponent) {
                Tool tool = (Tool) ((JComponent) c).getClientProperty(Tool.class);
                if (tool != null && tool.getDataTable() == getSelectedTable()) {
                    tool.refresh(table == null || (table instanceof GraphDataTable && ((GraphDataTable) table).getModel() == null));
                }
            }
        }
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

    void refreshModel(final DataTable table, boolean actionsOnly) {
        if (!actionsOnly) {
            table.refreshModel(display);
        }
        if (table == getSelectedTable()) {
            refreshActionsAndTools(table);
        }
    }

    private void refreshTables(final MongkieDisplay d) {
        this.display = d;
        refreshingTables = true;
        if (d != null && !refreshing.isBusy()) {
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
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (d == null) {
                                refreshing.setBusy(false);
                                refreshActionsAndTools(null);
                            } else {
                                DataTable table = getSelectedTable();
                                JComponent view = table.getView();
                                configureViewScrollPane(view);
                                refreshing.setBusy(false, view);
                                refreshActionsAndTools(table);
                                table.selected();
                            }
                            refreshingTables = false;
                        }
                    });
                }
            }
        };
        if (refreshingQ != null && refreshingQ.isAccumulating()) {
            refreshingQ.eventAttended(refresh);
        } else {
            refreshingQ = new AccumulativeEventsProcessor(refresh);
            refreshingQ.start();
        }
    }
    private AccumulativeEventsProcessor refreshingQ;
    private volatile boolean refreshingTables = false;

    boolean isRefreshing(DataTable table) {
        return refreshingTables;
    }

    private void configureViewScrollPane(JComponent view) {
        if (view != null) {
            if (view instanceof JScrollPane) {
                viewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
                view.setPreferredSize(viewScrollPane.getViewport().getViewSize());
            } else {
                viewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            }
        }
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        DataTable currentTable = getSelectedTable();
        for (GraphDataTable table : Lookup.getDefault().lookupAll(GraphDataTable.class)) {
            table.clear();
            if (currentTable == table) {
                refreshActionsAndTools(null);
            }
        }
        if (d.isLoading() && !refreshing.isBusy()) {
            refreshing.setBusy(true);
        }
    }

    @Override
    public void graphChanged(final MongkieDisplay d, Graph g) {
        //TODO: refresh only graph tables
        refreshTables(d);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tableButtonGroup = new javax.swing.ButtonGroup();
        glueAfterTableActions = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
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
    private javax.swing.Box.Filler glueAfterTableActions;
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
