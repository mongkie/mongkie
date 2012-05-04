package org.mongkie.clustering.plugins.clustermaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cern.colt.function.IntIntDoubleFunction;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mongkie.clustering.plugins.clustermaker.converters.EdgeWeightConverter;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

public class DistanceMatrix {

    private double minWeight = Double.MAX_VALUE;
    private double maxWeight = Double.MIN_VALUE;
    private double minAttribute = Double.MAX_VALUE;
    private double maxAttribute = Double.MIN_VALUE;
    private double edgeCutOff = 0.0;
    private boolean unDirectedEdges = true;
    private boolean distanceValues = false;
    private boolean logValues = false;
    private List<Node> nodes = null;
    private List<Edge> edges = null;
    private DoubleMatrix2D matrix = null;
    private double[] edgeWeights = null;

    private DistanceMatrix(Graph g, Collection<Node> selectedNodes, EdgeWeightConverter converter) {
        if (selectedNodes == null || selectedNodes.isEmpty()) {
            nodes = new ArrayList<Node>(g.getNodeCount());
            for (Iterator<Node> nodeIter = g.nodes(); nodeIter.hasNext();) {
                nodes.add(nodeIter.next());
            }
            edges = new ArrayList<Edge>(g.getEdgeCount());
            for (Iterator<Edge> edgeIter = g.edges(); edgeIter.hasNext();) {
                edges.add(edgeIter.next());
            }
        } else {
            nodes.addAll(selectedNodes);
            edges = g.getEdges(nodes);
        }

        edgeWeights = new double[edges.size()];
        // We do a fair amount of massaging the data, so let's just do it once
        for (int edgeIndex = 0; edgeIndex < edges.size(); edgeIndex++) {
            edgeWeights[edgeIndex] = Double.MIN_VALUE;
            double edgeWeight = edges.get(edgeIndex).getWeight();
            minAttribute = Math.min(minAttribute, edgeWeight);
            maxAttribute = Math.max(maxAttribute, edgeWeight);
            edgeWeights[edgeIndex] = edgeWeight;
        }

        // We now have two lists, one with the edges, one with the weights,
        // now massage the edgeWeights data as requested
        // Note that we need to go through this again to handle some of the edge cases
        List<Integer> edgeCase = new ArrayList<Integer>();
        for (int edgeIndex = 0; edgeIndex < edges.size(); edgeIndex++) {
            double edgeWeight = edgeWeights[edgeIndex];
            if (edgeWeight == Double.MIN_VALUE) {
                continue;
            }
            edgeWeight = converter.convert(edgeWeight, minAttribute, maxAttribute);
            if (edgeWeight == Double.MIN_VALUE) {
                edgeCase.add(edgeIndex);
            }
            edgeWeights[edgeIndex] = edgeWeight;
            if (edgeWeight != Double.MIN_VALUE) {
                minWeight = Math.min(minWeight, edgeWeight);
                maxWeight = Math.max(maxWeight, edgeWeight);
            }
        }

        // OK, now we have our two arrays with the exception of the edge cases -- we can fix those, now
        for (Integer index : edgeCase) {
            edgeWeights[index] = maxWeight + maxWeight / 10.0;
        }
    }

    public static DistanceMatrix create(Graph g, Collection<Node> selectedNodes, EdgeWeightConverter converter,
            boolean directed, Double edgeCutOff, boolean adjustLoops) {
        DistanceMatrix matrix = new DistanceMatrix(g, selectedNodes, converter);
        matrix.setUndirectedEdges(!directed);
        if (edgeCutOff != null) {
            matrix.setEdgeCutOff(edgeCutOff.doubleValue());
        }
        if (adjustLoops) {
            matrix.adjustLoops();
        }
        return matrix;
    }

    public double[] getEdgeValues() {
        return edgeWeights;
    }

    public double getEdgeValueFromMatrix(int row, int column) {
        if (matrix == null) {
            getDistanceMatrix();
        }
        return matrix.get(row, column);
    }

