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
package kobic.prefuse.controls;

import java.awt.event.MouseEvent;
import javax.swing.ToolTipManager;
import kobic.prefuse.display.DataViewSupport;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class HoverTooltipControl extends ControlAdapter {

    private StringBuilder html = new StringBuilder();
    private String group;
    private String lineSeparator = System.getProperty("line.separator");
    private DataViewSupport view;

    public HoverTooltipControl(String dataGroup, DataViewSupport view) {
        this.group = dataGroup;
        this.view = view;
    }

    public void setInitialDelay(int delay) {
        ToolTipManager.sharedInstance().setInitialDelay(delay);
    }

    public void setLineSeparator(String separator) {
        this.lineSeparator = separator;
    }

    @Override
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (group != null && !item.isInGroup(group)) {
            return;
        }
        Schema s = view.getTooltipSchema();
        html.delete(0, html.length());
        html.append("<html><table width=\"350\">");
        String contents;
        Tuple data = item.getSourceTuple();
        for (int i = 0; i < s.getColumnCount(); i++) {
            String field = s.getColumnName(i);
            if (data.canGetString(field)) {
                contents = view.getStringAt(data, field);
                html.append("<tr valign='top'><td><b>");
                html.append(view.getColumnTitle(field)).append("</b></td><td>");
                contents = (contents != null && contents.length() > 0) ? escapeHTML(contents).replaceAll(lineSeparator, "<br>") : "-";
                html.append(contents);
                html.append("</td></tr>");
            }
        }
        html.append("</table>");
        html.append("<hr size=1 width=\"97%\"><div align=\"right\"><font color=\"#999999\">esc to close&nbsp;</font></div>");
        html.append("</html>");
        ((Display) e.getSource()).setToolTipText(html.toString());
    }

    @Override
    public void itemExited(VisualItem item, MouseEvent e) {
        ((Display) e.getSource()).setToolTipText(null);
    }

    private String escapeHTML(String s) {
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;

                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
}
