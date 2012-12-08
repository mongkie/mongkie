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
package org.mongkie.clustering.plugins.mcl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.mongkie.clustering.DefaultClusterImpl;
import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * MarkovClustering implements the Markov clustering (MCL) algorithm for graphs,
 * using a HashMap-based sparse representation of a Markov matrix, i.e., an
 * adjacency matrix m that is normalised to one. Elements in a column / node can
 * be interpreted as decision probabilities of a random walker being at that
 * node. Note: whereas we explain the algorithms with columns, the actual
 * implementation works with rows because the used sparse matrix has row-major
 * format.
 * <p>
 * The basic idea underlying the MCL algorithm is that dense regions in sparse
 * graphs correspond with regions in which the number of k-length paths is
 * relatively large, for small k in N, which corresponds to multiplying
 * probabilities in the matrix appropriately. Random walks of length k have
 * higher probability (product) for paths with beginning and ending in the same
 * dense region than for other paths.
 * <p>
 * The algorithm starts by creating a Markov matrix from the graph, for which
 * first the adjacency matrix is added diagonal elements to include self-loops
 * for all nodes, i.e., probabilities that the random walker stays at a
 * particular node. After this initialisation, the algorithm works by
 * alternating two operations, expansion and inflation, iteratively recomputing
 * the set of transition probabilities. The expansion step corresponds to matrix
 * multiplication (on stochastic matrices), the inflation step corresponds with
 * a parametrized inflation operator Gamma_r, which acts column-wise on (column)
 * stochastic matrices (here, we use row-wise operation, which is analogous).
 * <p>
 * The inflation operator transforms a stochastic matrix into another one by
 * raising each element to a positive power p and re-normalising columns to keep
 * the matrix stochastic. The effect is that larger probabilities in each column
 * are emphasised and smaller ones deemphasised. On the other side, the matrix
 * multiplication in the expansion step creates new non-zero elements, i.e.,
 * edges. The algorithm converges very fast, and the result is an idempotent
 * Markov matrix, M = M * M, which represents a hard clustering of the graph
 * into components.
 * <p>
 * Expansion and inflation have two opposing effects: While expansion flattens
 * the stochastic distributions in the columns and thus causes paths of a random
 * walker to become more evenly spread, inflation contracts them to favoured
 * paths.
 * <p>
 * Description is based on the introduction of Stijn van Dongen's thesis Graph
 * Clustering by Flow Simulation (2000); for a mathematical treatment of the
 * algorithm and the associated MCL process, see there.
 *
 * Original author: Gregor Heinrich
 *
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>, Yeongjun Jang <yjjang@kribb.re.kr>
 * @deprecated
 */
class MarkovCLustering implements Clustering<DefaultClusterImpl> {

    private final MarkovCLusteringBuilder builder;
    private List<DefaultClusterImpl> clusters = new ArrayList<DefaultClusterImpl>();
    private volatile boolean cancelled = false;
    // Parameters
    private double maxResidual = 0.001;
    private double gammaExp = 2.0;
    private double loopGain = 0.;
    private double zeroMax = 0.001;

    MarkovCLustering(MarkovCLusteringBuilder builder) {
        this.builder = builder;
    }

    @Override
    public Collection<DefaultClusterImpl> execute(Graph g) {
        clusters.clear();
        cancelled = false;

        HashMap<Integer, Node> nodeMap = new HashMap<Integer, Node>();
        HashMap<Node, Integer> intMap = new HashMap<Node, Integer>();

        //Load matrix
        SparseMatrix matrix = new SparseMatrix();
        int nodeId = 0;
        for (Iterator<Edge> edgeIter = g.edges(); edgeIter.hasNext();) {
            Edge e = edgeIter.next();
            Node source = e.getSourceNode();
            Node target = e.getTargetNode();
            Integer sourceId;
            Integer targetId;
            if ((sourceId = intMap.get(source)) == null) {
                sourceId = nodeId++;
                intMap.put(source, sourceId);
                nodeMap.put(sourceId, source);
            }
            if ((targetId = intMap.get(target)) == null) {
                targetId = nodeId++;
                intMap.put(target, targetId);
                nodeMap.put(targetId, target);
            }
            double weight = e.getWeight();
            double w2 = matrix.add(sourceId, targetId, weight);
            if (w2 > weight) {
                System.out.println("sid: " + sourceId + "(" + source.getString("Label") + ")" + ", tid: " + targetId + "(" + target.getString("Label") + ")" + ", weight: " + weight + ", w2: " + w2);
            }
            if (cancelled) {
                return clusters;
            }
        }

        System.out.println("> " + nodeId);
        System.out.println(">> " + matrix.getSize()[0] + ", " + matrix.getSize()[1]);
        System.out.println(">>> " + nodeMap.size());
        matrix = matrix.transpose();
        matrix = run(matrix, maxResidual, gammaExp, loopGain, zeroMax);

        if (cancelled) {
            return clusters;
        }

        Map<Integer, ArrayList<Integer>> map = getClusterMap(matrix);

        if (cancelled) {
            return clusters;
        }

        Set<ArrayList<Integer>> sortedClusters = new HashSet<ArrayList<Integer>>();
        int rank = 0;
        for (ArrayList<Integer> nodeIdList : map.values()) {
            if (!sortedClusters.contains(nodeIdList)) {
                sortedClusters.add(nodeIdList);
                DefaultClusterImpl c = new DefaultClusterImpl(g) {
                    @Override
                    public void setRank(int rank) {
                        super.setRank(rank);
                        setName("Cluster " + (rank + 1));
                    }
                };
                for (Integer nId : nodeIdList) {
                    c.addNode(nodeMap.get(nId));
                }
                c.setRank(rank++);
                clusters.add(c);
            }
            if (cancelled) {
                break;
            }
        }

        return clusters;
    }

