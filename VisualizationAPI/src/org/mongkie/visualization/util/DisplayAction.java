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
package org.mongkie.visualization.util;

import java.util.List;
import static kobic.prefuse.Constants.*;
import kobic.prefuse.display.DisplayListener;
import org.mongkie.util.SingleContextAction;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import static prefuse.Visualization.*;
import prefuse.data.Graph;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.util.DataLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DisplayAction extends SingleContextAction<MongkieDisplay>
        implements DisplayListener<MongkieDisplay> {

    protected MongkieDisplay display;

    protected DisplayAction() {
        this(Utilities.actionsGlobalContext());
    }

    protected DisplayAction(Lookup lookup) {
        super(MongkieDisplay.class, lookup);
    }

    @Override
    protected final void contextChanged(MongkieDisplay d) {
        super.contextChanged(d);
        MongkieDisplay old = display;
        if (old == d) {
            return;
        }
        if (old != null) {
            old.removeDisplayListener(this);
        }
        if (d != null) {
            d.addDisplayListener(this);
        }
        display = d;
        displayChanged(old, display);
    }

    protected void displayChanged(MongkieDisplay old, MongkieDisplay display) {
    }

    @Override
    public void graphDisposing(MongkieDisplay d, Graph g) {
        setEnabled(false);
    }

    @Override
    public void graphChanged(MongkieDisplay d, Graph g) {
        setEnabled(isEnabled(d));
    }

    public static abstract class Focused<I extends VisualItem> extends DisplayAction implements TupleSetListener {

        public Focused() {
            super();
        }

        public Focused(Lookup lookup) {
            super(lookup);
        }

        @Override
        protected final void displayChanged(MongkieDisplay old, MongkieDisplay display) {
            if (old != null) {
                old.getVisualization().getFocusGroup(FOCUS_ITEMS).removeTupleSetListener(this);
            }
            if (display != null) {
                display.getVisualization().getFocusGroup(FOCUS_ITEMS).addTupleSetListener(this);
            }
        }

        @Override
        public void tupleSetChanged(TupleSet tupleSet, Tuple[] added, Tuple[] removed) {
            setEnabled(tupleSet.getTupleCount() > 0);
        }

        @Override
        protected boolean isEnabled(MongkieDisplay display) {
            return display.getVisualization().items(FOCUS_ITEMS, new InGroupPredicate(getGroup())).hasNext();
        }

        private String getGroup() {
            Class<I> type = getItemType();
            if (type == NodeItem.class) {
                return NODES;
            } else if (type == EdgeItem.class) {
                return EDGES;
            } else if (type == AggregateItem.class) {
                return AGGR_ITEMS;
            }
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        protected abstract Class<I> getItemType();

        @Override
        protected final void performAction(MongkieDisplay display) {
            performAction(display, DataLib.asList(display.getVisualization().items(FOCUS_ITEMS, new InGroupPredicate(getGroup()))));
        }

        protected abstract void performAction(MongkieDisplay display, List<I> items);

        protected final void clearFocusedItems() {
            display.getVisualization().getFocusGroup(FOCUS_ITEMS).clear();
        }
    }
}
