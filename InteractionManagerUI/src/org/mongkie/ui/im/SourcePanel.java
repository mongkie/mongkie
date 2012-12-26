/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.ui.im;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import kobic.prefuse.display.NetworkDisplay;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXHyperlink;
import org.mongkie.im.InteractionController;
import org.mongkie.im.QueryEvent;
import org.mongkie.im.SourceModel;
import org.mongkie.im.SourceModelListener;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import prefuse.data.Graph;
import prefuse.data.Table;
import static prefuse.data.event.EventConstants.*;
import prefuse.data.event.TableListener;
import prefuse.util.TypeLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class SourcePanel extends javax.swing.JPanel implements SourceModelListener, TableListener {

    private final InteractionSource is;
    private final SourceModel model;

    /**
     * Creates new form ProcessPanel
     */
    public SourcePanel(MongkieDisplay d, final InteractionSource is) {
        initComponents();
        querying = new JXBusyLabel(
                new Dimension(interactionLinkButton.getPreferredSize().width, interactionLinkButton.getPreferredSize().height));
        querying.setToolTipText("Querying interactions...");
        ((JXHyperlink) interactionNameLink).setText(is.getName());
        this.is = is;
        model = Lookup.getDefault().lookup(InteractionController.class).getModel(is);
        model.addModelListener(SourcePanel.this);
        columnComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                Lookup.getDefault().lookup(InteractionController.class).setKeyField(is, (String) e.getItem());
                updateInteractionLinkButton();
            }
        });
        partiallyLinkedMenu = new JPopupMenu();
        partiallyLinkedMenu.add(new JMenuItem(
                new AbstractAction("Add interactions", ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/link.png", false)) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Lookup.getDefault().lookup(InteractionController.class).executeLink(is);
                    }
                }));
        partiallyLinkedMenu.add(new JMenuItem(
                new AbstractAction("Remove interactions", ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/link_break.png", false)) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Lookup.getDefault().lookup(InteractionController.class).executeUnlink(is);
                    }
                }));
        graphChanged(d.getGraph());
    }
    private JXBusyLabel querying;
    private JPopupMenu partiallyLinkedMenu;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(2, 0), new java.awt.Dimension(32767, 0));
        interactionLinkButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(2, 0), new java.awt.Dimension(32767, 0));
        interactionNameLink = new JXHyperlink();
        keyLabel = new javax.swing.JLabel();
        columnComboBox = new javax.swing.JComboBox();
        actionMenuButton = new javax.swing.JButton();
        settingButton = new javax.swing.JButton();

        setOpaque(false);
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 1, 3));
        add(filler2);

        interactionLinkButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        interactionLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/uncheck.png"))); // NOI18N
        interactionLinkButton.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.text")); // NOI18N
        interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.toolTipText")); // NOI18N
        interactionLinkButton.setBorderPainted(false);
        interactionLinkButton.setContentAreaFilled(false);
        interactionLinkButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        interactionLinkButton.setFocusPainted(false);
        interactionLinkButton.setPreferredSize(new java.awt.Dimension(18, 18));
        interactionLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                interactionLinkButtonActionPerformed(evt);
            }
        });
        add(interactionLinkButton);
        add(filler1);

        interactionNameLink.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionNameLink.text")); // NOI18N
        interactionNameLink.setFocusPainted(false);
        interactionNameLink.setPreferredSize(new java.awt.Dimension(100, 24));
        ((JXHyperlink) interactionNameLink).setClickedColor(new Color(0, 51, 255));
        add(interactionNameLink);

        keyLabel.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        keyLabel.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.keyLabel.text")); // NOI18N
        add(keyLabel);

        columnComboBox.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        columnComboBox.setMinimumSize(new java.awt.Dimension(37, 22));
        columnComboBox.setPreferredSize(new java.awt.Dimension(100, 22));
        add(columnComboBox);

        actionMenuButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        actionMenuButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/menu_dropdown.png"))); // NOI18N
        actionMenuButton.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.actionMenuButton.text")); // NOI18N
        actionMenuButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.actionMenuButton.toolTipText")); // NOI18N
        actionMenuButton.setBorderPainted(false);
        actionMenuButton.setContentAreaFilled(false);
        actionMenuButton.setFocusPainted(false);
        actionMenuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionMenuButtonActionPerformed(evt);
            }
        });
        add(actionMenuButton);

        settingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/settings.png"))); // NOI18N
        settingButton.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.settingButton.text")); // NOI18N
        settingButton.setBorderPainted(false);
        settingButton.setContentAreaFilled(false);
        settingButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        settingButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/settings_disabled.png"))); // NOI18N
        settingButton.setEnabled(false);
        settingButton.setFocusPainted(false);
        settingButton.setFocusable(false);
        add(settingButton);
    }// </editor-fold>//GEN-END:initComponents

    private void interactionLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interactionLinkButtonActionPerformed
        if (!model.isLinking()) {
            if (model.isLinked()) {
                Lookup.getDefault().lookup(InteractionController.class).executeUnlink(is);
            } else if (model.isPartiallyLinked()) {
                partiallyLinkedMenu.show(interactionLinkButton, 0, interactionLinkButton.getHeight());
            } else {
                Lookup.getDefault().lookup(InteractionController.class).executeLink(is);
            }
        }
    }//GEN-LAST:event_interactionLinkButtonActionPerformed

    private void actionMenuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionMenuButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_actionMenuButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionMenuButton;
    private javax.swing.JComboBox columnComboBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JButton interactionLinkButton;
    private javax.swing.JButton interactionNameLink;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JButton settingButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void processQueryEvent(QueryEvent e) {
        switch (e.getType()) {
            case LINK_STARTED:
            case EXPAND_STARTED:
                remove(interactionLinkButton);
                querying.setBusy(true);
                add(querying, 1);
                revalidate();
                repaint();
                break;
            case LINK_FINISHED:
            case EXPAND_FINISHED:
                remove(querying);
                querying.setBusy(false);
                updateInteractionLinkButton();
                add(interactionLinkButton, 1);
                revalidate();
                repaint();
                break;
            case UNLINKED:
                interactionLinkButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/uncheck.png", false));
                interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.toolTipText"));
                break;
            default:
                break;
        }
    }

    private void updateInteractionLinkButton() {
        if (model.getKeyField() == null) {
            interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.disabled.toolTipText"));
        } else if (model.isLinked()) {
            interactionLinkButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/check.png", false));
            interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.linked.toolTipText"));
        } else if (model.isPartiallyLinked()) {
            interactionLinkButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/check2.png", false));
            interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.partiallyLinked.toolTipText"));
        } else {
            interactionLinkButton.setIcon(ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/uncheck.png", false));
            interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.toolTipText"));
        }
    }

    @Override
    public void graphDisposing(Graph g) {
        g.getNodeTable().removeTableListener(this);
        refreshColumnComboBox(null);
    }

    @Override
    public void graphChanged(Graph g) {
        updateInteractionLinkButton();
        if (g != null) {
            g.getNodeTable().addTableListener(this);
        }
        refreshColumnComboBox(g);

    }

    private void refreshColumnComboBox(Graph g) {
        columnComboBox.removeAllItems();
        if (g != null) {
            Table t = g.getNodeTable();
            String key = model.getKeyField();
            for (int i = 0; i < t.getColumnCount(); i++) {
                if (is.getKeyType().equals(TypeLib.getWrapperType(t.getColumnType(i)))) {
                    columnComboBox.addItem(t.getColumnName(i));
                }
            }
            if (key != null) {
                columnComboBox.setSelectedItem(key);
            }
        }
        updateInteractionLinkButton();
    }

    @Override
    public void tableChanged(Table t, int start, int end, int col, int type) {
        if (col != ALL_COLUMNS && (type == INSERT || type == DELETE)) {
            refreshColumnComboBox(((NetworkDisplay) t.getClientProperty(NetworkDisplay.PROP_KEY)).getGraph());
        }
    }
}
