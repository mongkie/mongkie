/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed nodeIter the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 ** User: Gary Bader
 ** Date: Jan 20, 2004
 ** Time: 6:18:03 PM
 ** Description: An implementation of the MCODE algorithm
 **/
package org.mongkie.clustering.plugins.mcode;

import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * An implementation of the MCODE algorithm
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MCODEAlgorithm {

    private volatile boolean cancelled = false;

    /**
     * Data structure for storing information required for each nodeIter
     */
    private static class NodeInfo {

        double density;         //neighborhood density
        int numNodeNeighbors;   //number of nodeIter nieghbors
        int[] nodeNeighbors;    //stores nodeIter indices of all neighborsIter
        int coreLevel;          //e.g. 2 = a 2-core
        double coreDensity;     //density of the core neighborhood
        double score;           //nodeIter score

        public NodeInfo() {
            this.density = 0.0;
            this.numNodeNeighbors = 0;
            this.coreLevel = 0;
            this.coreDensity = 0.0;
        }

        @Override
        public String toString() {
            return "density:" + density + ", numNodeNeighbors:" + numNodeNeighbors + ", coreLevel:" + coreLevel + ", coreDensity:" + coreDensity + ", score:" + score;
        }
    }
    // data structures useful to have around for more than one cluster finding iteration
    private Map<Integer, NodeInfo> currentNodeInfoHashMap = null; // key is the nodeIter index, clusterNodes is a NodeInfo instance
    private TreeMap<Double, List<Integer>> currentNodeScoreSortedMap = null; // key is nodeIter score, clusterNodes is clusterNodes
    // because every network can be scored and clustered several times with different parameters
    // these results have to be stored so that the same scores are used during exploration when
    // the user is switching between the various results
    // Since the network is not always rescored whenever a new result is generated (if the scoring parameters
    // haven't changed for example) the clustering method must save the current nodeIter scores under the new result
    // title for later reference
    private Map<String, TreeMap<Double, List<Integer>>> nodeScoreResultsMap = new HashMap<String, TreeMap<Double, List<Integer>>>(); //key is result, clusterNodes is nodeScoreSortedMap
    private Map<String, Map<Integer, NodeInfo>> nodeInfoResultsMap = new HashMap<String, Map<Integer, NodeInfo>>(); //key is result, clusterNodes is nodeInfroHashMap
    private MCODEParameterSet params; //the parameters used for this instance of the algorithm
    // stats
    private long lastScoreTime;
    private long lastFindTime;

    /**
     * The constructor.  Use this to get an instance of MCODE to run.
     *
     * @param networkID Allows the algorithm to get the parameters of the focused network
     */
    public MCODEAlgorithm() {
        params = new MCODEParameterSet();
    }

    /**
     * Get the time taken by the last score operation nodeIter this instance of the algorithm
     *
     * @return the duration of the scoring portion
     */
    public long getLastScoreTime() {
        return lastScoreTime;
    }

    /**
     * Get the time taken by the last find operation nodeIter this instance of the algorithm
     *
     * @return the duration of the finding process
     */
    public long getLastFindTime() {
        return lastFindTime;
    }

    /**
     * Get the parameter set used for this instance of MCODEAlgorithm
     *
     * @return The parameter set used
     */
    public MCODEParameterSet getParams() {
        return params;
    }

    /**
     * If set, will schedule the algorithm to be cancelled at the next convenient opportunity
     *
     * @param cancelled Set to true if the algorithm should be cancelled
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Gets the calculated nodeIter score of a nodeIter from a given result.  Used nodeIter MCODEResultsPanel
     * during the attribute setting method.
     *
     * @param rootGraphIndex Integer which is used to identify the nodesIterator nodeIter the score-sorted tree map
     * @param resultTitle Title of the results for which we are retrieving a nodeIter score
     * @return nodeIter score as a Double
     */
    public Double getNodeScore(int rootGraphIndex, String resultTitle) {
        Double nodeScore = new Double(0.0);
        Map<Double, List<Integer>> nodeScoreSortedMap = nodeScoreResultsMap.get(resultTitle);

        for (Iterator score = nodeScoreSortedMap.keySet().iterator(); score.hasNext();) {
            nodeScore = (Double) score.next();
            List<Integer> nodeIndices = nodeScoreSortedMap.get(nodeScore);
            if (nodeIndices.contains(rootGraphIndex)) {
                return nodeScore;
            }
        }
        return nodeScore;
    }

    /**
     * Gets the highest nodeIter score nodeIter a given result.  Used nodeIter the MCODEVisualStyleAction class to
     * re-initialize the visual calculators.
     *
     * @param resultTitle Title of the result
     * @return First key nodeIter the nodeScoreSortedMap corresponding to the highest score
     */
    public double getMaxScore(String resultTitle) {
        TreeMap<Double, List<Integer>> nodeScoreSortedMap = nodeScoreResultsMap.get(resultTitle);
        // Since the map is sorted, the first key is the highest clusterNodes
        return nodeScoreSortedMap.firstKey();
    }

    /**
     * Step 1: Score the graph and save scores as nodeIter attributes.  Scores are also
     * saved internally nodeIter your instance of MCODEAlgorithm.
     *
     * @param graph The network that will be scored
     * @param resultTitle Title of the result, used as an identifier nodeIter various hash maps
     */
    public void scoreGraph(Graph graph, String resultTitle) {
        String callerID = "MCODEAlgorithm.MCODEAlgorithm";
        if (graph == null) {
            System.err.println("In " + callerID + ": inputNetwork was null.");
            return;
        }

        Logger.getLogger(MCODEAlgorithm.class.getName()).info("Scoring the graph...");

        // initialize
        long msTimeBefore = System.currentTimeMillis();
        Map<Integer, NodeInfo> nodeInfoHashMap = new HashMap<Integer, NodeInfo>(graph.getNodeCount());
        TreeMap<Double, List<Integer>> nodeScoreSortedMap = new TreeMap<Double, List<Integer>>(
                new Comparator<Double>() {
                    //will store Doubles (score) as the key, Lists as nodeIndiceses
                    //sort Doubles nodeIter descending order

                    @Override
                    public int compare(Double k1, Double k2) {
                        double s1 = k1.doubleValue();
                        double s2 = k2.doubleValue();
                        if (s1 == s2) {
                            return 0;
                        } else if (s1 < s2) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });
        // iterate over all nodesIterator and calculate MCODE score
        NodeInfo nodeInfo = null;
        double nodeScore;
        List<Integer> nodeIndices;
//        int i = 0;
        Iterator<Node> nodes = graph.nodes();
        while (nodes.hasNext() && (!cancelled)) {
            Node n = nodes.next();
            int nodeIdx = n.getRow();
            nodeInfo = calcNodeInfo(graph, nodeIdx);
            nodeInfoHashMap.put(nodeIdx, nodeInfo);
            // score nodeIter TODO: add support for other scoring functions (low priority)
            nodeScore = scoreNode(nodeInfo);
            // save score for later use nodeIter TreeMap
            // add a list of nodesIterator to each score nodeIter case nodesIterator have the same score
            if (nodeScoreSortedMap.containsKey(nodeScore)) {
                // already have a nodeIter with this score, add it to the list
                nodeIndices = nodeScoreSortedMap.get(nodeScore);
                nodeIndices.add(nodeIdx);
            } else {
                nodeIndices = new ArrayList<Integer>();
                nodeIndices.add(nodeIdx);
                nodeScoreSortedMap.put(nodeScore, nodeIndices);
            }
//            if (taskMonitor != null) {
//                i++;
//                taskMonitor.setPercentCompleted((i * 100) / graph.getNodeCount());
//            }
        }
        nodeScoreResultsMap.put(resultTitle, nodeScoreSortedMap);
        nodeInfoResultsMap.put(resultTitle, nodeInfoHashMap);

        currentNodeScoreSortedMap = nodeScoreSortedMap;
        currentNodeInfoHashMap = nodeInfoHashMap;

        long msTimeAfter = System.currentTimeMillis();
        lastScoreTime = msTimeAfter - msTimeBefore;
    }

    /**
     * Step 2: Find all clusters given a scored graph.  If the input network has not been scored,
     * this method will return null.  This method is called when the user selects network scope or
     * single nodeIter scope.
     *
     * @param graph The scored network to find clusters nodeIter.
     * @param resultTitle Title of the result
     * @return An array containing an MCODECluster object for each cluster.
     */
    public MCODECluster[] findClusters(Graph graph, String resultTitle) {

        TreeMap<Double, List<Integer>> nodeScoreSortedMap;
        Map<Integer, NodeInfo> nodeInfoHashMap;
        // First we check if the network has been scored under this result title (i.e. scoring
        // was required due to a scoring parameter change).  If it hasn't then we want to use the
        // current scores that were generated the last time the network was scored and store them
        // under the title of this result set for later use
        if (!nodeScoreResultsMap.containsKey(resultTitle)) {
            nodeScoreSortedMap = currentNodeScoreSortedMap;
            nodeInfoHashMap = currentNodeInfoHashMap;
            nodeScoreResultsMap.put(resultTitle, nodeScoreSortedMap);
            nodeInfoResultsMap.put(resultTitle, nodeInfoHashMap);
        } else {
            nodeScoreSortedMap = nodeScoreResultsMap.get(resultTitle);
            nodeInfoHashMap = nodeInfoResultsMap.get(resultTitle);
        }
        MCODECluster currentCluster;
        String callerID = "MCODEAlgorithm.findClusters";
        if (graph == null) {
            System.err.println("In " + callerID + ": inputNetwork was null.");
            return (null);
        }
        if ((nodeInfoHashMap == null) || (nodeScoreSortedMap == null)) {
            System.err.println("In " + callerID + ": nodeInfoHashMap or nodeScoreSortedMap was null.");
            return (null);
        }

        Logger.getLogger(MCODEAlgorithm.class.getName()).info("Finding all clusters given the scored graph...");

        // initialization
        long msTimeBefore = System.currentTimeMillis();
        HashMap<Integer, Boolean> nodeSeenHashMap = new HashMap<Integer, Boolean>(); // key is nodeIndex, clusterNodes is true/false
        int currentNode;
//        int findingProgress = 0;
//        int findingTotal = 0;
        Collection<List<Integer>> nodeIndiceses = nodeScoreSortedMap.values(); // returns a Collection sorted by key order (descending)
        // In order to track the progress without significant lags (for times when many nodesIterator have the same score
        // and no progress is reported) we count all the scored nodesIterator and track those instead
//        for (Iterator<List<Integer>> nodeIndicesIter = nodeIndiceses.nodeIndicesIter(); nodeIndicesIter.hasNext();) {
//            List<Integer> clusterNodes = nodeIndicesIter.next();
//            for (Iterator<Integer> nodeIndexIter = clusterNodes.nodeIndicesIter(); nodeIndexIter.hasNext();) {
//                nodeIndexIter.next();
//                findingTotal++;
//            }
////            findingTotal += clusterNodes.size();
//        }
        // stores the list of clusters as ArrayLists of nodeIter indices nodeIter the input Network
        List<MCODECluster> allClusters = new ArrayList<MCODECluster>();
        // iterate over nodeIter indices sorted descending by their score
        List<Integer> alNodesWithSameScore;
        for (Iterator<List<Integer>> nodeIndicesIter = nodeIndiceses.iterator(); nodeIndicesIter.hasNext();) {
            // each score may be associated with multiple nodesIterator, iterate over these lists
            alNodesWithSameScore = nodeIndicesIter.next();
            for (int j = 0; j < alNodesWithSameScore.size(); j++) {
                currentNode = alNodesWithSameScore.get(j);
                if (!nodeSeenHashMap.containsKey(currentNode)) {
                    currentCluster = new MCODECluster(graph);
                    currentCluster.setSeedNode(currentNode);// store the current nodeIter as the seed nodeIter
                    // we store the current nodeIter seen hash map for later exploration purposes
                    Map<Integer, Boolean> nodeSeenHashMapSnapShot = new HashMap<Integer, Boolean>((HashMap<Integer, Boolean>) nodeSeenHashMap.clone());

                    List<Integer> coreIndices = getClusterCore(currentNode, nodeSeenHashMap, params.getNodeScoreCutoff(), params.getMaxDepthFromStart(), nodeInfoHashMap); // here we use the original nodeIter score cutoff
                    if (coreIndices.size() > 0) {
                        // make sure seed nodeIter is part of cluster, if not already nodeIter there
                        if (!coreIndices.contains(currentNode)) {
                            coreIndices.add(currentNode);
                        }
                        // createGraph an input graph for the filter and haircut methods
                        GraphPerspective corePerspective = GraphPerspective.create(coreIndices, graph);
                        if (!filterCluster(corePerspective)) {
                            if (params.isHaircut()) {
                                haircutCluster(corePerspective, coreIndices, graph);
                            }
                            if (params.isFluff()) {
                                fluffClusterBoundary(coreIndices, nodeSeenHashMap, nodeInfoHashMap);
                            }
                            currentCluster.setNodeIndices(coreIndices);
                            corePerspective = GraphPerspective.create(coreIndices, graph);
                            currentCluster.setGraphPerspective(corePerspective);
                            currentCluster.setClusterScore(scoreCluster(currentCluster));
                            currentCluster.setNodeSeenHashMap(nodeSeenHashMapSnapShot);// store the list of all the nodesIterator that have already been seen and incorporated nodeIter other clusters
                            currentCluster.setResultTitle(resultTitle);
                            // store detected cluster for later
                            allClusters.add(currentCluster);
                        }
                    }
                }
//                if (taskMonitor != null) {
//                    findingProgress++;
//                    //We want to be sure that only progress changes are reported and not
//                    //miniscule decimal increments so that the taskMonitor isn't overwhelmed
//                    int newProgress = (findingProgress * 100) / findingTotal;
//                    int oldProgress = ((findingProgress - 1) * 100) / findingTotal;
//                    if (newProgress != oldProgress) {
//                        taskMonitor.setPercentCompleted(newProgress);
//                    }
//                }
                if (cancelled) {
                    break;
                }
            }
        }
        // Once the clusters have been found we either return them or nodeIter the case of selection scope, we select only
        // the ones that contain the selected nodeIter(s) and return those
        if (!params.getScope().equals(MCODEParameterSet.NETWORK)) {
            List<MCODECluster> onSelectionClusters = new ArrayList<MCODECluster>();
            for (Iterator<MCODECluster> clusterIter = allClusters.iterator(); clusterIter.hasNext();) {
                MCODECluster cluster = clusterIter.next();
                List<Integer> clusterNodes = cluster.getNodeIndices();
                List<Integer> selectedNodes = new ArrayList<Integer>();
                selectedNodes.addAll(Arrays.asList(params.getSelectedNodes()));
                // method for returning only clusters that contain all of the selected noes
                /*
                if (coreIndices.containsAll(selectedNodes)) {
                selectedAlClusters.add(cluster);
                }
                 */
                // method for returning all clusters that contain any of the selected nodesIterator
                boolean hit = false;
                for (Iterator<Integer> nodeIter = selectedNodes.iterator(); nodeIter.hasNext();) {
                    if (clusterNodes.contains(nodeIter.next())) {
                        hit = true;
                    }
                }
                if (hit) {
                    onSelectionClusters.add(cluster);
                }
            }
            allClusters = onSelectionClusters;
        }

        // Finally convert the arraylist into a fixed array
//        MCODECluster[] clusters = new MCODECluster[allClusters.size()];
//        for (int c = 0; c < clusters.length; c++) {
//            clusters[c] = (MCODECluster) allClusters.get(c);
//        }
        MCODECluster[] clusters = allClusters.toArray(new MCODECluster[allClusters.size()]);

        Arrays.sort(clusters, new Comparator<MCODECluster>() {
            // sorting clusters by decreasing score

            @Override
            public int compare(MCODECluster c1, MCODECluster c2) {
                double s1 = c1.getClusterScore();
                double s2 = c2.getClusterScore();
                if (s1 == s2) {
                    return 0;
                } else if (s1 < s2) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].setRank(i);
        }

        long msTimeAfter = System.currentTimeMillis();
        lastFindTime = msTimeAfter - msTimeBefore;

        Logger.getLogger(MCODEAlgorithm.class.getName()).log(Level.INFO, "{0} clusters found", clusters.length);

        return clusters;
    }

//    /**
//     * Finds the cluster based on user's input via size slider.
//     *
//     * @param cluster cluster being explored
//     * @param nodeScoreCutoff slider source clusterNodes
//     * @param inputNetwork network
//     * @param resultTitle title of the result set being explored
//     * @return explored cluster
//     */
//    public MCODECluster exploreCluster(MCODECluster cluster, double nodeScoreCutoff, CyNetwork inputNetwork, String resultTitle) {
//        //This method is similar to the finding method with the exception of the filtering so that the decrease of the cluster size
//        //can produce a single nodeIter, also the use of the nodeIter seen hash map is differentially applied...
//        HashMap nodeInfoHashMap = (HashMap) nodeInfoResultsMap.get(resultTitle);
//        MCODEParameterSet params = MCODECurrentParameters.getResultParams(cluster.getResultTitle());
//        HashMap nodeSeenHashMap;
//        //if the size slider is below the set nodeIter score cutoff we use the nodeIter seen hash map so that clusters
//        //with higher scoring seeds have priority, however when the slider moves higher than the nodeIter score cutoff
//        //we allow the cluster to accrue nodesIterator from all around without the priority restriction
//        if (nodeScoreCutoff <= params.getNodeScoreCutoff()) {
//            nodeSeenHashMap = new HashMap(cluster.getNodeSeenHashMap());
//        } else {
//            nodeSeenHashMap = new HashMap();
//        }
//        Integer seedNode = cluster.getSeedNode();
//
//        ArrayList clusterNodes = getClusterCore(seedNode, nodeSeenHashMap, nodeScoreCutoff, params.getMaxDepthFromStart(), nodeInfoHashMap);
//        //make sure seed nodeIter is part of cluster, if not already nodeIter there
//        if (!clusterNodes.contains(seedNode)) {
//            clusterNodes.add(seedNode);
//        }
//        //createGraph an input graph for the filter and haircut methods
//        GraphPerspective corePerspective = createGraphPerspective(clusterNodes, inputNetwork);
//        if (params.isHaircut()) {
//            haircutCluster(corePerspective, clusterNodes, inputNetwork);
//        }
//        if (params.isFluff()) {
//            fluffClusterBoundary(clusterNodes, nodeSeenHashMap, nodeInfoHashMap);
//        }
//        cluster.setNodeIndices(clusterNodes);
//        corePerspective = createGraphPerspective(clusterNodes, inputNetwork);
//        cluster.setGraphPerspective(corePerspective);
//        cluster.setClusterScore(scoreCluster(cluster));
//
//        return cluster;
//    }
    /**
     * Score nodeIter using the formula from original MCODE paper.
     * This formula selects for larger, denser cores.
     * This is a utility function for the algorithm.
     *
     * @param nodeInfo The internal data structure to fill with nodeIter information
     * @return The score of this nodeIter.
     */
    private double scoreNode(NodeInfo nodeInfo) {
        if (nodeInfo.numNodeNeighbors > params.getDegreeCutoff()) {
            nodeInfo.score = nodeInfo.coreDensity * (double) nodeInfo.coreLevel;
        } else {
            nodeInfo.score = 0.0;
        }
        return (nodeInfo.score);
    }

    /**
     * Score a cluster.  Currently this ranks larger, denser clusters higher, although
     * nodeIter the future other scoring functions could be created
     *
     * @param cluster - The GINY GraphPerspective version of the cluster
     * @return The score of the cluster
     */
    public double scoreCluster(MCODECluster cluster) {
        int numNodes = 0;
        double density = 0.0, score = 0.0;

        numNodes = cluster.getGraphPerspective().getNodeCount();
        density = calcDensity(cluster.getGraphPerspective(), true);
        score = density * numNodes;

        return (score);
    }

    /**
     * Calculates nodeIter information for each nodeIter according to the original MCODE publication.
     * This information is used to score the nodesIterator nodeIter the scoring stage.
     * This is a utility function for the algorithm.
     *
     * @param graph The input network for reference
     * @param nodeRow    The index of the nodeIter nodeIter the input network to score
     * @return A NodeInfo object containing nodeIter information required for the algorithm
     */
    private NodeInfo calcNodeInfo(Graph graph, int nodeRow) {
        int[] neighborhood;

        String callerID = "MCODEAlgorithm.calcNodeInfo";
        if (graph == null) {
            System.err.println("In " + callerID + ": gpInputGraph was null.");
            return null;
        }

        //get neighborhood of this nodeIter (including the nodeIter)
        Set<Node> neighborSet = graph.neighborSet(graph.getNode(nodeRow));
        int[] neighbors = new int[neighborSet.size()];
        int i = 0;
        for (Node neighbor : neighborSet) {
            neighbors[i++] = neighbor.getRow();
        }

        if (neighbors.length < 2) {
            //if there are no neighborsIter or just one neighbor, nodeInfo calculation is trivial
            NodeInfo nodeInfo = new NodeInfo();
            if (neighbors.length == 1) {
                nodeInfo.coreLevel = 1;
                nodeInfo.coreDensity = 1.0;
                nodeInfo.density = 1.0;
            }
            return (nodeInfo);
        }
        //add original nodeIter to extract complete neighborhood
        Arrays.sort(neighbors);
        if (Arrays.binarySearch(neighbors, nodeRow) < 0) {
            neighborhood = new int[neighbors.length + 1];
            System.arraycopy(neighbors, 0, neighborhood, 1, neighbors.length);
            neighborhood[0] = nodeRow;
        } else {
            neighborhood = neighbors;
        }

        //extract neighborhood subgraph
        List<Integer> neighborhoodList = new ArrayList<Integer>(neighborhood.length);
        for (int row : neighborhood) {
            neighborhoodList.add(row);
        }
        GraphPerspective gpNodeNeighborhood = GraphPerspective.create(neighborhoodList, graph);
        if (gpNodeNeighborhood == null) {
            //this shouldn't happen
            System.err.println("In " + callerID + ": gpNodeNeighborhood was null.");
            return null;
        }

        //calculate the nodeIter information for each nodeIter
        NodeInfo nodeInfo = new NodeInfo();
        //density
        if (gpNodeNeighborhood != null) {
            nodeInfo.density = calcDensity(gpNodeNeighborhood, params.isIncludeLoops());
        }
        nodeInfo.numNodeNeighbors = neighborhood.length;
        //calculate the highest k-core
        GraphPerspective gpCore = null;
        Integer k = null;
        Object[] returnArray = getHighestKCore(gpNodeNeighborhood);
        k = (Integer) returnArray[0];
        gpCore = (GraphPerspective) returnArray[1];
        nodeInfo.coreLevel = k.intValue();
        //calculate the core density - amplifies the density of heavily interconnected regions and attenuates
        //that of less connected regions
        if (gpCore != null) {
            nodeInfo.coreDensity = calcDensity(gpCore, params.isIncludeLoops());
        }
        //record neighbor array for later use nodeIter cluster detection step
        nodeInfo.nodeNeighbors = neighborhood;

        return (nodeInfo);
    }

    /**
     * Find the high-scoring central region of the cluster.
     * This is a utility function for the algorithm.
     *
     * @param startNode       The nodeIter that is the seed of the cluster
     * @param nodeSeenHashMap The list of nodesIterator seen already
     * @param nodeScoreCutoff Slider input used for cluster exploration
     * @param maxDepthFromStart Limits the number of recursions
     * @param nodeInfoHashMap Provides the nodeIter scores
     * @return A list of nodeIter IDs representing the core of the cluster
     */
    private List<Integer> getClusterCore(int startNode, Map<Integer, Boolean> nodeSeenHashMap, double nodeScoreCutoff, int maxDepthFromStart, Map<Integer, NodeInfo> nodeInfoHashMap) {
        List<Integer> cluster = new ArrayList<Integer>(); // stores Integer clusterNodes
        getClusterCoreInternal(startNode, nodeSeenHashMap, nodeInfoHashMap.get(startNode).score, 1, cluster, nodeScoreCutoff, maxDepthFromStart, nodeInfoHashMap);
        return (cluster);
    }

    /**
     * An internal function that does the real work of getClusterCore, implemented to enable recursion.
     *
     * @param startNode         The nodeIter that is the seed of the cluster
     * @param nodeSeenHashMap   The list of nodesIterator seen already
     * @param startNodeScore    The score of the seed nodeIter
     * @param currentDepth      The depth away from the seed nodeIter that we are currently at
     * @param cluster           The cluster to add to if we find a cluster nodeIter nodeIter this method
     * @param nodeScoreCutoff   Helps determine if the nodesIterator being added are within the given threshold
     * @param maxDepthFromStart Limits the recursion
     * @param nodeInfoHashMap   Provides score info
     * @return true
     */
    private boolean getClusterCoreInternal(int startNode, Map<Integer, Boolean> nodeSeenHashMap, double startNodeScore, int currentDepth, List<Integer> cluster, double nodeScoreCutoff, int maxDepthFromStart, Map<Integer, NodeInfo> nodeInfoHashMap) {
        //base cases for recursion
        if (nodeSeenHashMap.containsKey(startNode)) {
            return (true);  //don't recheck a nodeIter
        }
        nodeSeenHashMap.put(startNode, true);

        if (currentDepth > maxDepthFromStart) {
            return (true);  //don't exceed given depth from start nodeIter
        }

        //Initialization
        int currentNeighbor;
        int i = 0;
        for (i = 0; i < nodeInfoHashMap.get(startNode).numNodeNeighbors; i++) {
            //go through all currentNode neighborsIter to check their core density for cluster inclusion
            currentNeighbor = nodeInfoHashMap.get(startNode).nodeNeighbors[i];
            if (!nodeSeenHashMap.containsKey(currentNeighbor)
                    && nodeInfoHashMap.get(currentNeighbor).score >= (startNodeScore - startNodeScore * nodeScoreCutoff)) {
                //add current neighbor
                if (!cluster.contains(currentNeighbor)) {
                    cluster.add(currentNeighbor);
                }
                //try to extend cluster at this nodeIter
                getClusterCoreInternal(currentNeighbor, nodeSeenHashMap, startNodeScore, currentDepth + 1, cluster, nodeScoreCutoff, maxDepthFromStart, nodeInfoHashMap);
            }
        }

        return (true);
    }

    /**
     * Fluff up the cluster at the boundary by adding lower scoring, non cluster-core neighborsIter
     * This implements the cluster fluff feature.
     *
     * @param cluster         The cluster to fluff
     * @param nodeSeenHashMap The list of nodesIterator seen already
     * @param nodeInfoHashMap Provides neighbour info
     * @return true
     */
    private boolean fluffClusterBoundary(List<Integer> cluster, Map<Integer, Boolean> nodeSeenHashMap, Map<Integer, NodeInfo> nodeInfoHashMap) {
        int currentNode = 0, nodeNeighbor = 0;
        //createGraph a temp list of nodesIterator to add to avoid concurrently modifying 'cluster'
        List<Integer> nodesToAdd = new ArrayList<Integer>();

        //Keep a separate internal nodeSeenHashMap because nodesIterator seen during a fluffing should not be marked as permanently seen,
        //they can be included nodeIter another cluster's fluffing step.
        Map<Integer, Boolean> nodeSeenHashMapInternal = new HashMap<Integer, Boolean>();

        //add all current neighbour's neighbours into cluster (if they have high enough clustering coefficients) and mark them all as seen
        for (int i = 0; i < cluster.size(); i++) {
            currentNode = cluster.get(i);
            for (int j = 0; j < nodeInfoHashMap.get(currentNode).numNodeNeighbors; j++) {
                nodeNeighbor = nodeInfoHashMap.get(currentNode).nodeNeighbors[j];
                if (!nodeSeenHashMap.containsKey(nodeNeighbor) && !nodeSeenHashMapInternal.containsKey(nodeNeighbor)
                        && (nodeInfoHashMap.get(nodeNeighbor).density) > params.getFluffNodeDensityCutoff()) {
                    nodesToAdd.add(nodeNeighbor);
                    nodeSeenHashMapInternal.put(nodeNeighbor, true);
                }
            }
        }

        //Add fluffed nodesIterator to cluster
        if (nodesToAdd.size() > 0) {
            cluster.addAll(nodesToAdd.subList(0, nodesToAdd.size()));
        }

        return (true);
    }

    /**
     * Checks if the cluster needs to be filtered according to heuristics nodeIter this method
     *
     * @param gpClusterGraph The cluster to check if it passes the filter
     * @return true if cluster should be filtered, false otherwise
     */
    private boolean filterCluster(GraphPerspective gpClusterGraph) {
        if (gpClusterGraph == null) {
            return (true);
        }

        //filter if the cluster does not satisfy the user specified k-core
        GraphPerspective gpCore = getKCore(gpClusterGraph, params.getKCore());
        if (gpCore == null) {
            return (true);
        }

        return (false);
    }

    /**
     * Gives the cluster a haircut (removed singly connected nodesIterator by taking a 2-core)
     *
     * @param gpClusterGraph The cluster graph
     * @param cluster        The cluster nodeIter ID list (nodeIter the original graph)
     * @param gpInputGraph   The original input graph
     * @return true
     */
    private boolean haircutCluster(GraphPerspective gpClusterGraph, List<Integer> cluster, Graph gpInputGraph) {
        //get 2-core
        GraphPerspective gpCore = getKCore(gpClusterGraph, 2);
        if (gpCore != null) {
            //clear the cluster and add all 2-core nodesIterator back into it
            cluster.clear();
            //must add back the nodesIterator nodeIter a way that preserves gpInputGraph nodeIter indices
//            for (int i = 0; i < rootGraphIndices.length; i++) {
//                cluster.add(new Integer(gpInputGraph.getRootGraphNodeIndex(rootGraphIndices[i])));
//            }
            for (int nodeIdx : gpCore.getNodeIndicesArray()) {
                cluster.add(nodeIdx);
            }
        }
        return (true);
    }

    /**
     * Calculate the density of a network
     * The density is defined as the number of edges/the number of possible edges
     *
     * @param gpInputGraph The input graph to calculate the density of
     * @param includeLoops Include the possibility of loops when determining the number of
     *                     possible edges.
     * @return The density of the network
     */
    public double calcDensity(GraphPerspective gpInputGraph, boolean includeLoops) {
        int possibleEdgeNum = 0, actualEdgeNum = 0, loopCount = 0;
        double density = 0;

        String callerID = "MCODEAlgorithm.calcDensity";
        if (gpInputGraph == null) {
            System.err.println("In " + callerID + ": gpInputGraph was null.");
            return (-1.0);
        }

        if (includeLoops) {
            //count loops
            Iterator<Node> nodes = gpInputGraph.nodesIterator();
            while (nodes.hasNext()) {
                Node n = nodes.next();
                if (gpInputGraph.isNeighbor(n, n)) {
                    loopCount++;
                }
            }
            possibleEdgeNum = gpInputGraph.getNodeCount() * gpInputGraph.getNodeCount();
            actualEdgeNum = gpInputGraph.getEdgeCount() - loopCount;
        } else {
            possibleEdgeNum = gpInputGraph.getNodeCount() * gpInputGraph.getNodeCount();
            actualEdgeNum = gpInputGraph.getEdgeCount();
        }

        density = (double) actualEdgeNum / (double) possibleEdgeNum;
        return (density);
    }

    /**
     * Find a k-core of a network. A k-core is a subgraph of minimum degree k
     *
     * @param gpInputGraph The input network
     * @param k            The k of the k-core to find e.g. 4 will find a 4-core
     * @return Returns a subgraph with the core, if any was found at given k
     */
    public GraphPerspective getKCore(GraphPerspective gpInputGraph, int k) {
        String callerID = "MCODEAlgorithm.getKCore";
        if (gpInputGraph == null) {
            System.err.println("In " + callerID + ": gpInputGraph was null.");
            return (null);
        }

        //filter all nodesIterator with degree less than k until convergence
        boolean firstLoop = true;
        int numDeleted;
        GraphPerspective gpOutputGraph = null;
        while (true) {
            numDeleted = 0;
            List<Integer> alCoreNodeIndices = new ArrayList<Integer>(gpInputGraph.getNodeCount());
            Iterator<Node> nodes = gpInputGraph.nodesIterator();
            while (nodes.hasNext()) {
                Node n = nodes.next();
                if (gpInputGraph.getDegree(n) >= k) {
                    alCoreNodeIndices.add(n.getRow()); //contains all nodesIterator with degree >= k
                } else {
                    numDeleted++;
                }
            }
            if ((numDeleted > 0) || (firstLoop)) {
                //convert ArrayList to int[] for creation of a GraphPerspective for this core
                List<Integer> outputNodeIndices = new ArrayList<Integer>(alCoreNodeIndices);
                gpOutputGraph = gpInputGraph.createPerspective(outputNodeIndices);
                if (gpOutputGraph.getNodeCount() == 0) {
                    return (null);
                }
                //iterate again, but with a new k-core input graph
                gpInputGraph = gpOutputGraph;
                if (firstLoop) {
                    firstLoop = false;
                }
            } else {
                //stop the loop
                break;
            }
        }

        return (gpOutputGraph);
    }

    /**
     * Find the highest k-core nodeIter the input graph.
     *
     * @param gpInputGraph The input network
     * @return Returns the k-clusterNodes and the core as an Object array.
     *         The first object is the highest k clusterNodes i.e. objectArray[0]
     *         The second object is the highest k-core as a GraphPerspective i.e. objectArray[1]
     */
    public Object[] getHighestKCore(GraphPerspective gpInputGraph) {
        String callerID = "MCODEAlgorithm.getHighestKCore";
        if (gpInputGraph == null) {
            System.err.println("In " + callerID + ": gpInputGraph was null.");
            return (null);
        }

        int i = 1;
        GraphPerspective gpCurCore = null, gpPrevCore = null;

        while ((gpCurCore = getKCore(gpInputGraph, i)) != null) {
            gpInputGraph = gpCurCore;
            gpPrevCore = gpCurCore;
            i++;
        }

        Integer k = new Integer(i - 1);
        Object[] returnArray = new Object[2];
        returnArray[0] = k;
        returnArray[1] = gpPrevCore;    //nodeIter the last iteration, gpCurCore is null (loop termination condition)

        return (returnArray);
    }
}