    public double scaleValue(double value) {
        if (distanceValues) {
            if (value != 0.0) {
                value = 1 / value;
            } else {
                value = Double.MAX_VALUE;
            }
        }
        if (logValues) {
            if (minAttribute < 0.0) {
                value += Math.abs(minAttribute);
            }
            if (value != 0.0 && value != Double.MAX_VALUE) {
                value = -Math.log10(value);
            } else {
                value = 500; // Assume 1e-500 as a reasonble upper bound
            }
        }
        return value;
    }

    public boolean hasDistanceValues() {
        return this.distanceValues;
    }

    public boolean hasLogValues() {
        return this.logValues;
    }

    public double getNormalizedValue(double value) {
        double span = maxWeight - minWeight;
        return ((value - minWeight) / span);
    }

    public void normalizeMatrix(double factor) {
        if (matrix == null) {
            getDistanceMatrix();
        }
        matrix.forEachNonZero(new Normalize(minWeight, maxWeight, factor));
    }

    public Map<Integer, List<Node>> findConnectedComponents() {
        if (matrix == null) {
            getDistanceMatrix();
        }
        Map<Integer, List<Node>> cmap = new HashMap<Integer, List<Node>>();
        matrix.forEachNonZero(new FindComponents(cmap));
        return cmap;
    }

    public DoubleMatrix2D getDistanceMatrix(Double edgeCutOff, boolean undirectedEdges) {
        setEdgeCutOff(edgeCutOff);
        setUndirectedEdges(undirectedEdges);
        matrix = null;
        return getDistanceMatrix();
    }

    public DoubleMatrix2D getDistanceMatrix() {
        if (matrix != null) {
            return matrix;
        }
        matrix = DoubleFactory2D.sparse.make(nodes.size(), nodes.size());
        int sourceIndex;
        int targetIndex;
        for (int edgeIndex = 0; edgeIndex < edges.size(); edgeIndex++) {
            Edge edge = edges.get(edgeIndex);
            // Is this weight above the cutoff?
            if (edgeWeights[edgeIndex] < edgeCutOff) {
                // System.out.println("Skipping edge "+edge.getIdentifier()+" weight "+edgeWeights[edgeIndex]+" < "+edgeCutOff);
                continue; // Nope, don't add it
            }
            /*Add edge to matrix*/
            sourceIndex = nodes.indexOf(edge.getSourceNode());
            targetIndex = nodes.indexOf(edge.getTargetNode());
            matrix.set(targetIndex, sourceIndex, edgeWeights[edgeIndex]);
            if (unDirectedEdges) {
                matrix.set(sourceIndex, targetIndex, edgeWeights[edgeIndex]);
            }
        }
        return matrix;
    }

