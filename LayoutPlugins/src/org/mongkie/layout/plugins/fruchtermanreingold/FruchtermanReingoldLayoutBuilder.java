/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.layout.plugins.fruchtermanreingold;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.mongkie.layout.spi.LayoutBuilder;
import org.mongkie.layout.spi.LayoutBuilder.UI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = LayoutBuilder.class)
public class FruchtermanReingoldLayoutBuilder extends LayoutBuilder<FruchtermanReingold> {

    private final LayoutUI ui = new LayoutUI();

    @Override
    public String getName() {
        return NbBundle.getMessage(FruchtermanReingold.class, "name");
    }

    @Override
    public UI<FruchtermanReingold> getUI() {
        return ui;
    }

    @Override
    public FruchtermanReingold buildLayout() {
        return new FruchtermanReingold(this);
    }

    private static class LayoutUI implements UI<FruchtermanReingold> {

        @Override
        public String getDescription() {
            return NbBundle.getMessage(FruchtermanReingold.class, "description");
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public JPanel getSettingPanel(FruchtermanReingold layout) {
            return null;
        }

        @Override
        public int getQualityRank() {
            return 2;
        }

        @Override
        public int getSpeedRank() {
            return 3;
        }
    }
}
