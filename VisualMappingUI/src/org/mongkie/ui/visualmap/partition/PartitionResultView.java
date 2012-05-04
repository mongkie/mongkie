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
import javax.swing.table.TableColumn;
import kobic.prefuse.Config;
import net.java.dev.colorchooser.ColorChooser;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.mongkie.visualmap.spi.partition.Partition;
import org.netbeans.swing.outline.DefaultOutlineCellRenderer;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
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

        Outline outline = getOutline();
        outline.setRootVisible(false);
        outline.setBackground(Color.WHITE);
        outline.setForeground(Color.BLACK);
        outline.setFont(outline.getFont().deriveFont(outline.getFont().getSize() - 1f));
        outline.setOpaque(true);
        outline.setRowMargin(4);
        outline.setShowHorizontalLines(false);
        outline.setShowVerticalLines(false);
        outline.setTableHeader(null);

        TableColumn visualCol = outline.getColumnModel().getColumn(1);
//        visualCol.setCellRenderer(new StringPropertyRenderer(outline.getFont()));
        visualCol.setPreferredWidth(120);
        visualCol.setMaxWidth(120);
        TableColumn colorCol = outline.getColumnModel().getColumn(2);
        colorCol.setCellRenderer(new ColorPropertyRenderer());
        colorCol.setCellEditor(new ColorChooserEditor());
        colorCol.setPreferredWidth(outline.getRowHeight());
        colorCol.setMaxWidth(outline.getRowHeight());

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

    private static class StringPropertyRenderer extends DefaultOutlineCellRenderer {

        public StringPropertyRenderer(Font f) {
            setFont(f);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            try {
                setIcon(EMPTY_ICON);
                setText(value == null ? "" : ((Property<String>) value).getValue());
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return this;
        }
    }
    private static final Icon EMPTY_ICON = new EmptyIcon(6, 6);
//    private static class TextPropertyRenderer extends JLabel implements TableCellRenderer {
//
//        public TextPropertyRenderer(Font f) {
//            setFont(f);
//            setOpaque(true);
//        }
//
//        @Override
//        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            if (isSelected) {
//                setBackground(table.getSelectionBackground());
//                setForeground(table.getSelectionForeground());
//            } else {
//                setBackground(table.getBackground());
//                setForeground(table.getForeground());
//            }
//            if (value != null) {
//                try {
//                    setIcon(EMPTY_ICON);
//                    setText(((Property<String>) value).getValue());
//                } catch (IllegalAccessException ex) {
//                    Exceptions.printStackTrace(ex);
//                } catch (InvocationTargetException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//            return this;
//        }
//    }

    private static class ColorPropertyRenderer extends DefaultOutlineCellRenderer {

        public ColorPropertyRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                try {
                    Color c = ((Property<Color>) value).getValue();
                    setBackground(c == null ? table.getBackground() : c);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return this;
        }
    }

    private static class ColorChooserEditor extends AbstractCellEditor implements TableCellEditor {

        private final ColorChooser chooser;
        private Object property;

        public ColorChooserEditor() {
            chooser = new ColorChooser();
            chooser.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(ColorChooser.PROP_COLOR)) {
                        try {
                            ((Property<Color>) property).setValue((Color) evt.getNewValue());
                        } catch (IllegalAccessException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        stopCellEditing();
                    } else if (evt.getPropertyName().equals(ColorChooser.PROP_PICKER_VISIBLE)
                            && !(Boolean) evt.getNewValue() && chooser.getTransientColor() == null) {
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
                return ((Property<Color>) property).getValue();
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return ColorLib.getColor(Config.COLOR_DEFAULT_AGGR_FILL);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                int row, int column) {
            property = value;
            try {
                chooser.setColor(((Property<Color>) value).getValue());
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
            return chooser;
        }
    }

    private static class OutlinePanel extends JPanel {

        public OutlinePanel(Outline outline) {
            setBackground(Color.WHITE);
            setLayout(new GridBagLayout());
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.fill = GridBagConstraints.BOTH;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.insets = new Insets(4, 0, 0, 4);
            add(outline, gridBagConstraints);
        }
    }
}
