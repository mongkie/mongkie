/*
 Copyright 2008-2010 Gephi
 Authors : Helder Suzuki <heldersuzuki@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.layout.spi;

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import org.mongkie.layout.LayoutProperty;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.data.Schema;
import prefuse.visual.VisualItem;

/**
 * A Layout algorithm should implement the
 * <code>Layout</code> interface to allow the
 * <code>LayoutController</code> to run it properly. <p> See the
 * <code>LayoutBuilder</code> documentation to know how layout should be
 * instanciated. <p> To have fully integrated properties that can be changed in
 * real-time by users, properly define the various
 * <code>LayoutProperty</code> returned by the {@link #getProperties()} method
 * and provide getter and setter for each.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 * @author Youngjun Jang <yjjang@kribb.re.kr>
 * @see LayoutBuilder
 */
public interface Layout {

    public void addPropertyChangeListener(PropertyChangeListener l);

    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * initAlgo() is called to initialize the algorithm (prepare to run).
     */
    public void initAlgo();

    /**
     * Injects the visualization model for the graph this Layout should operate
     * on.
     *
     * @param d the Display having visualization model that the layout is to be
     * working on
     * @author Youngjun Jang <yjjang@kribb.re.kr>
     */
    public void setDisplay(MongkieDisplay d);

    /**
     * Run a step in the algorithm, should be called only if canAlgo() returns
     * true.
     */
    public void goAlgo();

    /**
     * Tests if the algorithm has a next step to go on, called before each pass.
     *
     * @return <code>true</code> if the algorithm has a next step to go
     * on, <code>false</code> otherwise
     */
    public boolean hasNextStep();

    /**
     * Called when the algorithm is finished (canAlgo() returns false).
     */
    public void endAlgo();

    public boolean cancelAlgo();

    /**
     * The properties for this layout.
     *
     * @return the layout properties
     * @throws NoSuchMethodException
     */
    public LayoutProperty[] getProperties();

    /**
     * Resets the properties values to the default values.
     */
    public void resetPropertyValues();

    /**
     * The reference to the LayoutBuilder that instanciated this Layout.
     *
     * @return the reference to the builder that builds this instance
     */
    public LayoutBuilder<? extends Layout> getBuilder();

    public static final class LayoutData implements Cloneable {

        protected double startX, startY;
        protected double endX, endY;
        protected double X, Y;

        @Override
        protected Object clone() throws CloneNotSupportedException {
            LayoutData data = new LayoutData();
            data.startX = startX;
            data.startY = startY;
            data.endX = endX;
            data.endY = endY;
            data.X = X;
            data.Y = Y;
            return data;
        }
        private static final String COLUMN = "_layoutData";
        public static final Schema SCHEMA = new Schema();

        static {
            SCHEMA.addColumn(COLUMN, LayoutData.class);
        }

        public static LayoutData get(VisualItem item) {
            LayoutData data = (LayoutData) item.get(COLUMN);
            if (data == null) {
                data = new LayoutData();
                data.setStartX(item.getStartX());
                data.setStartY(item.getStartY());
                data.setEndX(item.getEndX());
                data.setEndY(item.getEndY());
                data.setX(item.getX());
                data.setY(item.getY());
                item.set(COLUMN, data);
            }
            return data;
        }

        public static <I extends VisualItem> void init(Iterator<I> items) {
            while (items.hasNext()) {
                I item = items.next();
                LayoutData data = LayoutData.get(item);
                data.setStartX(item.getStartX());
                data.setStartY(item.getStartY());
                data.setEndX(item.getEndX());
                data.setEndY(item.getEndY());
                data.setX(item.getX());
                data.setY(item.getY());
            }
        }

        public static <I extends VisualItem> void restore(Iterator<I> items) {
            while (items.hasNext()) {
                I item = items.next();
                LayoutData data = LayoutData.get(item);
                item.setStartX(data.getStartX());
                item.setStartY(data.getStartY());
                item.setEndX(data.getEndX());
                item.setEndY(data.getEndY());
                item.setX(data.getX());
                item.setY(data.getY());
            }
        }

        public double getStartX() {
            return startX;
        }

        public void setStartX(double startX) {
            this.startX = startX;
        }

        public double getStartY() {
            return startY;
        }

        public void setStartY(double startY) {
            this.startY = startY;
        }

        public double getEndX() {
            return endX;
        }

        public void setEndX(double endX) {
            this.endX = endX;
        }

        public double getEndY() {
            return endY;
        }

        public void setEndY(double endY) {
            this.endY = endY;
        }

        public double getX() {
            return X;
        }

        public void setX(double X) {
            this.X = X;
        }

        public double getY() {
            return Y;
        }

        public void setY(double Y) {
            this.Y = Y;
        }
    }
}
