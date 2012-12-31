/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
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

import java.awt.BasicStroke;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import kobic.prefuse.Config;
import kobic.prefuse.EdgeArrow;
import kobic.prefuse.EdgeStroke;
import kobic.prefuse.NodeShape;
import kobic.prefuse.data.io.SerializableBasicStroke;
import kobic.prefuse.data.io.SerializableFont;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.color.ColorController;
import org.mongkie.visualization.color.ColorProvider;
import org.openide.util.Lookup;
import prefuse.Visualization;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class VisualStyle<I extends VisualItem> {

    public void apply(Iterator<I> items) {
        boolean redraw = false;
        I it = null;
        while (items.hasNext()) {
            it = items.next();
            for (String field : getVisualFields()) {
                if (apply(field, it)) {
                    redraw = true;
                }
            }
        }
        if (it != null) {
            if (redraw) {
                it.getVisualization().rerun(Visualization.DRAW);
            } else {
                it.getVisualization().repaint();
            }
        }
    }

    public void apply(String field, Iterator<I> items) {
        if (!getVisualFields().contains(field)) {
            throw new IllegalArgumentException("Unknown visual field: " + field);
        }
        boolean redraw = false;
        I it = null;
        while (items.hasNext()) {
            it = items.next();
            if (apply(field, it)) {
                redraw = true;
            }
        }
        if (it != null) {
            if (redraw) {
                it.getVisualization().rerun(Visualization.DRAW);
            } else {
                it.getVisualization().repaint();
            }
        }
    }

    public void apply(I... items) {
        if (items.length < 1) {
            return;
        }
        boolean redraw = false;
        for (String field : getVisualFields()) {
            for (I n : items) {
                if (apply(field, n)) {
                    redraw = true;
                }
            }
        }
        if (redraw) {
            items[0].getVisualization().rerun(Visualization.DRAW);
        } else {
            items[0].getVisualization().repaint();
        }
    }

    public void apply(String field, I... items) {
        if (items.length < 1) {
            return;
        }
        if (!getVisualFields().contains(field)) {
            throw new IllegalArgumentException("Unknown visual field: " + field);
        }
        boolean redraw = false;
        for (I n : items) {
            if (apply(field, n)) {
                redraw = true;
            }
        }
        if (redraw) {
            items[0].getVisualization().rerun(Visualization.DRAW);
        } else {
            items[0].getVisualization().repaint();
        }
    }

    protected abstract Set<String> getVisualFields();

    protected abstract boolean apply(String field, I item);

    public abstract void reset();

    public abstract Object get(String field);

    public abstract void set(String field, Object value);

    public static Node createNodeStyle() {
        return new Node();
    }

    public static Edge createEdgeStyle() {
        return new Edge();
    }

    public static class Node extends VisualStyle<NodeItem> implements Serializable {

        private int shape;
        private double size;
        private int fillColor, strokeColor;
        private transient Font labelFont;
        private int labelColor;
        private static final Set<String> FIELDS = new HashSet<String>(
                Arrays.asList(new String[]{
                    VisualItem.SHAPE,
                    VisualItem.SIZE,
                    VisualItem.FONT,
                    VisualItem.FILLCOLOR,
                    VisualItem.STROKECOLOR,
                    VisualItem.TEXTCOLOR}));

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeObject(new SerializableFont(labelFont));
        }

        private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
            in.defaultReadObject();
            labelFont = ((SerializableFont) in.readObject()).getFont();
        }

        private Node() {
            init();
        }

        private void init() {
            shape = NodeShape.ELLIPSE.getCode();
            size = 1.0D;
            fillColor = Config.COLOR_DEFAULT_NODE_FILL;
            strokeColor = Config.COLOR_DEFAULT_NODE_STROKE;
            labelFont = Config.FONT_DEFAULT_NODETEXT;
            labelColor = Config.COLOR_DEFAULT_NODE_TEXT;
        }

        @Override
        public void reset() {
            init();
        }

        @Override
        protected Set<String> getVisualFields() {
            return FIELDS;
        }

        @Override
        protected boolean apply(String field, NodeItem n) {
            if (field.equals(VisualItem.SHAPE) && n.getShape() != shape) {
                n.setShape(shape);
            } else if (field.equals(VisualItem.SIZE) && n.getSize() != size) {
                PrefuseLib.updateDouble(n, VisualItem.SIZE, size);
            } else if (field.equals(VisualItem.FONT) && !n.getFont().equals(labelFont)) {
                PrefuseLib.update(n, VisualItem.FONT, labelFont);
            } else if (field.equals(VisualItem.FILLCOLOR) && n.getFillColor() != fillColor) {
                Lookup.getDefault().lookup(ColorController.class).getModel((MongkieDisplay) n.getVisualization().getDisplay(0))
                        .getNodeColorProvider().addFillColor(n, ColorLib.getColor(fillColor));
                return true;
            } else if (field.equals(VisualItem.STROKECOLOR) && n.getStrokeColor() != strokeColor) {
                Lookup.getDefault().lookup(ColorController.class).getModel((MongkieDisplay) n.getVisualization().getDisplay(0))
                        .getNodeColorProvider().addStrokeColor(n, ColorLib.getColor(strokeColor));
                return true;
            } else if (field.equals(VisualItem.TEXTCOLOR) && n.getTextColor() != labelColor) {
                Lookup.getDefault().lookup(ColorController.class).getModel((MongkieDisplay) n.getVisualization().getDisplay(0))
                        .getNodeColorProvider().addTextColor(n, ColorLib.getColor(labelColor));
                return true;
            }
            return false;
        }

        @Override
        public void set(String field, Object value) {
            if (field.equals(VisualItem.SHAPE)) {
                shape = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.SIZE)) {
                size = ((Double) value).doubleValue();
            } else if (field.equals(VisualItem.FILLCOLOR)) {
                fillColor = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.STROKECOLOR)) {
                strokeColor = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.TEXTCOLOR)) {
                labelColor = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.FONT)) {
                Font f = (Font) value;
                labelFont = FontLib.getFont(f.getName(), f.getStyle(), f.getSize());
            } else {
                throw new IllegalArgumentException("Unkown visual field: " + field);
            }
        }

        @Override
        public Object get(String field) {
            if (field.equals(VisualItem.SHAPE)) {
                return shape;
            } else if (field.equals(VisualItem.SIZE)) {
                return size;
            } else if (field.equals(VisualItem.FILLCOLOR)) {
                return fillColor;
            } else if (field.equals(VisualItem.STROKECOLOR)) {
                return strokeColor;
            } else if (field.equals(VisualItem.TEXTCOLOR)) {
                return labelColor;
            } else if (field.equals(VisualItem.FONT)) {
                return labelFont;
            }
            throw new IllegalArgumentException("Unkown visual field: " + field);
        }
    }

    public static class Edge extends VisualStyle<EdgeItem> implements Serializable {

        private transient BasicStroke stroke;
        private double width;
        private int strokeColor;
        private int arrow;
        private transient Font labelFont;
        private int labelColor;
        private static final Set<String> FIELDS = new HashSet<String>(
                Arrays.asList(new String[]{
                    VisualItem.STROKE,
                    VisualItem.SHAPE,
                    VisualItem.SIZE,
                    VisualItem.FONT,
                    VisualItem.STROKECOLOR,
                    VisualItem.TEXTCOLOR}));

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeObject(new SerializableFont(labelFont));
            out.writeObject(new SerializableBasicStroke(stroke));
        }

        private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
            in.defaultReadObject();
            labelFont = ((SerializableFont) in.readObject()).getFont();
            stroke = ((SerializableBasicStroke) in.readObject()).getStroke();
        }

        private Edge() {
            init();
        }

        private void init() {
            stroke = EdgeStroke.SOLID.getStroke();
            width = 1.0D;
            strokeColor = Config.COLOR_DEFAULT_EDGE_STROKE;
            arrow = EdgeArrow.ARROW.getCode();
            labelFont = Config.FONT_DEFAULT_EDGETEXT;
            labelColor = Config.COLOR_DEFAULT_EDGE_TEXT;
        }

        @Override
        public void reset() {
            init();
        }

        @Override
        protected Set<String> getVisualFields() {
            return FIELDS;
        }

        @Override
        protected boolean apply(String field, EdgeItem e) {
            if (field.equals(VisualItem.STROKE) && !stroke.equals(e.getStroke())) {
                e.setStroke(stroke);
            } else if (field.equals(VisualItem.SHAPE) && e.getShape() != arrow) {
                e.setShape(arrow);
            } else if (field.equals(VisualItem.SIZE) && e.getSize() != width) {
                PrefuseLib.updateDouble(e, VisualItem.SIZE, width);
            } else if (field.equals(VisualItem.FONT) && !e.getFont().equals(labelFont)) {
                PrefuseLib.update(e, VisualItem.FONT, labelFont);
            } else if (field.equals(VisualItem.STROKECOLOR) && e.getFillColor() != strokeColor) {
                ColorProvider<EdgeItem> colors = Lookup.getDefault().lookup(ColorController.class).getModel((MongkieDisplay) e.getVisualization().getDisplay(0)).getEdgeColorProvider();
                colors.addFillColor(e, ColorLib.getColor(strokeColor));
                colors.addStrokeColor(e, ColorLib.getColor(strokeColor));
                return true;
            } else if (field.equals(VisualItem.TEXTCOLOR) && e.getTextColor() != labelColor) {
                Lookup.getDefault().lookup(ColorController.class).getModel((MongkieDisplay) e.getVisualization().getDisplay(0))
                        .getEdgeColorProvider().addTextColor(e, ColorLib.getColor(labelColor));
                return true;
            }
            return false;
        }

        @Override
        public void set(String field, Object value) {
            if (field.equals(VisualItem.STROKE)) {
                stroke = (BasicStroke) value;
            } else if (field.equals(VisualItem.SHAPE)) {
                arrow = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.SIZE)) {
                width = ((Double) value).doubleValue();
            } else if (field.equals(VisualItem.STROKECOLOR)) {
                strokeColor = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.TEXTCOLOR)) {
                labelColor = ((Integer) value).intValue();
            } else if (field.equals(VisualItem.FONT)) {
                Font f = (Font) value;
                labelFont = FontLib.getFont(f.getName(), f.getStyle(), f.getSize());
            } else {
                throw new IllegalArgumentException("Unkown visual field: " + field);
            }
        }

        @Override
        public Object get(String field) {
            if (field.equals(VisualItem.STROKE)) {
                return stroke;
            } else if (field.equals(VisualItem.SHAPE)) {
                return arrow;
            } else if (field.equals(VisualItem.SIZE)) {
                return width;
            } else if (field.equals(VisualItem.STROKECOLOR)) {
                return strokeColor;
            } else if (field.equals(VisualItem.TEXTCOLOR)) {
                return labelColor;
            } else if (field.equals(VisualItem.FONT)) {
                return labelFont;
            }
            throw new IllegalArgumentException("Unkown visual field: " + field);
        }
    }
}
