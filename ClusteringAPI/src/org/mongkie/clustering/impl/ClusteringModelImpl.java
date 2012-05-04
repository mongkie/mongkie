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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.clustering.ClusteringModel;
import org.mongkie.clustering.ClusteringModelListener;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.longtask.LongTask;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.LongTaskListener;
import org.mongkie.visualization.MongkieDisplay;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ClusteringModelImpl extends ClusteringModel {

    private LongTaskExecutor executor;

    public ClusteringModelImpl(MongkieDisplay display) {
        super(display);

        executor = new LongTaskExecutor(true, "Clustering");
        executor.setLongTaskListener(new LongTaskListener() {

            @Override
            public void taskStarted(LongTask task) {
                setRunning(true);
            }

            @Override
            public void taskFinished(LongTask task) {
                setRunning(false);
            }
        });
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        });
    }

    LongTaskExecutor getExecutor() {
        return executor;
    }

    @Override
    public boolean isRunning() {
        return executor.isRunning();
    }

    @Override
    protected void setRunning(boolean running) {
        if (running) {
            for (ClusteringModelListener l : listeners) {
                l.clusteringStarted(get());
            }
        } else {
            for (ClusteringModelListener l : listeners) {
                l.clusteringFinished(get());
            }
        }
    }

    @Override
    protected void changed(Clustering o, Clustering n) {
        for (ClusteringModelListener l : listeners) {
            l.clusteringChanged(o, n);
        }
    }

    @Override
    protected void load(Clustering cl) {
//        cl.clearClusters();
    }

    @Override
    protected void unload(Clustering cl) {
        if (isRunning()) {
            executor.cancel();
        }
    }
}
