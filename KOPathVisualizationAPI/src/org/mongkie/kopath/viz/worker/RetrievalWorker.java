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
package org.mongkie.kopath.viz.worker;

import static org.mongkie.kopath.Config.FIELD_NAME;
import static org.mongkie.kopath.Config.FIELD_NODEID;
import org.mongkie.kopath.viz.PathwayDisplay;
import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.visual.NodeItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class RetrievalWorker extends PathwayWorker {

    public RetrievalWorker(PathwayDisplay display, int dbId, String... pathwayIds) {
        super(display, dbId, pathwayIds);
    }

    @Override
    public void precess() {
        display.setLoading(true, true);
    }

    @Override
    public void process(Graph g) {
        g.setNodeLabelField(FIELD_NAME);
        display.setIntegratedPathway(dbId == 0);
        display.reset(g);
        if (display.isIntegratedPathway()) {
            for (String pathwayId : pathwayIds) {
                ((NodeItem) display.getVisualGraph().getNodeFrom(FIELD_NODEID, pathwayId)).setExpandable(false);
            }
        }

    }

    @Override
    public void lastly() {
        if (display.isIntegratedPathway()) {
            for (String pathwayId : pathwayIds) {
                display.getVisualization().getFocusGroup(Visualization.FOCUS_ITEMS).addTuple(
                        (NodeItem) display.getVisualGraph().getNodeFrom(FIELD_NODEID, pathwayId));
            }
        }
        display.setLoading(false);
    }
}
