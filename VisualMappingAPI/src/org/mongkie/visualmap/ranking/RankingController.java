/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualmap.ranking;

import org.mongkie.visualmap.spi.ranking.Transformer;
import org.mongkie.visualmap.spi.ranking.Ranking;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.workspace.AbstractController;
import org.mongkie.visualmap.spi.ranking.TransformerUI;

/**
 * Controller that maintains the ranking models, one per workspace.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>RankingController rc = Lookup.getDefault().lookup(RankingController.class);</pre>
 * <p>
 * Use <code>transform()</code> to apply transformers on ranking's elements. Transform
 * is a one shot action. For continuous transformation, start an auto transformer
 * using <code>startAutoTransform()</code>.
 * @see RankingModel
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class RankingController<M extends RankingModel> extends AbstractController<M, Ranking[]> {

    /**
     * Sets the interpolator to be used when transforming values. This is set to the
     * current model only. If the model is changed (i.e. switch workspace), call 
     * this again.
     * <p>
     * Default interpolator implementations can be found in the {@link Interpolator}
     * class.
     * @param interpolator the interpolator to use for transformation. 
     */
    public abstract void setInterpolator(Interpolator interpolator);

    /**
     * Apply the transformation of <code>transformer</code> on <code>ranking</code>.
     * The transformer will modify element's color or size according to the values
     * returned by the ranking. Before passing values to the transformer, they may
     * be transformer by the current interpolator.
     * @param ranking the ranking to give to the transformer
     * @param transformer the transformer to apply on the ranking's elements
     */
    public abstract void transform(Ranking ranking, Transformer transformer);

    /**
     * Starts an auto transformation using <code>ranking</code> and 
     * <code>transformer</code>. The transformation is continuously applied to
     * the current graph. The operation is the same as <code>transform()</code>, 
     * except it is applied in a loop until <code>stopAutoTransform()</code> is
     * called.
     * <p>
     * Note that auto transformation work only in the current workspace and are
     * paused when the workspace is not current.
     * @param ranking the ranking to give to the transformer
     * @param transformer the transformer to apply on the ranking's elements
     */
//    public abstract void startAutoTransform(Ranking ranking, Transformer transformer);
    /**
     * Stops the auto transformation of <code>transfromer</code>.
     * @param transformer the transformer to stop auto transformation
     */
//    public abstract void stopAutoTransform(Transformer transformer);
    public abstract TransformerUI getUI(Transformer transformer);

    public abstract String[] getElementTypes();

    public abstract void setCurrentElementType(String elementType);

    public abstract void setCurrentRanking(Ranking ranking);

    public abstract void setCurrentTransformer(Transformer transformer);

    public abstract void setRankingListVisible(boolean visible);

    @Override
    public M getModel() {
        return super.getModel();
    }

    @Override
    public synchronized M getModel(MongkieDisplay d) {
        return super.getModel(d);
    }
}
