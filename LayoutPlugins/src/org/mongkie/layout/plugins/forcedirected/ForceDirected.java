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
package org.mongkie.layout.plugins.forcedirected;

import java.util.ArrayList;
import java.util.List;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.PrefuseLayout;
import org.mongkie.longtask.progress.DeterminateTask;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.DragForce;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RungeKuttaIntegrator;
import prefuse.util.force.SpringForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import static kobic.prefuse.Constants.*;
import prefuse.action.ActionList;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ForceDirected extends PrefuseLayout.Delegation<ForceDirectedLayout> implements DeterminateTask {

    private float gravConst = NBodyForce.DEFAULT_GRAV_CONSTANT;
    private float distance = NBodyForce.DEFAULT_DISTANCE;
    private float theta = NBodyForce.DEFAULT_THETA;
    private float dragCoeff = DragForce.DEFAULT_DRAG_COEFF;
    private float springCoeff = SpringForce.DEFAULT_SPRING_COEFF;
    private float springLength = SpringForce.DEFAULT_SPRING_LENGTH;
    private static final int NUMBER_OF_ITERATIONS = 80;

    ForceDirected(LayoutBuilder<ForceDirected> builder) {
        super(builder);
    }

    @Override
    protected void setLayoutParameters(ForceDirectedLayout layout) {
        ForceSimulator fsim = layout.getForceSimulator();
        for (Force f : fsim.getForces()) {
            int count = f.getParameterCount();
            for (int i = 0; i < count; i++) {
                if (f instanceof NBodyForce) {
                    switch (NBodyForce.ParameterName.valueOf(f.getParameterName(i))) {
                        case GravitationalConstant:
                            f.setParameter(i, getGravConstant());
                            break;
                        case Distance:
                            f.setParameter(i, getDistance());
                            break;
                        case BarnesHutTheta:
                            f.setParameter(i, getTheta());
                            break;
                        default:
                            break;
                    }
                } else if (f instanceof DragForce) {
                    switch (DragForce.ParameterName.valueOf(f.getParameterName(i))) {
                        case DragCoefficient:
                            f.setParameter(i, getDragCoefficient());
                            break;
                        default:
                            break;
                    }
                } else if (f instanceof SpringForce) {
                    switch (SpringForce.ParameterName.valueOf(f.getParameterName(i))) {
                        case SpringCoefficient:
                            f.setParameter(i, getSpringCoefficient());
                            break;
                        case DefaultSpringLength:
                            f.setParameter(i, getSpringLength());
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    @Override
    public LayoutProperty[] createProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String NBODY_FORCE = "NBodyForce";
        final String DRAG_FORCE = "DragForce";
        final String SPRING_FORCE = "SpringForce";

        LayoutProperty p;
        try {
            properties.add(p = LayoutProperty.createProperty(
                    NbBundle.getMessage(getClass(), "ForceDirected.nbodyForce.gravitationalConstant.name"),
                    NbBundle.getMessage(getClass(), "ForceDirected.nbodyForce.gravitationalConstant.description"),
                    NBODY_FORCE,
                    this, Float.TYPE, "getGravConstant", "setGravConstant"));
            p.setValue("inplaceEditor", new ForceInplaceEditor(getDeligateLayout().getForceSimulator().getForces()[0], 0));
            properties.add(p = LayoutProperty.createProperty(
                    NbBundle.getMessage(getClass(), "ForceDirected.nbodyForce.distance.name"),
                    NbBundle.getMessage(getClass(), "ForceDirected.nbodyForce.distance.description"),
                    NBODY_FORCE,
                    this, Float.TYPE, "getDistance", "setDistance"));
            p.setValue("inplaceEditor", new ForceInplaceEditor(getDeligateLayout().getForceSimulator().getForces()[0], 1, "%- 1.0f"));
            properties.add(p = LayoutProperty.createProperty(
                    NbBundle.getMessage(getClass(), "ForceDirected.nbodyForce.theta.name"),
                    NbBundle.getMessage(getClass(), "ForceDirected.nbodyForce.theta.description"),
                    NBODY_FORCE,
                    this, Float.TYPE, "getTheta", "setTheta"));
            p.setValue("inplaceEditor", new ForceInplaceEditor(getDeligateLayout().getForceSimulator().getForces()[0], 2, "%- 1.2f"));
            properties.add(p = LayoutProperty.createProperty(
                    NbBundle.getMessage(getClass(), "ForceDirected.dragForce.dragCoefficient.name"),
                    NbBundle.getMessage(getClass(), "ForceDirected.dragForce.dragCoefficient.description"),
                    DRAG_FORCE,
                    this, Float.TYPE, "getDragCoefficient", "setDragCoefficient"));
            p.setValue("inplaceEditor", new ForceInplaceEditor(getDeligateLayout().getForceSimulator().getForces()[1], 0, "%- 1.2f"));
            properties.add(p = LayoutProperty.createProperty(
                    NbBundle.getMessage(getClass(), "ForceDirected.springForce.springCoefficient.name"),
                    NbBundle.getMessage(getClass(), "ForceDirected.springForce.springCoefficient.description"),
                    SPRING_FORCE,
                    this, Float.TYPE, "getSpringCoefficient", "setSpringCoefficient"));
            p.setValue("inplaceEditor", new ForceInplaceEditor(getDeligateLayout().getForceSimulator().getForces()[2], 0, "%- 1.1e"));
            properties.add(p = LayoutProperty.createProperty(
                    NbBundle.getMessage(getClass(), "ForceDirected.springForce.springLength.name"),
                    NbBundle.getMessage(getClass(), "ForceDirected.springForce.springLength.description"),
                    SPRING_FORCE,
                    this, Float.TYPE, "getSpringLength", "setSpringLength"));
            p.setValue("inplaceEditor", new ForceInplaceEditor(getDeligateLayout().getForceSimulator().getForces()[2], 1));
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public float getGravConstant() {
        return gravConst;
    }

    public void setGravConstant(float gravConst) {
        this.gravConst = gravConst;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public float getDragCoefficient() {
        return dragCoeff;
    }

    public void setDragCoefficient(float dragCoeff) {
        this.dragCoeff = dragCoeff;
    }

    public float getSpringCoefficient() {
        return springCoeff;
    }

    public void setSpringCoefficient(float springCoeff) {
        this.springCoeff = springCoeff;
    }

    public float getSpringLength() {
        return springLength;
    }

    public void setSpringLength(float springLength) {
        this.springLength = springLength;
    }

    @Override
    public void resetProperties() {
        setGravConstant(NBodyForce.DEFAULT_MIN_GRAV_CONSTANT);
        setDistance(NBodyForce.DEFAULT_MIN_DISTANCE);
        setTheta(NBodyForce.DEFAULT_THETA);
        setDragCoefficient(DragForce.DEFAULT_DRAG_COEFF);
        setSpringCoefficient(SpringForce.DEFAULT_SPRING_COEFF / 10 * 4);
        setSpringLength(SpringForce.DEFAULT_SPRING_LENGTH * 4);
    }

    @Override
    protected ForceDirectedLayout createDeligateLayout() {
        ForceSimulator forceSimulator = new ForceSimulator(new RungeKuttaIntegrator());
        forceSimulator.addForce(new NBodyForce(
                NBodyForce.DEFAULT_MIN_GRAV_CONSTANT,
                NBodyForce.DEFAULT_MIN_DISTANCE,
                NBodyForce.DEFAULT_THETA));
        forceSimulator.addForce(new DragForce(DragForce.DEFAULT_DRAG_COEFF));
        forceSimulator.addForce(new SpringForce(
                SpringForce.DEFAULT_SPRING_COEFF / 10 * 4,
                SpringForce.DEFAULT_SPRING_LENGTH * 4));
        return new ForceDirectedLayout(GRAPH, forceSimulator, isRunOnce()) {
            @Override
            public void run(double frac) {
                super.run(frac);
                if (isBigGraph) {
                    handle.progress(++step);
                    if (step > NUMBER_OF_ITERATIONS) {
                        display.cancelLayoutAction();
                    }
                }
            }

            @Override
            protected float getSpringLength(EdgeItem e) {
                if (e.isAggregating()) {
                    return SpringForce.DEFAULT_MIN_SPRING_LENGTH;
                } else {
                    return SpringForce.DEFAULT_MAX_SPRING_LENGTH;
                }
            }

            @Override
            protected void setX(VisualItem item, double x) {
                if (isBigGraph) {
                    LayoutData.get(item).setX(x);
                } else {
                    super.setX(item, x);
                }
            }

            @Override
            protected void setY(VisualItem item, double y) {
                if (isBigGraph) {
                    LayoutData.get(item).setY(y);
                } else {
                    super.setY(item, y);
                }
            }

            @Override
            protected double getX(VisualItem item) {
                return isBigGraph ? LayoutData.get(item).getX() : super.getX(item);
            }

            @Override
            protected double getEndX(VisualItem item) {
                return isBigGraph ? LayoutData.get(item).getEndX() : super.getEndX(item);
            }

            @Override
            protected double getEndY(VisualItem item) {
                return isBigGraph ? LayoutData.get(item).getEndY() : super.getEndY(item);
            }

            @Override
            public void setX(VisualItem item, VisualItem referrer, double x) {
                if (isBigGraph) {
                    LayoutData data = LayoutData.get(item);
                    double sx = data.getX();
                    if (Double.isNaN(sx)) {
                        sx = (referrer != null ? referrer.getX() : x);
                    }
                    data.setStartX(sx);
                    data.setEndX(x);
                    data.setX(x);
                } else {
                    super.setX(item, referrer, x);
                }
            }

            @Override
            public void setY(VisualItem item, VisualItem referrer, double y) {
                if (isBigGraph) {
                    LayoutData data = LayoutData.get(item);
                    double sy = data.getY();
                    if (Double.isNaN(sy)) {
                        sy = (referrer != null ? referrer.getY() : y);
                    }
                    data.setStartY(sy);
                    data.setEndY(y);
                    data.setY(y);
                } else {
                    super.setY(item, referrer, y);
                }
            }
        };
    }

    @Override
    public void initAlgo() {
        super.initAlgo();
        isBigGraph = isBigGraph();
        if (isBigGraph) {
            display.getVisualGraph().getNodeTable().addColumns(LayoutData.SCHEMA);
            LayoutData.init(display.getVisualGraph().getNodeTable().tuples());
            step = 0;
            // Disable other registered layout actions
            ActionList layoutAction = display.getLayoutAction();
            for (int i = 1; i < layoutAction.size(); i++) {
                layoutAction.get(i).setEnabled(false);
            }
        }
    }
    private boolean isBigGraph;
    private int step;

    @Override
    public void endAlgo() {
        super.endAlgo();
        if (isBigGraph) {
            if (!isCanceled()) {
                LayoutData.restore(display.getVisualGraph().getNodeTable().tuples());
                display.getVisualization().repaint();
            }
            // Reenable other registered layout actions
            ActionList layoutAction = display.getLayoutAction();
            for (int i = 1; i < layoutAction.size(); i++) {
                layoutAction.get(i).setEnabled(true);
            }
        }
    }

    @Override
    protected boolean isRunOnce() {
        return false;
    }

    // Following methods will be called *BEFORE* initAlgo()
    @Override
    public boolean isProgressDialogEnabled() {
        return isBigGraph();
    }

    @Override
    public void setTaskHandle(Handle handle) {
        this.handle = handle;
    }
    private Handle handle;

    @Override
    public int getWorkunits() {
        return isBigGraph() ? NUMBER_OF_ITERATIONS : 0;
    }
}
