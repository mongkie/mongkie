/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
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
 * @author Mathieu Bastian, Yeongjun Jang <yjjang@kribb.re.kr>
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
