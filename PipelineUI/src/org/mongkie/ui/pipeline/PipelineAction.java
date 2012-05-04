package org.mongkie.ui.pipeline;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.JXHyperlink;

/**
 *
 * @author yjjang
 */
public class PipelineAction extends AbstractAction {

    private final ButtonUI button;

    public PipelineAction(String name, Icon icon, String tooltip) {
        super(name, icon);
        button = new ButtonUI(this, tooltip);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(">> " + button.getText());
    }

    public JButton getButton() {
        return button;
    }

    private static class ButtonUI extends JXHyperlink {

        public ButtonUI(Action action, String tooltip) {
            super(action);
            setClickedColor(new Color(0, 51, 255));
            setIcon((Icon) action.getValue(Action.SMALL_ICON));
            setText((String) action.getValue(Action.NAME));
            setToolTipText(tooltip);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setFocusPainted(false);
//            setHorizontalTextPosition(SwingConstants.CENTER);
//            setVerticalTextPosition(SwingConstants.BOTTOM);
        }
    }
}
