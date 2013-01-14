/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.ui.datatable.graph.actions;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.mongkie.datatable.DataNode;
import org.mongkie.datatable.spi.DataAction;
import org.mongkie.ui.datatable.graph.AbstractDataTable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import prefuse.data.Schema;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SearchAndReplaceUI extends javax.swing.JPanel
        implements DataAction.UI<AbstractDataTable, SearchAndReplace>, ItemListener {

    private AbstractDataTable table;
    private static final String ALL_COLUMNS = "---All Columns";
    private SearchOptions options;
    private final SearchResult result;
    private boolean forward = true;

    /**
     * Creates new form SearchAndReplaceUI
     */
    public SearchAndReplaceUI() {
        initComponents();
        result = new SearchResult();
        searchTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                clearResult();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                clearResult();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                clearResult();
            }
        });
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forward) {
                    findNext();
                } else {
                    findPrevious();
                }
            }
        });
    }

    void refreshSearchColumns() {
        columnChooseComboBox.removeAllItems();
        columnChooseComboBox.addItem(ALL_COLUMNS);
        Schema s = table.getModel().getDisplay().getDataViewSupport(table.getDataGroup()).getOutlineSchema();
        for (int i = 0; i < s.getColumnCount(); i++) {
            if (s.getColumnType(i) == String.class) { // only for string types
                columnChooseComboBox.addItem(s.getColumnName(i));
            }
        }
    }

    void clearResult() {
        result.clear();
        refreshUI();
    }

    private void showMessageDialog(String title, String message) {
        DialogDescriptor dd = new DialogDescriptor(message, title);
        dd.setMessageType(NotifyDescriptor.INFORMATION_MESSAGE);
        dd.setOptions(new Object[]{NotifyDescriptor.OK_OPTION});
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    void findNext() {
        String text = searchTextField.getText();
        if (result.isEmpty()) {
            if (!search(text).isEmpty()) {
                table.setSelectedNodes(new Node[]{result.current()});
            }
        } else {
            table.setSelectedNodes(new Node[]{result.next()});
        }
        forward = true;
        refreshUI();
        if (result.isEmpty()) {
            showMessageDialog("Find Next", "No search result.");
        }
    }

    void findPrevious() {
        String text = searchTextField.getText();
        if (result.isEmpty()) {
            if (!search(text).isEmpty()) {
                table.setSelectedNodes(new Node[]{result.previous()});
            }
        } else {
            table.setSelectedNodes(new Node[]{result.previous()});
        }
        forward = false;
        refreshUI();
        if (result.isEmpty()) {
            showMessageDialog("Find Previous", "No search result.");
        }
    }

    SearchResult search(String text) {
        assert result.isEmpty();
        String query = options.isWholeWords() ? ".*\\b(" + text + ")\\b.*" : ".*(" + text + ").*";
        Pattern pattern = options.isCaseSensitive()
                ? Pattern.compile(query) : Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        result.setPattern(pattern);
        for (Node n : table.getExplorerManager().getRootContext().getChildren().getNodes(true)) {
            DataNode data = (DataNode) n;
            if (!result.contains(data) && match(data, pattern)) {
                result.add(data);
            }
        }
        return result;
    }

    boolean match(DataNode n, Pattern pattern) {
        Tuple data = n.getTuple();
        String col = (String) columnChooseComboBox.getSelectedItem();
        if (col.equals(ALL_COLUMNS)) {
            for (int i = 1; i < columnChooseComboBox.getItemCount(); i++) {
                String value = data.getString((String) columnChooseComboBox.getItemAt(i));
                if (value != null && !value.isEmpty() && pattern.matcher(value).matches()) {
                    return true;
                }
            }
            return false;
        } else {
            String value = data.getString(col);
            return value != null && !value.isEmpty() && pattern.matcher(value).matches();
        }
    }

    @Deprecated
    boolean contains(DataNode n, String text) {
        Tuple data = n.getTuple();
        String col = (String) columnChooseComboBox.getSelectedItem();
        if (!options.isCaseSensitive()) {
            text = text.toLowerCase();
        }
        if (col.equals(ALL_COLUMNS)) {
            for (int i = 1; i < columnChooseComboBox.getItemCount(); i++) {
                String value = data.getString((String) columnChooseComboBox.getItemAt(i));
                if (value == null || value.isEmpty()) {
                    continue;
                }
                if (!options.isCaseSensitive()) {
                    value = value.toLowerCase();
                }
                if (value.contains(text)) {
                    return true;
                }
            }
            return false;
        } else {
            String value = data.getString(col);
            if (value == null || value.isEmpty()) {
                return false;
            }
            if (!options.isCaseSensitive()) {
                value = value.toLowerCase();
            }
            return value.contains(text);
        }
    }

    void replace() {
        replace(result.current());
        result.remove(forward);
        if (!result.isEmpty()) {
            table.setSelectedNodes(new Node[]{result.current()});
        } else {
            table.setSelectedNodes(new Node[]{});
        }
        refreshUI();
    }

    void replaceAll() {
        for (Iterator<DataNode> nodes = result.iterator(); nodes.hasNext();) {
            replace(nodes.next());
        }
        int occurrences = result.size();
        table.setSelectedNodes(new Node[]{});
        result.clear();
        refreshUI();
        showMessageDialog("Replace All", occurrences + " occurrences are replaced.");
    }

    void replace(DataNode n) {
        String replacement = replaceTextField.getText();
        Tuple data = n.getTuple();
        String col = (String) columnChooseComboBox.getSelectedItem();
        if (col.equals(ALL_COLUMNS)) {
            for (int i = 1; i < columnChooseComboBox.getItemCount(); i++) {
                String field = (String) columnChooseComboBox.getItemAt(i);
                String value = data.getString(field);
                Matcher m;
                if (value != null && !value.isEmpty() && (m = result.getPattern().matcher(value)).matches()) {
                    data.setString(field, value.replace(m.group(1), replacement));
                }
            }
        } else {
            String value = data.getString(col);
            data.setString(col, value.replace(result.getPattern().matcher(value).group(1), replacement));
        }
    }

    void refreshUI() {
        findNextButton.setEnabled(!searchTextField.getText().isEmpty());
        findPreviousButton.setEnabled(!searchTextField.getText().isEmpty());
        if (forward) {
            findNextButton.setIcon(nextImg);
            findPreviousButton.setIcon(prevImgInactive);
        } else {
            findNextButton.setIcon(nextImgInactive);
            findPreviousButton.setIcon(prevImg);
        }
        replaceButton.setEnabled(!result.isEmpty());
        replaceAllButton.setEnabled(!result.isEmpty());
    }
    javax.swing.Icon prevImg = new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/go_previous.png"));
    javax.swing.Icon prevImgInactive = new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/go_previous_inactive.png"));
    javax.swing.Icon nextImg = new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/go_next.png"));
    javax.swing.Icon nextImgInactive = new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/go_next_inactive.png"));

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            clearResult();
        }
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

        columnChooseLabel = new javax.swing.JLabel();
        columnChooseComboBox = new javax.swing.JComboBox();
        searchLabel = new javax.swing.JLabel();
        searchTextField = new javax.swing.JTextField();
        replaceLabel = new javax.swing.JLabel();
        replaceTextField = new javax.swing.JTextField();
        findPreviousButton = new javax.swing.JButton();
        replaceButton = new javax.swing.JButton();
        replaceAllButton = new javax.swing.JButton();
        optionPanel = new javax.swing.JPanel();
        wholeWordsCheckBox = new javax.swing.JCheckBox();
        matchCaseCheckBox = new javax.swing.JCheckBox();
        regularExpressionCheckBox = new javax.swing.JCheckBox();
        findNextButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(columnChooseLabel, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.columnChooseLabel.text")); // NOI18N

        columnChooseComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "--- All Columns" }));

        org.openide.awt.Mnemonics.setLocalizedText(searchLabel, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.searchLabel.text")); // NOI18N

        searchTextField.setText(org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.searchTextField.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(replaceLabel, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.replaceLabel.text")); // NOI18N

        replaceTextField.setText(org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.replaceTextField.text")); // NOI18N

        findPreviousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/go_previous.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(findPreviousButton, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.findPreviousButton.text")); // NOI18N
        findPreviousButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findPreviousButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(replaceButton, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.replaceButton.text")); // NOI18N
        replaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(replaceAllButton, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.replaceAllButton.text")); // NOI18N
        replaceAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(wholeWordsCheckBox, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.wholeWordsCheckBox.text")); // NOI18N
        wholeWordsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wholeWordsCheckBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(matchCaseCheckBox, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.matchCaseCheckBox.text")); // NOI18N
        matchCaseCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseCheckBoxActionPerformed(evt);
            }
        });

        regularExpressionCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(regularExpressionCheckBox, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.regularExpressionCheckBox.text")); // NOI18N
        regularExpressionCheckBox.setEnabled(false);

        javax.swing.GroupLayout optionPanelLayout = new javax.swing.GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(wholeWordsCheckBox)
                    .addComponent(matchCaseCheckBox)
                    .addComponent(regularExpressionCheckBox))
                .addGap(0, 37, Short.MAX_VALUE))
        );
        optionPanelLayout.setVerticalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addComponent(wholeWordsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(matchCaseCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(regularExpressionCheckBox))
        );

        findNextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/datatable/resources/go_next.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(findNextButton, org.openide.util.NbBundle.getMessage(SearchAndReplaceUI.class, "SearchAndReplaceUI.findNextButton.text")); // NOI18N
        findNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findNextButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(searchLabel)
                                    .addComponent(replaceLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(replaceTextField)
                                    .addComponent(searchTextField)))
                            .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(findPreviousButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(findNextButton))
                            .addComponent(replaceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(replaceAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(columnChooseLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(columnChooseComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(columnChooseLabel)
                    .addComponent(columnChooseComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchLabel)
                    .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(findPreviousButton)
                    .addComponent(findNextButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(replaceLabel)
                    .addComponent(replaceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(replaceAllButton)
                    .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void findNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findNextButtonActionPerformed
        findNext();
    }//GEN-LAST:event_findNextButtonActionPerformed

    private void findPreviousButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findPreviousButtonActionPerformed
        findPrevious();
    }//GEN-LAST:event_findPreviousButtonActionPerformed

    private void matchCaseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchCaseCheckBoxActionPerformed
        options.setCaseSensitive(matchCaseCheckBox.isSelected());
        clearResult();
    }//GEN-LAST:event_matchCaseCheckBoxActionPerformed

    private void wholeWordsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wholeWordsCheckBoxActionPerformed
        options.setWholeWords(wholeWordsCheckBox.isSelected());
        clearResult();
    }//GEN-LAST:event_wholeWordsCheckBoxActionPerformed

    private void replaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceButtonActionPerformed
        replace();
    }//GEN-LAST:event_replaceButtonActionPerformed

    private void replaceAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceAllButtonActionPerformed
        replaceAll();
    }//GEN-LAST:event_replaceAllButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox columnChooseComboBox;
    private javax.swing.JLabel columnChooseLabel;
    private javax.swing.JButton findNextButton;
    private javax.swing.JButton findPreviousButton;
    private javax.swing.JCheckBox matchCaseCheckBox;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JCheckBox regularExpressionCheckBox;
    private javax.swing.JButton replaceAllButton;
    private javax.swing.JButton replaceButton;
    private javax.swing.JLabel replaceLabel;
    private javax.swing.JTextField replaceTextField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JCheckBox wholeWordsCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void load(AbstractDataTable table, SearchAndReplace action) {
        this.table = table;
        options = (SearchOptions) table.getClientProperty(SearchOptions.class);
        if (options == null) {
            options = new SearchOptions();
            table.putClientProperty(SearchOptions.class, options);
        }
        refreshSearchColumns();
        columnChooseComboBox.addItemListener(this);
        forward = true;
        refreshUI();
        searchTextField.selectAll();
        searchTextField.requestFocusInWindow();
    }

    @Override
    public boolean close(Object option) {
        columnChooseComboBox.removeItemListener(this);
        result.clear();
        return false;
    }

    @Override
    public Object[] getDialogOptions() {
        return new Object[]{"Close"};
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    private static final class SearchOptions {

        private boolean wholeWords = false;
        private boolean caseSensitive = false;

        boolean isWholeWords() {
            return wholeWords;
        }

        void setWholeWords(boolean wholeWords) {
            this.wholeWords = wholeWords;
        }

        boolean isCaseSensitive() {
            return caseSensitive;
        }

        void setCaseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
        }
    }

    private static final class SearchResult implements Iterable<DataNode> {

        private final List<DataNode> nodes = new ArrayList<DataNode>();
        private final Set<DataNode> set = new HashSet<DataNode>();
        private int current = 0;
        private Pattern pattern;

        Pattern getPattern() {
            return pattern;
        }

        void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        boolean isEmpty() {
            return nodes.isEmpty();
        }

        void add(DataNode n) {
            if (set.add(n)) {
                nodes.add(n);
            }
        }

        void remove(boolean forward) {
            assert !nodes.isEmpty();
            DataNode n = nodes.get(current);
            assert set.remove(n);
            assert nodes.remove(n);
            if (nodes.isEmpty() || (forward && current >= nodes.size())) {
                current = 0;
            } else if (!forward && --current < 0) {
                current = nodes.size() - 1;
            }
        }

        boolean contains(DataNode n) {
            return set.contains(n);
        }

        void clear() {
            nodes.clear();
            set.clear();
            current = 0;
        }

        DataNode current() {
            assert !nodes.isEmpty();
            return nodes.get(current);
        }

        DataNode next() {
            assert !nodes.isEmpty();
            if (++current >= nodes.size()) {
                current = 0;
            }
            return nodes.get(current);
        }

        DataNode previous() {
            assert !nodes.isEmpty();
            if (--current < 0) {
                current = nodes.size() - 1;
            }
            return nodes.get(current);
        }

        @Override
        public Iterator<DataNode> iterator() {
            return nodes.iterator();
        }

        int size() {
            return nodes.size();
        }
    }
}
