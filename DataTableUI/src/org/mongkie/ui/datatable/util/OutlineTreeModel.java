/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.ui.datatable.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import prefuse.data.Table;
import prefuse.data.Tuple;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class OutlineTreeModel implements TreeModel {

    private final Object root = new Object();
    private List<Tuple> tuples;

    public OutlineTreeModel(Table table) {
        this.tuples = new ArrayList<Tuple>(Arrays.asList(table.toArray()));
    }

    public OutlineTreeModel() {
        this.tuples = new ArrayList<Tuple>();
    }

    public void clear() {
        assert SwingUtilities.isEventDispatchThread();
        tuples.clear();
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (root != parent) {
            throw new IllegalStateException("Only the root can be a parent because depth of this tree is 1.");
        }
        return tuples.get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return parent == root ? tuples.size() : 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return node != root;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return tuples.indexOf(child);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }
}
