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
import java.util.Iterator;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class HighlightControl extends ControlAdapter {

    protected String activity = null;

    public HighlightControl() {
        this(null);
    }

    public HighlightControl(String activity) {
        this.activity = activity;
    }

    @Override
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem) {
            setHighlighted((NodeItem) item, true);
        } else if (item instanceof EdgeItem) {
            setHighlighted((EdgeItem) item, true);
        } else if (item instanceof AggregateItem) {
            rerunActivity(item.getVisualization());
        }
    }

    @Override
    public void itemExited(VisualItem item, MouseEvent e) {
        if (item instanceof NodeItem) {
            setHighlighted((NodeItem) item, false);
        } else if (item instanceof EdgeItem) {
            setHighlighted((EdgeItem) item, false);
        } else if (item instanceof AggregateItem) {
            rerunActivity(item.getVisualization());
        }
    }

    protected void setHighlighted(NodeItem n, boolean on) {
        Iterator<EdgeItem> edges = n.edges();
        while (edges.hasNext()) {
            EdgeItem e = edges.next();
            if (e.isVisible()) {
                e.setHighlighted(on);
                e.getAdjacentItem(n).setHighlighted(on);
            }
        }
        rerunActivity(n.getVisualization());
    }

    protected void setHighlighted(EdgeItem e, boolean on) {
        if (e.isVisible()) {
            e.setHighlighted(on);
            e.getSourceItem().setHighlighted(on);
            e.getTargetItem().setHighlighted(on);
        }
        rerunActivity(e.getVisualization());
    }

    protected void rerunActivity(Visualization v) {
        if (activity != null) {
            v.rerun(activity);
        }
    }
}
