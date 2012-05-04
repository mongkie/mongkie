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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.mongkie.kopath.rest.PathwayService;
import org.mongkie.kopath.spi.PathwayDatabase;
import org.mongkie.kopath.viz.PathwayDisplay;
import prefuse.data.Graph;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class PathwayWorker extends SwingWorker<Graph, Void> {

    protected final PathwayDisplay display;
    protected final int dbId;
    protected final String[] pathwayIds;

    public PathwayWorker(PathwayDisplay display, int dbId, String... pathwayIds) {
        this.display = display;
        this.dbId = dbId;
        this.pathwayIds = Arrays.copyOf(pathwayIds, pathwayIds.length);
    }

    public abstract void precess();

    @Override
    protected Graph doInBackground() throws Exception {
        precess();
        display.revalidate();
        display.repaint();

        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "Retrieving the pathway: {0} from {1}", new Object[]{Arrays.toString(pathwayIds), PathwayDatabase.Lookup.valueOf(dbId).getName()});
        long timeIn = System.currentTimeMillis();
        Graph g = PathwayService.getGraph(dbId, pathwayIds);
        long time = System.currentTimeMillis() - timeIn;
        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "Retrieving completed: {0}.{1} seconds.", new Object[]{time / 1000, time % 1000});
        return g;
    }

    @Override
    protected void done() {
        Graph g = null;
        try {
            g = get();
            Logger.getLogger(getClass().getName()).log(Level.INFO,
                    "Entity count: {0}, Relation count: {1}", new Integer[]{g.getNodeCount(), g.getEdgeCount()});
        } catch (InterruptedException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }

        if (g != null) {
            process(g);
        } else {
            JOptionPane.showMessageDialog(display,
                    "Can not retrieve the pathway: " + Arrays.toString(pathwayIds) + " from " + PathwayDatabase.Lookup.valueOf(dbId).getName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        lastly();
        display.revalidate();
        display.repaint();
    }

    public abstract void process(Graph g);

    public abstract void lastly();
}
