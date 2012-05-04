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

import static org.mongkie.visualization.Config.MODE_DISPLAY;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationControllerUI;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualizationControllerUI.class)
public class VisualizationControllerUIImpl implements VisualizationControllerUI {

    @Override
    public org.mongkie.visualization.DisplayTopComponent openNewDisplayTopComponent(Graph g) {
        return openNewDisplayTopComponent("Network", g);
    }

    @Override
    public org.mongkie.visualization.DisplayTopComponent openNewDisplayTopComponent(String title, Graph g) {
        return openNewDisplayTopComponent(title, g, false);
    }

    @Override
    public org.mongkie.visualization.DisplayTopComponent openNewDisplayTopComponent(String title, Graph g, boolean loading) {
        DisplayTopComponent tc = (DisplayTopComponent) WindowManager.getDefault().findMode(MODE_DISPLAY).getSelectedTopComponent();
        MongkieDisplay currentDisplay = tc != null ? tc.getDisplay() : null;
        if (currentDisplay != null && currentDisplay.getGraph().getNodeCount() < 1) {
            if (g != null) {
                currentDisplay.resetGraph(g);
            } else {
                currentDisplay.setLoading(loading);
            }
        } else {
            tc = new DisplayTopComponent(g, loading);
            WindowManager.getDefault().findMode(MODE_DISPLAY).dockInto(tc);
        }
        if (title != null && !title.isEmpty()) {
            tc.setDisplayName(title);
        }
        tc.open();
        tc.requestActive();
        return tc;
    }

    @Override
    public org.mongkie.visualization.DisplayTopComponent openEmptyDisplayTopComponent(String title, boolean loading) {
        return openNewDisplayTopComponent((title == null || title.isEmpty()) ? "Network" : title, (Graph) null, loading);
    }

    @Override
    public void invokeWhenUIReady(Runnable run) {
        WindowManager.getDefault().invokeWhenUIReady(run);
    }
}
