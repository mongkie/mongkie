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
package org.mongkie.ui.exporter.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.exporter.spi.Exporter;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.CallableSystemAction;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ActionID(category = "File",
id = "org.mongkie.ui.exporter.actions.Export")
@ActionRegistration(displayName = "#CTL_Export")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 400)
})
@Messages("CTL_Export=Export")
public final class Export extends CallableSystemAction implements DisplayListener<MongkieDisplay> {

    private final JMenu exportMenu;

    public Export() {
        exportMenu = new JMenu(NbBundle.getMessage(Export.class, "CTL_Export"));
        Lookup.getDefault().lookup(VisualizationController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void displaySelected(MongkieDisplay display) {
                exportMenu.setEnabled(display.getGraph().getNodeCount() > 0);
                display.addDisplayListener(Export.this);
            }

            @Override
            public void displayDeselected(MongkieDisplay display) {
                display.removeDisplayListener(Export.this);
            }

            @Override
            public void displayClosed(MongkieDisplay display) {
            }

            @Override
            public void displayClosedAll() {
                exportMenu.setEnabled(false);
            }
        });
        MongkieDisplay d = Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
        if (d != null) {
            exportMenu.setEnabled(d.getGraph().getNodeCount() > 0);
            d.addDisplayListener(Export.this);
        }
    }

    @Override
    public JMenuItem getMenuPresenter() {
        for (final Exporter.MenuUI menu : Lookup.getDefault().lookupAll(Exporter.MenuUI.class)) {
            final JMenuItem menuItem = new JMenuItem(menu.getDisplayName(), menu.getIcon());
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    menu.performAction();
                }
            });
            exportMenu.add(menuItem);
            exportMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    menuItem.setEnabled(menu.isEnabled());
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
        return exportMenu;
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Export.class, "CTL_Export");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        exportMenu.setEnabled(false);
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        exportMenu.setEnabled(g.getNodeCount() > 0);
    }
}
