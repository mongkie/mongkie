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

import org.mongkie.visualization.MongkieDisplay;

/**
 * A ILayout algorithm should implement the <code>ILayout</code> interface to allow the
 * <code>LayoutController</code> to run it properly.
 * <p>
 * See the <code>LayoutBuilder</code> documentation to know how layout should
 * be instanciated.
 * <p>
 * To have fully integrated properties that can be changed in real-time by users,
 * properly define the various <code>LayoutProperty</code> returned by the
 * {@link #getProperties()} method and provide getter and setter for each.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 * @author Youngjun Jang <yjjang@kribb.re.kr>
 * @see LayoutBuilder
 */
public interface ILayout {

    /**
     * initAlgo() is called to initialize the algorithm (prepare to run).
     */
    public void initAlgo();

    /**
     * Injects the visualization model for the graph this ILayout should operate on.
     * @param d the Display having visualization model that the layout is to be working on
     * @author Youngjun Jang <yjjang@kribb.re.kr>
     */
    public void setDisplay(MongkieDisplay d);

    /**
     * Run a step in the algorithm, should be called only if canAlgo() returns
     * true.
     */
    public void goAlgo();

    /**
     * Tests if the algorithm can run, called before each pass.
     * @return              <code>true</code> if the algorithm can run, <code>
     *                      false</code> otherwise
     */
    public boolean canAlgo();

    /**
     * Called when the algorithm is finished (canAlgo() returns false).
     */
    public void endAlgo();

    public void cancel();

    /**
     * The properties for this layout.
     * @return              the layout properties
     * @throws NoSuchMethodException 
     */
    public LayoutProperty[] getProperties();

    /**
     * Resets the properties values to the default values.
     */
    public void resetPropertyValues();

    /**
     * The reference to the LayoutBuilder that instanciated this ILayout.
     * @return              the reference to the builder that builds this instance
     */
    public LayoutBuilder<? extends ILayout> getBuilder();
}
