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
package kobic.prefuse;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import prefuse.render.ShapeRenderer;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum NodeShape {

    RECTANGLE(prefuse.Constants.SHAPE_RECTANGLE,
    "Rectangle",
    new SampleFactory() {

        private final Rectangle2D rectangle = new Rectangle2D.Double();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.rectangle(x, y, width, height, rectangle);
        }
    }),
    ELLIPSE(prefuse.Constants.SHAPE_ELLIPSE,
    "Ellipse",
    new SampleFactory() {

        private final Ellipse2D ellipse = new Ellipse2D.Double();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.ellipse(x, y, width, height, ellipse);
        }
    }),
    DIAMOND(prefuse.Constants.SHAPE_DIAMOND,
    "Diamond",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.diamond((float) x, (float) y, (float) height, path);
        }
    }),
    CROSS(prefuse.Constants.SHAPE_CROSS,
    "Cross",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.cross((float) x, (float) y, (float) height, path);
        }
    }),
    STAR(prefuse.Constants.SHAPE_STAR,
    "Star",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.star((float) x, (float) y, (float) height, path);
        }
    }),
    TRIANGLE_UP(prefuse.Constants.SHAPE_TRIANGLE_UP,
    "Triangle Up",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.triangle_up(x, y, height, path);
        }
    }),
    TRIANGLE_DOWN(prefuse.Constants.SHAPE_TRIANGLE_DOWN,
    "Triangle Down",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.triangle_down(x, y, height, path);
        }
    }),
    TRIANGLE_LEFT(prefuse.Constants.SHAPE_TRIANGLE_LEFT,
    "Triangle Left",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.triangle_left(x, y, height, path);
        }
    }),
    TRIANGLE_RIGHT(prefuse.Constants.SHAPE_TRIANGLE_RIGHT,
    "Triangle Right",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.triangle_right(x, y, height, path);
        }
    }),
    HEXAGON(prefuse.Constants.SHAPE_HEXAGON,
    "Hexagon",
    new SampleFactory() {

        private final GeneralPath path = new GeneralPath();

        @Override
        public Shape getSampleShape(double x, double y, double width, double height) {
            return ShapeRenderer.hexagon((float) x, (float) y, (float) height, path);
        }
    });
    private final int code;
    private final String name;
    private final SampleFactory sf;

    private NodeShape(int code, String name, SampleFactory sf) {
        this.code = code;
        this.name = name;
        this.sf = sf;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Shape getSample(double x, double y, double width, double height) {
        return sf.getSampleShape(x, y, width, height);
    }

    public static NodeShape get(int code) {
        NodeShape s = codes.get(code);
        if (s != null) {
            return s;
        }
        throw new AssertionError("Illegal shape code : " + code);
    }
    private static final Map<Integer, NodeShape> codes = new HashMap<Integer, NodeShape>();

    static {
        for (NodeShape s : values()) {
            codes.put(s.getCode(), s);
        }
    }

    public static NodeShape get(String name) {
        NodeShape s = names.get(name);
        if (s != null) {
            return s;
        }
        throw new AssertionError("Illegal shape name : " + name);
    }
    private static final Map<String, NodeShape> names = new HashMap<String, NodeShape>();

    static {
        for (NodeShape s : values()) {
            names.put(s.getName(), s);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private static interface SampleFactory {

        public Shape getSampleShape(double x, double y, double width, double height);
    }
}
