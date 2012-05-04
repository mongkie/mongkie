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
package kobic.prefuse.display;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import javax.swing.BorderFactory;
import prefuse.Display;
import static prefuse.Visualization.ALL_ITEMS;
import prefuse.controls.ControlAdapter;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class OverviewDisplay extends Display {

    private final Rectangle2D.Double allItemsBounds = new Rectangle2D.Double(0, 0, 1, 1);

    public OverviewDisplay(final Display display) {

        super(display.getVisualization());

//        setSize(OVERVIEW_PREFERRED_SIZE);
        setBackground(Color.WHITE);
//        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setBorder(BorderFactory.createEmptyBorder());
//        setDamageRedraw(false);
//        setHighQuality(true);

//        DisplayLib.fitViewToBounds(this, getVisualization().getBounds(ALL_ITEMS, allItemsBounds), 0);
        DisplayLib.fitViewToBounds(OverviewDisplay.this, allItemsBounds, 0);

        OverviewControl control = new OverviewControl(display, this);
        addItemBoundsListener(control);
        addControlListener(control);
        addPaintListener(control);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (display.getVisibleItemCount() > 0) {
                    DisplayLib.fitViewToBounds(OverviewDisplay.this, getVisualization().getBounds(ALL_ITEMS, allItemsBounds), 0);
                }
            }
        });
        
        // Visualization.repaint() call repaint() of display and overview,
        // therfore below may induce redundant call to overview.repaint()
