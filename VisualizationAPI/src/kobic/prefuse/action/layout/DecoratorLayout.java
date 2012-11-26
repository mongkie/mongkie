/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
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
package kobic.prefuse.action.layout;

import java.awt.geom.Rectangle2D;
import java.util.*;
import kobic.prefuse.render.DecoratorLabelRenderer;
import prefuse.Visualization;
import prefuse.action.layout.Layout;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DecoratorLayout extends Layout {

    public DecoratorLayout(VisualTable decorators) {
        super(decorators.getGroup());
        setVisualization(decorators.getVisualization());
    }

    public DecoratorLayout(String group) {
        super(group);
    }

    public DecoratorLayout(Visualization v, String group) {
        super(group);
        setVisualization(v);
    }

    @Override
    public void run(double frac) {
        Iterator<DecoratorItem> iter = m_vis.items(m_group);
        while (iter.hasNext()) {
            DecoratorItem decorator = iter.next();
            VisualItem item = decorator.getDecoratedItem();
            run(decorator, item);
        }
    }

    public final void runImmediately() {
        run(0.0D);
    }

    public abstract void run(DecoratorItem decorator, VisualItem item);

    public static final class Center extends DecoratorLayout {

        public Center(VisualTable decorators) {
            super(decorators);
        }

        public Center(String group) {
            super(group);
        }

        public Center(Visualization v, String group) {
            super(v, group);
        }

        @Override
        public void run(DecoratorItem decorator, VisualItem item) {
            Rectangle2D ibounds = item.getBounds();
            double x = ibounds.getCenterX();
            double y = ibounds.getCenterY();
            setX(decorator, item, x);
            setY(decorator, item, y);
        }
    }

    public static class DecoratedTableListener implements TableListener {

        private final Map<DecoratorLabelRenderer, DecoratorLayout> layouts =
                new HashMap<DecoratorLabelRenderer, DecoratorLayout>();

        public void addDecoratorRendererLayout(DecoratorLabelRenderer renderer, DecoratorLayout layout) {
            if (layout == null) {
                layouts.remove(renderer);
            } else {
                layouts.put(renderer, layout);
            }
        }

        @Override
        public void tableChanged(Table table, int start, int end, int col, int type) {
            if (type == EventConstants.UPDATE && col != EventConstants.ALL_COLUMNS && col != VisualItem.IDX_VALIDATED) {
                VisualTable parent = (VisualTable) table;
                for (int r = start; r <= end; ++r) {
                    Set<DecoratorLabelRenderer> renderers = layouts.keySet();
                    for (Iterator<DecoratorLabelRenderer> rendererIter = renderers.iterator(); rendererIter.hasNext();) {
                        DecoratorLabelRenderer renderer = rendererIter.next();
                        VisualTable decorators = renderer.getDecoratorTable();
                        int decoratorRow = decorators.getChildRow(r);
                        if (decoratorRow > -1
                                && (col == VisualItem.IDX_VISIBLE || renderer.getColumnFilter().include(parent.getColumnName(col)))) {
                            DecoratorItem decorator = (DecoratorItem) decorators.getItem(decoratorRow);
                            if (col == VisualItem.IDX_VISIBLE) {
                                decorator.setVisible(parent.getBoolean(r, col));
                            } else if (decorator.isVisible() && !renderer.isInvisible(decorator)) {
                                renderer.runLayout(parent.getItem(r), col, decorator, layouts.get(renderer));
                            }
                        }
                    }
                }
            }
        }

        public DecoratorLayout getLayout(DecoratorLabelRenderer renderer) {
            return layouts.get(renderer);
        }

        public Collection<DecoratorLayout> getLayouts() {
            return layouts.values();
        }
    }
}
