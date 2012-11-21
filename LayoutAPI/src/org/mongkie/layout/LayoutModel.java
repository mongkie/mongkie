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

import java.beans.PropertyChangeListener;
import org.mongkie.layout.spi.Layout;
import org.mongkie.visualization.MongkieDisplay;

/**
 * Layout model contains data and flags relative to the layout execution and
 * user interface. There is one model per {@link MongkieDisplay}
 * <p>
 * <code>PropertyChangeListener</code> can be used to receive events about
 * a change in the model.
 * 
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface LayoutModel {

    public static final String SELECTED_LAYOUT = "selectedLayout";
    public static final String IS_RUNNING = "isRunning";

    public MongkieDisplay getDisplay();

    /**
     * Returns the currently selected layout or <code>null</code> if no
     * layout is selected.
     */
    public Layout getSelectedLayout();

    /**
     * Returns <code>true</code> if a layout is currently running, <code>false</code>
     * otherwise.
     */
    public boolean isRunning();

    /**
     * Add a property change listener for this model. The <code>listener</code>
     * is notified when layout is selected and when running flag change.
     * @param listener a property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove listener.
     * @param listener a property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
