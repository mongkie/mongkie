package org.mongkie.desktop.branding;

import java.awt.Color;
import javax.swing.UIManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        UIManager.put("Table.columnSelection", ImageUtilities.loadImageIcon("org/mongkie/desktop/branding/resources/column-selection.png", false));
        if (isGTK()) {
            UIManager.put("Label.disabledForeground", Color.gray);
        }
    }

    private boolean isGTK() {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }
}
