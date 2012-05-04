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
package org.mongkie.clustering.plugins.clustermaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.mongkie.clustering.spi.Cluster;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class AbstractClustering<B extends ClusteringBuilder> implements Clustering {

    private final List<Cluster> clusters = new ArrayList<Cluster>();
    private final B builder;

    protected AbstractClustering(B builder) {
        this.builder = builder;
    }

    @Override
    public void clearClusters() {
        clusters.clear();
    }

    @Override
    public Collection<Cluster> getClusters() {
        return clusters;
    }

    @Override
    public ClusteringBuilder getBuilder() {
        return builder;
    }

    public static double mean(Double[] vector) {
        double result = 0.0;
        for (int i = 0; i < vector.length; i++) {
            result += vector[i].doubleValue();
        }
        return (result / (double) vector.length);
    }

    // Inefficient, but simple approach to finding the median
    public static double median(Double[] vector) {
        // Clone the input vector
        Double[] vectorCopy = new Double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            vectorCopy[i] = new Double(vector[i].doubleValue());
        }

        // sort it
        Arrays.sort(vectorCopy);

        // Get the median
        int mid = vector.length / 2;
        if (vector.length % 2 == 1) {
            return (vectorCopy[mid].doubleValue());
        }
        return ((vectorCopy[mid - 1].doubleValue() + vectorCopy[mid].doubleValue()) / 2);
    }
}
