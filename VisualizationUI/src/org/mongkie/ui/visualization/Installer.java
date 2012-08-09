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
package org.mongkie.ui.visualization;

import java.util.logging.Logger;
import org.mongkie.ui.visualization.util.StatusLogDisplayer;
import static org.mongkie.visualization.Config.MODE_DISPLAY;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        Logger.getLogger("").addHandler(StatusLogDisplayer.getInstance());
    }

    @Override
    public boolean closing() {
        Logger.getLogger("").removeHandler(StatusLogDisplayer.getInstance());
        // Close all TopComponents in the MODE_DISPLAY mode.
        // This required to open only one display at start up,
        // because DisplayTopComponent's persistent type is set to PERSISTENCE_ONLY_OPENED.
        for (TopComponent tc : WindowManager.getDefault().findMode(MODE_DISPLAY).getTopComponents()) {
            tc.close();
        }
        return true;
    }
}
