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
package org.mongkie.visualization.workspace;

import org.mongkie.visualization.MongkieDisplay;

/**
 * When a user click close-button of a display window,
 * {@link #displayDeselected(org.mongkie.visualization.api.MongkieDisplay) } and
 * {@link #displayClosed(org.mongkie.visualization.api.MongkieDisplay) } are called in order.
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface WorkspaceListener {

    /**
     * Notify a display is selected, when a window of the display opened or selected.
     *
     * @param display Newly selected display
     */
    public void displaySelected(MongkieDisplay display);

    /**
     * Notify a display is deselected, when another window of the display opened or selected.
     *
     * @param display Deselected display
     */
    public void displayDeselected(MongkieDisplay display);

    /**
     * Notify a display is closed.
     *
     * @param display   The closed display
     */
    public void displayClosed(MongkieDisplay display);

    /**
     * Notify all display are closed. Namely, no display is available.
     */
    public void displayClosedAll();
}
