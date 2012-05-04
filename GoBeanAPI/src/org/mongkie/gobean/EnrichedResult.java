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
 * You should have received a addAll of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.gobean;

import gobean.DagEdge;
import gobean.GoBranch;
import gobean.GoGraph;
import gobean.GoId;
import gobean.TermInfo;
import gobean.calculation.Enriched;
import gobean.domain.Dbxref;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@XmlRootElement
public class EnrichedResult {

    private int totalStudyCount;
    private int totalPopCount;
    private List<GoId> selectedGoIds = new ArrayList<GoId>();
    private Map<GoId, Double> rawP = new HashMap<GoId, Double>();
    private Map<GoId, Double> adjustedP = new HashMap<GoId, Double>();
    private Map<GoId, int[]> coverageMap = new HashMap<GoId, int[]>();
    private Map<GoId, String> branchMap = new HashMap<GoId, String>();
    private Map<GoId, String> nameMap = new HashMap<GoId, String>();
    private Map<GoId, Integer> childCountMap = new HashMap<GoId, Integer>();
    private Map<GoId, GoId[]> childMap = new HashMap<GoId, GoId[]>();
    private Map<GoId, String> descriptionMap = new HashMap<GoId, String>();
    private Map<GoId, String[]> fullStudyMap = new HashMap<GoId, String[]>();
    private Map<GoId, String[]> currentStudyMap = new HashMap<GoId, String[]>();

    private EnrichedResult() {
    }

    private static <K, V> void addAll(Map<K, V> from, Map<K, V> to) {
        for (K k : from.keySet()) {
            to.put(k, from.get(k));
        }
    }

    public static EnrichedResult createFrom(Enriched enriched) {
        EnrichedResult result = new EnrichedResult();
        result.totalStudyCount = enriched.getTotalStudyCount();
        result.totalPopCount = enriched.getTotalPopCount();
        result.selectedGoIds.addAll(enriched.getSelectedGoIds());
        addAll(enriched.getRawP(), result.rawP);
        addAll(enriched.getAdjustedP(), result.adjustedP);
        GoGraph subgraph = getSubgraph(result.selectedGoIds);
        for (GoId goId : subgraph.getAllGoIds()) {
            result.coverageMap.put(goId, enriched.getCoverage(goId));
            result.branchMap.put(goId, GoGraph.getGoBranch(goId).name());
            result.nameMap.put(goId, TermInfo.getName(goId));
            result.descriptionMap.put(goId, TermInfo.getDefinition(goId));
            result.fullStudyMap.put(goId, toAccessKeys(enriched.getFullStudy(goId)));
            result.currentStudyMap.put(goId, toAccessKeys(enriched.getCurrentStudy(goId)));
            result.childCountMap.put(goId, subgraph.childCount(goId));
            Set<DagEdge> downEdges = subgraph.getDownEdges(goId);
            if (downEdges == null) {
                continue;
            }
            Set<GoId> children = new HashSet<GoId>();
            for (DagEdge edge : downEdges) {
                children.add(edge.getNodeId());
            }
            GoId[] childrenArr = new GoId[children.size()];
            children.toArray(childrenArr);
            result.childMap.put(goId, childrenArr);
        }
        return result;
    }

    private static String[] toAccessKeys(Set<Dbxref> xrefs) {
        String[] acessKeys = new String[xrefs.size()];
        int i = 0;
        for (Dbxref xref : xrefs) {
            acessKeys[i++] = xref.getAccessKey();
        }
        return acessKeys;
    }

    private static GoGraph getSubgraph(Collection<GoId> terms) {
        return GoGraph.getSubgraph("Subgraph for the tree of selected GO terms", terms);
    }

