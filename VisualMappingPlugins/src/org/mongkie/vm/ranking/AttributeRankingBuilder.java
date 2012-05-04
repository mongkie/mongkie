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
package org.mongkie.vm.ranking;

import org.mongkie.visualmap.spi.ranking.AbstractRanking;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.ranking.RankingModel;
import org.mongkie.visualmap.spi.ranking.RankingBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.column.Column;
import prefuse.util.DataLib;
import prefuse.util.TypeLib;
import static org.mongkie.visualmap.VisualMapping.*;

/**
 * Ranking builder for attributes. Builds the {@link Ranking} instances that
 * maps to all numerical attribute columns.
 * <p>
 * The ranking is built for the workspace associated to the given {@link RankingModel}.
 * 
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = RankingBuilder.class)
public class AttributeRankingBuilder implements RankingBuilder {

    private final VisualizationController vizController;

    public AttributeRankingBuilder() {
        vizController = Lookup.getDefault().lookup(VisualizationController.class);
    }

    @Override
    public Ranking[] buildRankings(RankingModel model) {
        List<Ranking> rankings = new ArrayList<Ranking>();
        Graph graph = vizController.getDisplay().getGraph();
        //Nodes
        Table nodeTable = graph.getNodeTable();
        for (String column : DataLib.getColumnNames(nodeTable)) {
            if (TypeLib.isNumericType(nodeTable.getColumnType(column))) {
                AttributeRanking ranking = new AttributeRanking(NODE_ELEMENT, column, graph);
                rankings.add(ranking);
            }
        }
        //Edges
        Table edgeTable = graph.getEdgeTable();
        for (String column : DataLib.getColumnNames(edgeTable)) {
            if (TypeLib.isNumericType(edgeTable.getColumnType(column))) {
                AttributeRanking ranking = new AttributeRanking(EDGE_ELEMENT, column, graph);
                rankings.add(ranking);
            }
        }
        //Sort attributes by alphabetical order
        Ranking[] rankingArray = rankings.toArray(new Ranking[0]);
        Arrays.sort(rankingArray, new Comparator<Ranking>() {

            @Override
            public int compare(Ranking a, Ranking b) {
                return (a.getName().compareTo(b.getName()));
            }
        });
        return rankingArray;
    }

    @Override
    public Ranking refreshRanking(Ranking ranking) {
        if (ranking == null) {
            throw new NullPointerException();
        }
        if (ranking instanceof AttributeRanking) {
            return ((AttributeRanking) ranking).clone();
        } else {
            throw new IllegalArgumentException("Ranking must be an AttributeRanking");
        }
    }

    private static class AttributeRanking<T extends Tuple> extends AbstractRanking<T> {

        private final Column column;
        private final Graph graph;

        AttributeRanking(String elementType, String column, Graph graph) {
            super(elementType, column);
            if (elementType.equals(NODE_ELEMENT)) {
                this.column = graph.getNodeTable().getColumn(column);
            } else if (elementType.equals(EDGE_ELEMENT)) {
                this.column = graph.getEdgeTable().getColumn(column);
            } else {
                throw new IllegalArgumentException("Element type must be nodes or edges");
            }
            this.graph = graph;
        }

        @Override
        public Number getValue(T tuple) {
            return tuple.getDouble(getName());
        }

        @Override
        public float normalize(Number value) {
            return (value.floatValue() - getMinimumValue().floatValue()) / (float) (getMaximumValue().floatValue() - getMinimumValue().floatValue());
        }

        @Override
        public Number unnormalize(float normalizedValue) {
            double val = (normalizedValue * (getMaximumValue().doubleValue() - getMinimumValue().doubleValue())) + getMinimumValue().doubleValue();
            return new Double(val);
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public Number getMaximumValue() {
            if (maximum == null) {
                AbstractRanking.refreshMinMax(this, graph);
            }
            return maximum;
        }

        @Override
        public Number getMinimumValue() {
            if (minimum == null) {
                AbstractRanking.refreshMinMax(this, graph);
            }
            return minimum;
        }

        @Override
        protected AttributeRanking clone() {
            AttributeRanking newRanking = new AttributeRanking(elementType, getName(), graph);
            return newRanking;
        }
    }
}
