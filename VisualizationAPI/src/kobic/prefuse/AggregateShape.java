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

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.*;
import static prefuse.Constants.POLY_TYPE_CURVE;
import static prefuse.Constants.POLY_TYPE_LINE;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public enum AggregateShape {

    CIRCLE(1, "Circle", "Circle that encircles member items of a group",
    new ConvexHullLayout(2),
    new ShapeRenderer() {

        @Override
        protected Shape getRawShape(VisualItem item) {
            float[] polygon = (float[]) item.get(VisualItem.POLYGON);
            if (polygon != null) {
                float[] center = DisplayLib.getCentroid(polygon);
                double radius = DisplayLib.getMaxDistanceFrom(center, polygon);
                double width = radius * 2;
                double x = center[0] - radius;
                double y = center[1] - radius;
                return ellipse(x, y, width, width);
            }
            return null;
        }
    }),
    CONVEX_HULL_LINE(2, "Convex hull of the lines", "Straight lines connecting most outter member items of a group",
    new PolygonLayout(),
    new PolygonRenderer(POLY_TYPE_LINE)),
    CONVEX_HULL_CURVE(3, "Convex hull of the curve", "Convex hull of the curve emcompasses member items of a group",
    new ConvexHullLayout(2),
    new PolygonRenderer(POLY_TYPE_CURVE, 0.15F)),
    RECTANGLE(4, "Rectangle", "Rectangle that encompasses member items of a group",
    new RectangleLayout(5),
    new ShapeRenderer() {

        @Override
        protected Shape getRawShape(VisualItem item) {
            float[] polygon = (float[]) item.get(VisualItem.POLYGON);
            if (polygon != null) {
                double minX = polygon[0];
                double minY = polygon[1];
                double maxX = polygon[2];
                double maxY = polygon[3];
                return rectangle(minX, minY, maxX - minX, maxY - minY);
            }
            return null;
        }
    });
    private final int code;
    private final String name, description;
    private final Layout layout;
    private final AbstractShapeRenderer renderer;

    private AggregateShape(int code, String name, String description, Layout layout, AbstractShapeRenderer renderer) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.layout = layout;
        this.renderer = renderer;
    }

    public boolean layout(AggregateItem aggregate) {
        if (layout.run(aggregate)) {
            aggregate.setValidated(false);
            return true;
        }
        return false;
    }

    public Shape getShape(AggregateItem aggregate) {
        return renderer.getShape(aggregate);
    }

    public static AggregateShape get(int code) {
        return codes.get(code);
    }
    private static final Map<Integer, AggregateShape> codes = new HashMap<Integer, AggregateShape>();

    static {
        for (AggregateShape s : values()) {
            codes.put(s.getCode(), s);
        }
    }

    public static AggregateShape get(String name) {
        return names.get(name);
    }
    private static final Map<String, AggregateShape> names = new HashMap<String, AggregateShape>();

    static {
        for (AggregateShape s : values()) {
            names.put(s.getName(), s);
        }
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    static interface Layout {

        /**
         * Calculate polygons of the given aggregate, then update values of the
         * {@link VisualItem#POLYGON} field if changed.
         * <p/>
         * <b>**Implementation Note</b><br/>
         * <b>DO NOT USE</b> {@link VisualItem#getBounds()} to get bounds of the contained visual items.
         * Instead, use {@link VisualItem#get(java.lang.String)} with the {@link VisualItem#BOUNDS} parameter.
         * <pre>
         * Rectangle2D b = (Rectangle2D) item.get(VisualItem.BOUNDS);
         * </pre>
         *
         * @param aggregate an aggregate to layout
         * @return true if polygons of the given aggregate changed, otherwise false
         */
        public boolean run(AggregateItem aggregate);
    }

    static class PolygonLayout implements Layout {

        private VisualItem[] boundaries, uppers, lowers;

        @Override
        public boolean run(AggregateItem aggregate) {
            int boundariesCount = findBoundaryItems(aggregate);
            int polyLen = boundariesCount * 2;
            float[] poly = (float[]) aggregate.get(VisualItem.POLYGON);
            if (poly == null || poly.length < polyLen) {
                poly = new float[polyLen];
                aggregate.set(VisualItem.POLYGON, poly);
            } else if (poly.length > polyLen) {
                poly[polyLen] = Float.NaN;
            }
            for (int b = 0, p = 0; b < boundariesCount; b++) {
                VisualItem item = boundaries[b];
                poly[p++] = (float) item.getX();
                poly[p++] = (float) item.getY();
            }
            return true;
        }

        private int findBoundaryItems(AggregateItem aggregate) {
            int aggrSize = aggregate.getAggregateSize();
            if (boundaries == null || boundaries.length < aggrSize) {
                boundaries = new VisualItem[aggrSize];
            }
            Iterator<VisualItem> items = aggregate.items();
            int ii = 0;
            while (items.hasNext()) {
                boundaries[ii++] = items.next();
            }
            Arrays.sort(boundaries, 0, aggrSize, LEFT2RIGHT);
            if (aggrSize < 3) {
                return aggrSize;
            }

            if (uppers == null || uppers.length < aggrSize) {
                uppers = new VisualItem[aggrSize];
                lowers = new VisualItem[aggrSize];
            }
            int ucount = 0;
            uppers[ucount++] = boundaries[0];
            uppers[ucount++] = boundaries[1];
            for (int i = 2; i < aggrSize; i++) {
                while ((ucount >= 2) && !isRightTurn(uppers[ucount - 2], uppers[ucount - 1], boundaries[i])) {
                    ucount--;
                }
                uppers[ucount++] = boundaries[i];
            }

            int lcount = 0;
            lowers[lcount++] = boundaries[aggrSize - 1];
            lowers[lcount++] = boundaries[aggrSize - 2];
            for (int i = aggrSize - 3; i >= 0; i--) {
                while ((lcount >= 2) && !isRightTurn(lowers[lcount - 2], lowers[lcount - 1], boundaries[i])) {
                    lcount--;
                }
                lowers[lcount++] = boundaries[i];
            }

            for (int i = 1; i < lcount - 1; i++) {
                uppers[ucount++] = lowers[i];
            }

            System.arraycopy(uppers, 0, boundaries, 0, ucount);

            return ucount;
        }

        private boolean isRightTurn(VisualItem a, VisualItem b, VisualItem c) {
            double det = b.getX() * c.getY() + a.getX() * b.getY() + a.getY() * c.getX();
            det -= b.getY() * c.getX() + a.getX() * c.getY() + a.getY() * b.getX();
            return det < 0;
        }
        private static final Comparator<VisualItem> LEFT2RIGHT = new Comparator<VisualItem>() {

            @Override
            public int compare(VisualItem i1, VisualItem i2) {
                if (i1.getX() < i2.getX()) {
                    return -1;
                }
                if (i1.getX() > i2.getX()) {
                    return +1;
                }
                if (i1.getY() < i2.getY()) {
                    return -1;
                }
                if (i1.getY() > i2.getY()) {
                    return +1;
                }
                return 0;
            }
        };
    }

    static class RectangleLayout implements Layout {

        private final int margin;
        private final Rectangle2D aggregateBounds = new Rectangle2D.Double();

        public RectangleLayout(int margin) {
            this.margin = margin;
        }

        @Override
        public boolean run(AggregateItem aggregate) {
            float[] rect = (float[]) aggregate.get(VisualItem.POLYGON);
            if (rect == null || rect.length < 4) {
                rect = new float[4];
                aggregate.set(VisualItem.POLYGON, rect);
            }
//              DisplayLib.getBounds(aggrItem.items(), margin, aggregateBounds);
            DisplayLib.getUnionBoundsWithoutValidating(aggregate.items(), margin, aggregateBounds);
            rect[0] = (float) aggregateBounds.getMinX();
            rect[1] = (float) aggregateBounds.getMinY();
            rect[2] = (float) aggregateBounds.getMaxX();
            rect[3] = (float) aggregateBounds.getMaxY();
//            aggregate.set(VisualItem.POLYGON, rect);
            return true;
        }
    }

    static class ConvexHullLayout implements Layout {

        private final int margin; // convex hull pixel margin
        private double[] points;   // buffer for computing convex hulls

        public ConvexHullLayout(int margin) {
            this.margin = margin;
        }

        @Override
        public boolean run(AggregateItem aggrItem) {
            int maxSize = 0;
            for (Iterator<AggregateItem> aggrs = aggrItem.getTable().tuples(); aggrs.hasNext();) {
                maxSize = Math.max(maxSize, 4 * 2 * aggrs.next().getAggregateSize());
            }
            if (points == null || maxSize > points.length) {
                points = new double[maxSize];
            }
            // compute and assign convex hull for the aggregate
            int idx = 0;
            if (aggrItem.getAggregateSize() == 0) {
                return false;
            }
            Iterator<VisualItem> items = aggrItem.items();
            VisualItem item;
            while (items.hasNext()) {
                item = items.next();
                if (item.isVisible()) {
                    addPoint(points, idx, item, margin);
                    idx += 2 * 4;
                }
            }
            // if no aggregates are visible, do nothing
            if (idx == 0) {
                return false;
            }
            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(points, idx);
            // prepare viz attribute array
            float[] fhull = (float[]) aggrItem.get(VisualItem.POLYGON);
            if (fhull == null || fhull.length < nhull.length) {
                fhull = new float[nhull.length];
                aggrItem.set(VisualItem.POLYGON, fhull);
            } else if (fhull.length > nhull.length) {
                fhull[nhull.length] = Float.NaN;
            }
            // copy hull values
            for (int j = 0; j < nhull.length; j++) {
                fhull[j] = (float) nhull[j];
            }
//            aggrItem.set(VisualItem.POLYGON, fhull);
            return true;
        }

        private static void addPoint(double[] points, int idx, VisualItem item, int growth) {
            Rectangle2D b = (Rectangle2D) item.get(VisualItem.BOUNDS);
            double minX = (b.getMinX()) - growth, minY = (b.getMinY()) - growth;
            double maxX = (b.getMaxX()) + growth, maxY = (b.getMaxY()) + growth;
            points[idx] = minX;
            points[idx + 1] = minY;
            points[idx + 2] = minX;
            points[idx + 3] = maxY;
            points[idx + 4] = maxX;
            points[idx + 5] = minY;
            points[idx + 6] = maxX;
            points[idx + 7] = maxY;
        }
    }
}
