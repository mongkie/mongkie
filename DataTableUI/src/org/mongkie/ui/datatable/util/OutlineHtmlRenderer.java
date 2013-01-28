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
package org.mongkie.ui.datatable.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import org.openide.awt.HtmlRenderer;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@Deprecated
public class OutlineHtmlRenderer {

    private final JLabel renderer;

    private OutlineHtmlRenderer() {
        renderer = HtmlRenderer.createLabel();
    }

    private void reset() {
        ((HtmlRenderer.Renderer) renderer).reset();
    }

    public HtmlRenderer.Renderer getHtmlRenderer() {
        return (HtmlRenderer.Renderer) renderer;
    }

    public static OutlineHtmlRenderer getDefault() {
        OutlineHtmlRenderer.Default.INSTANCE.reset();
        return OutlineHtmlRenderer.Default.INSTANCE;
    }

    private static class Default {

        private static OutlineHtmlRenderer INSTANCE = new OutlineHtmlRenderer();
    }

    public Icon getEllipsisIcon() {
        if (ellipsis == null) {
            ellipsis = new EllipsisIcon();
        }
        return ellipsis;
    }
    private EllipsisIcon ellipsis;

    private static class EllipsisIcon implements Icon {

        private boolean larger;

        private EllipsisIcon() {
            Font f = UIManager.getFont("Table.font"); //NOI18N
            larger = (f != null) ? (f.getSize() > 13) : false;
        }

        @Override
        public int getIconHeight() {
            return 12;
        }

        @Override
        public int getIconWidth() {
            return larger ? 16 : 12;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            int w = c.getWidth();
            int h = c.getHeight();
            int ybase = h - 5;
            int pos2 = (w / 2);
            int pos1 = pos2 - 4;
            int pos3 = pos2 + 4;
            Color foreground = UIManager.getColor("PropSheet.customButtonForeground");
            g.setColor((foreground == null) ? c.getForeground() : foreground);
            drawDot(g, pos1 + 1, ybase, larger);
            drawDot(g, pos2, ybase, larger);
            drawDot(g, pos3 - 1, ybase, larger);
        }

        private void drawDot(Graphics g, int x, int y, boolean larger) {
            if (!larger) {
                g.drawLine(x, y, x, y);
            } else {
                g.drawLine(x - 1, y, x + 1, y);
                g.drawLine(x, y - 1, x, y + 1);
            }
        }
    }
}
