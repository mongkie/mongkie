package org.mongkie.lib.widgets;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXBusyLabel;

/**
 *
 * @author Mathieu Bastian, Youngjun Jang
 */
public class BusyLabel {

    private JScrollPane scrollPane;
    private JXBusyLabel label;
    private final JComponent component;

    public BusyLabel(String text, JScrollPane scrollPane, JComponent component) {
        this(text, 20, 20, scrollPane, component);
    }

    /**
     * Creates a new <code>JXBusyLabel</code> wrapper and set it at the center of <code>scrollPane</code>. When users
     * calls <code>BusyLabel.setBusy(false)</code>, the busyLabel is removed from <code>scrollPanel</code> and
     * <code>component</code> is set instead.
     * @param text the text set to the newly created busyLabel
     * @param scrollPane the scroll Panel where the busyLabel is to be put
     * @param component the component to set in <code>scrollPane</code> when it is not busy anymore
     * @return the newly created <code>JXBusyLabel</code> wrapper
     */
    public BusyLabel(String text, int width, int height, JScrollPane scrollPane, JComponent component) {
        this.scrollPane = scrollPane;
        this.component = component;
        label = new JXBusyLabel(new Dimension(width, height));
        label.setText(text);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public void setBusy(boolean busy) {
        setBusy(busy, component);
    }

    public void setBusy(boolean busy, JComponent comp) {
        if (busy) {
            if (scrollPane != null) {
                WidgetUtilities.setViewportView(scrollPane, label);
            }
            label.setBusy(true);
        } else {
            label.setBusy(false);
            if (scrollPane != null) {
                WidgetUtilities.setViewportView(scrollPane, comp);
            }
        }
    }

    public boolean isBusy() {
        return label.isBusy();
    }
}
