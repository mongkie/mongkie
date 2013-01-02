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
package org.mongkie.ui.visualization.options;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.mongkie.ui.visualization.options.spi.Options;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.VisualStyle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import prefuse.visual.EdgeItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProviders({
    @ServiceProvider(service = Options.class, position = 3),
    @ServiceProvider(service = VisualStyle.Edge.UIFactory.class)
})
public class EdgeOptions implements Options, VisualStyle.Edge.UIFactory {

    @Override
    public String getName() {
        return "Edges";
    }

    @Override
    public List<JComponent> createTools(MongkieDisplay display) {
        return null;
    }

    @Override
    public JPanel createSettingPanel(MongkieDisplay display) {
        return new EdgeSettingPanel(display);
    }

    @Override
    public boolean hasTools() {
        return false;
    }

    @Override
    public boolean hasSettingPanel() {
        return true;
    }

    @Override
    public VisualStyle.UI<EdgeItem> createUI(MongkieDisplay display) {
        return new EdgeSettingPanel(display);
    }
}
