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
package org.mongkie.visualization.selection;

import java.util.Set;
import org.mongkie.visualization.MongkieDisplay;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface SelectionListener {

    /**
     * On selection event, notify with total items have been selected and just selected items.
     *
     * @param display current display
     * @param members total members(items) has been selected until now
     * @param items   items just selected.
     */
    public void selected(Set<VisualItem> members, VisualItem... items);

    /**
     * On un-selection event, notify with total items remaining selected and just unselected items.
     *
     * @param display current display
     * @param members total members(items) remaining selected
     * @param items   items just unselected.
     */
    public void unselected(Set<VisualItem> members, VisualItem... items);

    /**
     * Display to listen change of items selection
     *
     * @return display to listen change of items selection
     */
    public MongkieDisplay getDisplay();
}
