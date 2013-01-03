/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2013 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.ui.visualization.options;

import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.VisualStyle;
import org.openide.util.lookup.ServiceProvider;
import prefuse.visual.NodeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualStyle.Node.UIFactory.class)
public class NodeStyleUIFactory implements VisualStyle.Node.UIFactory {

    @Override
    public VisualStyle.UI<NodeItem> createUI(MongkieDisplay display, VisualStyle<NodeItem> style, Iterable<NodeItem> nodes) {
        return new NodeSettingPanel(display, style, nodes);
    }
}
