/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualmap.ranking;

import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.workspace.AbstractModel;
import org.mongkie.visualmap.VisualMapping;

/**
 * Model for ranking data. 
 * <p>
 * That includes the list of rankings currently available,
 * separated in categories with different element types. It can returns all rankings
 * for nodes or edges, or any element type.
 * <p>
 * Rankings are builds thanks to <code>RankingBuilder</code> implementation. Implement
 * a new <code>RankingBuider</code> service to create new rankings.
 * <p>
 * The model also hosts the currently defined interpolator.
 * 
 * @see Ranking
 * @see Transformer
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class RankingModel extends AbstractModel<Ranking[], RankingModelListener> {
    
    public RankingModel(MongkieDisplay display) {
        super(display);
    }
    
    public Ranking[] getRankings() {
//        return get();
        return getRankings(getCurrentElementType());
    }
    
    public abstract String getCurrentElementType();
    
    public abstract Transformer getCurrentTransformer();
    
    public abstract Transformer getCurrentTransformer(String elementType);
    
    public abstract Ranking getCurrentRanking();
    
    public abstract boolean isRankingListVisible();

    /**
     * Get all rankings for node elements. Rankings are classified with the type
     * of element they are manipulating. Rankings specific to node elements are
     * defined by the {@link VisualMapping#NODE_ELEMENT}.
     * @return All rankings for node elements
     */
    public abstract Ranking[] getNodeRankings();

    /**
     * Get all rankings for edge elements. Rankings are classified with the type
     * of element they are manipulating. Rankings specific to edge elements are
     * defined by the {@link VisualMapping#EDGE_ELEMENT}.
     * @return All rankings for edge elements
     */
    public abstract Ranking[] getEdgeRankings();

    /**
     * Get all rankings for <code>elementType</code> elements. Rankings are 
     * classified with the type of element they are manipulating. If 
     * <code>elementType</code> equals {@link VisualMapping#NODE_ELEMENT} this is
     * equivalent to {@link #getNodeRankings() } method
     * @param elementType the element type of the rankings
     * @return All rankings for <code>elementType</code>
     */
    public abstract Ranking[] getRankings(String elementType);

    /**
     * Return the specific ranking for <code>elementType</code> and with
     * the given <code>name</code>. Returns <code>null</code> if not found.
     * <p>
     * Default ranking names can be found in the {@link Ranking} interface. For 
     * attribute rankings, simply use the column identifier.
     * @param elementType the element type of the ranking
     * @param name the name of the ranking
     * @return the found ranking or <code>null</code> if not found
     */
    public abstract Ranking getRanking(String elementType, String name);

    /**
     * Return all transformers specific to <code>elementType</code>. A transformer
     * defines his ability to transformer different element types. 
     * @param elementType the element type of the transformers
     * @return all transformers working with <code>elementType</code>
     */
    public abstract Transformer[] getTransformers(String elementType);

    /**
     * Returns the specific transformer for <code>elementType</code> and with the
     * given <code>name</code>. Returns <code>null</code> if not found.
     * <p>
     * Default transformers name can be found in the {@link Transformer} interface.
     * @param elementType   the element type of the transformer
     * @param name  the name of the transformer
     * @return the transformer defined as <code>name</code> and <code>elementType</code>
     * or <code>null</code> if not found
     */
    public abstract Transformer getTransformer(String elementType, String name);

    /**
     * Returns the current interpolator. The default interpolator is a simple
     * linear interpolation.
     * @return the current interpolator
     */
    public abstract Interpolator getInterpolator();
}
