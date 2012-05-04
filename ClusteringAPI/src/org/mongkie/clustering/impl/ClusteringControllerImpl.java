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
package org.mongkie.clustering.impl;

import java.awt.Color;
import java.util.Collection;
import org.mongkie.clustering.ClusteringController;
import org.mongkie.clustering.spi.Cluster;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.longtask.progress.Progress;
import org.mongkie.longtask.progress.ProgressTask;
import org.mongkie.longtask.progress.ProgressTicket;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.group.GroupingSupport;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ClusteringController.class)
public class ClusteringControllerImpl extends ClusteringController<ClusteringModelImpl> {

    private GroupingSupport<Cluster> gs = new GroupingSupport<Cluster>(this);

    @Override
    protected Class<ClusteringModelImpl> getModelClass() {
        return ClusteringModelImpl.class;
    }

    @Override
    protected ClusteringModelImpl createModel(MongkieDisplay d) {
        return new ClusteringModelImpl(d);
    }

    @Override
    public void clusterize() {
        final Clustering cl = model.get();
        final ClusteringTask task = new ClusteringTask(cl);
        model.getExecutor().execute(task, new Runnable() {

            @Override
            public void run() {
                ProgressTicket ticket = task.getProgressTicket();
                Progress.setDisplayName(ticket, cl.getBuilder().getName() + " Clustering");
                Progress.start(ticket);
                cl.execute(model.getDisplay().getGraph());
                Progress.finish(ticket);
            }
        });
    }

    @Override
    public void cancelClustering() {
        model.getExecutor().cancel();
    }

    @Override
    public AggregateItem group(Cluster c) {
        return gs.group(c);
    }

    @Override
    public void ungroup(Cluster c) {
        gs.ungroup(c);
    }

    @Override
    public boolean isGrouped(Cluster c) {
        return gs.isGrouped(c);
    }

    @Override
    public AggregateItem getGroup(Cluster c) {
        return gs.getGroup(c);
    }

    @Override
    public Collection<Node> getNodes(Cluster c) {
        return c.getNodes();
    }

    @Override
    public String getName(Cluster c) {
        int rank = c.getRank();
        return String.valueOf((rank < 0) ? 0 : rank + 1);
    }

    @Override
    public Color getColor(Cluster c) {
        return c.getColor();
    }

    @Override
    public void ungrouped(AggregateItem group) {
    }

    private static class ClusteringTask extends ProgressTask {

        private final Clustering cl;

        public ClusteringTask(Clustering cl) {
            this.cl = cl;
        }

        @Override
        public boolean cancel() {
            return cl.cancel();
        }
    }
}
