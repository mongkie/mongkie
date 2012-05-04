package org.mongkie.desktop.branding;

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
        UIManager.put("Table.columnSelection", ImageUtilities.loadImageIcon("org/mongkie/desktop/branding/resources/column-selector.png", false));
    }
}
