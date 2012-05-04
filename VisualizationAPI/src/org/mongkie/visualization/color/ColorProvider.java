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
package org.mongkie.visualization.color;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import static kobic.prefuse.Config.*;
import prefuse.util.ColorLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @param <I>
 */
public abstract class ColorProvider<I extends VisualItem> {

    protected final Map<I, Color> textColorMap = new HashMap<I, Color>();
    protected final Map<I, Color> strokeColorMap = new HashMap<I, Color>();
    protected final Map<I, Color> fillColorMap = new HashMap<I, Color>();
    protected ColorProvider<I> parent;

    ColorProvider() {
    }

    public final void setParent(ColorProvider<I> parent) {
        this.parent = parent;
    }

    public ColorProvider<I> getTextColorProvider(I item) {
        return (textColorMap.get(item) != null || getItemTextColor(item) != null) ? this
                : parent != null ? parent.getTextColorProvider(item) : null;
    }

    public ColorProvider<I> getStrokeColorProvider(I item) {
        return (strokeColorMap.get(item) != null || getItemStrokeColor(item) != null) ? this
                : parent != null ? parent.getStrokeColorProvider(item) : null;
    }

    public ColorProvider<I> getFillColorProvider(I item) {
        return (fillColorMap.get(item) != null || getItemFillColor(item) != null) ? this
                : parent != null ? parent.getFillColorProvider(item) : null;
    }

    public Color addTextColor(I item, Color c) {
        return textColorMap.put(item, c);
    }

    public Color addStrokeColor(I item, Color c) {
        return strokeColorMap.put(item, c);
    }

    public Color addFillColor(I item, Color c) {
        return fillColorMap.put(item, c);
    }

    public Color removeTextColor(I item) {
        textColorMap.remove(item);
        return getTextColor(item);
    }

    public Color removeStrokeColor(I item) {
        strokeColorMap.remove(item);
        return getStrokeColor(item);
    }

    public Color removeFillColor(I item) {
        fillColorMap.remove(item);
        return getFillColor(item);
    }

    public void clearTextColors() {
        textColorMap.clear();
    }

    public void clearStrokeColors() {
        strokeColorMap.clear();
    }

    public void clearFillColors() {
        fillColorMap.clear();
    }

    public void clearColors() {
        clearTextColors();
        clearStrokeColors();
        clearFillColors();
    }

    public Color getTextColor(I item) {
        if (ForGroup.getInstance().textColorMap.containsKey(item)) {
            return ForGroup.getInstance().textColorMap.get(item);
        } else if (textColorMap.containsKey(item)) {
            return textColorMap.get(item);
        }
        Color c = getItemTextColor(item);
        return c == null ? (parent != null ? parent.getTextColor(item) : null) : c;
    }

    protected abstract Color getItemTextColor(I item);

    public Color getStrokeColor(I item) {
        if (ForGroup.getInstance().strokeColorMap.containsKey(item)) {
            return ForGroup.getInstance().strokeColorMap.get(item);
        } else if (strokeColorMap.containsKey(item)) {
            return strokeColorMap.get(item);
        }
        Color c = getItemStrokeColor(item);
        return c == null ? parent.getStrokeColor(item) : c;
    }

    protected abstract Color getItemStrokeColor(I item);

    public Color getFillColor(I item) {
        if (ForGroup.getInstance().fillColorMap.containsKey(item)) {
            return ForGroup.getInstance().fillColorMap.get(item);
        } else if (fillColorMap.containsKey(item)) {
            return fillColorMap.get(item);
        }
        Color c = getItemFillColor(item);
        return c == null ? parent.getFillColor(item) : c;
    }

    protected abstract Color getItemFillColor(I item);

    public static class ForGroup extends ColorProvider<VisualItem> {

        private ForGroup() {
        }

        public static ForGroup getInstance() {
            return Holder.instance;
        }

        @Override
        public Color getTextColor(VisualItem item) {
            Color c = textColorMap.get(item);
            return (item instanceof AggregateItem && c == null) ? getItemTextColor(item) : c;
        }

        @Override
        protected Color getItemTextColor(VisualItem item) {
            return ColorLib.getColor(COLOR_DEFAULT_AGGR_TEXT);
        }

        @Override
        public Color getStrokeColor(VisualItem item) {
            Color c = strokeColorMap.get(item);
            return (item instanceof AggregateItem && c == null) ? getItemStrokeColor(item) : c;
        }

        @Override
        protected Color getItemStrokeColor(VisualItem item) {
            return ColorLib.getColor(COLOR_DEFAULT_AGGR_STROKE);
        }

        @Override
        public Color getFillColor(VisualItem item) {
            Color c = fillColorMap.get(item);
            return (item instanceof AggregateItem && c == null) ? getItemFillColor(item) : c;
        }

        @Override
        protected Color getItemFillColor(VisualItem item) {
            return ColorLib.getColor(COLOR_DEFAULT_AGGR_FILL);
        }

        private static class Holder {

            private static final ForGroup instance = new ForGroup();
        }
    }
}
