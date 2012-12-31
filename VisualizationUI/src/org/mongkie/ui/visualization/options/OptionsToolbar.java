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

import java.awt.Component;
import javax.swing.*;
import org.mongkie.lib.widgets.WidgetUtilities;
import org.mongkie.ui.visualization.options.spi.Options;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.util.Lookup;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class OptionsToolbar extends JToolBar {

    private final MongkieDisplay display;

    public OptionsToolbar(MongkieDisplay display) {
        this.display = display;

        initComponents();

        for (Options o : Lookup.getDefault().lookupAll(Options.class)) {
            if (o.hasTools()) {
                addSeparator();
                for (JComponent c : o.createTools(display)) {
                    c.setFocusable(false);
                    add(c);
                }
            }
        }
        addSeparator();
        add(new InstantSearchPanel(display));
    }

    private void initComponents() {
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        if (WidgetUtilities.isAquaLookAndFeel()) {
            setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
        super.setEnabled(enabled);
    }

    @Override
    public Component add(Component comp) {
        if (comp instanceof JButton) {
            WidgetUtilities.fixButtonUI((JButton) comp);
        }
        return super.add(comp);
    }
}