    /**
     * run the MCL process.
     *
     * @param a           matrix
     * @param maxResidual maximum difference between row elements and row square
     *                    sum (measure of idempotence)
     * @param pGamma      inflation exponent for Gamma operator
     * @param loopGain    values for cycles
     * @param maxZero     maximum value considered zero for pruning operations
     * @return the resulting matrix
     */
    private SparseMatrix run(SparseMatrix a, double maxResidual, double pGamma, double loopGain, double maxZero) {

        //System.out.println("original matrix\n" + a.transpose().toStringDense());

        // add cycles
        addLoops(a, loopGain);

        // make stochastic
        a.normaliseRows();
        //System.out.println("normalised\n" + a.transpose().toStringDense());

        double residual = 1.;
        int i = 0;

        if (cancelled) {
            return a;
        }

        // main iteration
        while (residual > maxResidual) {
            i++;
            a = expand(a);
            residual = inflate(a, pGamma, maxZero);
            System.out.println("residual energy = " + residual);
            if (cancelled) {
                return a;
            }
        }
        return a;
    }

    /**
     * add loops with specific energy, which corresponds to adding loopGain to
     * the diagonal elements.
     *
     * @param a
     * @param loopGain
     */
    private void addLoops(SparseMatrix a, double loopGain) {
        if (loopGain <= 0) {
            return;
        }
        for (int i = 0; i < a.size(); i++) {
            a.add(i, i, loopGain);
            if (cancelled) {
                return;
            }
        }
    }

    /**
     * expand stochastic quadratic matrix by sqaring it with itself: result = m *
     * m. Here normalisation is rowwise.
     *
     * @param matrix
     * @return new matrix (pointer != argument)
     */
    private SparseMatrix expand(SparseMatrix m) {
        m = m.times(m);
        return m;
    }

    /**
     * inflate stochastic matrix by Hadamard (elementwise) exponentiation,
     * pruning and normalisation :
     * <p>
     * result = Gamma ( m, p ) = normalise ( prune ( m .^ p ) ).
     * <p>
     * By convention, normalisation is done along rows (SparseMatrix has
     * row-major representation)
     *
     * @param m       matrix (mutable)
     * @param p       exponent as a double
     * @param zeromax below which elements are pruned from the sparse matrix
     * @return residuum value, m is modified.
     */
    private double inflate(SparseMatrix m, double p, double zeromax) {
        double res = 0.;

        // matlab: m = m .^ p
        m.hadamardPower(p);
        // matlab: m(find(m < threshold)) = 0
        m.prune(zeromax);
        // matlab [for cols]: dinv = diag(1./sum(m)); m = m * dinv; return
        // sum(m)
        SparseVector rowsums = m.normalise(1.);

        // check if done: if the maximum element
        for (int i : rowsums.keySet()) {
            SparseVector row = m.get(i);
            double max = row.max();
            double sumsq = row.sum(2.);
            res = Math.max(res, max - sumsq);
            if (cancelled) {
                return res;
            }
        }
        return res;
    }

    private Map<Integer, ArrayList<Integer>> getClusterMap(SparseMatrix matrix) {

        Map<Integer, ArrayList<Integer>> clusterMap = new HashMap<Integer, ArrayList<Integer>>();
        int clusterCount = 0;

        double[][] mat = matrix.getDense();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[0].length; j++) {
                double value = mat[i][j];
                if (value != 0.0) {
                    if (i == j) {
                        continue;
                    }

                    if (clusterMap.containsKey(j)) {
                        // Already seen "column" -- get the cluster and add column
                        ArrayList<Integer> columnCluster = clusterMap.get(j);
                        if (clusterMap.containsKey(i)) {
                            // We've already seen row also -- join them
                            ArrayList<Integer> rowCluster = clusterMap.get(i);
                            if (rowCluster == columnCluster) {
                                continue;
                            }
                            columnCluster.addAll(rowCluster);
                            clusterCount--;
                        } else {
                            // debugln("Adding "+row+" to "+columnCluster.getClusterNumber());
                            columnCluster.add(i);
                        }
                        for (Integer in : columnCluster) {
                            clusterMap.put(in, columnCluster);
                        }
                    } else {
                        ArrayList<Integer> rowCluster;
                        // First time we've seen "column" -- have we already seen "row"
                        if (clusterMap.containsKey(i)) {
                            // Yes, just add column to row's cluster
                            rowCluster = clusterMap.get(i);
                            // debugln("Adding "+column+" to "+rowCluster.getClusterNumber());
                            rowCluster.add(j);
                        } else {
                            rowCluster = new ArrayList<Integer>();
                            clusterCount++;
                            // debugln("Created new cluster "+rowCluster.getClusterNumber()+" with "+row+" and "+column);
                            rowCluster.add(j);
                            rowCluster.add(i);
                        }
                        for (Integer in : rowCluster) {
                            clusterMap.put(in, rowCluster);
                        }
                    }
                }
                if (cancelled) {
                    return clusterMap;
                }
            }
        }

        return clusterMap;
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }

    @Override
    public ClusteringBuilder getBuilder() {
        return builder;
    }
}
