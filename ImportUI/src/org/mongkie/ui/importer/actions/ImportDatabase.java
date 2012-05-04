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

import javax.swing.Icon;
import org.mongkie.importer.spi.Importer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = Importer.MenuUI.class, position = 300)
public class ImportDatabase implements Importer.MenuUI {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ImportDatabase.class, "ImportDatabase.menu.displayName");
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/mongkie/ui/importer/resources/databaseConnection.png", false);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void performAction() {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Not supported yet.", NotifyDescriptor.INFORMATION_MESSAGE));
    }
}
