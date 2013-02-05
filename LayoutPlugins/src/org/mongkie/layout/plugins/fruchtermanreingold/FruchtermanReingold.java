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
package org.mongkie.layout.plugins.fruchtermanreingold;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.AbstractLayout;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.lib.widgets.pe.JSliderInplaceEditor;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class FruchtermanReingold extends AbstractLayout {

    private static final float SPEED_DIVISOR = 600; // Originally 800
    private static final float AREA_MULTIPLICATOR = 10000;
    //Properties
    private float area;
    private double gravity;
    private double speed;

    FruchtermanReingold(LayoutBuilder<FruchtermanReingold> builder) {
        super(builder);
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public float getGravity() {
        return (float) gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public float getSpeed() {
        return (float) speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    protected LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        LayoutProperty p;
        try {
            p = new LayoutProperty(
                    NbBundle.getMessage(FruchtermanReingold.class, "param.speed.name"),
                    NbBundle.getMessage(FruchtermanReingold.class, "param.speed.description"),
                    "Parameters",
                    this, float.class, "getSpeed", "setSpeed");
            p.setValue("inplaceEditor", new JSliderInplaceEditor.Float(1, 10, "%- 1.0f",
                    new JSliderInplaceEditor.SliderListener<Float>() {
                        @Override
                        public void valueChanged(PropertyModel model, Float value) {
                            setSpeed(value);
                        }
                    }));
            properties.add(p);
            p = new LayoutProperty(
                    NbBundle.getMessage(FruchtermanReingold.class, "param.gravity.name"),
                    NbBundle.getMessage(FruchtermanReingold.class, "param.gravity.description"),
                    "Parameters",
                    this, float.class, "getGravity", "setGravity");
            p.setValue("inplaceEditor", new JSliderInplaceEditor.Float(1, 30, "%- 1.0f",
                    new JSliderInplaceEditor.SliderListener<Float>() {
                        @Override
                        public void valueChanged(PropertyModel model, Float value) {
                            setGravity(value);
                        }
                    }));
            properties.add(p);
            p = new LayoutProperty(
                    NbBundle.getMessage(FruchtermanReingold.class, "param.area.name"),
                    NbBundle.getMessage(FruchtermanReingold.class, "param.area.description"),
                    "Parameters",
                    this, float.class, "getArea", "setArea");
            p.setValue("inplaceEditor", new JSliderInplaceEditor.Float(100, 50000, "%- 1.0f",
                    new JSliderInplaceEditor.SliderListener<Float>() {
                        @Override
                        public void valueChanged(PropertyModel model, Float value) {
                            setArea(value);
                        }
                    }));
            properties.add(p);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    protected void resetProperties() {
        speed = 4;
        area = 4000;
        gravity = 10;
    }

    @Override
    public void prepare() {
        display.getVisualGraph().getNodeTable().addColumns(ForceVector.SCHEMA);
    }

    @Override
    public void run(int step) {
        TupleSet nodes = display.getVisualGraph().getNodes();
        TupleSet edges = display.getVisualGraph().getEdges();

        for (Iterator<NodeItem> iter = nodes.tuples(); iter.hasNext();) {
            ForceVector force = ForceVector.get(iter.next());
            force.dx = 0;
            force.dy = 0;
        }

        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.getTupleCount()));

        for (Iterator<NodeItem> iter1 = nodes.tuples(); iter1.hasNext();) {
            NodeItem N1 = iter1.next();
            for (Iterator<NodeItem> iter2 = nodes.tuples(); iter2.hasNext();) {
                NodeItem N2 = iter2.next();
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

        for (Iterator<EdgeItem> iter = edges.tuples(); iter.hasNext();) {
            EdgeItem E = iter.next();
            NodeItem source = E.getSourceItem();
            NodeItem target = E.getTargetItem();
            if (source != target) {
                float xDist = (float) (source.getX() - target.getX());
                float yDist = (float) (source.getY() - target.getY());
                float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                float attractiveF = dist * dist / k;
                if (dist > 0) {
                    ForceVector sourceForce = ForceVector.get(source);
                    ForceVector targetForce = ForceVector.get(target);
                    sourceForce.dx -= xDist / dist * attractiveF;
                    sourceForce.dy -= yDist / dist * attractiveF;
                    targetForce.dx += xDist / dist * attractiveF;
                    targetForce.dy += yDist / dist * attractiveF;
                }
            }
        }

        // gravity
        for (Iterator<NodeItem> iter = nodes.tuples(); iter.hasNext();) {
            NodeItem N = iter.next();
            ForceVector force = ForceVector.get(N);
            float d = (float) Math.sqrt(N.getX() * N.getX() + N.getY() * N.getY());
            float gf = 0.01f * k * (float) gravity * d;
            force.dx -= gf * N.getX() / d;
            force.dy -= gf * N.getY() / d;
        }

        // speed
        double _speed = speed + nodes.getTupleCount() / 50;
        for (Iterator<NodeItem> iter = nodes.tuples(); iter.hasNext();) {
            ForceVector force = ForceVector.get(iter.next());
            force.dx *= _speed / SPEED_DIVISOR;
            force.dy *= _speed / SPEED_DIVISOR;
        }

        for (Iterator<NodeItem> iter = nodes.tuples(); iter.hasNext();) {
            NodeItem N = iter.next();
            if (!isEnabled(N)) {
                continue;
            }
            ForceVector force = ForceVector.get(N);
            float xDist = force.dx;
            float yDist = force.dy;
            float dist = (float) Math.sqrt(force.dx * force.dx + force.dy * force.dy);
            if (dist > 0) {
                float limitedDist = Math.min(maxDisplace * ((float) _speed / SPEED_DIVISOR), dist);
                setX(N, N.getX() + xDist / dist * limitedDist);
                setY(N, N.getY() + yDist / dist * limitedDist);
            }
        }
        display.getVisualization().repaint();
    }

    protected boolean isEnabled(NodeItem n) {
        return !n.isFixed();
    }

    @Override
    protected void finish(boolean canceled) {
    }

    @Override
    protected boolean more() {
        return true;
    }

    @Override
    public boolean supportsSelectionOnly() {
        return false;
    }

    private static class ForceVector implements Cloneable {

        float dx = 0f;
        float dy = 0f;
        float old_dx = 0f;
        float old_dy = 0f;
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
