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
package org.mongkie.ui.perspective;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JToggleButton;
import org.mongkie.perspective.NonSingletonTopComponent;
import org.mongkie.perspective.PerspectiveController;
import org.mongkie.perspective.spi.Perspective;
import static org.mongkie.visualization.Config.MODE_DISPLAY;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = PerspectiveController.class)
public final class PerspectiveTopComponent extends javax.swing.JPanel implements PerspectiveController {

    private JToggleButton[] tabs;
    static final String LAST_PERSPECTIVE = "PerspectiveTopComponent.lastSelectedPerspective";

    public PerspectiveTopComponent() {
        initComponents();
        addPerspectiveTabs();
    }

    private void addPerspectiveTabs() {
        Collection<? extends Perspective> perspectives = Lookup.getDefault().lookupAll(Perspective.class);
        tabs = new PerspectiveButton[perspectives.size()];
        int i = 0;
        for (final Perspective perspective : perspectives) {
            final List<TopComponent> openedNonSingletons = new ArrayList<TopComponent>();
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

                @Override
                public void run() {
                    for (TopComponent tc : WindowManager.getDefault().findMode(MODE_DISPLAY).getTopComponents()) {
                        if (tc instanceof NonSingletonTopComponent && ((NonSingletonTopComponent) tc).belongsTo(perspective)) {
                            openedNonSingletons.add(tc);
                        }
                    }
                }
            });
            PerspectiveButton toggleButton = new PerspectiveButton(perspective.getDisplayName(), perspective.getIcon());
            toggleButton.getModel().addItemListener(new ItemListener() {

                TopComponent selectedTopComponent = null;

                @Override
                public void itemStateChanged(ItemEvent e) {
                    String role = perspective.getRole();
                    switch (e.getStateChange()) {
                        case ItemEvent.DESELECTED:
                            if (WindowManager.getDefault().getRole().equals(role)) {
                                selectedTopComponent = closeNonSingletonsInDisplayMode(perspective, openedNonSingletons);
                            }
                            break;
                        case ItemEvent.SELECTED:
                            if (!WindowManager.getDefault().getRole().equals(role)) {
                                WindowManager.getDefault().setRole(role);
                                updateWindowMenuItems(role);
                                selectedTopComponent = openNonSingletonsInDisplayMode(openedNonSingletons, selectedTopComponent);
                                if (selectedTopComponent != null) {
                                    selectedTopComponent.requestActive();
                                }
                                openedNonSingletons.clear();
                                NbPreferences.forModule(PerspectiveTopComponent.class).put(LAST_PERSPECTIVE, role);
                            } else {
                                Mode displayMode = WindowManager.getDefault().findMode(MODE_DISPLAY);
                                TopComponent selected = displayMode.getSelectedTopComponent();
                                if (selected == null) {
                                    TopComponent[] tcs = displayMode.getTopComponents();
                                    if (tcs.length > 0) {
                                        selected = tcs[0];
                                    }
                                }
                                if (selected != null) {
                                    selected.requestActive();
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
            });
            perspectivesButtonGroup.add(toggleButton);
            buttonsPanel.add(toggleButton);
            tabs[i++] = toggleButton;
        }
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {

            @Override
            public void run() {
                perspectivesButtonGroup.setSelected(tabs[getPerspectiveIndex(WindowManager.getDefault().getRole())].getModel(), true);
            }
        });
    }

    static void updateWindowMenuItems(String role) {
        // First, delete action shadows(references) of other roles from the Menu/Window
        for (FileObject fo : FileUtil.getConfigFile("Menu/Window").getChildren()) {
            try {
                DataObject shadow = DataObject.find(fo);
                FileObject action = null;
                String actionRoles = null;
                if (shadow instanceof DataShadow
                        && (action = ((DataShadow) shadow).getOriginal().getPrimaryFile()) != null
                        && (actionRoles = (String) action.getAttribute(ROLES)) != null && !Arrays.asList(actionRoles.trim().split(",")).contains(role)) {
//                    System.out.println("<< " + shadow.getName());
                    shadow.delete();
                    Object position = null;
                    if (action.getAttribute(POSITION) == null && (position = fo.getAttribute(POSITION)) != null) {
                        action.setAttribute(POSITION, position);
//                        System.out.println("position saved: " + position);
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        // Then, add action shadows(references) of the given role into the Menu/Window
        DataFolder menu = DataFolder.findFolder(FileUtil.getConfigFile("Menu/Window"));
        for (FileObject action : FileUtil.getConfigFile("Actions/Window").getChildren()) {
            try {
                String actionRoles = (String) action.getAttribute(ROLES);
                if (actionRoles == null || !Arrays.asList(actionRoles.trim().split(",")).contains(role)
                        || containsAction(menu, action)) {
                    continue;
                }
                DataObject actionInstance = DataObject.find(action);
                DataShadow shadow = actionInstance.createShadow(menu);
//                System.out.println(">> " + shadow.getName());
                Object position = action.getAttribute(POSITION);
                if (position != null) {
                    shadow.getPrimaryFile().setAttribute(POSITION, position);
//                    System.out.println("position restored: " + position);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    private static final String ROLES = "roles";
    private static final String POSITION = "position";

    private static boolean containsAction(DataFolder menu, FileObject action) {
        for (DataObject shadow : menu.getChildren()) {
            if (shadow instanceof DataShadow
                    && action.equals(((DataShadow) shadow).getOriginal().getPrimaryFile())) {
                return true;
            }
        }
        return false;
    }

    private TopComponent openNonSingletonsInDisplayMode(List<TopComponent> openedNonSingletons, TopComponent selectedTopComponent) {
        Mode displayMode = WindowManager.getDefault().findMode(MODE_DISPLAY);
        for (TopComponent tc : openedNonSingletons) {
            if (!tc.isOpened()) {
                displayMode.dockInto(tc);
                tc.open();
            }
        }
        if (selectedTopComponent == null) {
            if (!openedNonSingletons.isEmpty()) {
                selectedTopComponent = openedNonSingletons.get(0);
            } else {
                TopComponent[] openedTopComponents = WindowManager.getDefault().getOpenedTopComponents(displayMode);
                if (openedTopComponents.length > 0) {
                    selectedTopComponent = openedTopComponents[0];
                }
            }
        }
        return selectedTopComponent;
    }

    private TopComponent closeNonSingletonsInDisplayMode(Perspective p, List<TopComponent> openedNonSingletons) {
        Mode displayMode = WindowManager.getDefault().findMode(MODE_DISPLAY);
        TopComponent selected = displayMode.getSelectedTopComponent();
        for (TopComponent tc : WindowManager.getDefault().getOpenedTopComponents(displayMode)) {
            if (tc instanceof NonSingletonTopComponent && ((NonSingletonTopComponent) tc).belongsTo(p)) {
                tc.close();
                openedNonSingletons.add(tc);
            }
        }
        return selected;
    }

    private int getPerspectiveIndex(String role) {
        int idx = 0;
        for (Perspective p : Lookup.getDefault().lookupAll(Perspective.class)) {
            if (p.getRole().equals(role)) {
                return idx;
            } else {
                idx++;
            }
        }
        return -1;
    }

    private Perspective getPerspective(int idx) {
        int i = 0;
        for (Perspective p : Lookup.getDefault().lookupAll(Perspective.class)) {
            if (i++ == idx) {
                return p;
            }
        }
        return null;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10, 30);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(32000, 30);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        perspectivesButtonGroup = new javax.swing.ButtonGroup();
        logoButton = new javax.swing.JButton();
        groupsPanel = new javax.swing.JPanel();
        buttonsPanel = new javax.swing.JPanel();
        bannerBackground = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(804, 30));
        setLayout(new java.awt.GridBagLayout());

        logoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/perspective/resources/kobicLogo.png"))); // NOI18N
        logoButton.setToolTipText(org.openide.util.NbBundle.getMessage(PerspectiveTopComponent.class, "PerspectiveTopComponent.logoButton.toolTipText")); // NOI18N
        logoButton.setBorder(null);
        logoButton.setBorderPainted(false);
        logoButton.setContentAreaFilled(false);
        logoButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logoButton.setFocusPainted(false);
        logoButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logoButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/perspective/resources/kobicMouseover.png"))); // NOI18N
        logoButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/perspective/resources/kobicMouseover.png"))); // NOI18N
        logoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logoButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(logoButton, gridBagConstraints);

        groupsPanel.setBackground(new java.awt.Color(255, 255, 255));
        groupsPanel.setMinimumSize(new java.awt.Dimension(642, 32));
        groupsPanel.setPreferredSize(new java.awt.Dimension(642, 32));
        groupsPanel.setLayout(new java.awt.GridBagLayout());

        buttonsPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonsPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(buttonsPanel, gridBagConstraints);

        bannerBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/perspective/resources/bannerback.png"))); // NOI18N
        bannerBackground.setMaximumSize(new java.awt.Dimension(642, 32));
        bannerBackground.setMinimumSize(new java.awt.Dimension(642, 32));
        bannerBackground.setPreferredSize(new java.awt.Dimension(642, 32));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        groupsPanel.add(bannerBackground, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(groupsPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void logoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoButtonActionPerformed
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            java.net.URI uri;
            try {
                uri = new java.net.URI("http://kobic.re.kr");
                desktop.browse(uri);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_logoButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bannerBackground;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JPanel groupsPanel;
    private javax.swing.JButton logoButton;
    private javax.swing.ButtonGroup perspectivesButtonGroup;
    // End of variables declaration//GEN-END:variables

    @Override
    public Perspective getSelectedPerspective() {
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i].isSelected()) {
                return getPerspective(i);
            }
        }
        return null;
    }

    @Override
    public Perspective setSelectedPerspective(String role) {
        int idx = getPerspectiveIndex(role);
        if (idx < 0) {
            return null;
        }
        perspectivesButtonGroup.setSelected(tabs[idx].getModel(), true);
        return getPerspective(idx);
    }

    private static class PerspectiveButton extends JToggleButton {

        public PerspectiveButton(String text, Icon icon) {
            setFont(new Font("Tahoma", Font.PLAIN, 12));
            setText(text);
            setBorder(null);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            setIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/mongkie/ui/perspective/resources/vistaEnabled.png"),
                    ImageUtilities.icon2Image(icon), 6, 3)));
            setRolloverIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/mongkie/ui/perspective/resources/vistaMouseover.png"),
                    ImageUtilities.icon2Image(icon), 6, 3)));
            setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.mergeImages(ImageUtilities.loadImage("org/mongkie/ui/perspective/resources/vistaSelected.png"),
                    ImageUtilities.icon2Image(icon), 6, 3)));
        }
    }
}
