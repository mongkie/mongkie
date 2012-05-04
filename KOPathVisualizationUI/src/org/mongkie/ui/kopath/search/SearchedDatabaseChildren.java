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
package org.mongkie.ui.kopath.search;

import org.mongkie.kopath.spi.PathwayDatabase;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SearchedDatabaseChildren extends Children.Keys<PathwayDatabase> {

    private final String[] genes;
    private final String pathway;

    public SearchedDatabaseChildren(String... genes) {
        for (int i = 0; i < genes.length; i++) {
            genes[i] = genes[i].trim();
        }
        this.genes = genes;
        this.pathway = null;
    }

    public SearchedDatabaseChildren(String pathway) {
        this.genes = null;
        this.pathway = pathway;
    }

    @Override
    protected Node[] createNodes(PathwayDatabase db) {
        return genes == null
                ? new Node[]{new SearchedDatabaseNode(db, pathway)}
                : new Node[]{new SearchedDatabaseNode(db, genes)};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        setKeys(Lookup.getDefault().lookupAll(PathwayDatabase.class));
    }
}
