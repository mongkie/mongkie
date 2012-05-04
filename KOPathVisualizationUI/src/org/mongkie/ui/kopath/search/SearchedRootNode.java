/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.ui.kopath.search;

import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SearchedRootNode extends AbstractNode {

    private final Image icon;

    public SearchedRootNode(Children children) {
        super(children);
        this.icon = null;
    }

    public SearchedRootNode(Image icon) {
        super(Children.LEAF);
        this.icon = icon;
    }

    @Override
    public Image getIcon(int type) {
        return icon == null ? ImageUtilities.loadImage("org/mongkie/ui/kopath/resources/searchCollapse.png") : icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/mongkie/ui/kopath/resources/searchOpen.png");
    }
}
