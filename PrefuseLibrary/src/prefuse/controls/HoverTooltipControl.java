package prefuse.controls;

import java.awt.event.MouseEvent;
import javax.swing.ToolTipManager;
import prefuse.Display;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class HoverTooltipControl extends ControlAdapter {

    private String[] fields;
    private StringBuilder html = new StringBuilder();
    private String filterGroup;
    private static final int DEFAULT_INITIAL_DELAY = 512;
    private String lineDelimiter = System.getProperty("line.separator");

    public HoverTooltipControl(String filterGroup, Schema s) {
        this.filterGroup = filterGroup;
        fields = new String[s.getColumnCount()];
        for (int i = 0; i < s.getColumnCount(); i++) {
            fields[i] = s.getColumnName(i);
        }

//        ToolTipManager.sharedInstance().setInitialDelay(DEFAULT_INITIAL_DELAY);
//        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
//        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }

    public void setInitialDelay(int delay) {
        ToolTipManager.sharedInstance().setInitialDelay(delay);
    }

    public void setLineDelimiter(String lineDelimiter) {
        this.lineDelimiter = lineDelimiter;
    }
    
    public void setFields(Schema s) {
        fields = new String[s.getColumnCount()];
        for (int i = 0; i < s.getColumnCount(); i++) {
            fields[i] = s.getColumnName(i);
        }
    }

    @Override
    public void itemEntered(VisualItem item, MouseEvent e) {
        if (filterGroup != null && !item.isInGroup(filterGroup)) {
            return;
        }
        html.delete(0, html.length());
        html.append("<html><table width=\"350\">");
        String value;
        Tuple data = item.getSourceTuple();
        for (int i = 0; i < fields.length; i++) {
            if (data.canGetString(fields[i])) {
                value = getString(data, fields[i]);
                html.append("<tr valign='top'><td><b>");
                html.append(getTitle(fields[i])).append("</b></td><td>");
                value = (value != null && value.length() > 0) ? escapeHTML(value).replaceAll(lineDelimiter, "<br>") : "-";
                html.append(value);
                html.append("</td></tr>");
            }
        }
        html.append("</table>");
        html.append("<hr size=1 width=\"97%\"><div align=\"right\"><font color=\"#999999\">esc to close&nbsp;</font></div>");
        html.append("</html>");
        ((Display) e.getSource()).setToolTipText(html.toString());
    }

    protected String getTitle(String field) {
        return field;
    }

    protected String getString(Tuple data, String field) {
        return data.canGetString(field) ? data.getString(field) : null;
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
