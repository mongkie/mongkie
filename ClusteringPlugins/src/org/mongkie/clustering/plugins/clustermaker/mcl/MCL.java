/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKIE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.clustering.plugins.clustermaker.mcl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.mongkie.clustering.plugins.clustermaker.DistanceMatrix;
import org.mongkie.clustering.plugins.clustermaker.converters.DistanceConverter;
import org.mongkie.clustering.plugins.clustermaker.converters.EdgeWeightConverter;
import org.mongkie.clustering.plugins.clustermaker.converters.LogConverter;
import org.mongkie.clustering.plugins.clustermaker.converters.NegLogConverter;
import org.mongkie.clustering.plugins.clustermaker.converters.NoneConverter;
import org.mongkie.clustering.plugins.clustermaker.converters.SCPSConverter;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import org.mongkie.visualization.VisualizationController;
import org.openide.util.Lookup;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MCL implements Clustering<MCLCluster> {

    private double inflationParameter = 2.0;
    private int rNumber = 16;
    private double clusteringThresh = 1.0E-15;
    private double maxResidual = 1.0E-4;
    private int maxThreads = 0;
    // Distance matrix parameters
    private boolean directed = false;
    private Double edgeCutOff = 1.0D;
    private boolean adjustLoops = true;
    private boolean selectedOnly = false;
    private EdgeWeightConverter converter;
    // State variable
    private volatile boolean cancelled = false;
    private MCLAlgorithm algo = null;
    private final List<EdgeWeightConverter> converters;
    private final MCLBuilder builder;

    public MCL(MCLBuilder builder) {
        this.builder = builder;
        converters = new ArrayList<EdgeWeightConverter>();
        converters.add(new NoneConverter());
        converters.add(new DistanceConverter());
        converters.add(new LogConverter());
        converters.add(new NegLogConverter());
        converters.add(new SCPSConverter());
        converter = converters.get(0);
    }

    @Override
    public Collection<MCLCluster> execute(Graph g) {
        cancelled = false;

        List<Node> selectedNodes = null;
        if (selectedOnly) {
            for (VisualItem item :
                    Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().getSelectedItems()) {
                if (item instanceof NodeItem) {
                    selectedNodes.add((Node) item.getSourceTuple());
                }
            }
        }
        DistanceMatrix matrix = DistanceMatrix.create(g, selectedNodes, converter, directed, edgeCutOff, adjustLoops);
        if (matrix == null) {
            Logger.getLogger(MCL.class.getName()).severe(
                    "Can't get distance matrix: no attribute value?");
            return null;
        }

        algo = new MCLAlgorithm(g, matrix, inflationParameter, rNumber, clusteringThresh, maxResidual, maxThreads);

        if (cancelled) {
            return null;
        }

        algo.setDebug(false);
        List<MCLCluster> results = algo.run();
        return results;
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        algo.cancel();
        return false;
    }

    @Override
    public ClusteringBuilder getBuilder() {
        return builder;
    }
}