//        display.addPaintListener(new PaintListener() {
//
//            @Override
//            public void prePaint(Display d, Graphics2D g) {
//            }
//
//            @Override
//            public void postPaint(Display d, Graphics2D g) {
//                repaint();
//            }
//        });
    }

    private static final class OverviewControl extends ControlAdapter
            implements PaintListener, ItemBoundsListener {

        private final Point clickedPoint;
        private final int m_button;
        private boolean buttonPressed = false;
        private final Display display, overview;

        public OverviewControl(Display display, Display overview) {
            this(display, overview, LEFT_MOUSE_BUTTON);
        }

        public OverviewControl(Display display, Display overview, int button) {
            super();
            this.display = display;
            this.overview = overview;
            m_button = button;
            clickedPoint = new Point();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            e.getComponent().setCursor(Cursor.getDefaultCursor());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (UILib.isButtonPressed(e, m_button)) {
                buttonPressed = true;
                clickedPoint.setLocation(e.getX(), e.getY());
                panDisplayTo(clickedPoint);
            }
        }

        @Override
        public void itemPressed(VisualItem item, MouseEvent e) {
            mousePressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == m_button) {
                buttonPressed = false;
                clickedPoint.setLocation(e.getX(), e.getY());
                panDisplayTo(clickedPoint);
            }
        }

        @Override
        public void itemReleased(VisualItem item, MouseEvent e) {
            mouseReleased(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (buttonPressed) {
                clickedPoint.setLocation(e.getX(), e.getY());
                panDisplayTo(clickedPoint);
            }
        }

        @Override
        public void itemDragged(VisualItem item, MouseEvent e) {
            mouseDragged(e);
        }

        private void panDisplayTo(Point point) {
            Point to = overview2display(point);
            display.panTo(to);
            display.repaint();
            overview.repaint();
        }
        private final Point tPoint = new Point();
        private final Point displayPoint = new Point();
        private final Point overviewPoint = new Point();

        private Point overview2display(Point point) {
            try {
                AffineTransform displayT = display.getTransform();
                AffineTransform overviewT = overview.getTransform();
                overviewT.inverseTransform(point, tPoint);   // Overview -> 1:1
                displayT.transform(tPoint, displayPoint);   // 1:1 -> Display
                return displayPoint;
            } catch (NoninvertibleTransformException nitex) {
            }
            return null;
        }

        private Point display2overview(Point point) {
            try {
                AffineTransform displayT = display.getTransform();
                AffineTransform overviewT = overview.getTransform();
                displayT.inverseTransform(point, tPoint);   // Display -> 1:1
                overviewT.transform(tPoint, overviewPoint);   // 1:1 -> Overview
                return overviewPoint;
            } catch (NoninvertibleTransformException nitex) {
            }
            return null;
        }

//        private void fixBounds(Point point) {
//            Point boundsStart = new Point(insets.left, insets.top);
//            Point boundsEnd = new Point(overview.getWidth() - insets.right, overview.getHeight() - insets.bottom);
//
//            // Gets rectangle coordinates.
//            Point rectStart = new Point();
//            Point rectEnd = new Point();
//            getRectangleCoordinates(display, rectStart, rectEnd);
//
//            int dx = (rectEnd.x - rectStart.x) / 2;
//            int dy = (rectEnd.y - rectStart.y) / 2;
//
//            // Check bounds.
//            int fixedX = point.x;
//            int fixedY = point.y;
//
//            if (rectStart.x < boundsStart.x) {
//                fixedX = boundsStart.x + dx;
//            }
//            if (rectStart.y < boundsStart.y) {
//                fixedY = boundsStart.y + dy;
//            }
//            if (rectEnd.x > boundsEnd.x) {
//                fixedX = boundsEnd.x - dx;
//            }
//            if (rectEnd.y > boundsEnd.y) {
//                fixedY = boundsEnd.y - dy;
//            }
//
//            // System.out.println(" >> (" + point.x + "," + point.y + ") ->> (" + fixedX + "," + fixedY + ")");
//
//            point.setLocation(fixedX, fixedY);
//        }
        @Override
        public void postPaint(Display d, Graphics2D g) {
            drawClearRectangle(g);
        }

        @Override
        public void prePaint(Display d, Graphics2D g) {
        }

        private void getRectangleCoordinates(Display display, Point rectangleStart, Point rectangleEnd) {
            // Gets visible rect...
            tmpStart.setLocation(display.getVisibleRect().x, display.getVisibleRect().y);
            tmpEnd.setLocation(display.getVisibleRect().x, display.getVisibleRect().y);
            tmpEnd.translate(display.getVisibleRect().width, display.getVisibleRect().height);
            // ... converts it to the overview coordinates system...
            rectangleStart.setLocation(display2overview(tmpStart));
            rectangleEnd.setLocation(display2overview(tmpEnd));
        }
        private final Point tmpStart = new Point();
        private final Point tmpEnd = new Point();

        private void drawClearRectangle(Graphics2D g) {
            // Updates rectangle coordinates...
            getRectangleCoordinates(display, rectangleStart, rectangleEnd);
            // Prepares drawing area.
            Color before = g.getColor();
            Shape clip = g.getClip();
            Insets insets = overview.getInsets();
            g.setClip(insets.left, insets.top,
                    overview.getWidth() - insets.left - insets.right,
                    overview.getHeight() - insets.bottom - insets.top);
            g.setColor(bgColor);
            // ...and draws it:
            // Top.
            g.fillRect(insets.left, insets.top,
                    overview.getWidth() - insets.left - insets.right,
                    rectangleStart.y - insets.top);
            // Bottom.
            g.fillRect(insets.left, rectangleEnd.y,
                    overview.getWidth() - insets.left - insets.right,
                    overview.getHeight() - insets.bottom - rectangleEnd.y);
            // Left.
            g.fillRect(insets.left, rectangleStart.y,
                    rectangleStart.x - insets.left,
                    rectangleEnd.y - rectangleStart.y);
            // Right.
            g.fillRect(rectangleEnd.x, rectangleStart.y,
                    overview.getWidth() - rectangleEnd.x - insets.right,
                    rectangleEnd.y - rectangleStart.y);
            // Draws the rectangle.
            g.setColor(rectangleColor);
            g.drawRect(rectangleStart.x, rectangleStart.y,
                    rectangleEnd.x - rectangleStart.x,
                    rectangleEnd.y - rectangleStart.y);
            // ... and a cross.
            //drawCross(g, rectangleStart, rectangleEnd);
            // Restore drawing area.
            g.setColor(before);
            g.setClip(clip);
        }
        private final Point rectangleStart = new Point();
        private final Point rectangleEnd = new Point();
        private final Color rectangleColor = ColorLib.getColor(67, 110, 238);
        private final Color bgColor = new Color(rectangleColor.getRed(), rectangleColor.getGreen(), rectangleColor.getBlue(), 60);
//        private void drawCross(Graphics g, Point start, Point end) {
//            Color anterior = g.getColor();
//            g.setColor(Color.RED);
//
//            g.drawLine(start.x, start.y, end.x, end.y);
//            g.drawLine(start.x, end.y, end.x, start.y);
//
//            g.setColor(anterior);
//        }

        @Override
        public void itemBoundsChanged(Display d) {
            d.getItemBounds(m_temp);
            GraphicsLib.expand(m_temp, 25 / d.getScale());

            double dd = m_d / d.getScale();
            double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
            double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
            double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
            double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
            if (xd > dd || yd > dd || wd > dd || hd > dd) {
                m_bounds.setFrame(m_temp);
                DisplayLib.fitViewToBounds(d, m_bounds, 0);
            }
        }
        private final Rectangle2D m_bounds = new Rectangle2D.Double();
        private final Rectangle2D m_temp = new Rectangle2D.Double();
        private final double m_d = 15;
    }
}
