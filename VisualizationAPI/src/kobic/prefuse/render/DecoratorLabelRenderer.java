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
package kobic.prefuse.render;

import java.awt.Font;
import java.awt.Shape;
import kobic.prefuse.action.layout.DecoratorLayout;
import prefuse.Visualization;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.data.util.AcceptAllColumnProjection;
import prefuse.data.util.ColumnProjection;
import prefuse.data.util.NamedColumnProjection;
import prefuse.render.LabelRenderer;
import prefuse.util.PredicateChain;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;
import prefuse.visual.VisualTable;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class DecoratorLabelRenderer extends LabelRenderer {

    protected final Visualization v;
    protected final String group;
    private PredicateChain invisibilities;
    private ColumnProjection columnFilter;

    protected DecoratorLabelRenderer(VisualTable decorators, String textField, String imageField,
            ColumnProjection columnFilter) {
        super(textField, imageField);
        this.v = decorators.getVisualization();
        this.group = decorators.getGroup();
        this.columnFilter = (columnFilter == null) ? new AcceptAllColumnProjection() : columnFilter;
    }

    public void addInvisibility(String expr) {
        addInvisibility(ExpressionParser.predicate(expr));
    }

    public void addInvisibility(Predicate p) {
        if (invisibilities == null) {
            invisibilities = new PredicateChain();
        }
        invisibilities.add(p, Boolean.TRUE);
    }

    public boolean isInvisible(VisualItem item) {
        return invisibilities != null && invisibilities.get(item) != null;
    }

    public ColumnProjection getColumnFilter() {
        return columnFilter;
    }

    public Visualization getVisualization() {
        return v;
    }

    public String getDecoratorGroup() {
        return group;
    }

    public VisualTable getDecoratorTable() {
        return (VisualTable) v.getVisualGroup(group);
    }

    public void setLabelField(String field) {
        if (getTextField().equals(field)) {
            return;
        }
        super.setTextField(field);
        v.setValue(group, null, VisualItem.VALIDATED, false);
    }

    @Override
    protected Shape getRawShape(VisualItem item) {
        Shape s = null;
        try {
            s = isInvisible(item) ? null : super.getRawShape(item);
        } catch (Exception ex) {
            //TODO: Invalid row...
        }
        return s;
    }

    @Override
    //TODO: Invalid row...
    protected String getText(VisualItem item) {
        return super.getText(((DecoratorItem) item).getDecoratedItem());
    }

    public abstract void runLayout(VisualItem item, int col, DecoratorItem decorator, DecoratorLayout layout);

    public static class Text extends DecoratorLabelRenderer {

        public Text(VisualTable decorators, String textField) {
            super(decorators, textField, null,
                    new NamedColumnProjection(new String[]{
                        VisualItem.TEXTCOLOR, VisualItem.X, VisualItem.Y,
                        VisualItem.SIZE, VisualItem.FONT,
                        VisualItem.BOUNDS
                    }, true));
            setRenderType(RENDER_TYPE_NONE);
        }

        @Override
        protected Font getFont(VisualItem item) {
            return ((DecoratorItem) item).getDecoratedItem().getFont();
        }

        @Override
        protected int getTextColor(VisualItem item) {
            return ((DecoratorItem) item).getDecoratedItem().getTextColor();
        }

        @Override
        protected double getSize(VisualItem item) {
            return ((DecoratorItem) item).getDecoratedItem().getSize();
        }

        @Override
        public void runLayout(VisualItem item, int col, DecoratorItem decorator, DecoratorLayout layout) {
            if (col == VisualItem.IDX_TEXTCOLOR || col == VisualItem.IDX_X || col == VisualItem.IDX_Y) {
                decorator.setValidated(false);
            } else if (col == VisualItem.IDX_SIZE || col == VisualItem.IDX_FONT) {
                decorator.setValidated(false);
                decorator.validateBounds();
            } else if (col == VisualItem.IDX_BOUNDS) {
                layout.run(decorator, item);
            }
        }
    }

    public static class Icon extends DecoratorLabelRenderer {

        public Icon(VisualTable decorators, String imageField) {
            this(decorators, null, imageField);
        }

        public Icon(VisualTable decorators, String textField, String imageField) {
            super(decorators, textField, imageField,
                    new NamedColumnProjection(new String[]{
                        VisualItem.X, VisualItem.Y,
                        VisualItem.SIZE,
                        VisualItem.BOUNDS
                    }, true));
            setRenderType(RENDER_TYPE_NONE);
        }

        @Override
        public void runLayout(VisualItem item, int col, DecoratorItem decorator, DecoratorLayout layout) {
            if (col == VisualItem.IDX_X || col == VisualItem.IDX_Y) {
                decorator.setValidated(false);
            } else if (col == VisualItem.IDX_SIZE) {
                decorator.setValidated(false);
                decorator.validateBounds();
            } else if (col == VisualItem.IDX_BOUNDS) {
                layout.run(decorator, item);
            }
        }
    }
}
