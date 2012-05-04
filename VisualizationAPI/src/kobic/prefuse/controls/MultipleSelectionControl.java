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
package kobic.prefuse.controls;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import prefuse.Visualization;
import prefuse.controls.FocusControl;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MultipleSelectionControl extends FocusControl {

    private final Visualization v;

    public MultipleSelectionControl(Visualization v) {
        this(v, null);
    }

    public MultipleSelectionControl(Visualization v, String filterGroup) {
        this(v, filterGroup, null);
    }

    public MultipleSelectionControl(Visualization v, String filterGroup, String activity) {
        super(1, activity);
        this.v = v;
        setFilter(new InGroupPredicate(filterGroup));
    }

    public MultipleSelectionControl setClicks(int clicks) {
        ccount = clicks;
        return this;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!UILib.isButtonPressed(e, button)) {
            return;
        }
        curFocus = null;
        v.getFocusGroup(group).clear();
        runActivity(v);
    }

    @Override
    public void itemClicked(VisualItem item, MouseEvent e) {
        if (v != item.getVisualization()
                || !UILib.isButtonPressed(e, button) || e.getClickCount() != ccount) {
            return;
        }
        TupleSet focusedTupleSet = v.getFocusGroup(group);
        if (!filterCheck(item)) {
            if (curFocus != null) {
                List<VisualItem> unfocusedItems = new ArrayList<VisualItem>();
                for (Iterator<VisualItem> focusedItems = focusedTupleSet.tuples(getFilter()); focusedItems.hasNext();) {
                    unfocusedItems.add(focusedItems.next());
                }
                for (VisualItem unfocused : unfocusedItems) {
                    focusedTupleSet.removeTuple(unfocused);
                }
                curFocus = null;
            }
        } else if (e.isControlDown()) {
            if (focusedTupleSet.containsTuple(item)) {
                focusedTupleSet.removeTuple(item);
                if (item == curFocus) {
                    curFocus = null;
                    Iterator<VisualItem> focusedItems = focusedTupleSet.tuples(getFilter());
                    while (focusedItems.hasNext()) {
                        curFocus = focusedItems.next();
                    }
                }
            } else {
                focusedTupleSet.addTuple(item);
                curFocus = item;
            }
        } else {
            focusedTupleSet.setTuple(item);
            curFocus = item;
        }
        runActivity(v);
    }
}
