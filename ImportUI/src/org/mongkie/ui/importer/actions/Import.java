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
package org.mongkie.ui.importer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.mongkie.importer.spi.Importer;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ActionID(category = "File", id = "org.mongkie.ui.importer.actions.Import")
@ActionRegistration(displayName = "#CTL_Import")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 300)
})
public final class Import extends CallableSystemAction {

    private final JMenu importMenu;

    public Import() {
        importMenu = new JMenu(NbBundle.getMessage(Import.class, "CTL_Import"));
    }

    @Override
    public JMenuItem getMenuPresenter() {
        for (final Importer.MenuUI menu : Lookup.getDefault().lookupAll(Importer.MenuUI.class)) {
            final JMenuItem menuItem = new JMenuItem(menu.getDisplayName(), menu.getIcon());
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    menu.performAction();
                }
            });
            importMenu.add(menuItem);
            importMenu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {

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
        return importMenu;
    }

    @Override
    public void performAction() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(Import.class, "CTL_Import");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
}
