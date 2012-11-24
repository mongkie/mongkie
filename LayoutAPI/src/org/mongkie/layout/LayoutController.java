/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.mongkie.layout;

import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.visualization.MongkieDisplay;

/**
 * A LayoutController is the one responsible for controlling the states of
 * the {@link LayoutModel}. It always controls the current workspace model.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>LayoutController lc = Lookup.getDefault().lookup(LayoutController.class);</pre>
 *
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface LayoutController {

    /**
     * Returns the model of the currently selected {@link MongkieDisplay}.
     */
    public LayoutModel getModel();

    public void addModelChangeListener(LayoutModelChangeListener l);

    public void removeModelChangeListener(LayoutModelChangeListener l);

    /**
     * Sets the Layout to execute.
     *
     * @param layout the layout that is to be selected
     */
    public void setLayout(LayoutBuilder builder);

    /**
     * Executes the current Layout.
     */
    public void executeLayout();

    /**
     * Determine if the current Layout can be executed.
     *
     * @return <code>true</code> if the layout is executable.
     */
    public boolean canExecute();

    /**
     * Stop the Layout's execution.
     */
    public void stopLayout();

    /**
     * Determine if the current Layout execution can be stopped.
     * If the current Layout is not running, it generally cannot be stopped.
     *
     * @return <code>true</code> if the layout can be stopped.
     */
    public boolean canStop();
}
