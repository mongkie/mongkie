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
package org.mongkie.ui.visualmap.partition;

import java.util.List;
import org.mongkie.visualedit.VisualEditController;
import org.mongkie.visualedit.spi.VisualEdit;
import org.mongkie.visualmap.spi.partition.Part;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class VisualEditChildFactory extends ChildFactory<VisualEdit> {

    private Part p;

    public VisualEditChildFactory(Part p) {
        this.p = p;
    }

    @Override
    protected boolean createKeys(List<VisualEdit> edits) {
        edits.addAll(Lookup.getDefault().lookup(VisualEditController.class).getVisualEdits(p.getVisualItems().toArray(new VisualItem[]{})));
        return true;
    }

    @Override
    protected Node createNodeForKey(VisualEdit edit) {
        return new VisualEditNode(p, edit);
    }
}
