/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualization;

import org.mongkie.visualization.group.GroupManager;
import org.mongkie.visualization.selection.SelectionManager;
import org.mongkie.visualization.workspace.WorkspaceListener;
import org.openide.util.LookupListener;
import prefuse.Visualization;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface VisualizationController extends LookupListener {

    public Visualization getVisualization();

    public MongkieDisplay getDisplay();

    public void displayClosed(MongkieDisplay d);

    public void addWorkspaceListener(WorkspaceListener l);

    public void removeWorkspaceListener(WorkspaceListener l);

    public SelectionManager getSelectionManager();

    public GroupManager getGroupManager();
}