    public double getMaxAttribute() {
        return maxAttribute;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public double getMinAttribute() {
        return minAttribute;
    }

    public double getMinWeight() {
        return minWeight;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdgeCutOff(Double edgeCutOff) {
        matrix = null;
        this.edgeCutOff = edgeCutOff.doubleValue();
    }

    public void setUndirectedEdges(boolean undirectedEdges) {
        matrix = null;
        this.unDirectedEdges = undirectedEdges;
    }

    /**
     * This method handles the loop adjustment, if desired by the user.
     * The basic approach is to go through the diagonal and set the value of the
     * diagonal to the maximum value of the column.  In the von Dongen code, this
     * is handled in separate steps (zero the diagonal, (maybe) preinflate, set diagonal
     * to max).
     *
     * @param matrix the (sparse) data matrix we're going to adjust
     */
    public void adjustLoops() {
        if (matrix == null) {
            getDistanceMatrix();
        }
        double[] max = new double[matrix.columns()];
        // Calculate the max value for each column
        matrix.forEachNonZero(new MatrixFindMax(max));
        // Set it in the diagonal
        for (int col = 0; col < matrix.columns(); col++) {
            if (max[col] != 0.0) {
                matrix.set(col, col, max[col]);
            } else {
                matrix.set(col, col, 1.0);
            }
        }
    }

    /**
     * Debugging routine to print out information about a matrix
     *
     * @param matrix the matrix we're going to print out information about
     */
    public void printMatrixInfo(Logger logger, DoubleMatrix2D m) {
        logger.log(Level.INFO, "Matrix({0}, {1})", new Object[]{m.rows(), m.columns()});
        if (m instanceof SparseDoubleMatrix2D) {
            logger.log(Level.INFO, " matrix is sparse");
        } else {
            logger.log(Level.INFO, " matrix is dense");
        }
        logger.log(Level.INFO, " cardinality is {0}", m.cardinality());
    }

    /**
     * Debugging routine to print out information about a matrix
     *
     * @param matrix the matrix we're going to print out information about
     */
    public void printMatrix(Logger logger, DoubleMatrix2D m) {
        String s = "";
        for (int row = 0; row < m.rows(); row++) {
            s += nodes.get(row).getRow() + ":\t";
            for (int col = 0; col < m.columns(); col++) {
                s += "" + m.get(row, col) + "\t";
            }
            logger.log(Level.INFO, s);
        }
    }

    /**
     * MatrixFindMax simply records the maximum value in a column
     */
    private class MatrixFindMax implements IntIntDoubleFunction {

        double[] colMax;

        public MatrixFindMax(double[] colMax) {
            this.colMax = colMax;
        }

        @Override
        public double apply(int row, int column, double value) {
            if (value > colMax[column]) {
                colMax[column] = value;
            }
            return value;
        }
    }

    /**
     * Normalize normalizes a cell in the matrix
     */
    private class Normalize implements IntIntDoubleFunction {

        private double maxValue;
        private double minValue;
        private double span;
        private double factor;

        public Normalize(double minValue, double maxValue, double factor) {
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.factor = factor;
            span = maxValue - minValue;
        }

        @Override
        public double apply(int row, int column, double value) {
            return ((value - minWeight) / span) * factor;
        }
    }

    /**
     * Find the connected components in a matrix
     */
    private class FindComponents implements IntIntDoubleFunction {

        Map<Node, Integer> nodeToCluster;
        Map<Integer, List<Node>> clusterMap;
        int clusterNumber = 0;

        public FindComponents(Map<Integer, List<Node>> cMap) {
            clusterMap = cMap;
            nodeToCluster = new HashMap<Node, Integer>();
        }

        @Override
        public double apply(int row, int column, double value) {
            // For the purposes of determining connected components, we can
            // safely ignore self-edges
            if (row == column) {
                return value;
            }
            Node node1 = nodes.get(row);
            Node node2 = nodes.get(column);
            if (nodeToCluster.containsKey(node1)) {
                if (!nodeToCluster.containsKey(node2)) {
                    addNodeToCluster(nodeToCluster.get(node1), node2);
                } else {
                    combineClusters(nodeToCluster.get(node1), nodeToCluster.get(node2));
                }
            } else {
                if (nodeToCluster.containsKey(node2)) {
                    addNodeToCluster(nodeToCluster.get(node2), node1);
                } else {
                    createCluster(node1, node2);
                }
            }
            return value;
        }

        private void addNodeToCluster(Integer cluster, Node node) {
            // System.out.println("Adding "+node+" to cluster "+cluster);
            List<Node> nodeList = clusterMap.get(cluster);
            nodeList.add(node);
            nodeToCluster.put(node, cluster);
        }

        private void createCluster(Node node1, Node node2) {
            // System.out.println("Creating cluster "+clusterNumber+" with "+node1+" and "+node2);
            List<Node> nodeList = new ArrayList<Node>();
            clusterMap.put(clusterNumber, nodeList);
            addNodeToCluster(clusterNumber, node1);
            addNodeToCluster(clusterNumber, node2);
            clusterNumber++;
        }

        private void combineClusters(Integer cluster1, Integer cluster2) {
            if (cluster1.intValue() == cluster2.intValue()) {
                return;
            }
            // System.out.println("Combining cluster "+cluster1+" and "+cluster2);
            List<Node> list1 = clusterMap.get(cluster1);
            List<Node> list2 = clusterMap.get(cluster2);
            clusterMap.remove(cluster2);
            for (Node node : list2) {
                nodeToCluster.put(node, cluster1);
            }
            list1.addAll(list2);
        }
    }
}
