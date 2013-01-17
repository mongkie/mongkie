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
package org.mongkie.ui.visualization.options;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.lib.widgets.JPopupButton;
import org.mongkie.ui.visualization.options.spi.Options;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.util.DataLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = Options.class, position = 2)
public class NodeOptions implements Options {

    @Override
    public String getName() {
        return "Nodes";
    }

    private void updateLabelColumnTool(JPopupButton b, Graph g) {
        b.clearItems(false);
        if (g.getNodeTable().getColumnCount() > 0) {
            for (String col : DataLib.getColumnNames(g.getNodeTable())) {
                b.addItem(col, null);
            }
            Table table = g.getNodeTable();
            String label = g.getNodeLabelField();
            b.setSelectedItem(table.getColumn(label) == null
                    ? DataLib.getTypedColumnName(table, String.class, table.getColumnName(0)) : label);
            b.setEnabled(true);
        } else {
            b.setSelectedItem(null);
            b.setEnabled(false);
        }
    }

    @Override
    public List<JComponent> createTools(final MongkieDisplay display) {
        List<JComponent> tools = new ArrayList<JComponent>();
        final JPopupButton labelColumnButton = new JPopupButton();
        labelColumnButton.putClientProperty(MongkieDisplay.class, display);
        labelColumnButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/fontdown.png", false));
        labelColumnButton.setToolTipText("Choose a column to be used for labeling the nodes");
        labelColumnButton.setChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final String col = e != null ? (String) e.getSource() : null;
                display.getVisualization().process(new Runnable() {
                    @Override
                    public void run() {
                        if (col == null || !col.equals(display.getGraph().getNodeLabelField())) {
                            display.getGraph().setNodeLabelField(col);
                            display.getNodeLabelRenderer().setLabelField(col);
                        }
                    }
                });
                display.getVisualization().repaint();
            }
        });
        display.addDisplayListener(new DisplayListener() {
            private Graph g;
            private TableListener l;

            @Override
            public void graphDisposing(NetworkDisplay d, Graph g) {
                labelColumnButton.clearItems();
                labelColumnButton.setEnabled(false);
            }

            @Override
            public void graphChanged(NetworkDisplay d, final Graph g) {
                if (this.g != g) {
                    if (this.g != null) {
                        this.g.getNodeTable().removeTableListener(l);
                    }
                    g.getNodeTable().addTableListener(l = new TableListener() {
                        @Override
                        public void tableChanged(Table t, int start, int end, int col, int type) {
                            if (col != EventConstants.ALL_COLUMNS) {
                                switch (type) {
                                    case EventConstants.DELETE:
                                    case EventConstants.INSERT:
                                        // case EventConstants.UPDATE:
                                        updateLabelColumnTool(labelColumnButton, g);
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }
                    });
                    updateLabelColumnTool(labelColumnButton, g);
                    this.g = g;
                }
            }
        });
        updateLabelColumnTool(labelColumnButton, display.getGraph());
        tools.add(labelColumnButton);
        return tools;
    }

    @Override
    public JPanel createSettingPanel(MongkieDisplay display) {
        return new NodeSettingPanel(display);
    }

    @Override
    public boolean hasTools() {
        return true;
    }

    @Override
    public boolean hasSettingPanel() {
        return true;
    }
}
