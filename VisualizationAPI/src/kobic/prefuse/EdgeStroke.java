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
package kobic.prefuse;

import java.awt.BasicStroke;
import java.util.HashMap;
import java.util.Map;
import prefuse.util.StrokeLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum EdgeStroke {

    SOLID("Solid Line", StrokeLib.getStroke()),
    DOT("Dot", StrokeLib.getStroke(new float[]{2.0f, 4.0f})),
    DASH_SHORT("Dash I", StrokeLib.getStroke(new float[]{4.0f})),
    DASH_MEDIUM("Dash II", StrokeLib.getStroke(new float[]{10.0f, 10.0f})),
    DASH_LONG("Dash III", StrokeLib.getStroke(new float[]{20.0f, 10.0f})),
    DASH_AND_DOT("Dash and Dot", StrokeLib.getStroke(new float[]{15.0f, 4.0f, 2.0f, 4.0f}));
    private final String name;
    private final BasicStroke stroke;

    private EdgeStroke(String name, BasicStroke stroke) {
        this.name = name;
        this.stroke = stroke;
    }

    public String getName() {
        return name;
    }

    public BasicStroke getStroke() {
        return stroke;
    }

    public static BasicStroke[] getStrokes() {
        return strokes;
    }
    private static final BasicStroke[] strokes = new BasicStroke[values().length];

    static {
        EdgeStroke[] values = values();
        for (int i = 0; i < values.length; i++) {
            strokes[i] = values[i].getStroke();
        }
    }

    public static EdgeStroke get(BasicStroke stroke) {
        EdgeStroke s = strokeMap.get(stroke);
        if (s != null) {
            return s;
        }
        throw new AssertionError("Illegal basic stroke : " + stroke);
    }
    private static final Map<BasicStroke, EdgeStroke> strokeMap = new HashMap<BasicStroke, EdgeStroke>();

    static {
        for (EdgeStroke s : values()) {
            strokeMap.put(s.getStroke(), s);
        }
    }

    public static EdgeStroke get(String name) {
        EdgeStroke s = names.get(name);
        if (s != null) {
            return s;
        }
        throw new AssertionError("Illegal stroke name : " + name);
    }
    private static final Map<String, EdgeStroke> names = new HashMap<String, EdgeStroke>();

    static {
        for (EdgeStroke s : values()) {
            names.put(s.getName(), s);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
