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
import static org.mongkie.kopath.Config.FIELD_NAME;
import org.mongkie.kopath.rest.PathwayService;
import org.mongkie.kopath.spi.PathwayDatabase;
import org.mongkie.kopath.viz.PathwayDisplay;
import prefuse.data.Graph;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SuperPathwayWorker extends SwingWorker<Graph, Void> {

    protected final PathwayDisplay display;
    protected final String[] geneInput;
    protected final String[] pathwayInput;
    protected final String xTableName;

    public SuperPathwayWorker(PathwayDisplay display, String[] geneInput, String[] pathwayInput, String xTableName) {
        this.display = display;
        this.geneInput = geneInput;
        this.pathwayInput = Arrays.copyOf(pathwayInput, pathwayInput.length);
        this.xTableName = xTableName;
    }

    public void precess() {
        display.setLoading(true, true);
    }

    @Override
    protected Graph doInBackground() throws Exception {
        precess();

        Logger.getLogger(getClass().getName()).log(Level.INFO,
                "Retrieving interactions from {0} in the SuperPathway DB", new Object[]{Arrays.toString(pathwayInput)});
        long timeIn = System.currentTimeMillis();
        Graph g = PathwayService.getGraphFromSuperPathway(xTableName, geneInput, pathwayInput);
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
                    "Can not retrieve interactions from " + Arrays.toString(pathwayInput) + " in the " + PathwayDatabase.Lookup.valueOf(0).getName(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        lastly();
    }

    public void process(Graph g) {
        g.setNodeLabelField(FIELD_NAME);
        display.setIntegratedPathway(true);
        display.resetGraph(g);
    }

    public void lastly() {
        display.setLoading(false);
    }
}
