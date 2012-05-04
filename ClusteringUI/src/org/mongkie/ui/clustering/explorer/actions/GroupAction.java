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
package org.mongkie.ui.clustering.explorer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.mongkie.clustering.ClusteringController;
import org.mongkie.clustering.spi.Cluster;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ActionID(category = "Clustering",
id = "org.mongkie.ui.clustering.explorer.actions.GroupAction")
@ActionRegistration(iconBase = "org/mongkie/ui/clustering/resources/group.png",
displayName = "#CTL_GroupAction")
@ActionReferences({})
@NbBundle.Messages("CTL_GroupAction=Group")
public final class GroupAction implements ActionListener {

    private final List<Cluster> clusters;

    public GroupAction(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        for (Cluster c : clusters) {
            Lookup.getDefault().lookup(ClusteringController.class).group(c);
        }
    }
}
