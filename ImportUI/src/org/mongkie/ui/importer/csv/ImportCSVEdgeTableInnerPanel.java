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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.GroupValidator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.SwingValidationGroup;
import org.openide.util.NbBundle;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
class ImportCSVEdgeTableInnerPanel extends ImportCSVInnerPanel {

    private static final String NO_LABEL =
            NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.labelColumn.noLabel");

    /** Creates new form ImportCSVEdgeTableInnerPanel */
    ImportCSVEdgeTableInnerPanel(ImportCSVEdgeTableWizardPanel wizardPanel) {
        super(wizardPanel);
    }

    @Override
    protected void initUI() {
        initComponents();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.displayName");
    }

    @Override
    protected JButton getFileButton() {
        return fileButton;
    }

    @Override
    protected JTextField getPathTextField() {
        return pathTextField;
    }

    @Override
    protected JScrollPane getPreviewScroll() {
        return previewScroll;
    }

    @Override
    protected JTable getPreviewTable() {
        return previewTable;
    }

    @Override
    protected JCheckBox getSkipCheckBox() {
        return skipEdgeTableCheckBox;
    }

    @Override
    protected JCheckBox getHasHeaderCheckBox() {
        return hasHeaderCheckBox;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        fileButton = new javax.swing.JButton();
        targetColumnLabel = new javax.swing.JLabel();
        targetColumnComboBox = new javax.swing.JComboBox();
        previewScroll = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();
        sourceColumnLabel = new javax.swing.JLabel();
        sourceColumnComboBox = new javax.swing.JComboBox();
        previewSeparator = new org.jdesktop.swingx.JXTitledSeparator();
        skipEdgeTableCheckBox = new javax.swing.JCheckBox();
        labelColumnLabel = new javax.swing.JLabel();
        labelColumnComboBox = new javax.swing.JComboBox();
        hasHeaderCheckBox = new javax.swing.JCheckBox();
        editColumnExplainLabel = new javax.swing.JLabel();

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVInnerPanel.descriptionLabel.text")); // NOI18N

        pathTextField.setEditable(false);
        pathTextField.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVInnerPanel.pathTextField.text")); // NOI18N

        fileButton.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        fileButton.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVInnerPanel.fileButton.text")); // NOI18N
        fileButton.setMargin(new java.awt.Insets(0, 4, 0, 2));

        targetColumnLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.targetColumnLabel.text")); // NOI18N

        targetColumnComboBox.setName("Target column"); // NOI18N
        targetColumnComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                targetColumnComboBoxItemStateChanged(evt);
            }
        });

        previewScroll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        previewScroll.setViewportView(previewTable);

        sourceColumnLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.sourceColumnLabel.text")); // NOI18N

        sourceColumnComboBox.setName("Source column"); // NOI18N
        sourceColumnComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                sourceColumnComboBoxItemStateChanged(evt);
            }
        });

        previewSeparator.setTitle(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVInnerPanel.previewSeparator.title")); // NOI18N

        skipEdgeTableCheckBox.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.skipEdgeTableCheckBox.text")); // NOI18N
        skipEdgeTableCheckBox.setFocusPainted(false);

        labelColumnLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVInnerPanel.labelColumnLabel.text")); // NOI18N

        labelColumnComboBox.setName("Label column"); // NOI18N

        hasHeaderCheckBox.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        hasHeaderCheckBox.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.hasHeaderCheckBox.text")); // NOI18N

        editColumnExplainLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        editColumnExplainLabel.setForeground(new java.awt.Color(242, 70, 200));
        editColumnExplainLabel.setText(org.openide.util.NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class, "ImportCSVEdgeTableInnerPanel.editColumnExplainLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(previewScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(sourceColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(sourceColumnComboBox, 0, 126, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(targetColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(targetColumnComboBox, 0, 126, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(labelColumnLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                                .addComponent(labelColumnComboBox, 0, 126, Short.MAX_VALUE))
                            .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(pathTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(fileButton)
                            .addGap(12, 12, 12))
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(previewSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                            .addContainerGap())
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(skipEdgeTableCheckBox)
                                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(hasHeaderCheckBox))
                            .addContainerGap(78, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(editColumnExplainLabel)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(skipEdgeTableCheckBox)
                .addGap(18, 18, 18)
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fileButton)
                    .addComponent(pathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hasHeaderCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sourceColumnLabel)
                            .addComponent(targetColumnLabel)
                            .addComponent(labelColumnLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sourceColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(targetColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelColumnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(previewSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editColumnExplainLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fileButton, pathTextField});

    }// </editor-fold>//GEN-END:initComponents

    private void sourceColumnComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_sourceColumnComboBoxItemStateChanged
//        refreshUI();
        getWizardPanel().fireChangeEvent();
    }//GEN-LAST:event_sourceColumnComboBoxItemStateChanged

    private void targetColumnComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_targetColumnComboBoxItemStateChanged
