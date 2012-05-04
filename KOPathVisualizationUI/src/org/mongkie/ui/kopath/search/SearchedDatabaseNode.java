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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SearchedDatabaseNode extends AbstractNode {

    private final PathwayDatabase db;
    private final int pathwayCount;

    public SearchedDatabaseNode(PathwayDatabase db, String... genes) {
        super(Children.create(new SearchedPathwayChildFactory(db, genes), true), Lookups.singleton(db));
        setDisplayName(db.getName() + " (" + (pathwayCount = db.countPathway(genes)) + ")");
        setIconBaseWithExtension("org/mongkie/ui/kopath/resources/database.png");
        this.db = db;
    }

    public SearchedDatabaseNode(PathwayDatabase db, String pathway) {
        super(Children.create(new SearchedPathwayChildFactory(db, pathway), true), Lookups.singleton(db));
        setDisplayName(db.getName() + " (" + (pathwayCount = db.countPathway(pathway, true)) + ")");
        setIconBaseWithExtension("org/mongkie/ui/kopath/resources/database.png");
        this.db = db;
    }

    public PathwayDatabase getPathwayDatabase() {
        return db;
    }

    public int getPathwayCount() {
        return pathwayCount;
    }
}
