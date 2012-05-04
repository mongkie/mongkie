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
package org.mongkie.ui.importer.csv;

import com.csvreader.CsvReader;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import org.mongkie.lib.widgets.BusyLabel;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.util.io.DialogFileFilter;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import prefuse.data.io.AbstractTextTableReader;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class ImportCSVInnerPanel extends JPanel {
    
    private ValidationPanel validationPanel;
    private File selectedFile = null;
    private ImportCSVWizardPanel wizardPanel;
    private boolean duplicatedColumnNames;
    private boolean failToRead;
    private boolean noColumn;
    private BusyLabel preview;
    private final LongTaskExecutor executor;
    private static final int PREVIEW_MAX_ROWS = 25;
    private boolean skippable = true;
    
    public ImportCSVInnerPanel(ImportCSVWizardPanel wizardPanel) {
        initUI();
        this.wizardPanel = wizardPanel;
        preview = new BusyLabel(NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.csvReader.busyText"),
                getPreviewScroll(), getPreviewTable());
        executor = new LongTaskExecutor(true, "CSVImporter");
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
        getFileButton().addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        getSkipCheckBox().addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshUI();
            }
        });
        getHasHeaderCheckBox().addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (selectedFile != null) {
                    refreshPreview();
                }
            }
        });
        getPreviewTable().getTableHeader().addMouseListener(new MouseAdapter() {
            
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
    
    protected abstract void headerNameChanged(TableColumn col, String oldName, String newName);
    
    protected final void comboBoxItemChanged(JComboBox columnComboBox, String oldItemValue, String newItemValue) {
        int itemCount = columnComboBox.getItemCount();
        int selectedItemIdx = columnComboBox.getSelectedIndex();
        for (int i = 0; i < itemCount; i++) {
            if (columnComboBox.getItemAt(i).equals(oldItemValue)) {
                columnComboBox.removeItemAt(i);
                columnComboBox.insertItemAt(newItemValue, i);
            }
        }
        columnComboBox.setSelectedIndex(selectedItemIdx);
    }
    
    protected abstract void initUI();
    
    protected abstract JScrollPane getPreviewScroll();
    
    protected abstract JTable getPreviewTable();
    
    private void refreshPreview() {
        final JTable previewTable = getPreviewTable();
        previewTable.setModel(EMPTY_TABLE_MODEL);
        if (fileExists()) {
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
                        CsvReader reader = new CsvReader(new FileInputStream(selectedFile), getSelectedSeparator(), getSelectedCharset());
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
                                    NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.csvReader.warning.noHeader", selectedFile.getName()),
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
                                NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.csvReader.error", ex.getMessage()), ex);
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
    
    protected char getSelectedSeparator() {
        return ',';
    }
    
    protected Charset getSelectedCharset() {
        return Charset.forName("UTF-8");
    }
    
    protected abstract void refreshColumnChoosers(String... columnNames);
    
    protected final ValidationPanel getValidationPanel() {
        if (validationPanel != null) {
            return validationPanel;
        }
        
        validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(this);
        ValidationGroup validationGroup = validationPanel.getValidationGroup();
        addColumnChooserValidators(validationGroup);
        validationGroup.add(getPathTextField(), new Validator<String>() {
            
            @Override
            public void validate(Problems problems, String compName, String model) {
                if (isValidationSuspended()) {
                    return;
                }
                if (selectedFile == null) {
                    problems.add(NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.validation.info.chooseFile"), Severity.INFO);
                } else if (!selectedFile.exists()) {
                    problems.add(NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.validation.fatal.fileNotExist"), Severity.FATAL);
                } else if (noColumn) {
                    problems.add(NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.validation.fatal.noColumn"), Severity.FATAL);
                } else if (failToRead) {
                    problems.add(NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.validation.fatal.canNotRead"), Severity.FATAL);
                } else if (duplicatedColumnNames) {
                    problems.add(NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.validation.warning.duplicatedColumnNames"), Severity.FATAL);
                }
            }
            
            @Override
            public Class<String> modelType() {
                return String.class;
            }
        });
        
        validationPanel.setName(getName());
        return validationPanel;
    }
    
    protected ImportCSVWizardPanel getWizardPanel() {
        return wizardPanel;
    }
    
    protected abstract void addColumnChooserValidators(ValidationGroup validationGroup);
    
    protected abstract JTextField getPathTextField();
    
    protected File getSelectedFile() {
        return isSkipped() ? null : selectedFile;
    }
    
    protected String[] getHeaderNames() {
        JTableHeader previewTableHeader = getPreviewTable().getTableHeader();
        String[] headerNames = new String[previewTableHeader.getColumnModel().getColumnCount()];
        for (int i = 0; i < headerNames.length; i++) {
            headerNames[i] = (String) previewTableHeader.getColumnModel().getColumn(i).getHeaderValue();
        }
        return headerNames;
    }
    
    protected boolean isSkipped() {
        return isSkippable() && getSkipCheckBox().isSelected();
    }
    
    protected boolean isProblem() {
        return !isValidFile() || !isValidColumns();
    }
    
    protected boolean isValidFile() {
        return isSkipped()
                || (fileExists() && !noColumn && !failToRead && !duplicatedColumnNames && !preview.isBusy());
    }
    
    protected abstract boolean isValidColumns();
    
    private boolean fileExists() {
        return selectedFile != null && selectedFile.exists();
    }
    
    protected final void refreshUI() {
        boolean skip = getSkipCheckBox().isSelected();
        boolean loadingPreview = preview.isBusy();
        getSkipCheckBox().setEnabled(isSkippable() && !loadingPreview);
        getHasHeaderCheckBox().setEnabled(!skip && !loadingPreview);
        getPathTextField().setEnabled(!skip);
        getFileButton().setEnabled(!skip && !loadingPreview);
        setColumnChoosersEnabled(!skip);
        
        validateAll();
        wizardPanel.fireChangeEvent();
    }
    
    protected boolean isSkippable() {
        return skippable;
    }
    
    protected void setSkippable(boolean skippable) {
        this.skippable = skippable;
        JCheckBox skipCheckBox = getSkipCheckBox();
        skipCheckBox.setEnabled(skippable);
        if (!skippable && skipCheckBox.isSelected()) {
            skipCheckBox.setSelected(false);
            refreshUI();
        }
    }
    
    protected abstract JCheckBox getSkipCheckBox();
    
    protected abstract JButton getFileButton();
    
    protected abstract JCheckBox getHasHeaderCheckBox();
    
    Object hasHeader() {
        return getHasHeaderCheckBox().isSelected();
    }
    
    protected abstract void setColumnChoosersEnabled(boolean enabled);
    
    protected void validateAll() {
        JTextField pathTextField = getPathTextField();
        pathTextField.setText(pathTextField.getText());
    }
    
    private void openFile() {
        final String LAST_PATH = "ImportCSV.fileChooser.lastPath";
        String lastPath = NbPreferences.forModule(ImportCSVInnerPanel.class).get(LAST_PATH, null);
        final JFileChooser chooser = new JFileChooser(lastPath);
        chooser.setAcceptAllFileFilterUsed(false);
        DialogFileFilter dialogFileFilter = new DialogFileFilter(
                NbBundle.getMessage(ImportCSVInnerPanel.class, "ImportCSVInnerPanel.fileChooser.csvFilter.description"));
        dialogFileFilter.addExtensions(".csv", ".txt");
        chooser.addChoosableFileFilter(dialogFileFilter);
        chooser.setSelectedFile(selectedFile);
        int returnFile = chooser.showOpenDialog(null);
        if (returnFile != JFileChooser.APPROVE_OPTION) {
            return;
        }
        selectedFile = chooser.getSelectedFile();
        String path = selectedFile.getAbsolutePath();
        getPathTextField().setText(path);
        //Save last path
        String defaultDirectory = selectedFile.getParentFile().getAbsolutePath();
        NbPreferences.forModule(ImportCSVInnerPanel.class).put(LAST_PATH, defaultDirectory);
        refreshPreview();
    }
    
    protected synchronized final void runWithValidationSuspended(Runnable runnable) {
        validationSuspended = true;
        runnable.run();
        validationSuspended = false;
    }
    
    protected synchronized final boolean isValidationSuspended() {
        return validationSuspended;
    }
    private volatile boolean validationSuspended = false;
}
