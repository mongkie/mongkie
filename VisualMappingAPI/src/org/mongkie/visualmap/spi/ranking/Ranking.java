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
package org.mongkie.visualmap.spi.ranking;

import org.mongkie.visualmap.VisualMapping;

/**
 * Rankings role is to provide numerical values from objects. These values are
 * then send to transformer to be converted in visual signs (e.g. color or size).
 * <p>
 * For instance for nodes, ranking can be the degree of the node or a numerical
 * value like an 'age' or 'duration'.
 * <p>
 * One can reuse the {@link AbstractRanking}
 * 
 * @see Transformer
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface Ranking<T> extends VisualMapping<T, Number> {

    /**
     * Default in degree ranking's name
     */
    public static final String DEGREE_RANKING = "degree";
    /**
     * Default out degree ranking's name
     */
    public static final String INDEGREE_RANKING = "indegree";
    /**
     * Default out degree ranking's name
     */
    public static final String OUTDEGREE_RANKING = "outdegree";

    /**
     * Returns the value of the element. 
     * @param tuple the tuple to get the value from
     * @return the element's value
     */
    @Override
    public Number getValue(T tuple);

    /**
     * Returns the minimum value of this ranking.
     * @return the minimum value
     */
    public Number getMinimumValue();

    /**
     * Returns the maximum value of this ranking.
     * @return the maximum value
     */
    public Number getMaximumValue();

    /**
     * Normalize <code>value</code> between 0 and 1 using the minimum and the
     * maximum value. For example if <code>value</code> is equal to the maximum,
     * it returns 1.0.
     * @param value the value to normalize
     * @return the normalized value between zero and one
     */
    public float normalize(Number value);

    /**
     * Unnormalize <code>normalizedValue</code> and returns the original element
     * value.
     * @param normalizedValue the value to unnormalize
     * @return the original value of the element
     */
    public Number unnormalize(float normalizedValue);
}
