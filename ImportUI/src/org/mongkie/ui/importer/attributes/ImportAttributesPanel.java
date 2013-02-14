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

/*
 * ImportAttributesPanel.java
 *
 * Created on Sep 1, 2011, 5:40:00 PM
 */
package org.mongkie.ui.importer.attributes;

import com.csvreader.CsvReader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import org.mongkie.lib.widgets.BusyLabel;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.ui.importer.csv.ImportCSVInnerPanel;
import org.mongkie.util.io.DialogFileFilter;
import org.mongkie.visualization.VisualizationController;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.io.AbstractTextTableReader;
import prefuse.util.DataLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ImportAttributesPanel extends javax.swing.JPanel {

    private File selectedFile = null;
    private ValidationPanel validationPanel = null;
    private BusyLabel preview;
    private boolean duplicatedColumnNames;
    private boolean failToRead;
    private boolean noColumn;
    private final LongTaskExecutor executor;
    private static final int PREVIEW_MAX_ROWS = 25;
    private final List<ChangeListener> listeners = Collections.synchronizedList(new ArrayList<ChangeListener>());
    private final TableCellRenderer defaultTableHeaderRenderer;
    private final Graph g;

    /** Creates new form ImportAttributesPanel */
    public ImportAttributesPanel() {
        initComponents();
        defaultTableHeaderRenderer = previewTable.getTableHeader().getDefaultRenderer();
        preview = new BusyLabel(NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.csvReader.busyText"),
                previewAttributesScrollPane, previewTable);
        executor = new LongTaskExecutor(true, "AttributesImporter");
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                if (t instanceof OutOfMemoryError) {
                    return;
                }
                String message = t.getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getCause().getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        });

        g = Lookup.getDefault().lookup(VisualizationController.class).getDisplay().getGraph();
        forNodeRadioButton.putClientProperty(FOR_TABLE_KEY, ForTable.NODE_TABLE);
        forNodeRadioButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    refreshKeyAttributesChooser(g.getNodeLabelField(), ForTable.getColumnNames(ForTable.NODE_TABLE, g));
                    fireStateChangeEvent(new ChangeEvent(validationPanel));
                }
            }
        });
        forEdgeRadioButton.putClientProperty(FOR_TABLE_KEY, ForTable.EDGE_TABLE);
        forEdgeRadioButton.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    refreshKeyAttributesChooser(g.getEdgeLabelField(), ForTable.getColumnNames(ForTable.EDGE_TABLE, g));
                    fireStateChangeEvent(new ChangeEvent(validationPanel));
                }
            }
        });
        keyAttributeComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                refreshKeyAttributesFromNetwork(getTargetTable(), e.getStateChange() == ItemEvent.DESELECTED ? null : (String) e.getItem());
            }
        });
        refreshKeyAttributesChooser(g.getNodeLabelField(), ForTable.getColumnNames(ForTable.NODE_TABLE, g));
        refreshKeyAttributesFromNetwork(g.getNodeTable(), g.getNodeLabelField());

        keyColumnComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                keyColumnChanged((String) e.getItem());
            }
        });

        hasHeaderCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (selectedFile != null) {
                    refreshPreview();
                }
            }
        });

        previewTable.getTableHeader().addMouseListener(new MouseAdapter() {

            final NotifyDescriptor.InputLine editor = new NotifyDescriptor.InputLine("Column Name:", "Edit Column Name");

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2) {
                    JTableHeader th = (JTableHeader) e.getSource();
                    int colIdx = getClickedColumnIndex(th, e.getPoint());
                    TableColumn column = th.getColumnModel().getColumn(colIdx);
                    String currentName = (String) column.getHeaderValue();
                    String newName = openEditor(currentName);
                    if (!newName.isEmpty() && !newName.equals(currentName) && !isDuplicatedName(th, newName)) {
                        column.setHeaderValue(newName);
                        th.resizeAndRepaint();
                        headerNameChanged(column, currentName, newName);
                    }
                }
            }

            private boolean isDuplicatedName(JTableHeader header, String name) {
                for (Enumeration<TableColumn> columns = header.getColumnModel().getColumns(); columns.hasMoreElements();) {
                    if (columns.nextElement().getHeaderValue().equals(name)) {
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message("<html><b>" + name + "</b> already exists.</html>", NotifyDescriptor.ERROR_MESSAGE));
                        return true;
                    }
                }
                return false;
            }

            private String openEditor(String currentName) {
                editor.setInputText(currentName);
                Object retVal = DialogDisplayer.getDefault().notify(editor);
                return (retVal == NotifyDescriptor.OK_OPTION) ? editor.getInputText() : currentName;
            }

            private int getClickedColumnIndex(JTableHeader th, Point p) {
                TableColumnModel model = th.getColumnModel();
                for (int col = 0; col < model.getColumnCount(); col++) {
                    if (th.getHeaderRect(col).contains(p)) {
                        return col;
                    }
                }
                return -1;
            }
        });
    }

    private static enum ForTable {

        NODE_TABLE, EDGE_TABLE;

        static Table getTable(ForTable forTable, Graph g) {
            switch (forTable) {
                case NODE_TABLE:
                    return g.getNodeTable();
                case EDGE_TABLE:
                    return g.getEdgeTable();
                default:
                    return null;
            }
        }

        static String[] getColumnNames(ForTable forTable, Graph g) {
            switch (forTable) {
                case NODE_TABLE:
                    return DataLib.getColumnNames(g.getNodeTable());
                case EDGE_TABLE:
                    String sourceField = g.getEdgeSourceField();
                    String targetField = g.getEdgeTargetField();
                    Table edgeTable = g.getEdgeTable();
                    String[] columnNames = new String[edgeTable.getColumnCount() - 2];
                    for (int i = 0, j = 0; i < columnNames.length; j++) {
                        String fieldName = edgeTable.getColumnName(j);
                        if (fieldName.equals(sourceField) || fieldName.equals(targetField)) {
                            continue;
                        }
                        columnNames[i++] = fieldName;
                    }
                    return columnNames;
                default:
                    return null;
            }
        }
    }

    public Table getTargetTable() {
        for (Enumeration<AbstractButton> forButtons = attributesForGroup.getElements(); forButtons.hasMoreElements();) {
            AbstractButton radioButton = forButtons.nextElement();
            if (radioButton.isSelected()) {
                return ForTable.getTable((ForTable) radioButton.getClientProperty(FOR_TABLE_KEY), g);
            }
        }
        return null;
    }
    private static final String FOR_TABLE_KEY = "ATRRIBUTES_FOR_TABLE";

    public File getAttributeFile() {
        return selectedFile;
    }

    public String[] getHeaderNames() {
        JTableHeader previewTableHeader = previewTable.getTableHeader();
        String[] headerNames = new String[previewTableHeader.getColumnModel().getColumnCount()];
        for (int i = 0; i < headerNames.length; i++) {
            headerNames[i] = (String) previewTableHeader.getColumnModel().getColumn(i).getHeaderValue();
        }
        return headerNames;
    }

    private void headerNameChanged(TableColumn col, String oldName, String newName) {
        int itemCount = keyColumnComboBox.getItemCount();
        int selectedItemIdx = keyColumnComboBox.getSelectedIndex();
        for (int i = 0; i < itemCount; i++) {
            if (keyColumnComboBox.getItemAt(i).equals(oldName)) {
                keyColumnComboBox.removeItemAt(i);
                keyColumnComboBox.insertItemAt(newName, i);
            }
        }
        keyColumnComboBox.setSelectedIndex(selectedItemIdx);
    }

    private void refreshKeyAttributesChooser(String keyField, String... columnNames) {
        keyAttributeComboBox.removeAllItems();
        for (int i = 0; i < columnNames.length; i++) {
            keyAttributeComboBox.addItem(columnNames[i]);
        }
        keyAttributeComboBox.setSelectedItem(keyField != null ? keyField : keyAttributeComboBox.getItemAt(0));
    }

    private void refreshKeyAttributesFromNetwork(Table table, String keyField) {
        DefaultListModel listModel = (DefaultListModel) keyAttributesList.getModel();
        listModel.clear();
        if (keyField != null && !keyField.isEmpty()) {
            List attributes = table.getColumnValues(keyField);
            Collections.sort(attributes);
            int count = 0;
            for (Object attribute : new LinkedHashSet(attributes)) {
                if (++count > PREVIEW_MAX_ROWS) {
                    break;
                }
                listModel.addElement(attribute);
            }
        }
    }

    public ValidationPanel getValidationPanel() {
        if (validationPanel != null) {
            return validationPanel;
        }

        validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);

        ValidationGroup validationGroup = validationPanel.getValidationGroup();
        validationGroup.add(filePathTextField, new Validator<String>() {

            @Override
            public void validate(Problems problems, String compName, String model) {
                if (selectedFile == null) {
                    problems.add(NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.validation.info.chooseFile"), Severity.INFO);
                } else if (!selectedFile.exists()) {
                    problems.add(NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.validation.fatal.fileNotExist"), Severity.FATAL);
                } else if (noColumn) {
                    problems.add(NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.validation.fatal.noColumn"), Severity.FATAL);
                } else if (failToRead) {
                    problems.add(NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.validation.fatal.canNotRead"), Severity.FATAL);
                } else if (duplicatedColumnNames) {
                    problems.add(NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.validation.warning.duplicatedColumnNames"), Severity.FATAL);
                }
            }

            @Override
            public Class<String> modelType() {
                return String.class;
            }
        });
        validationGroup.add(keyAttributeComboBox, StringValidators.REQUIRE_NON_EMPTY_STRING);

        return validationPanel;
    }

    private void fireStateChangeEvent(ChangeEvent e) {
        synchronized (listeners) {
            for (Iterator<ChangeListener> listenerIter = listeners.iterator(); listenerIter.hasNext();) {
                listenerIter.next().stateChanged(e);
            }
        }
    }

    public void addChangeListener(ChangeListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public boolean isOk() {
        return selectedFile != null && !preview.isBusy() && !validationPanel.isFatalProblem();
    }

    private void refreshPreview() {
        previewTable.setModel(EMPTY_TABLE_MODEL);
        if (selectedFile != null && selectedFile.exists()) {
            preview.setBusy(true);
            duplicatedColumnNames = false;
            noColumn = false;
            failToRead = false;
            refreshUI();

            executor.execute(null, new Runnable() {

                @Override
                public void run() {
                    String[] columns = new String[0];
                    try {
                        CsvReader reader = new CsvReader(new FileInputStream(selectedFile), ',', Charset.forName("UTF-8"));
                        reader.setTrimWhitespace(false);
                        try {
                            reader.readHeaders();
                            if ((Boolean) hasHeader()) {
                                columns = reader.getHeaders();
                            } else {
                                columns = new String[reader.getHeaderCount()];
                                for (int i = 0; i < columns.length; i++) {
                                    columns[i] = AbstractTextTableReader.getDefaultHeader(i);
                                }
                            }
                        } catch (IOException ex) {
                            // Some charsets can be problematic with unreal columns length. Don't show table when there are problems
                        }

                        //Check for repeated column names:
                        if (new HashSet<String>(Arrays.asList(columns)).size() < columns.length) {
                            duplicatedColumnNames = true;
                        }

                        ArrayList<String[]> records = new ArrayList<String[]>();
                        if (columns.length > 0) {
                            if (!(Boolean) hasHeader()) {
                                records.add(reader.getHeaders());
                            }
                            String[] record;
                            while (reader.readRecord() && records.size() < PREVIEW_MAX_ROWS) {
                                record = new String[columns.length];
                                for (int i = 0; i < columns.length; i++) {
                                    record[i] = reader.get(i);
                                }
                                records.add(record);
                            }
                        } else {
                            noColumn = true;
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                    NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportAttributesPanel.csvReader.warning.noHeader", selectedFile.getName()),
                                    NotifyDescriptor.WARNING_MESSAGE));
                        }
                        reader.close();
                        final String[] columnNames = columns;
                        final String[][] values = records.toArray(new String[0][]);
                        previewTable.setModel(new TableModel() {

                            @Override
                            public int getRowCount() {
                                return values.length;
                            }

                            @Override
                            public int getColumnCount() {
                                return columnNames.length;
                            }

                            @Override
                            public String getColumnName(int columnIndex) {
                                return columnNames[columnIndex];
                            }

                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                return String.class;
                            }

                            @Override
                            public boolean isCellEditable(int rowIndex, int columnIndex) {
                                return false;
                            }

                            @Override
                            public Object getValueAt(int rowIndex, int columnIndex) {
                                if (values[rowIndex].length > columnIndex) {
                                    return values[rowIndex][columnIndex];
                                } else {
                                    return null;
                                }
                            }

                            @Override
                            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                            }

                            @Override
                            public void addTableModelListener(TableModelListener l) {
                            }

                            @Override
                            public void removeTableModelListener(TableModelListener l) {
                            }
                        });
                    } catch (Exception ex) {
                        failToRead = true;
                        throw new RuntimeException(
                                NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportAttributesPanel.csvReader.error", ex.getMessage()), ex);
                    } finally {
                        final String[] columnNames = columns;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                preview.setBusy(false);
                                refreshColumnChoosers(columnNames);
                                refreshUI();
                            }
                        });
                    }
                }
            });
        } else {
            refreshColumnChoosers();
            refreshUI();
        }
    }
    private static final TableModel EMPTY_TABLE_MODEL = new DefaultTableModel();

    public boolean hasHeader() {
        return hasHeaderCheckBox.isSelected();
    }
    
    public boolean isMultipleValueEnabled() {
        return enableMultiValueCheckbox.isSelected();
    }

    public String getAttributeKeyField() {
        return (String) keyColumnComboBox.getSelectedItem();
    }

    public String getNetworkKeyField() {
        return (String) keyAttributeComboBox.getSelectedItem();
    }

    private void refreshColumnChoosers(String... columnNames) {
        keyColumnComboBox.removeAllItems();
        for (String column : columnNames) {
            keyColumnComboBox.addItem(column);
        }
        String selectedKey = columnNames.length > 0 ? columnNames[0] : null;
        keyColumnComboBox.setSelectedItem(selectedKey);
        keyColumnChanged(selectedKey);
    }

    private void keyColumnChanged(final String keyField) {
        for (int i = 0; i < previewTable.getColumnCount(); i++) {
            previewTable.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    c.setForeground(keyField != null && previewTable.getTableHeader().getColumnModel().getColumn(column).getHeaderValue().equals(keyField) ? Color.BLUE : Color.BLACK);
                    return c;
                }
            });
        }
        previewTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultTableHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(keyField != null && previewTable.getTableHeader().getColumnModel().getColumn(column).getHeaderValue().equals(keyField) ? Color.BLUE : Color.BLACK);
                return c;
            }
        });

        previewTable.repaint();
    }

    private void refreshUI() {
        boolean loadingPreview = preview.isBusy();
        hasHeaderCheckBox.setEnabled(!loadingPreview);
        openFileButton.setEnabled(!loadingPreview);
        filePathTextField.setText(filePathTextField.getText());
        fireStateChangeEvent(new ChangeEvent(validationPanel));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        attributesForGroup = new javax.swing.ButtonGroup();
        importAttributesHeader = new org.jdesktop.swingx.JXHeader();
        chooseFileLabel = new javax.swing.JLabel();
        attributesForLabel = new javax.swing.JLabel();
        forNodeRadioButton = new javax.swing.JRadioButton();
        forEdgeRadioButton = new javax.swing.JRadioButton();
        hasHeaderCheckBox = new javax.swing.JCheckBox();
        keyAttributeLabel = new javax.swing.JLabel();
        keyColumnComboBox = new javax.swing.JComboBox();
        keyColumnLabel = new javax.swing.JLabel();
        previewSeparator = new org.jdesktop.swingx.JXTitledSeparator();
        previewAttributesScrollPane = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();
        editColumnExplainLabel = new javax.swing.JLabel();
        keyAttributeListLabel = new javax.swing.JLabel();
        keyAttributesScrollPane = new javax.swing.JScrollPane();
        keyAttributesList = new javax.swing.JList();
        tableImportIcon = new javax.swing.JLabel();
        keyAttributeComboBox = new javax.swing.JComboBox();
        filePathTextField = new javax.swing.JTextField();
        openFileButton = new javax.swing.JButton();
        mappingSeparator = new org.jdesktop.swingx.JXTitledSeparator();
        enableMultiValueCheckbox = new javax.swing.JCheckBox();

        setPreferredSize(new java.awt.Dimension(600, 480));

        importAttributesHeader.setDescription(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.importAttributesHeader.description")); // NOI18N
        importAttributesHeader.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/importer/resources/csv_text.png"))); // NOI18N
        importAttributesHeader.setTitle(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.importAttributesHeader.title")); // NOI18N

        chooseFileLabel.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.chooseFileLabel.text")); // NOI18N

        attributesForLabel.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.attributesForLabel.text")); // NOI18N

        attributesForGroup.add(forNodeRadioButton);
        forNodeRadioButton.setSelected(true);
        forNodeRadioButton.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.forNodeRadioButton.text")); // NOI18N
        forNodeRadioButton.setFocusPainted(false);

        attributesForGroup.add(forEdgeRadioButton);
        forEdgeRadioButton.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.forEdgeRadioButton.text")); // NOI18N
        forEdgeRadioButton.setFocusPainted(false);

        hasHeaderCheckBox.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        hasHeaderCheckBox.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.hasHeaderCheckBox.text")); // NOI18N
        hasHeaderCheckBox.setFocusPainted(false);

        keyAttributeLabel.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.keyAttributeLabel.text")); // NOI18N

        keyColumnLabel.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.keyColumnLabel.text")); // NOI18N

        previewSeparator.setTitle(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.previewSeparator.title")); // NOI18N

        previewAttributesScrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        previewAttributesScrollPane.setViewportView(previewTable);

        editColumnExplainLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        editColumnExplainLabel.setForeground(new java.awt.Color(242, 70, 200));
        editColumnExplainLabel.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.editColumnExplainLabel.text")); // NOI18N

        keyAttributeListLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        keyAttributeListLabel.setForeground(java.awt.Color.blue);
        keyAttributeListLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/importer/resources/key_mini.png"))); // NOI18N
        keyAttributeListLabel.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.keyAttributeListLabel.text")); // NOI18N

        keyAttributesList.setForeground(java.awt.Color.blue);
        keyAttributesList.setModel(new DefaultListModel());
        keyAttributesList.setCellRenderer(new DefaultListCellRenderer() {

            public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, false, false);

                return this;
            }
        });
        keyAttributesScrollPane.setViewportView(keyAttributesList);

        tableImportIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/importer/resources/arrow.png"))); // NOI18N
        tableImportIcon.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.tableImportIcon.text")); // NOI18N

        keyAttributeComboBox.setName("Key attribute"); // NOI18N

        filePathTextField.setEditable(false);
        filePathTextField.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.filePathTextField.text")); // NOI18N

        openFileButton.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        openFileButton.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.openFileButton.text")); // NOI18N
        openFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileButtonActionPerformed(evt);
            }
        });

        mappingSeparator.setTitle(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.mappingSeparator.title")); // NOI18N

        enableMultiValueCheckbox.setText(org.openide.util.NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.enableMultiValueCheckbox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(importAttributesHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(editColumnExplainLabel)
                            .addComponent(previewAttributesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(tableImportIcon)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(keyAttributesScrollPane, 0, 0, Short.MAX_VALUE)
                            .addComponent(keyAttributeListLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)))
                    .addComponent(previewSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keyColumnComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(keyColumnLabel))
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(keyAttributeLabel)
                            .addComponent(keyAttributeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(attributesForLabel)
                            .addComponent(chooseFileLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(filePathTextField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(openFileButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(forNodeRadioButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(forEdgeRadioButton))
                            .addComponent(hasHeaderCheckBox)))
                    .addComponent(mappingSeparator, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(enableMultiValueCheckbox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(importAttributesHeader, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(attributesForLabel)
                    .addComponent(forNodeRadioButton)
                    .addComponent(forEdgeRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(openFileButton)
                    .addComponent(filePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseFileLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hasHeaderCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mappingSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyColumnLabel)
                    .addComponent(keyAttributeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keyAttributeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableMultiValueCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addComponent(tableImportIcon)
                        .addGap(63, 63, 63))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(keyAttributeListLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(editColumnExplainLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(keyAttributesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addComponent(previewAttributesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {filePathTextField, openFileButton});

    }// </editor-fold>//GEN-END:initComponents

private void openFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileButtonActionPerformed
    final String LAST_PATH = "ImportAttributes.fileChooser.lastPath";
    String lastPath = NbPreferences.forModule(ImportAttributesPanel.class).get(LAST_PATH, null);
    final JFileChooser chooser = new JFileChooser(lastPath);
    chooser.setAcceptAllFileFilterUsed(false);
    DialogFileFilter dialogFileFilter = new DialogFileFilter(
            NbBundle.getMessage(ImportAttributesPanel.class, "ImportAttributesPanel.fileChooser.csvFilter.description"));
    dialogFileFilter.addExtensions(".csv", ".txt");
    chooser.addChoosableFileFilter(dialogFileFilter);
    chooser.setSelectedFile(selectedFile);
    int returnFile = chooser.showOpenDialog(null);
    if (returnFile != JFileChooser.APPROVE_OPTION) {
        return;
    }
    selectedFile = chooser.getSelectedFile();
    String path = selectedFile.getAbsolutePath();
    filePathTextField.setText(path);
    //Save last path
    String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
    NbPreferences.forModule(ImportAttributesPanel.class).put(LAST_PATH, defaultDirectory);
    refreshPreview();
}//GEN-LAST:event_openFileButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup attributesForGroup;
    private javax.swing.JLabel attributesForLabel;
    private javax.swing.JLabel chooseFileLabel;
    private javax.swing.JLabel editColumnExplainLabel;
    private javax.swing.JCheckBox enableMultiValueCheckbox;
    private javax.swing.JTextField filePathTextField;
    private javax.swing.JRadioButton forEdgeRadioButton;
    private javax.swing.JRadioButton forNodeRadioButton;
    private javax.swing.JCheckBox hasHeaderCheckBox;
    private org.jdesktop.swingx.JXHeader importAttributesHeader;
    private javax.swing.JComboBox keyAttributeComboBox;
    private javax.swing.JLabel keyAttributeLabel;
    private javax.swing.JLabel keyAttributeListLabel;
    private javax.swing.JList keyAttributesList;
    private javax.swing.JScrollPane keyAttributesScrollPane;
    private javax.swing.JComboBox keyColumnComboBox;
    private javax.swing.JLabel keyColumnLabel;
    private org.jdesktop.swingx.JXTitledSeparator mappingSeparator;
    private javax.swing.JButton openFileButton;
    private javax.swing.JScrollPane previewAttributesScrollPane;
    private org.jdesktop.swingx.JXTitledSeparator previewSeparator;
    private javax.swing.JTable previewTable;
    private javax.swing.JLabel tableImportIcon;
    // End of variables declaration//GEN-END:variables
}
