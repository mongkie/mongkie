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
package org.mongkie.ui.datatable.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import org.jdesktop.swingx.JXSearchField;
import org.mongkie.datatable.spi.DataTable;
import org.mongkie.visualization.search.SearchController;
import org.openide.util.Lookup;
import prefuse.data.Schema;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class FilterToolsPanel extends javax.swing.JPanel implements DataTable.Tool<AbstractDataTable>, ItemListener {

    private final AbstractDataTable table;
    private static final String NONE = "---None";
    private static final String ALL_COLUMNS = "---All columns";
    private static final String PROP_FILTERCOLUMN = FilterToolsPanel.class.getName() + "_FilterColumn";

    /**
     * Creates new form FilterToolsPanel
     */
    FilterToolsPanel(AbstractDataTable table) {
        this.table = table;
        initComponents();

        ((JXSearchField) filterInputTextField).setInstantSearchDelay(500); // Default is 50 milliseconds
        ((JXSearchField) filterInputTextField).setCancelAction(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("clear");
                        filterInputTextField.setText(null);
                    }
                });
        ((JXSearchField) filterInputTextField).setAction(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text = filterInputTextField.getText();
                        if (!text.isEmpty()) {
                            System.out.println("filter: " + text);
                        }
                    }
                });
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filterLabel = new javax.swing.JLabel();
        filterInputTextField = new JXSearchField();
        ((JXSearchField) filterInputTextField).setLayoutStyle(JXSearchField.LayoutStyle.VISTA);
        ((JXSearchField) filterInputTextField).setUseNativeSearchFieldIfPossible(false);
        ((JXSearchField) filterInputTextField).setPromptBackround(java.awt.Color.white);
        filterColumnComboBox = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        setFocusable(false);
        setRequestFocusEnabled(false);

        filterLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/funnel.png"))); // NOI18N
        filterLabel.setText(org.openide.util.NbBundle.getMessage(FilterToolsPanel.class, "FilterToolsPanel.filterLabel.text")); // NOI18N

        filterInputTextField.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        filterInputTextField.setText(org.openide.util.NbBundle.getMessage(FilterToolsPanel.class, "FilterToolsPanel.filterInputTextField.text")); // NOI18N
        filterInputTextField.setEnabled(false);
        filterInputTextField.setPreferredSize(new java.awt.Dimension(100, 24));

        filterColumnComboBox.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        filterColumnComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "NONE" }));
        filterColumnComboBox.setEnabled(false);
        filterColumnComboBox.setPreferredSize(new java.awt.Dimension(100, 24));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(filterLabel)
                .addGap(8, 8, 8)
                .addComponent(filterInputTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(filterColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(filterInputTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(filterLabel))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {filterColumnComboBox, filterInputTextField});

    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox filterColumnComboBox;
    private javax.swing.JTextField filterInputTextField;
    private javax.swing.JLabel filterLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public AbstractDataTable getDataTable() {
        return table;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void refresh(boolean clear) {
        clear = clear || !Lookup.getDefault().lookup(SearchController.class).isStringColumnAvailable(
                table.getModel().getDisplay().getDataViewSupport(table.getDataGroup()).getOutlineSchema());
        refreshFilterColumns(clear);
        if (clear) {
            filterInputTextField.setText(null);
            filterInputTextField.setEnabled(false);
        } else {
            filterInputTextField.setEnabled(true);
        }
    }

    private void refreshFilterColumns(boolean clear) {
        if (clear) {
            filterColumnComboBox.removeItemListener(this);
            filterColumnComboBox.removeAllItems();
            filterColumnComboBox.addItem(NONE);
            filterColumnComboBox.setEnabled(false);
        } else {
            filterColumnComboBox.removeItemListener(this);
            String filterColumn = (String) table.getModel().getTable().getClientProperty(PROP_FILTERCOLUMN);
            filterColumnComboBox.removeAllItems();
            filterColumnComboBox.addItem(ALL_COLUMNS);
            Schema s = table.getModel().getDisplay().getDataViewSupport(table.getDataGroup()).getOutlineSchema();
            for (int i = 0; i < s.getColumnCount(); i++) {
                if (s.getColumnType(i) == String.class) { // only for string types
                    filterColumnComboBox.addItem(s.getColumnName(i));
                }
            }
            filterColumnComboBox.setEnabled(true);
            if (filterColumn == null) {
                filterColumnComboBox.setSelectedIndex(0);
            } else {
                filterColumnComboBox.setSelectedItem(filterColumn);
            }
            table.getModel().getTable().putClientProperty(PROP_FILTERCOLUMN, filterColumnComboBox.getSelectedItem());
            filterColumnComboBox.addItemListener(this);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            table.getModel().getTable().putClientProperty(PROP_FILTERCOLUMN, e.getItem());
        }
    }
}
