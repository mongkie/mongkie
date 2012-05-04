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

import javax.swing.Icon;
import org.mongkie.exporter.ExportControllerUI;
import org.mongkie.exporter.spi.Exporter;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@Messages({
    "ExportImage.menu.displayName=Image Files..."
})
@ServiceProvider(service = Exporter.MenuUI.class, position = 200)
public class ExportImage implements Exporter.MenuUI {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ExportImage.class, "ExportImage.menu.displayName");
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/mongkie/ui/exporter/resources/save_bitmap.png", false);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void performAction() {
        Lookup.getDefault().lookup(ExportControllerUI.class).exportImage();
    }
}
