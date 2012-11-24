/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.layout.plugins.fruchtermanreingold;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.PrefuseLayout;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class FruchtermanReingold extends PrefuseLayout {

    private static final float SPEED_DIVISOR = 800;
    private static final float AREA_MULTIPLICATOR = 10000;
    //Properties
    private float area;
    private double gravity;
    private double speed;

    FruchtermanReingold(LayoutBuilder<FruchtermanReingold> builder) {
        super(builder);
    }

    @Override
    public void initAlgo() {
        super.initAlgo();
        display.getVisualGraph().getNodeTable().addColumns(ForceVector.SCHEMA);
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetProperties() {
        speed = 2;
        area = 4000;
        gravity = 10;
    }

    @Override
    protected boolean isRunOnce() {
        return false;
    }

    @Override
    public void run(double frac) {
        Tuple[] nodes = display.getVisualGraph().getNodeTable().toArray();
        Tuple[] edges = display.getVisualGraph().getEdgeTable().toArray();

        for (Tuple n : nodes) {
            ForceVector force = ForceVector.get((NodeItem) n);
            force.dx = 0;
            force.dy = 0;
        }

        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.length));

        for (Tuple n1 : nodes) {
            NodeItem N1 = (NodeItem) n1;
            for (Tuple n2 : nodes) {
                NodeItem N2 = (NodeItem) n2;
                if (N1 != N2) {
                    float xDist = (float) (N1.getX() - N2.getX());
                    float yDist = (float) (N1.getY() - N2.getY());
                    float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                    if (dist > 0) {
                        float repulsiveF = k * k / dist;
                        ForceVector force = ForceVector.get(N1);
                        force.dx += xDist / dist * repulsiveF;
                        force.dy += yDist / dist * repulsiveF;
                    }
                }
            }
        }

        for (Tuple e : edges) {
            EdgeItem E = (EdgeItem) e;
            NodeItem Nf = E.getSourceItem();
            NodeItem Nt = E.getTargetItem();
            float xDist = (float) (Nf.getX() - Nt.getX());
            float yDist = (float) (Nf.getY() - Nt.getY());
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            float attractiveF = dist * dist / k;
            if (dist > 0) {
                ForceVector sourceForce = ForceVector.get(Nf);
                ForceVector targetForce = ForceVector.get(Nt);
                sourceForce.dx -= xDist / dist * attractiveF;
                sourceForce.dy -= yDist / dist * attractiveF;
                targetForce.dx += xDist / dist * attractiveF;
                targetForce.dy += yDist / dist * attractiveF;
            }
        }

        // gravity
        for (Tuple n : nodes) {
            NodeItem nitem = (NodeItem) n;
            ForceVector force = ForceVector.get(nitem);
            float d = (float) Math.sqrt(nitem.getX() * nitem.getX() + nitem.getY() * nitem.getY());
            float gf = 0.01f * k * (float) gravity * d;
            force.dx -= gf * nitem.getX() / d;
            force.dy -= gf * nitem.getY() / d;
        }

        // speed
        double _speed = speed * (nodes.length / 50);
        if (_speed < 2) {
            _speed = 2;
        } else if (_speed > 100) {
            _speed = 100;
        }
        for (Tuple n : nodes) {
            ForceVector force = ForceVector.get((NodeItem) n);
            force.dx *= _speed / SPEED_DIVISOR;
            force.dy *= _speed / SPEED_DIVISOR;
        }

        for (Tuple n : nodes) {
            NodeItem nitem = (NodeItem) n;
            ForceVector force = ForceVector.get(nitem);
            float xDist = force.dx;
            float yDist = force.dy;
            float dist = (float) Math.sqrt(force.dx * force.dx + force.dy * force.dy);
            if (dist > 0 && !nitem.isFixed()) {
                float limitedDist = Math.min(maxDisplace * ((float) _speed / SPEED_DIVISOR), dist);
                setX(nitem, null, nitem.getX() + xDist / dist * limitedDist);
                setY(nitem, null, nitem.getY() + yDist / dist * limitedDist);
            }
        }
    }

    private static class ForceVector implements Cloneable {

        float dx = 0;
        float dy = 0;
        float old_dx = 0;
        float old_dy = 0;
        float freeze = 0f;
        private static final String COLUMN = "_fruchtermanReingoldForceVector";
        static final Schema SCHEMA = new Schema();

        static {
            SCHEMA.addColumn(COLUMN, ForceVector.class);
        }

        static ForceVector get(VisualItem item) {
            ForceVector force = (ForceVector) item.get(COLUMN);
            if (force == null) {
                force = new ForceVector();
                item.set(COLUMN, force);
            }
            return force;
        }
    }
}
