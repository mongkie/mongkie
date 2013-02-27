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
package org.mongkie.ui.context;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import kobic.prefuse.display.DisplayListener;
import org.jdesktop.swingx.JXBusyLabel;
import org.mongkie.context.spi.ContextUI;
import static org.mongkie.kopath.viz.Config.ROLE_PATHWAY;
import org.mongkie.perspective.PerspectiveChangeListener;
import org.mongkie.perspective.PerspectiveController;
import org.mongkie.perspective.spi.Perspective;
import static org.mongkie.visualization.Config.MODE_CONTEXT;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ConvertAsProperties(dtd = "-//org.mongkie.ui.context//Context//EN",
        autostore = false)
@TopComponent.Description(preferredID = ContextTopComponent.PREFERRED_ID,
        iconBase = "org/mongkie/ui/context/resources/context.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = MODE_CONTEXT, openAtStartup = true, roles = {ROLE_NETWORK, ROLE_PATHWAY}, position = 100)
@ActionID(category = "Window", id = "org.mongkie.ui.context.ContextTopComponent")
@ActionReference(path = "Menu/Window", position = 210)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ContextAction",
        preferredID = ContextTopComponent.PREFERRED_ID)
public final class ContextTopComponent extends TopComponent implements DisplayListener<MongkieDisplay>, PerspectiveChangeListener {

    private static ContextTopComponent instance;
    /**
     * path to the icon used by the component and its open action
     */
    static final String PREFERRED_ID = "ContextTopComponent";
    private MongkieDisplay currentDisplay;
    private ContextUI selectedContext;
    private final JXBusyLabel perspectiveChanging = new JXBusyLabel(new Dimension(24, 24));

