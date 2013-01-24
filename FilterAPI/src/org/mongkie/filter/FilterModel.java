/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.filter;

import org.mongkie.filter.spi.Filter;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.data.expression.Predicate;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface FilterModel {

    public MongkieDisplay getDisplay();

    public Filter getNodeFilter(String name);

    public Filter getEdgeFilter(String name);

    public Filter getFilter(String group, String name);

    public Predicate getNodeVisiblePredicate();

    public Predicate getEdgeVisiblePredicate();

    public void addModelListener(FilterModelListener l);

    public void removeModelListener(FilterModelListener l);
}
