/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
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
package org.mongkie.ui.im;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JButton;
import org.mongkie.ui.visualization.tools.spi.AddonUI;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = AddonUI.class)
public class InteractionsAddonUI implements AddonUI {

    @Override
    public JButton buildActionButton(MongkieDisplay d) {
        return new ActionButton(d);
    }

    @Override
    public ContentPanel buildContentPanel(MongkieDisplay d) {
        return new InteractionsControlPanel(d);
    }

    private final class ActionButton extends JButton {

        private final MongkieDisplay display;

        public ActionButton(MongkieDisplay display) {
            super("Interactions");
            setToolTipText("Interaction Control Panel");
            setFont(new Font("Tahoma", Font.BOLD, 11));
            setOpaque(false);
            setMargin(new Insets(0, 10, 0, 10));
            setFocusPainted(false);
            setPreferredSize(new Dimension(95, 28));
            setContentAreaFilled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/mongkie/ui/im/resources/interaction.png")));
            setBorder(null);
            this.display = display;
        }
    }
}
