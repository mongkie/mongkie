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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.mongkie.lib.widgets.WidgetUtilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PropertiesBar extends JPanel {

    private JPanel propertiesBar;
    private SystemAction toggleFullScreenAction;

    public PropertiesBar() {
        super(new BorderLayout());
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(getFullScreenIcon(), BorderLayout.WEST);
        add(leftPanel, BorderLayout.WEST);
        setOpaque(false);
        toggleFullScreenAction = FileUtil.getConfigObject(
                "Actions/Window/org-netbeans-core-windows-actions-ToggleFullScreenAction.instance",
                SystemAction.class);
    }

    public void select(JPanel propertiesBar) {
        this.propertiesBar = propertiesBar;
        propertiesBar.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        add(propertiesBar, BorderLayout.CENTER);
        propertiesBar.setOpaque(false);
        for (Component c : propertiesBar.getComponents()) {
            if (c instanceof JPanel || c instanceof JToolBar) {
                ((JComponent) c).setOpaque(false);
            }
        }
        revalidate();
    }

    public void unselect() {
        if (propertiesBar != null) {
            remove(propertiesBar);
            revalidate();
            repaint();
            propertiesBar = null;
        }
    }

    private JComponent getFullScreenIcon() {
        int logoWidth = 27;
        int logoHeight = 27;
        if (WidgetUtilities.isAquaLookAndFeel()) {
            logoWidth = 34;
        }
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(Color.WHITE);
        final JButton fullScreenButton = new JButton();
//        fullScreenButton.setIcon(new ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/full-screen.png")));
//        fullScreenButton.setToolTipText("Full screen");
        fullScreenButton.setBorderPainted(false);
        fullScreenButton.setContentAreaFilled(false);
        fullScreenButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fullScreenButton.setBorder(BorderFactory.createEmptyBorder());
        fullScreenButton.setPreferredSize(new Dimension(logoWidth, logoHeight));
        fullScreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullScreenAction.actionPerformed(null);
            }
        });
        fullScreenButton.addPropertyChangeListener("ancestor", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (((JCheckBoxMenuItem) ((DynamicMenuContent) toggleFullScreenAction).getMenuPresenters()[0]).isSelected()) {
                    fullScreenButton.setIcon(new ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/restore.png")));
                    fullScreenButton.setToolTipText("Restore screen");
                } else {
                    fullScreenButton.setIcon(new ImageIcon(getClass().getResource("/org/mongkie/ui/visualization/resources/fullscreen.png")));
                    fullScreenButton.setToolTipText("Full screen");
                }
            }
        });
        c.add(fullScreenButton, BorderLayout.CENTER);
        return c;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });

    }
}
