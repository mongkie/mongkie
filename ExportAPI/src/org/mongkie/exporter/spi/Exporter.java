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
package org.mongkie.exporter.spi;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.mongkie.visualization.MongkieDisplay;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface Exporter {

    public boolean execute();

    public boolean cancel();

    public void setDisplay(MongkieDisplay display);

    // UI shown when options button clicked
    public static interface OptionUI<E extends Exporter> {

        public void load(E exporter);

        public JPanel getPanel();

        public void apply(boolean ok);

        public boolean isUIForExporter(Exporter exporter);
    }

    // UI located in the right side of file chooser dialog
    public static interface SettingUI<E extends Exporter> {

        public void load(E exporter);

        public JPanel getPanel();

        public void apply();

        public boolean isUIForExporter(Exporter exporter);
    }

    public static interface MenuUI {

        public String getDisplayName();

        public Icon getIcon();

        public boolean isEnabled();

        public void performAction();
    }
}
