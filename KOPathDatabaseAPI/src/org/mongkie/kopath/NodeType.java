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
package org.mongkie.kopath;

import java.awt.Color;
import prefuse.Constants;
import prefuse.util.ColorLib;
import prefuse.visual.VisualItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum NodeType {

    MOLECULE(Constants.SHAPE_ELLIPSE, ColorLib.rgb(130, 130, 255), ColorLib.color(Color.blue)),
    COMPOUND(Constants.SHAPE_ELLIPSE, ColorLib.rgb(90, 215, 90), ColorLib.rgb(0, 100, 0)),
    ENZYME(Constants.SHAPE_RECTANGLE, ColorLib.rgb(0, 200, 200), ColorLib.rgb(0, 128, 128)),
    BIOLOGICAL_PROCESS(Constants.SHAPE_RECTANGLE, ColorLib.rgb(197, 193, 170), ColorLib.gray(0, 220)),
    COMPLEX(Constants.SHAPE_ELLIPSE, ColorLib.rgb(255, 120, 120), ColorLib.color(Color.red)),
    SUPEREX(Constants.SHAPE_ELLIPSE, ColorLib.setAlpha(ColorLib.gray(255), 0), ColorLib.gray(0, 120), ColorLib.gray(0)),
    CONTROLLIE(Constants.SHAPE_ELLIPSE, ColorLib.setAlpha(ColorLib.gray(0), 0));
    private final int shape;
    private int strokeColor = ColorLib.gray(160);
    private int fillColor = ColorLib.gray(160, ALPHA_FILLCOLOR);
    private int textColor = ColorLib.gray(80);
    private static final int ALPHA_FILLCOLOR = 120;

    NodeType(int shape, int strokeColor) {
        this(shape);
        this.strokeColor = strokeColor;
        this.fillColor = ColorLib.setAlpha(strokeColor, ALPHA_FILLCOLOR);
    }

    NodeType(int shape, int strokeColor, int textColor) {
        this(shape);
        this.strokeColor = strokeColor;
        this.fillColor = ColorLib.setAlpha(strokeColor, ALPHA_FILLCOLOR);
        this.textColor = textColor;
    }

    NodeType(int shape, int strokeColor, int fillColor, int textColor) {
        this(shape);
        this.strokeColor = strokeColor;
        this.fillColor = fillColor;
        this.textColor = textColor;
    }

    NodeType(int shape) {
        this.shape = shape;
    }

    public int getShape() {
        return shape;
    }

    public int colorOf(String colorField) {
        if (colorField.equals(VisualItem.STROKECOLOR)) {
            return strokeColor;
        } else if (colorField.equals(VisualItem.FILLCOLOR)) {
            return fillColor;
        } else if (colorField.equals(VisualItem.TEXTCOLOR)) {
            return textColor;
        } else {
            throw new IllegalArgumentException("Unrecognized color field : " + colorField);
        }
    }

    public static int[] paletteOf(String colorField) {
        NodeType[] types = values();
        int[] palette = new int[types.length];
        int i = 0;
        for (NodeType it : types) {
            palette[i++] = it.colorOf(colorField);
        }
        return palette;
    }

    public static String[] names() {
        NodeType[] types = values();
        String[] names = new String[types.length];
        int i = 0;
        for (NodeType it : types) {
            names[i++] = it.toString();
        }
        return names;
    }
}
