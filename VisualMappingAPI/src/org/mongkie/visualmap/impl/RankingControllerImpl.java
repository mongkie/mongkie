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
package org.mongkie.visualmap.impl;

import java.util.Iterator;
import kobic.prefuse.Constants;
import org.mongkie.visualization.MongkieDisplay;
import static org.mongkie.visualmap.VisualMapping.EDGE_ELEMENT;
import static org.mongkie.visualmap.VisualMapping.NODE_ELEMENT;
import org.mongkie.visualmap.ranking.Interpolator;
import org.mongkie.visualmap.ranking.RankingController;
import static org.mongkie.visualmap.ranking.RankingEvent.Type.APPLY_TRANSFORMER;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.TransformerUI;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = RankingController.class)
public class RankingControllerImpl extends RankingController<RankingModelImpl> {

    static final String[] ELEMENT_TYPES = new String[]{NODE_ELEMENT, EDGE_ELEMENT};

    @Override
    public void setInterpolator(Interpolator interpolator) {
        if (model != null) {
            model.setInterpolator(interpolator);
        }
    }

    @Override
    public void transform(Ranking ranking, Transformer transformer) {
        ranking = model.getRanking(ranking.getElementType(), ranking.getName());

        MongkieDisplay display = model.getDisplay();
        Graph graph = display.getGraph();
        Interpolator interpolator = model.getInterpolator();

        if (ranking.getElementType().equals(NODE_ELEMENT)) {
            for (Iterator<Node> nodeIter = graph.nodes(); nodeIter.hasNext();) {
                Node node = nodeIter.next();
                Number value = ranking.getValue(node);
                if (value != null) {
                    float normalizedValue = ranking.normalize(value);
                    if (transformer.isInBounds(normalizedValue)) {
                        normalizedValue = interpolator.interpolate(normalizedValue);
                        transformer.transform(display.getVisualization().getVisualItem(Constants.NODES, node), normalizedValue);
                    }
                }
            }
        } else if (ranking.getElementType().equals(EDGE_ELEMENT)) {
            for (Iterator<Edge> edgeIter = graph.edges(); edgeIter.hasNext();) {
                Edge edge = edgeIter.next();
                Number value = ranking.getValue(edge);
                if (value != null) {
                    float normalizedValue = ranking.normalize(value);
                    if (transformer.isInBounds(normalizedValue)) {
                        normalizedValue = interpolator.interpolate(normalizedValue);
                        transformer.transform(display.getVisualization().getVisualItem(Constants.EDGES, edge), normalizedValue);
                    }
                }
            }
        }

        model.getDisplay().getVisualization().repaint();
        model.fireRankingEvent(APPLY_TRANSFORMER, ranking, transformer);
    }

    @Override
    public TransformerUI getUI(Transformer transformer) {
        for (TransformerUI ui : Lookup.getDefault().lookupAll(TransformerUI.class)) {
            if (ui.isUIForTransformer(transformer)) {
                return ui;
            }
        }
        return null;
    }

    @Override
    public String[] getElementTypes() {
        return ELEMENT_TYPES;
    }

    @Override
    public void setCurrentElementType(String elementType) {
        if (model != null) {
            model.setCurrentElementType(elementType);
        }
    }

    @Override
    public void setCurrentRanking(Ranking ranking) {
        if (model != null) {
            model.setCurrentRanking(ranking);
        }
    }

    @Override
    public void setCurrentTransformer(Transformer transformer) {
        if (model != null) {
            model.setCurrentTransformer(transformer);
        }
    }

    @Override
    public void setRankingListVisible(boolean visible) {
        if (model != null) {
            model.setRankingListVisible(visible);
        }
    }

    @Override
    protected Class<RankingModelImpl> getModelClass() {
        return RankingModelImpl.class;
    }

    @Override
    protected RankingModelImpl createModel(MongkieDisplay d) {
        return new RankingModelImpl(d);
    }
}
