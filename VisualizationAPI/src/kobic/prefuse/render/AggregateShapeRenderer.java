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

import java.awt.Shape;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.AggregateShape;
import prefuse.render.AbstractShapeRenderer;
import prefuse.visual.AggregateItem;
import prefuse.visual.VisualItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class AggregateShapeRenderer extends AbstractShapeRenderer {

    @Override
    protected Shape getRawShape(VisualItem item) {
        throw new AssertionError("Should not be called.");
    }

    @Override
    public Shape getShape(VisualItem item) {
        AggregateShape s = AggregateShape.get(item.getShape());
        if (s == null) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unknown aggregate shape code: {0}", item.getShape());
            return null;
        }
        return s.getShape((AggregateItem) item);
    }
}
