/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.visualedit;

import java.util.List;
import org.mongkie.visualedit.spi.VisualEdit;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface VisualEditController {

    public void openEditor();

    public void closeEditor();

    public void disableEditor();

    public boolean isEditorOpened();

    public <I extends VisualItem> void edit(I... items);

    /**
     * Returns list of {@link org.mongkie.visualedit.spi.VisualEdit} for the
     * specific item type.
     *
     * @param items visual items to edit
     * @return {@link org.mongkie.visualedit.spi.VisualEdit} list for the item
     * type of given visual items
     */
    public List<VisualEdit> getVisualEdits(VisualItem[] items);
}
