/**
 * Copyright (c) 2010 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.mongkie.clustering.plugins.clustermaker.mcl;

import org.mongkie.clustering.plugins.clustermaker.DistanceMatrix;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import cern.colt.function.IntIntDoubleFunction;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.clustering.spi.Cluster;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

public class MCLAlgorithm {

    private double inflationParameter; //density parameter 
    private int iterationsNumber; //number of inflation/expansion cycles
    private double clusteringThresh; //Threshold used to remove weak edges between distinct clusters
    private double maxResidual; //The maximum residual to look for
    private List<Node> nodes;
    private List<Edge> edges;
    private volatile boolean cancelled = false;
    private static final Logger logger = Logger.getLogger(MCLAlgorithm.class.getName());
    protected int clusterCount = 0;
    private boolean createMetaNodes = false;
    private DistanceMatrix distanceMatrix = null;
    private DoubleMatrix2D matrix = null;
    private boolean debug = false;
    private int nThreads = Runtime.getRuntime().availableProcessors() - 1;
    private Graph g;

    public MCLAlgorithm(Graph g, DistanceMatrix dmatrix,
            double inflationParameter, int iterationsNumber, double clusteringThresh, double maxResidual, int maxThreads) {
        this.g = g;
        this.distanceMatrix = dmatrix;
        this.inflationParameter = inflationParameter;
        this.iterationsNumber = iterationsNumber;
        this.clusteringThresh = clusteringThresh;
        this.maxResidual = maxResidual;
        nodes = distanceMatrix.getNodes();
        edges = distanceMatrix.getEdges();
        matrix = distanceMatrix.getDistanceMatrix();
        if (maxThreads > 0) {
            nThreads = maxThreads;
        } else {
            nThreads = Runtime.getRuntime().availableProcessors() - 1;
        }
//        logger.log(Level.INFO, "InflationParameter = {0}", inflationParameter);
//        logger.log(Level.INFO, "Iterations = {0}", iterationsNumber);
//        logger.log(Level.INFO, "Clustering Threshold = {0}", clusteringThresh);
    }

    public void cancel() {
        cancelled = true;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public List<? extends Cluster> run() {
        cancelled = false;
        long startTime = System.currentTimeMillis();

        debugln("Initial matrix:");
        printMatrix(matrix);

        // Normalize
        normalize(matrix, clusteringThresh, false);

        debugln("Normalized matrix:");
        printMatrix(matrix);

        // logger.info("Calculating clusters");

        double residual = 1.0;
        IntIntDoubleFunction myPow = new MatrixPow(inflationParameter);
        // debugln("residual = "+residual+" maxResidual = "+maxResidual);
        for (int i = 0; (i < iterationsNumber) && (residual > maxResidual); i++) {
            // Expand
            {
                long t = System.currentTimeMillis();
                logger.log(Level.INFO, "Iteration: {0} expanding..", (i + 1));
                // debugln("Iteration: "+(i+1)+" expanding ");
                // printMatrixInfo(matrix);
                if (nThreads > 1) {
                    matrix = multiplyMatrix(matrix, matrix);
                } else {
                    DoubleMatrix2D newMatrix = DoubleFactory2D.sparse.make(matrix.rows(), matrix.columns());
                    matrix = matrix.zMult(matrix, newMatrix);
                }
                // Normalize
                normalize(matrix, clusteringThresh, false);
                logger.log(Level.FINE, "Expansion {0} took {1}ms", new Object[]{i + 1, System.currentTimeMillis() - t});
            }

            // printMatrix(matrix);
            // debugln("^ "+(i+1)+" after expansion");

            // Inflate
            {
                long t = System.currentTimeMillis();
                logger.log(Level.INFO, "Iteration: {0} inflating..", (i + 1));
                // debugln("Iteration: "+(i+1)+" inflating");
                // printMatrixInfo(matrix);
                matrix.forEachNonZero(myPow);
                // Normalize
                normalize(matrix, clusteringThresh, true);
                logger.log(Level.FINE, "Inflation {0} took {1}ms", new Object[]{i + 1, System.currentTimeMillis() - t});
            }

            // printMatrix(matrix);
            // debugln("^ "+(i+1)+" after inflation");

            matrix.trimToSize();
            residual = calculateResiduals(matrix);
            // debugln("Iteration: "+(i+1)+" residual: "+residual);

            if (cancelled) {
                logger.log(Level.INFO, "cancelled");
                return null;
            }
        }

        // If we're in debug mode, output the matrix
        // printMatrixInfo(matrix);
        // printMatrix(matrix);

        logger.log(Level.INFO, "Assigning nodes to clusters...");

        clusterCount = 0;
        HashMap<Integer, MCLCluster> clusterMap = new HashMap<Integer, MCLCluster>();
        matrix.forEachNonZero(new ClusterMatrix(clusterMap));
        // System.out.println("Cluster map has "+clusterMap.keySet().size()+" clusters");

        //Update node attributes in network to include clusters. Create cygroups from clustered nodes
        logger.log(Level.INFO, "{0} clusters created", clusterCount);
        logger.log(Level.FINE, "Cluster map has {0} clusters", clusterMap.keySet().size());
        // debugln("Created "+clusterCount+" clusters:");
        //
        if (clusterCount == 0) {
            logger.log(Level.SEVERE, "Created 0 clusters!!!!");
            logger.log(Level.SEVERE, "Cluster map has {0} clusters", clusterMap.keySet().size());
            return null;
        }

        int clusterNumber = 1;
        HashMap<MCLCluster, MCLCluster> cMap = new LinkedHashMap<MCLCluster, MCLCluster>();
        for (MCLCluster cluster : MCLCluster.sortMap(clusterMap)) {

            if (cMap.containsKey(cluster)) {
                continue;
            }

            // for (Integer i: cluster) {
            // 	CyNode node = nodes.get(i.intValue());
            // 	debug(node.getIdentifier()+"\t");
            // }
            // debugln();
            cMap.put(cluster, cluster);

            cluster.setClusterNumber(clusterNumber);
            clusterNumber++;
        }

        logger.log(Level.FINE, "Total runtime: {0}ms", (System.currentTimeMillis() - startTime));

        Set<MCLCluster> clusters = cMap.keySet();
        return new ArrayList<MCLCluster>(clusters);
    }

    /**
     * This method does threshold and normalization.  First, we get rid of
     * any cells that have a value beneath our threshold and in the same pass
     * calculate all of the column sums.  Then we use the column sums to normalize
     * each column such that all of the cells in the column sum to 1.
     *
     * @param matrix the (sparse) data matrix we're operating on
     * @param clusteringThresh the maximum value that we will take as a "zero" value
     * @param prune if 'false', don't prune this pass
     */
    private void normalize(DoubleMatrix2D matrix, double clusteringThresh, boolean prune) {
        // Remove any really low values and create the sums array
        double[] sums = new double[matrix.columns()];
        matrix.forEachNonZero(new MatrixZeroAndSum(prune, clusteringThresh, sums));

        // Finally, adjust the values
        matrix.forEachNonZero(new MatrixNormalize(sums));

        // Last step -- find any columns that summed to zero and set the diagonal to 1
        for (int col = 0; col < sums.length; col++) {
            if (sums[col] == 0.0) {
                // debugln("Column "+col+" sums to 0");
                matrix.set(col, col, 1.0);
            }
        }
    }

    /**
     * This method normalizes the weights to between 0 and 1.
     *
     * @param matrix the (sparse) data matrix we're operating on
     * @param min the minimum weight
     * @param max the maximum weight
     */
    private void normalizeWeights(DoubleMatrix2D matrix, double min, double max) {
        matrix.forEachNonZero(new MatrixNormalizeWeights(min, max));
    }

    /**
     * This method calculates the residuals.  Calculate the sum and
     * sum of squares for each row, then return the maximum residual.
     *
     * @param matrix the (sparse) data matrix we're operating on
     * @return residual value
     */
    private double calculateResiduals(DoubleMatrix2D matrix) {
        // Calculate and return the residuals
        double[] sums = new double[matrix.columns()];
        double[] sumSquares = new double[matrix.columns()];
        matrix.forEachNonZero(new MatrixSumAndSumSq(sums, sumSquares));
        double residual = 0.0;
        for (int i = 0; i < sums.length; i++) {
            residual = Math.max(residual, sums[i] - sumSquares[i]);
        }
        return residual;
    }

    /**
     * Debugging routine to print out information about a matrix
     *
     * @param matrix the matrix we're going to print out information about
     */
    private void printMatrixInfo(DoubleMatrix2D matrix) {
        debugln("Matrix(" + matrix.rows() + ", " + matrix.columns() + ")");
        if (matrix instanceof SparseDoubleMatrix2D) {
            debugln(" matrix is sparse");
        } else {
            debugln(" matrix is dense");
        }
        debugln(" cardinality is " + matrix.cardinality());
    }

    /**
     * Debugging routine to print out information about a matrix
     *
     * @param matrix the matrix we're going to print out information about
     */
    private void printMatrix(DoubleMatrix2D matrix) {
        for (int row = 0; row < matrix.rows(); row++) {
            debug(nodes.get(row).getRow() + ":\t");
            for (int col = 0; col < matrix.columns(); col++) {
                debug("" + matrix.get(row, col) + "\t");
            }
            debugln();
        }
        debugln("Matrix(" + matrix.rows() + ", " + matrix.columns() + ")");
        if (matrix instanceof SparseDoubleMatrix2D) {
            debugln(" matrix is sparse");
        } else {
            debugln(" matrix is dense");
        }
        debugln(" cardinality is " + matrix.cardinality());
    }

    private void debugln(String message) {
        if (debug) {
            System.out.println(message);
        }
    }

    private void debugln() {
        if (debug) {
            System.out.println();
        }
    }

    private void debug(String message) {
        if (debug) {
            System.out.print(message);
        }
    }

    private DoubleMatrix2D multiplyMatrix(DoubleMatrix2D A, DoubleMatrix2D B) {
        int m = A.rows();
        int n = A.columns();
        int p = B.columns();

        // Create views into B
        final DoubleMatrix1D[] Brows = new DoubleMatrix1D[n];
        for (int i = n; --i >= 0;) {
            Brows[i] = B.viewRow(i);
        }

        // Create a series of 1D vectors
        final DoubleMatrix1D[] Crows = new DoubleMatrix1D[n];
        for (int i = m; --i >= 0;) {
            Crows[i] = B.like1D(m);
        }

        // Create the thread pools
        final ExecutorService[] threadPools = new ExecutorService[nThreads];
        for (int pool = 0; pool < threadPools.length; pool++) {
            threadPools[pool] = Executors.newFixedThreadPool(1);
        }

        A.forEachNonZero(
                new cern.colt.function.IntIntDoubleFunction() {

                    @Override
                    public double apply(int row, int column, double value) {

                        Runnable r = new ThreadedDotProduct(value, Brows[column], Crows[row]);
                        threadPools[row % nThreads].submit(r);
                        return value;
                    }
                });

        for (int pool = 0; pool < threadPools.length; pool++) {
            threadPools[pool].shutdown();
            try {
                boolean result = threadPools[pool].awaitTermination(7, TimeUnit.DAYS);
            } catch (Exception e) {
            }
        }
        // Recreate C
        return create2DMatrix(Crows);
    }

    private DoubleMatrix2D create2DMatrix(DoubleMatrix1D[] rows) {
        int columns = rows[0].size();
        DoubleMatrix2D C = DoubleFactory2D.sparse.make(rows.length, columns);
        for (int row = 0; row < rows.length; row++) {
            for (int col = 0; col < columns; col++) {
                double value = rows[row].getQuick(col);
                if (value != 0.0) {
                    C.setQuick(row, col, value);
                }
            }
        }
        return C;
    }

    private class ThreadedDotProduct implements Runnable {

        double value;
        DoubleMatrix1D Bcol;
        DoubleMatrix1D Crow;
        // final cern.jet.math.PlusMult fun = cern.jet.math.PlusMult.plusMult(0);

        ThreadedDotProduct(double value, DoubleMatrix1D Bcol,
                DoubleMatrix1D Crow) {
            this.value = value;
            this.Bcol = Bcol;
            this.Crow = Crow;
        }

        @Override
        public void run() {
            // fun.multiplicator = value;
            for (int k = 0; k < Bcol.size(); k++) {
                if (Bcol.getQuick(k) != 0.0) {
                    Crow.setQuick(k, Crow.getQuick(k) + Bcol.getQuick(k) * value);
                }
            }
            // Crow.assign(Bcol, fun);
        }
    }

    /**
     * The MatrixPow class raises the value of each non-zero cell of the matrix
     * to the power passed in it's constructor.
     */
    private class MatrixPow implements IntIntDoubleFunction {

        double pow;

        public MatrixPow(double power) {
            this.pow = power;
        }

        @Override
        public double apply(int row, int column, double value) {
            if (cancelled) {
                return 0.0;
            }
            return Math.pow(value, pow);
        }
    }

    /**
     * The MatrixZeroAndSum looks through all non-zero cells in a matrix
     * and if the value of the cell is beneath "threshold" it is set to
     * zero.  All non-zero cells in a column are added together to return
     * the sum of each column.
     */
    private class MatrixZeroAndSum implements IntIntDoubleFunction {

        double threshold;
        double[] colSums;
        boolean prune;

        public MatrixZeroAndSum(boolean prune, double threshold, double[] colSums) {
            this.threshold = threshold;
            this.colSums = colSums;
            this.prune = prune;
        }

        @Override
        public double apply(int row, int column, double value) {
            if (prune && (value < threshold)) {
                return 0.0;
            }
            colSums[column] += value;
            return value;
        }
    }

    /**
     * The MatrixSumAndSumSq looks through all non-zero cells in a matrix
     * and calculates the sums and sum of squares for each column.
     */
    private class MatrixSumAndSumSq implements IntIntDoubleFunction {

        double[] sumSquares;
        double[] colSums;

        public MatrixSumAndSumSq(double[] colSums, double[] sumSquares) {
            this.sumSquares = sumSquares;
            this.colSums = colSums;
        }

        @Override
        public double apply(int row, int column, double value) {
            colSums[column] += value;
            sumSquares[column] += value * value;
            return value;
        }
    }

    /**
     * The MatrixNormalize class takes as input an array of sums for
     * each column in the matrix and uses that to normalize the sum of the
     * column to 1.  If the sum of the column is 0, the diagonal is set to 1.
     */
    private class MatrixNormalize implements IntIntDoubleFunction {

        double[] colSums;

        public MatrixNormalize(double[] colSums) {
            this.colSums = colSums;
        }

        @Override
        public double apply(int row, int column, double value) {
            if (cancelled) {
                return 0.0;
            }
            return value / colSums[column];
        }
    }

    private class MatrixNormalizeWeights implements IntIntDoubleFunction {

        double min;
        double max;

        public MatrixNormalizeWeights(double min, double max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public double apply(int row, int column, double value) {
            return (value - min) / (max - min);
        }
    }

    private class ClusterMatrix implements IntIntDoubleFunction {

        Map<Integer, MCLCluster> clusterMap;

        public ClusterMatrix(Map<Integer, MCLCluster> clusterMap) {
            this.clusterMap = clusterMap;
        }

        @Override
        public double apply(int row, int column, double value) {
            if (cancelled) {
                return 0.0;
            }

            if (row == column) {
                return value;
            }

            if (clusterMap.containsKey(column)) {
                // Already seen "column" -- get the cluster and add column
                MCLCluster columnCluster = clusterMap.get(column);
                if (clusterMap.containsKey(row)) {
                    // We've already seen row also -- join them
                    MCLCluster rowCluster = clusterMap.get(row);
                    if (rowCluster == columnCluster) {
                        return value;
                    }
                    clusterCount--;
                    logger.log(Level.FINE, "Joining cluster {0} and {1}", new Object[]{columnCluster.getClusterNumber(), rowCluster.getClusterNumber()});
                    logger.log(Level.FINE, "clusterCount = {0}", clusterCount);
                    columnCluster.addAll(rowCluster);
                } else {
                    logger.log(Level.FINE, "Adding {0} to {1}", new Object[]{row, columnCluster.getClusterNumber()});
                    logger.log(Level.FINE, "clusterCount = {0}", clusterCount);
                    columnCluster.add(nodes, row);
                }
                updateClusters(columnCluster);
            } else {
                MCLCluster rowCluster;
                // First time we've seen "column" -- have we already seen "row"
                if (clusterMap.containsKey(row)) {
                    // Yes, just add column to row's cluster
                    rowCluster = clusterMap.get(row);
                    logger.log(Level.FINE, "Adding {0} to {1}", new Object[]{column, rowCluster.getClusterNumber()});
                    logger.log(Level.FINE, "clusterCount = {0}", clusterCount);
                    rowCluster.add(nodes, column);
                } else {
                    clusterCount++;
                    rowCluster = new MCLCluster(g);
                    logger.log(Level.FINE, "Created new cluster {0} with {1} and {2}", new Object[]{rowCluster.getClusterNumber(), row, column});
                    logger.log(Level.FINE, "clusterCount = {0}", clusterCount);
                    rowCluster.add(nodes, column);
                    rowCluster.add(nodes, row);
                }
                updateClusters(rowCluster);
            }
            return value;
        }

        private void updateClusters(MCLCluster c) {
            for (Node node : c) {
                clusterMap.put(nodes.indexOf(node), c);
            }
        }
    }
}