    public ContextTopComponent() {
        initComponents();
        perspectiveChanging.setHorizontalAlignment(SwingConstants.CENTER);
        setName(NbBundle.getMessage(ContextTopComponent.class, "CTL_ContextTopComponent"));
        setToolTipText(NbBundle.getMessage(ContextTopComponent.class, "HINT_ContextTopComponent"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                Lookup.getDefault().lookup(PerspectiveController.class).addPerspectiveChangeListener(ContextTopComponent.this);
            }
        });
        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(
                new WorkspaceListener() {
            @Override
            public void displaySelected(MongkieDisplay display) {
                currentDisplay = display;
                display.addDisplayListener(ContextTopComponent.this);
                refreshModel();
            }

            @Override
            public void displayDeselected(MongkieDisplay display) {
                display.removeDisplayListener(ContextTopComponent.this);
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                currentDisplay = null;
                refreshModel();
            }
        });
        currentDisplay = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        boolean actionTextVisible = NbPreferences.forModule(ContextTopComponent.class).getBoolean(ACTION_TEXT_VISIBILITY, true);
        String lastSelectedContextUI = NbPreferences.forModule(ContextTopComponent.class).get(LAST_SELECTED_CONTEXTUI, null);
        for (final ContextUI context : Lookup.getDefault().lookupAll(ContextUI.class)) {
            JToggleButton contextToggleButton = new JToggleButton();
            contextToggleButton.putClientProperty(ContextUI.class, context);
            contextToggleButton.setText(actionTextVisible ? context.getName() : null);
            contextToggleButton.setIcon(context.getIcon());
            contextToggleButton.setToolTipText(context.getTooltip() == null ? context.getName() : context.getTooltip());
            contextToggleButton.setActionCommand(context.getName());
            contextToggleButton.setFocusable(false);
            contextToggleButton.setOpaque(false);
            contextToggleButton.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    switch (e.getStateChange()) {
                        case ItemEvent.SELECTED:
                            selectedContext = context;
                            refreshModel();
                            add(context.getPanel(), BorderLayout.CENTER);
                            revalidate();
                            repaint();
                            NbPreferences.forModule(ContextTopComponent.class).put(LAST_SELECTED_CONTEXTUI, context.getName());
                            break;
                        case ItemEvent.DESELECTED:
                            selectedContext = null;
                            remove(context.getPanel());
                            revalidate();
                            repaint();
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            });
            toggleButtonGroup.add(contextToggleButton);
            toggleToolbar.add(contextToggleButton);
            if ((lastSelectedContextUI != null && lastSelectedContextUI.equals(context.getName()))) {
                contextToggleButton.setSelected(true);
            }
        }
        if (toggleButtonGroup.getSelection() == null) {
            ((JToggleButton) toggleToolbar.getComponentAtIndex(1)).setSelected(true);
        }
        JPopupMenu actionTextVisibilityPopup = new JPopupMenu();
        final JMenuItem toggleActionText = new JMenuItem(actionTextVisible ? HIDE_ACTION_TEXT : SHOW_ACTION_TEXT);
        toggleActionText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean textVisible = !NbPreferences.forModule(ContextTopComponent.class).getBoolean(ACTION_TEXT_VISIBILITY, true);
                for (Component c : toggleToolbar.getComponents()) {
                    if (c instanceof JToggleButton) {
                        JToggleButton b = (JToggleButton) c;
                        if (textVisible) {
                            b.setText(((ContextUI) b.getClientProperty(ContextUI.class)).getName());
                        } else if (b.getIcon() != null) {
                            b.setText(null);
                        }
                    }
                }
                NbPreferences.forModule(ContextTopComponent.class).putBoolean(ACTION_TEXT_VISIBILITY, textVisible);
                toggleActionText.setText(textVisible ? HIDE_ACTION_TEXT : SHOW_ACTION_TEXT);
            }
        });
        actionTextVisibilityPopup.add(toggleActionText);
        toggleToolbar.setComponentPopupMenu(actionTextVisibilityPopup);
    }
    private static final String ACTION_TEXT_VISIBILITY = "ActionTextVisibility";
    private static final String SHOW_ACTION_TEXT = "Show Text";
    private static final String HIDE_ACTION_TEXT = "Hide Text";
    private static final String LAST_SELECTED_CONTEXTUI = "LastSelectedContextUI";

    @Override
    public void perspectiveDeselected(Perspective p) {
        setPerspectiveChanging(true);
    }

    @Override
    public void perspectiveSelected(Perspective p) {
        setPerspectiveChanging(false);
    }

    private void setPerspectiveChanging(boolean changing) {
        if (changing && selectedContext != null) {
            remove(selectedContext.getPanel());
            perspectiveChanging.setBusy(true);
            add(perspectiveChanging, BorderLayout.CENTER);
        } else if (!changing && perspectiveChanging.isBusy()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    perspectiveChanging.setBusy(false);
                    remove(perspectiveChanging);
                    add(selectedContext.getPanel(), BorderLayout.CENTER);
                }
            });
        }
    }

    private void refreshModel() {
        if (currentDisplay == null) {
            selectedContext.unload();
        } else {
            selectedContext.refresh(currentDisplay);
        }
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        selectedContext.unload();
    }

    @Override
    public void graphChanged(final MongkieDisplay d, Graph g) {
        selectedContext.refresh(d);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toggleButtonGroup = new javax.swing.ButtonGroup();
        toggleToolbar = new javax.swing.JToolBar();
        rightAlignmentGlue = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));

        setLayout(new java.awt.BorderLayout());

        toggleToolbar.setFloatable(false);
        toggleToolbar.setRollover(true);
        toggleToolbar.setOpaque(false);
        toggleToolbar.add(rightAlignmentGlue);

        add(toggleToolbar, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler rightAlignmentGlue;
    private javax.swing.ButtonGroup toggleButtonGroup;
    private javax.swing.JToolBar toggleToolbar;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    public static synchronized ContextTopComponent getDefault() {
        if (instance == null) {
            instance = new ContextTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ContextTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized ContextTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ContextTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ContextTopComponent) {
            return (ContextTopComponent) win;
        }
        Logger.getLogger(ContextTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
}
