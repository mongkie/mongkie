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
public class SingleSelectionControl extends FocusControl {

    private final Visualization v;

    public SingleSelectionControl(Visualization v, String filterGroup) {
        this(v, filterGroup, null);
    }

    public SingleSelectionControl(Visualization v, String filterGroup, String activity) {
        super(1, activity);
        this.v = v;
        setFilter(new InGroupPredicate(filterGroup));
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
        if (!filterCheck(item)) {
            if (curFocus != null) {
                v.getFocusGroup(group).removeTuple(curFocus);
                curFocus = null;
                runActivity(v);
            }
            return;
        }
        if (UILib.isButtonPressed(e, button)
                && e.getClickCount() == ccount
                && item != curFocus) {
            TupleSet focusedTupleSet = v.getFocusGroup(group);
            curFocus = item;
            focusedTupleSet.setTuple(item);
            runActivity(v);
        }
    }
}
