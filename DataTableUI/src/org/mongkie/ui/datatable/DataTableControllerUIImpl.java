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

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mongkie.datatable.DataTableControllerUI;
import org.mongkie.datatable.spi.DataAction;
import org.mongkie.datatable.spi.DataTable;
import static org.mongkie.datatable.spi.DataTable.EDGES;
import static org.mongkie.datatable.spi.DataTable.NODES;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = DataTableControllerUI.class)
public class DataTableControllerUIImpl implements DataTableControllerUI {

    @Override
    public DataTable selectTable(String name) {
        DataTableTopComponent tc = DataTableTopComponent.findInstance();
        if (!tc.isOpened()) {
            tc.open();
            tc.requestActive();
        }
        return tc.selectTable(name);
    }

    @Override
    public DataTable selectNodeTable() {
        return selectTable(NODES);
    }

    @Override
    public DataTable selectEdgeTable() {
        return selectTable(EDGES);
    }

    @Override
    public DataTable getSelectedTable() {
        return DataTableTopComponent.findInstance().getSelectedTable();
    }

    @Override
    public void setActivatedNodes(Node... nodes) {
        DataTableTopComponent.findInstance().setActivatedNodes(nodes);
    }

    @Override
    public void executeDataAction(final DataTable table, final DataAction a) {
        if (a.isEnabled(table)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    final DataAction.UI actionUI = a.getUI();
                    if (actionUI != null) {
                        actionUI.load(table, a);
                        JPanel settingPanel = actionUI.getPanel();
                        final DialogDescriptor dd = new DialogDescriptor(settingPanel, a.getName());
                        Object[] options = actionUI.getDialogOptions();
                        if (options != null && options.length > 0) {
                            dd.setOptions(options);
                        }
                        if (settingPanel instanceof ValidationPanel) {
                            final ValidationPanel vp = (ValidationPanel) settingPanel;
                            vp.addChangeListener(new ChangeListener() {
                                @Override
                                public void stateChanged(ChangeEvent e) {
                                    dd.setValid(!vp.isFatalProblem());
                                }
                            });
                            dd.setValid(!vp.isFatalProblem());
                        }
                        if (actionUI.close(DialogDisplayer.getDefault().notify(dd))) {
                            executeDataActionInOtherThread(table, a);
                        }
                    } else {
                        executeDataActionInOtherThread(table, a);
                    }
                }
            });
        }
    }

    private void executeDataActionInOtherThread(final DataTable table, final DataAction a) {
        new Thread() {
            @Override
            public void run() {
                a.execute(table);
            }
        }.start();
    }

    @Override
    public void refreshModel(final DataTable table, final boolean actionsOnly) {
        if (SwingUtilities.isEventDispatchThread()) {
            DataTableTopComponent.findInstance().refreshModel(table, actionsOnly);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    DataTableTopComponent.findInstance().refreshModel(table, actionsOnly);
                }
            });
        }
    }

    @Override
    public boolean isRefreshing(DataTable table) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Must be accessed in the AWT event dispatching thread");
        }
        return DataTableTopComponent.findInstance().isRefreshing(table);
    }
}
