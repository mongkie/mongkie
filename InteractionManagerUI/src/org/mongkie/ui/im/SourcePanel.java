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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import kobic.prefuse.display.NetworkDisplay;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXHyperlink;
import org.mongkie.im.InteractionController;
import org.mongkie.im.QueryEvent;
import org.mongkie.im.SourceModel;
import org.mongkie.im.SourceModelListener;
import org.mongkie.im.spi.InteractionAction;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.lib.widgets.richtooltip.RichTooltip;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private final SettingsPanel settings;

    /**
     * Creates new form ProcessPanel
     */
    public SourcePanel(MongkieDisplay d, final InteractionSource is) {
        initComponents();
        setAlignmentY(TOP_ALIGNMENT);
        querying = new JXBusyLabel(
                new Dimension(interactionLinkButton.getPreferredSize().width, interactionLinkButton.getPreferredSize().height));
        querying.setToolTipText("Querying interactions...");
        this.is = is;
        settings = new SettingsPanel(d, is);
        model = Lookup.getDefault().lookup(InteractionController.class).getModel(is);
        model.addModelListener(SourcePanel.this);
        InteractionSource.RichDescription richDescription = is.getRichDescription();
        if (richDescription != null) {
            final RichTooltip richTooltip = new RichTooltip("About " + is.getName(), richDescription.getDescription());
            Image mainImg = richDescription.getMainImage(), footerImg = richDescription.getFooterImage();
            if (mainImg != null) {
                richTooltip.setMainImage(mainImg);
            }
            if (footerImg != null) {
                richTooltip.setFooterImage(footerImg);
            }
            String url = richDescription.getURL();
            if (url != null) {
                ((JXHyperlink) interactionNameLink).setURI(URI.create(url));
            }
            interactionNameLink.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    richTooltip.showTooltip(interactionNameLink);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    richTooltip.hideTooltip();
                }
//                @Override
//                public void mouseClicked(MouseEvent e) {
//                    richTooltip.hideTooltip();
//                }
            });
        } else {
            interactionNameLink.setToolTipText("Description is not available");
        }
        interactionNameLink.setText(is.getName());
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
        InteractionAction[] actions = is.getActions();
        actionsMenuButton.setEnabled(actions != null && actions.length > 0);
        if (actionsMenuButton.isEnabled()) {
            actionsMenuButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            actionsPopupMenu = new JPopupMenu();
            for (final InteractionAction a : actions) {
                JMenuItem menu = new JMenuItem(
                        new AbstractAction(a.getName(), a.getIcon()) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                a.execute(is);
                            }
                        });
                menu.setEnabled(a.isEnabled(is));
                menu.setToolTipText(a.getDescription());
                menu.putClientProperty(InteractionAction.class, a);
                actionsPopupMenu.add(menu);
            }
        }
        graphChanged(d.getGraph());
    }
    private JXBusyLabel querying;
    private JPopupMenu partiallyLinkedMenu, actionsPopupMenu;

    InteractionSource getInteractionSource() {
        return is;
    }

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
        actionsMenuButton = new javax.swing.JButton();
        settingButton = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(32767, 28));
        setMinimumSize(new java.awt.Dimension(305, 28));
        setOpaque(false);
        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 1, 0));
        add(filler2);

        interactionLinkButton.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        interactionLinkButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/uncheck.png"))); // NOI18N
        interactionLinkButton.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.text")); // NOI18N
        interactionLinkButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.interactionLinkButton.toolTipText")); // NOI18N
        interactionLinkButton.setBorderPainted(false);
        interactionLinkButton.setContentAreaFilled(false);
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

        actionsMenuButton.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        actionsMenuButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/menu_dropdown.png"))); // NOI18N
        actionsMenuButton.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.actionsMenuButton.text")); // NOI18N
        actionsMenuButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.actionsMenuButton.toolTipText")); // NOI18N
        actionsMenuButton.setBorderPainted(false);
        actionsMenuButton.setContentAreaFilled(false);
        actionsMenuButton.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/menu_dropdown_disabled.png"))); // NOI18N
        actionsMenuButton.setFocusPainted(false);
        actionsMenuButton.setMargin(new java.awt.Insets(2, 4, 2, 2));
        actionsMenuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                actionsMenuButtonActionPerformed(evt);
            }
        });
        add(actionsMenuButton);

        settingButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/settings.png"))); // NOI18N
        settingButton.setText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.settingButton.text")); // NOI18N
        settingButton.setToolTipText(org.openide.util.NbBundle.getMessage(SourcePanel.class, "SourcePanel.settingButton.toolTipText")); // NOI18N
        settingButton.setBorderPainted(false);
        settingButton.setContentAreaFilled(false);
        settingButton.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        settingButton.setFocusPainted(false);
        settingButton.setFocusable(false);
        settingButton.setMargin(new java.awt.Insets(2, 2, 2, 4));
        settingButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingButtonActionPerformed(evt);
            }
        });
        add(settingButton);
    }// </editor-fold>//GEN-END:initComponents

    private void interactionLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_interactionLinkButtonActionPerformed
        if (!model.isLinking()) {
            if (model.isLinked()) {
                Lookup.getDefault().lookup(InteractionController.class).executeUnlink(is);
            } else if (model.isPartiallyLinked()) {
                partiallyLinkedMenu.show(interactionLinkButton, 0, interactionLinkButton.getPreferredSize().height);
            } else {
                Lookup.getDefault().lookup(InteractionController.class).executeLink(is);
            }
        }
    }//GEN-LAST:event_interactionLinkButtonActionPerformed

    private void settingButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingButtonActionPerformed
        settings.load();
        if (DialogDisplayer.getDefault().notify(new DialogDescriptor(settings, is.getName() + " Settings"))
                .equals(NotifyDescriptor.OK_OPTION)) {
            settings.apply(true);
        } else {
            settings.apply(false);
        }
    }//GEN-LAST:event_settingButtonActionPerformed

    private void actionsMenuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionsMenuButtonActionPerformed
        for (Component c : actionsPopupMenu.getComponents()) {
            if (c instanceof JMenuItem) {
                JMenuItem menu = (JMenuItem) c;
                menu.setEnabled(((InteractionAction) menu.getClientProperty(InteractionAction.class)).isEnabled(is));
            }
        }
        actionsPopupMenu.show(actionsMenuButton, 4, actionsMenuButton.getPreferredSize().height);
    }//GEN-LAST:event_actionsMenuButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton actionsMenuButton;
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

    private void refreshColumnComboBox(final Graph g) {
        // Refreshing items of a combo box must be executed in the EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    @Override
    public void tableChanged(Table t, int start, int end, int col, int type) {
        if (col != ALL_COLUMNS && (type == INSERT || type == DELETE)) {
            refreshColumnComboBox(((NetworkDisplay) t.getClientProperty(NetworkDisplay.PROP_KEY)).getGraph());
        }
    }
}
