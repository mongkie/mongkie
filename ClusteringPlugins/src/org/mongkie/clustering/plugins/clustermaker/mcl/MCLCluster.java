/* vim: set ts=2: */
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.mongkie.clustering.spi.Cluster;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 * In it's simplist form, a Cluster is a group of nodes that represents the
 * nodes that are grouped together as the result of a clustering algorithm
 * of some sort.  A more complicated form of a cluster could include clusters
 * as part of the list, which complicates this class a little....
 */
public class MCLCluster extends ArrayList<Node> implements Cluster {

    int clusterNumber = 0;
    static int clusterCount = 0;
    static boolean hasScore = false;
    protected double score = 0.0;
    private Graph g;
    private String name = null;
    private Color color = Color.BLACK;

    public MCLCluster(Graph g) {
        super();
        this.g = g;
        clusterCount++;
        clusterNumber = clusterCount;
    }

    public MCLCluster(Graph g, Collection<Node> collection) {
        super(collection);
        this.g = g;
        clusterCount++;
        clusterNumber = clusterCount;
    }

    public boolean add(List<Node> nodeList, int index) {
        return add(nodeList.get(index));
    }

    public static void init() {
        clusterCount = 0;
        hasScore = false;
    }

    public static boolean hasScore() {
        return hasScore;
    }

    public int getClusterNumber() {
        return clusterNumber;
    }

    public void setClusterNumber(int clusterNumber) {
        this.clusterNumber = clusterNumber;
    }

    public void setClusterScore(double score) {
        this.score = score;
        hasScore = true;
    }

    public double getClusterScore() {
        return score;
    }

    @Override
    public String toString() {
        String str = "(" + clusterNumber + ": ";
        for (Node n : this) {
            str += (" " + n.getRow());
        }
        return str + ")";
    }

    public static List<MCLCluster> sortMap(Map<Integer, MCLCluster> map) {
        MCLCluster[] clusterArray = map.values().toArray(new MCLCluster[1]);
        Arrays.sort(clusterArray, new LengthComparator());
        return Arrays.asList(clusterArray);
    }

    public static List<MCLCluster> rankListByScore(List<MCLCluster> list) {
        MCLCluster[] clusterArray = list.toArray(new MCLCluster[1]);
        Arrays.sort(clusterArray, new ScoreComparator());
        for (int rank = 0; rank < clusterArray.length; rank++) {
            clusterArray[rank].setClusterNumber(rank + 1);
        }
        return Arrays.asList(clusterArray);
    }

    @Override
    public Graph getGraph() {
        return g;
    }

    @Override
    public Collection<Node> getNodes() {
        return this;
    }

    @Override
    public int getNodesCount() {
        return size();
    }

    @Override
    public String getName() {
        return name != null ? name : ("Cluster " + clusterNumber);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public int getRank() {
        return clusterNumber - 1;
    }

    static class LengthComparator implements Comparator<MCLCluster> {

        @Override
        public int compare(MCLCluster c1, MCLCluster c2) {
            if (c1.size() > c2.size()) {
                return -1;
            }
            if (c1.size() < c2.size()) {
                return 1;
            }
            return 0;
        }
    }

    static class ScoreComparator implements Comparator<MCLCluster> {

        @Override
        public int compare(MCLCluster c1, MCLCluster c2) {
            if (c1.getClusterScore() > c2.getClusterScore()) {
                return -1;
            }
            if (c1.getClusterScore() < c2.getClusterScore()) {
                return 1;
            }
            return 0;
        }
    }
}