//        refreshUI();
        getWizardPanel().fireChangeEvent();
    }//GEN-LAST:event_targetColumnComboBoxItemStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JLabel editColumnExplainLabel;
    private javax.swing.JButton fileButton;
    private javax.swing.JCheckBox hasHeaderCheckBox;
    private javax.swing.JComboBox labelColumnComboBox;
    private javax.swing.JLabel labelColumnLabel;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JScrollPane previewScroll;
    private org.jdesktop.swingx.JXTitledSeparator previewSeparator;
    private javax.swing.JTable previewTable;
    private javax.swing.JCheckBox skipEdgeTableCheckBox;
    private javax.swing.JComboBox sourceColumnComboBox;
    private javax.swing.JLabel sourceColumnLabel;
    private javax.swing.JComboBox targetColumnComboBox;
    private javax.swing.JLabel targetColumnLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void addColumnChooserValidators(ValidationGroup validationGroup) {
        SwingValidationGroup columnChoosersValidator = SwingValidationGroup.create(new GroupValidator() {

            @Override
            protected void performGroupValidation(Problems problems) {
                if (isValidationSuspended() || !isValidFile()) {
                    return;
                }
                if (!isValidColumns()) {
                    problems.add(NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class,
                            "ImportCSVEdgeTableInnerPanel.validation.fatal.sameSourceTarget"), Severity.FATAL);
                }
            }
        });
        Validator<String> mustBeSelected = new Validator<String>() {

            @Override
            public void validate(Problems problems, String compName, String model) {
                if (isValidationSuspended() || !isValidFile()) {
                    return;
                }
                if (model.isEmpty()) {
                    problems.add(NbBundle.getMessage(ImportCSVEdgeTableInnerPanel.class,
                            "ImportCSVEdgeTableInnerPanel.validation.fatal.noColumnSelected", compName), Severity.FATAL);
                }
            }

            @Override
            public Class<String> modelType() {
                return String.class;
            }
        };
        columnChoosersValidator.add((JComponent) sourceColumnComboBox, mustBeSelected);
        columnChoosersValidator.add((JComponent) targetColumnComboBox, mustBeSelected);

        validationGroup.addItem(columnChoosersValidator, false);
        validationGroup.add(labelColumnComboBox, mustBeSelected);
    }

    @Override
    protected void refreshColumnChoosers(final String... columnNames) {
        runWithValidationSuspended(new Runnable() {

            @Override
            public void run() {
                sourceColumnComboBox.removeAllItems();
                targetColumnComboBox.removeAllItems();
                labelColumnComboBox.removeAllItems();
                if (columnNames.length > 0) {
                    labelColumnComboBox.addItem(NO_LABEL);
                }
                for (String column : columnNames) {
                    sourceColumnComboBox.addItem(column);
                    targetColumnComboBox.addItem(column);
                    labelColumnComboBox.addItem(column);
                }
                sourceColumnComboBox.setSelectedItem(columnNames.length > 0 ? columnNames[0] : null);
                targetColumnComboBox.setSelectedItem(columnNames.length > 0 ? columnNames[0] : null);
                if (columnNames.length > 0) {
                    labelColumnComboBox.setSelectedItem(NO_LABEL);
                }
            }
        });
    }

    @Override
    protected void headerNameChanged(TableColumn col, String oldName, String newName) {
        comboBoxItemChanged(sourceColumnComboBox, oldName, newName);
        comboBoxItemChanged(targetColumnComboBox, oldName, newName);
        comboBoxItemChanged(labelColumnComboBox, oldName, newName);
    }

    @Override
    protected boolean isValidColumns() {
        String source = (String) sourceColumnComboBox.getSelectedItem();
        String target = (String) targetColumnComboBox.getSelectedItem();
        return isSkipped() || (source != null && target != null && !source.equals(target));
    }

    @Override
    protected void setColumnChoosersEnabled(final boolean enabled) {
        runWithValidationSuspended(new Runnable() {

            @Override
            public void run() {
                sourceColumnComboBox.setEnabled(enabled);
                targetColumnComboBox.setEnabled(enabled);
                labelColumnComboBox.setEnabled(enabled);
            }
        });
    }

    @Override
    protected void validateAll() {
        super.validateAll();
//        sourceColumnComboBox.setEnabled(sourceColumnComboBox.isEnabled());
//        targetColumnComboBox.setEnabled(targetColumnComboBox.isEnabled());
//        labelColumnComboBox.setEnabled(labelColumnComboBox.isEnabled());
        sourceColumnComboBox.firePropertyChange("enabled", !sourceColumnComboBox.isEnabled(), sourceColumnComboBox.isEnabled());
        targetColumnComboBox.firePropertyChange("enabled", !targetColumnComboBox.isEnabled(), targetColumnComboBox.isEnabled());
        labelColumnComboBox.firePropertyChange("enabled", !labelColumnComboBox.isEnabled(), labelColumnComboBox.isEnabled());
    }

    Object getSelectedSourceColumn() {
        return sourceColumnComboBox.getSelectedItem();
    }

    Object getSelectedTargetColumn() {
        return targetColumnComboBox.getSelectedItem();
    }

    Object getSelectedLabelColumn() {
        Object labelColumn = labelColumnComboBox.getSelectedItem();
        return labelColumn != null && labelColumn.equals(NO_LABEL) ? null : labelColumn;
    }

    Object isDirected() {
        return false;
    }
}
