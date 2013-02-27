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
package org.mongkie.ui.perspective;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.*;
import org.mongkie.perspective.spi.BottomTopComponent;
import static org.mongkie.ui.perspective.PerspectiveTopComponent.LAST_PERSPECTIVE;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.openide.modules.ModuleInstall;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class Installer extends ModuleInstall implements WindowSystemListener {

    @Override
    public void restored() {
        // Add a toolbar pane for switching perspectives on the top of the main window
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Get the main window of the NetBeans Platform:
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                //Get our custom main toolbar:
                JComponent toolbar = PerspectiveTopComponent.getInstance();
                //Set the new layout of our root pane:
                frame.getRootPane().setLayout(new PerspectiveRootPaneLayout(toolbar));
                //Install a new toolbar component into the layered pane
                //of the main frame on layer 0:
                toolbar.putClientProperty(JLayeredPane.LAYER_PROPERTY, 0);
                frame.getRootPane().getLayeredPane().add(toolbar, 0);
            }
        });
        // Add a pane on the bottom of the main window, if any exits
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                JFrame frame = (JFrame) WindowManager.getDefault().getMainWindow();
                //Get the bottom component
                //TODO: Manage multiple bottom components and then, show only a selected one?
                BottomTopComponent bcImpl = Lookup.getDefault().lookup(BottomTopComponent.class);
                JComponent bc = bcImpl != null ? bcImpl.getComponent() : null;
                //Replace the content pane with our creation
                JComponent statusLinePanel = null;
                for (Component c : frame.getContentPane().getComponents()) {
                    if (c.getName() != null && c.getName().equals("statusLine")) {
                        statusLinePanel = (JComponent) c;
                    }
                }
                if (bc != null && statusLinePanel != null) {
                    frame.getContentPane().remove(statusLinePanel);
                    JPanel southPanel = new JPanel(new BorderLayout());
                    southPanel.add(statusLinePanel, BorderLayout.SOUTH);
                    bc.setVisible(false);
                    southPanel.add(bc, BorderLayout.CENTER);
                    frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
                }
            }
        });

        WindowManager.getDefault().addWindowSystemListener(this);
    }

    @Override
    public boolean closing() {
        PerspectiveTopComponent.getInstance().clearPerspectiveChangeListeners();
        return super.closing();
    }

    @Override
    public void beforeLoad(WindowSystemEvent event) {
        String role = NbPreferences.forModule(PerspectiveTopComponent.class).get(LAST_PERSPECTIVE, ROLE_NETWORK);
        WindowManager.getDefault().setRole(role);
        PerspectiveTopComponent.updateWindowMenuItems(role);
        WindowManager.getDefault().removeWindowSystemListener(this);
    }

    @Override
    public void afterLoad(WindowSystemEvent event) {
    }

    @Override
    public void beforeSave(WindowSystemEvent event) {
    }

    @Override
    public void afterSave(WindowSystemEvent event) {
    }
}
