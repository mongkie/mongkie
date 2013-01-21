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

import java.awt.Component;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import kobic.prefuse.display.DisplayListener;
import kobic.prefuse.display.NetworkDisplay;
import org.mongkie.lib.widgets.JPopupButton;
import org.mongkie.lib.widgets.WidgetUtilities;
import org.mongkie.ui.visualization.options.spi.Options;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.util.DataLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class OptionsToolbar extends JToolBar {

    public OptionsToolbar(MongkieDisplay display) {
        initComponents();
        add(new NodeLabelColumnButton(display, (InstantSearchPanel) add(new InstantSearchPanel(display))));
        for (Options o : Lookup.getDefault().lookupAll(Options.class)) {
            if (o.hasTools()) {
                addSeparator();
                for (JComponent c : o.createTools(display)) {
                    c.setFocusable(false);
                    add(c);
                }
            }
        }
    }

    private void initComponents() {
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        if (WidgetUtilities.isAquaLookAndFeel()) {
            setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
        super.setEnabled(enabled);
    }

    @Override
    public Component add(Component comp) {
        if (comp instanceof JButton) {
            WidgetUtilities.fixButtonUI((JButton) comp);
        }
        return super.add(comp);
    }

    private static class NodeLabelColumnButton extends JPopupButton {

        NodeLabelColumnButton(final MongkieDisplay display, final InstantSearchPanel searcher) {
            setMargin(new Insets(0, 2, 0, 0));
            setFocusable(false);
            setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/fontdown.png", false));
            setToolTipText("Choose a column used for node labels and search for");
            setChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    final String col = e != null ? (String) e.getSource() : null;
                    display.getVisualization().process(new Runnable() {
                        @Override
                        public void run() {
                            if (display.setNodeLableColumn(col)) {
                                searcher.setTextField(null);
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
                    clearItems();
                    setEnabled(false);
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
                                            updateLabelColumnSelection(g);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }
                        });
                        updateLabelColumnSelection(g);
                        this.g = g;
                    }
                }
            });
            updateLabelColumnSelection(display.getGraph());
        }

        private void updateLabelColumnSelection(Graph g) {
            clearItems(false);
            if (g.getNodeTable().getColumnCount() > 0) {
                for (String col : DataLib.getColumnNames(g.getNodeTable())) {
                    addItem(col, null);
                }
                Table table = g.getNodeTable();
                String label = g.getNodeLabelField();
                setSelectedItem(table.getColumn(label) == null
                        ? DataLib.getTypedColumnName(table, String.class, table.getColumnName(0)) : label);
                setEnabled(true);
            } else {
                setSelectedItem(null);
                setEnabled(false);
            }
        }
    }
}
