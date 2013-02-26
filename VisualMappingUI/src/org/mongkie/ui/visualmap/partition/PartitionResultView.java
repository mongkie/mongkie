/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.ui.visualmap.partition;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import kobic.prefuse.Config;
import net.java.dev.colorchooser.ColorChooser;
import org.mongkie.visualmap.spi.partition.Partition;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import prefuse.util.ColorLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PartitionResultView extends OutlineView {

    private final OutlinePanel outlinePanel;
    private Partition partition;

    public PartitionResultView() {
        super("Partition");

        setBackground(Color.WHITE);
        setDragSource(false);
        setDropTarget(false);

        setPropertyColumns(
                NbBundle.getMessage(PartNode.class, "PartNode.column.visual.name"), NbBundle.getMessage(PartNode.class, "PartNode.column.visual.displayName"),
                NbBundle.getMessage(PartNode.class, "PartNode.column.color.name"), NbBundle.getMessage(PartNode.class, "PartNode.column.color.displayName"));

        final Outline outline = getOutline();
        outline.setAutoCreateColumnsFromModel(false);
        outline.setRootVisible(false);
        outline.setBackground(Color.WHITE);
        outline.setForeground(Color.BLACK);
        outline.setFont(outline.getFont().deriveFont(outline.getFont().getSize() - 1f));
        outline.setOpaque(true);
        outline.setRowMargin(4);
        outline.setShowHorizontalLines(false);
        outline.setShowVerticalLines(false);
        outline.setTableHeader(null);

        // Disable focus painting on the selected cell in outline view
        outline.getColumnModel().getColumn(0).setCellRenderer(new DefaultOutlineCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
        });
        TableColumn visualPropertyCol = outline.getColumnModel().getColumn(1);
        visualPropertyCol.setCellRenderer(new DefaultOutlineCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return outline.getDefaultRenderer(Node.Property.class).getTableCellRendererComponent(table, value, isSelected, false, row, column);
            }
        });
        visualPropertyCol.setPreferredWidth(120);
        visualPropertyCol.setMaxWidth(120);
        TableColumn groupColorCol = outline.getColumnModel().getColumn(2);
        groupColorCol.setCellRenderer(new GroupColorRenderer());
        groupColorCol.setCellEditor(new GroupColorEditor());
        groupColorCol.setPreferredWidth(outline.getRowHeight());
        groupColorCol.setMaxWidth(outline.getRowHeight());

        outlinePanel = new OutlinePanel(outline);
    }

    JPanel setPartition(ExplorerManager em, Partition partition) {
        this.partition = partition;
        getOutline().clearSelection();
        em.setRootContext(new AbstractNode(partition == null ? Children.LEAF : Children.create(new PartChildFactory(partition), true)) {
            @Override
            public Action[] getActions(boolean context) {
                return new Action[]{};
            }
        });
        return outlinePanel;
    }

    private static class GroupColorRenderer extends JLabel implements TableCellRenderer {

        GroupColorRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                try {
                    Color c = ((Property<Color>) value).getValue();
                    if (c != null) {
                        setBackground(c);
                        return this;
                    }
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            setBackground(table.getBackground());
            return this;
        }
    }

    private static class GroupColorEditor extends AbstractCellEditor implements TableCellEditor {

        final ColorChooser groupColorChooser;
        Node.Property<Color> property;

        GroupColorEditor() {
            groupColorChooser = new ColorChooser();
            groupColorChooser.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
                        try {
                            property.setValue((Color) evt.getNewValue());
                        } catch (IllegalAccessException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        stopCellEditing();
                    } else if (evt.getPropertyName().equals(ColorChooser.PROP_PICKER_VISIBLE)
                            && !(Boolean) evt.getNewValue() && groupColorChooser.getTransientColor() == null) {
                        cancelCellEditing();
                    }
                }
            });
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return false;
        }

        @Override
        public Object getCellEditorValue() {
            try {
                return property.getValue();
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return ColorLib.getColor(Config.COLOR_DEFAULT_AGGR_FILL);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            property = (Node.Property) value;
            try {
                groupColorChooser.setColor(((Node.Property<Color>) property).getValue());
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return groupColorChooser;
        }
    }

    private static class OutlinePanel extends JPanel {

        public OutlinePanel(Outline outline) {
            setBackground(Color.WHITE);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 4));
            add(outline, BorderLayout.CENTER);
        }
    }
}