    private void selectedGoIDsFiltered() {
        GoGraph subgraph = getSubgraph(selectedGoIds);
        Set<GoId> subAllGoIds = subgraph.getAllGoIds();
        for (Iterator<GoId> goIdIter = rawP.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = adjustedP.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = coverageMap.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = branchMap.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = nameMap.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = descriptionMap.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = fullStudyMap.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        for (Iterator<GoId> goIdIter = currentStudyMap.keySet().iterator(); goIdIter.hasNext();) {
            GoId goId = goIdIter.next();
            if (!subAllGoIds.contains(goId)) {
                goIdIter.remove();
            }
        }
        childCountMap.clear();
        childMap.clear();
        for (GoId goId : subAllGoIds) {
            childCountMap.put(goId, subgraph.childCount(goId));
            Set<DagEdge> downEdges = subgraph.getDownEdges(goId);
            if (downEdges == null) {
                continue;
            }
            Set<GoId> children = new HashSet<GoId>();
            for (DagEdge edge : downEdges) {
                children.add(edge.getNodeId());
            }
            GoId[] childrenArr = new GoId[children.size()];
            children.toArray(childrenArr);
            childMap.put(goId, childrenArr);
        }
    }

    public EnrichedResult cutoff(double pmax) {
        Set<GoId> toBeRemoved = new HashSet<GoId>();
        for (GoId goId : selectedGoIds) {
            if (adjustedP.get(goId) > pmax) {
                toBeRemoved.add(goId);
            }
        }
        selectedGoIds.removeAll(toBeRemoved);
        selectedGoIDsFiltered();
        return this;
    }

    public String getName(GoId id) {
        return nameMap.get(id);
    }

    public GoBranch getGoBranch(GoId termid) {
        if (termid.equals(GoBranch.GENE_ONTOLOGY.getGoId())) {
            return GoBranch.GENE_ONTOLOGY;
        }
        if (!branchMap.containsKey(termid)) {
            throw new RuntimeException("EnrichedResult : can not find GO branch for '" + termid + "'");
        }
        return GoBranch.valueOf(branchMap.get(termid));
    }

    public GoId getDownId(GoId parent, int index) {
        GoId[] children = childMap.get(parent);
        return children != null ? children[index] : null;
    }

    public int childCount(GoId parent) {
        return childCountMap.get(parent);
    }

    public int getTotalStudyCount() {
        return totalStudyCount;
    }

    public int getTotalPopCount() {
        return totalPopCount;
    }

    public List<GoId> getSelectedGoIds() {
        return selectedGoIds;
    }

    public void setSelectedGoIds(List<GoId> selectedGoIds) {
        this.selectedGoIds = selectedGoIds;
    }

    public void setTotalPopCount(int totalPopCount) {
        this.totalPopCount = totalPopCount;
    }

    public void setTotalStudyCount(int totalStudyCount) {
        this.totalStudyCount = totalStudyCount;
    }

    public Double getRawP(GoId goid) {
        return rawP.get(goid);
    }

    public Double getAdjustedP(GoId goid) {
        return adjustedP.get(goid);
    }

    public int[] getCoverage(GoId goid) {
        return coverageMap.get(goid);
    }

    public Map<GoId, Double> getAdjustedP() {
        return adjustedP;
    }

    public void setAdjustedP(Map<GoId, Double> adjustedP) {
        this.adjustedP = adjustedP;
    }

    public Map<GoId, int[]> getCoverageMap() {
        return coverageMap;
    }

    public void setCoverageMap(Map<GoId, int[]> coverageMap) {
        this.coverageMap = coverageMap;
    }

    public Map<GoId, Double> getRawP() {
        return rawP;
    }

    public void setRawP(Map<GoId, Double> rawP) {
        this.rawP = rawP;
    }

    public Map<GoId, Integer> getChildCountMap() {
        return childCountMap;
    }

    public void setChildCountMap(Map<GoId, Integer> childCountMap) {
        this.childCountMap = childCountMap;
    }

    public Map<GoId, GoId[]> getChildMap() {
        return childMap;
    }

    public void setChildMap(Map<GoId, GoId[]> childMap) {
        this.childMap = childMap;
    }

    public Map<GoId, String> getBranchMap() {
        return branchMap;
    }

    public void setBranchMap(Map<GoId, String> branchMap) {
        this.branchMap = branchMap;
    }

    public Map<GoId, String> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<GoId, String> nameMap) {
        this.nameMap = nameMap;
    }

    public Map<GoId, String> getDescriptionMap() {
        return descriptionMap;
    }

    public void setDescriptionMap(Map<GoId, String> descriptionMap) {
        this.descriptionMap = descriptionMap;
    }

    public Map<GoId, String[]> getFullStudyMap() {
        return fullStudyMap;
    }

    public void setFullStudyMap(Map<GoId, String[]> fullStudyMap) {
        this.fullStudyMap = fullStudyMap;
    }

    public Map<GoId, String[]> getCurrentStudyMap() {
        return currentStudyMap;
    }

    public void setCurrentStudyMap(Map<GoId, String[]> currentStudyMap) {
        this.currentStudyMap = currentStudyMap;
    }
}
