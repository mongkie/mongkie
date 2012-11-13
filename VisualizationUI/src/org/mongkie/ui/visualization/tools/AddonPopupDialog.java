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
package org.mongkie.ui.visualization.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.openide.windows.WindowManager;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class AddonPopupDialog extends javax.swing.JDialog {

    private Component invoker;
    private PopupDialogListener listener;
    private JPanel content;

    /**
     * Creates new form AddonPopupDialog
     */
    public AddonPopupDialog(Component invoker, boolean modal) {
        super(WindowManager.getDefault().getMainWindow(), modal);
        this.invoker = invoker;
        listener = new PopupDialogListener();
        initComponents();
        if (!isModal()) {
            remove(closePanel);
        }
        getRootPane().setBorder(new LeftBottomLineBorder());
    }

    public void setContentPane(JPanel content) {
        if (this.content != null) {
            remove(this.content);
        }
//        if (!isModal()) {
//            content.setBorder(new LeftBottomLineBorder());
//        }
        add(content, BorderLayout.CENTER);
        this.content = content;
    }

    @Override
    public void setModal(boolean modal) {
        super.setModal(modal);
        if (isModal() != modal) {
            if (modal) {
                getContentPane().add(closePanel, BorderLayout.SOUTH);
            } else {
                remove(closePanel);
            }
        }
    }

    public void showPopup() {
        if (!isVisible()) {
            relocatePopup();
            if (isModal()) {
                WindowManager.getDefault().getMainWindow().addComponentListener(listener);
            } else {
                addWindowFocusListener(listener);
            }
            setVisible(true);
            requestFocusInWindow();
        }
    }

    private void hidePopup() {
        if (isVisible()) {
            setVisible(false);
            if (isModal()) {
                WindowManager.getDefault().getMainWindow().removeComponentListener(listener);
            } else {
                removeWindowFocusListener(listener);
            }
        }
    }

    private void relocatePopup() {
        int x = invoker.getWidth() - getPreferredSize().width;
        int y = invoker.getHeight();
        Point invokerOrigin;
        if (invoker != null) {
            invokerOrigin = invoker.getLocationOnScreen();
            // To avoid integer overflow
            long lx, ly;
            lx = ((long) invokerOrigin.x)
                    + ((long) x);
            ly = ((long) invokerOrigin.y)
                    + ((long) y);
            if (lx > Integer.MAX_VALUE) {
                lx = Integer.MAX_VALUE;
            }
            if (lx < Integer.MIN_VALUE) {
                lx = Integer.MIN_VALUE;
            }
            if (ly > Integer.MAX_VALUE) {
                ly = Integer.MAX_VALUE;
            }
            if (ly < Integer.MIN_VALUE) {
                ly = Integer.MIN_VALUE;
            }

            setLocation((int) lx, (int) ly);
        } else {
            setLocation(x, y);
        }
    }

    private class PopupDialogListener extends ComponentAdapter implements WindowFocusListener {

        @Override
        public void componentResized(ComponentEvent e) {
            if (isVisible()) {
                relocatePopup();
            }
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            if (isVisible()) {
                relocatePopup();
            }
        }

        @Override
        public void windowGainedFocus(WindowEvent e) {
        }

        @Override
        public void windowLostFocus(WindowEvent e) {
            hidePopup();
        }
    }

    private static class LeftBottomLineBorder implements Border {

        private Insets ins = new Insets(0, 0, 1, 0);
        private Color col = new Color(221, 229, 248);

        public LeftBottomLineBorder() {
        }

        public @Override
        Insets getBorderInsets(Component c) {
            return ins;
        }

        public @Override
        boolean isBorderOpaque() {
            return false;
        }

        public @Override
        void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color old = g.getColor();
            g.setColor(col);
            g.drawRect(x, y, 1, height); // Left
            g.drawRect(x, y + height - 2, width, 1); // Bottom
            g.setColor(old);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closePanel = new javax.swing.JPanel();
        closeSeparator = new javax.swing.JSeparator();
        closeButton = new javax.swing.JButton();

        setUndecorated(true);
        setResizable(false);

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, org.openide.util.NbBundle.getMessage(AddonPopupDialog.class, "AddonPopupDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout closePanelLayout = new javax.swing.GroupLayout(closePanel);
        closePanel.setLayout(closePanelLayout);
        closePanelLayout.setHorizontalGroup(
            closePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(closeSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, closePanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addGap(3, 3, 3))
        );
        closePanelLayout.setVerticalGroup(
            closePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(closePanelLayout.createSequentialGroup()
                .addComponent(closeSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(closeButton)
                .addGap(3, 3, 3))
        );

        getContentPane().add(closePanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        hidePopup();
    }//GEN-LAST:event_closeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel closePanel;
    private javax.swing.JSeparator closeSeparator;
    // End of variables declaration//GEN-END:variables
}
