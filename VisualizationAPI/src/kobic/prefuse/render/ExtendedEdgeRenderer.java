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

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import kobic.prefuse.EdgeArrow;
import prefuse.render.EdgeRenderer;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ExtendedEdgeRenderer extends EdgeRenderer {

    public ExtendedEdgeRenderer() {
        super(false, true);
    }

    @Override
    protected Shape getRawShape(VisualItem item) {
        EdgeItem edge = (EdgeItem) item;
        VisualItem source = edge.getSourceItem();
        VisualItem target = edge.getTargetItem();
        if (source == target) {
            m_curWidth = (float) (m_width * getLineWidth(item));
            Rectangle2D itemBounds = source.getBounds();
            getAlignedPoint(m_tmpPoints[0], itemBounds, m_xAlign1, m_yAlign1);
            float radius = (float) itemBounds.getHeight();
            ellipse.setFrame(m_tmpPoints[0].getX(), m_tmpPoints[0].getY() - radius, radius, radius);
            Rectangle2D.intersect(itemBounds, ellipse.getBounds2D(), intersect);
            boolean scaled = m_curWidth > m_width;
            float scale = m_curWidth / 4;
            float arrowHeight = scaled ? EdgeArrow.get(edge.getShape()).getGap() * scale : EdgeArrow.get(edge.getShape()).getGap();
            arrowHeight = (float) Math.sqrt(arrowHeight * arrowHeight / 2);
            float arcStartX = (float) m_tmpPoints[0].getX(), arcStartY = (float) m_tmpPoints[0].getY();
            float arrowEndX = (float) intersect.getMinX(), arrowEndY = (float) intersect.getMinY();
            arc.setArc(ellipse.getBounds2D(), 0, 0, Arc2D.OPEN);
            arc.setAngles(arcStartX, arcStartY, arrowEndX + arrowHeight, arrowEndY - arrowHeight);
            float arcEndX = (float) arc.getEndPoint().getX(), arcEndY = (float) arc.getEndPoint().getY();
            m_curArrow = getArrowTrans(arcEndX, arcEndY, arrowEndX, arrowEndY, m_curWidth, scaled ? scale * 0.126 / source.getSize() : 0).createTransformedShape(getArrowHead(edge));
            return arc;
        }
        return super.getRawShape(item);
    }
    private final Ellipse2D ellipse = new Ellipse2D.Float();
    private final Rectangle2D intersect = new Rectangle2D.Float();
    private final Arc2D arc = new Arc2D.Float();

    @Override
    protected Polygon getArrowHead(EdgeItem e) {
        return EdgeArrow.get(e.getShape()).getArrowHead();
    }

    @Override
    protected void adjustLineEndByArrowHead(EdgeItem e, Point2D end) {
        end.setLocation(0, -EdgeArrow.get(e.getShape()).getGap());
    }
}
