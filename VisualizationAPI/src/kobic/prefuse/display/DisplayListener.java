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
package kobic.prefuse.display;

import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface DisplayListener<D extends NetworkDisplay> {

    /**
     * Notify the graph of a Display will be disposed.
     *
     * @param d a display, the graph of which will be disposed
     * @param g the graph will be disposed
     */
    public void graphDisposing(D d, Graph g);

    /**
     * Notify a graph model is changed to new one through {@link kobic.prefuse.display.NetworkDisplay#fireGraphChangedEvent()
     * }
     *
     * @param d a display, the graph of which changed
     * @param g a graph newly added
     *
     * @see kobic.prefuse.display.NetworkDisplay#addDisplayListener(kobic.prefuse.display.DisplayListener)
     * @see kobic.prefuse.display.NetworkDisplay#removeDisplayListener(kobic.prefuse.display.DisplayListener)
     */
    public void graphChanged(D d, Graph g);
}
