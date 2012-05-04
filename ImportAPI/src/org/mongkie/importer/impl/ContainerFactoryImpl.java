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
package org.mongkie.importer.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.mongkie.importer.*;
import org.mongkie.importer.spi.Processor.Hint;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.Table;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ContainerFactory.class, position = 100)
public class ContainerFactoryImpl implements ContainerFactory {

    @Override
    public GraphContainer createGraphContainer() {
        return new GraphContainerImpl();
    }

    private static class GraphContainerImpl implements GraphContainer {

        private String source;
        private Graph g;
        private boolean directed;
        private boolean autoScale;
        private boolean allowSelfLoop;
        private boolean allowParallelEdge;
        private Report report;
        protected final List<Hint> processorHints;

        private GraphContainerImpl() {
            processorHints = new ArrayList<Hint>();
        }

        @Override
        public void setSource(String source) {
            this.source = source;
        }

        @Override
        public String getSource() {
            return source;
        }

        @Override
        public void setResult(Graph g) {
            this.g = g;
            this.directed = g.isDirected();
        }

        @Override
        public Graph getResult() {
            return g;
        }

        @Override
        public Graph getGraph() {
            return getResult();
        }

        @Override
        public void setDirected(boolean directed) {
            this.directed = directed;
        }

        @Override
        public boolean isDirected() {
            return directed;
        }

        @Override
        public void setAutoScale(boolean autoScale) {
            this.autoScale = autoScale;
        }

        @Override
        public boolean isAutoScale() {
            return autoScale;
        }

        @Override
        public boolean isAllowSelfLoop() {
            return allowSelfLoop;
        }

        @Override
        public void setAllowSelfLoop(boolean allowSelfLoop) {
            this.allowSelfLoop = allowSelfLoop;
        }

        @Override
        public boolean isAllowParallelEdge() {
            return allowParallelEdge;
        }

        @Override
        public void setAllowParallelEdge(boolean allowParallelEdge) {
            this.allowParallelEdge = allowParallelEdge;
        }

        @Override
        public void setReport(Report report) {
            this.report = report;
        }

        @Override
        public Report getReport() {
            return report;
        }

        @Override
        public boolean verify() {
            if (g == null) {
                return false;
            }
            g.setDirected(directed);
            report.logIssue(new Issue(
                    NbBundle.getMessage(GraphContainerImpl.class, "GraphContainerImpl.issue.edgeType", directed ? "DIRECED" : "UNDIRECTED"),
                    Issue.Level.INFO));
            report.logIssue(new Issue(
                    NbBundle.getMessage(GraphContainerImpl.class, "GraphContainerImpl.issue.nodeCount", g.getNodeCount()),
                    Issue.Level.INFO));
            report.logIssue(new Issue(
                    NbBundle.getMessage(GraphContainerImpl.class, "GraphContainerImpl.issue.edgeCount", g.getEdgeCount()),
                    Issue.Level.INFO));
            return true;
        }

        @Override
        public void addProcessorHint(Hint... hints) {
            if (hints == null || hints.length == 0) {
                processorHints.clear();
            }
            processorHints.addAll(Arrays.asList(hints));
        }

        @Override
        public boolean isProcessorHinted(Hint... hints) {
            if (hints == null || hints.length == 0) {
                return false;
            }
            return processorHints.containsAll(Arrays.asList(hints));
        }
    }

    @Override
    public VizGraphContainer createVizGraphContainer() {
        return new VizGraphContainerImpl();
    }

    private static class VizGraphContainerImpl extends GraphContainerImpl
            implements VizGraphContainer {

        private Table nodeVizProperties, edgeVizProperties, aggrVizProperties;
        private Map<Integer, List<Integer>> aggregateId2NodeItemRows;

        @Override
        public void setNodeVisualProperties(Table table) {
            nodeVizProperties = table;
        }

        @Override
        public Table getNodeVisualProperties() {
            return nodeVizProperties;
        }

        @Override
        public void setEdgeVisualProperties(Table table) {
            edgeVizProperties = table;
        }

        @Override
        public Table getEdgeVisualProperties() {
            return edgeVizProperties;
        }

        @Override
        public void setAggregateVisualProperties(Table table) {
            aggrVizProperties = table;
        }

        @Override
        public Table getAggregateVisualProperties() {
            return aggrVizProperties;
        }

        @Override
        public void setAggregateId2NodeItemRows(Map<Integer, List<Integer>> rows) {
            aggregateId2NodeItemRows = rows;
        }

        @Override
        public Map<Integer, List<Integer>> getAggregateId2NodeRows() {
            return aggregateId2NodeItemRows;
        }
    }

    @Override
    public <T> Container<T> createDefault(Class<T> resultClass) {
        return new Container<T>() {

            private String source;
            private Report report;
            private T result;
            private final List<Hint> processorHints = new ArrayList<Hint>();

            @Override
            public void setSource(String source) {
                this.source = source;
            }

            @Override
            public String getSource() {
                return source;
            }

            @Override
            public void setReport(Report report) {
                this.report = report;
            }

            @Override
            public Report getReport() {
                return report;
            }

            @Override
            public boolean verify() {
                return true;
            }

            @Override
            public void addProcessorHint(Hint... hints) {
                if (hints == null || hints.length == 0) {
                    processorHints.clear();
                }
                processorHints.addAll(Arrays.asList(hints));
            }

            @Override
            public boolean isProcessorHinted(Hint... hints) {
                if (hints == null || hints.length == 0) {
                    return false;
                }
                return processorHints.containsAll(Arrays.asList(hints));
            }

            @Override
            public void setResult(T result) {
                this.result = result;
            }

            @Override
            public T getResult() {
                return result;
            }
        };
    }
}
