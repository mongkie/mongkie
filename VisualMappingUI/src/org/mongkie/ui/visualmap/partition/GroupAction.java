/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.ui.visualmap.partition;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualmap.partition.PartitionController;
import org.mongkie.visualmap.spi.partition.Part;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import prefuse.Visualization;

@ActionID(category = "Partition",
id = "org.mongkie.ui.visualmap.partition.GroupAction")
@ActionRegistration(displayName = "#CTL_GroupAction",
iconBase = "org/mongkie/ui/visualmap/resources/group.png")
@ActionReferences({})
@Messages("CTL_GroupAction=Group")
public final class GroupAction implements ActionListener {

    private final List<Part> parts;

    public GroupAction(List<Part> parts) {
        this.parts = parts;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (Part p : parts) {
            Lookup.getDefault().lookup(PartitionController.class).group(p);
        }
        Lookup.getDefault().lookup(VisualizationController.class).getVisualization().rerun(Visualization.DRAW);
    }
}
