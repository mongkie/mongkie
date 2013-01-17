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
package org.mongkie.visualization.impl;

import org.mongkie.visualization.search.SearchController;
import org.openide.util.lookup.ServiceProvider;
import prefuse.data.Schema;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = SearchController.class)
public class SearchControllerImpl implements SearchController {

    @Override
    public boolean isStringColumnAvailable(Schema s) {
        for (int i = 0; i < s.getColumnCount(); i++) {
            if (s.getColumnType(i) == String.class) {
                return true;
            }
        }
        return false;
    }
}
