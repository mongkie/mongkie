package org.mongkie.desktop.branding;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
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
        } else if (isWindows() && System.getProperty("user.language").equals("ko")) {
            setUIFont(new javax.swing.plaf.FontUIResource("Malgun Gothic", Font.PLAIN, 12));
//            UIManager.put("TabbedPane.font", new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 12));
            UIManager.put("Button.font", new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 12));
            UIManager.put("ToolBar.font", new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 12));
            UIManager.put("ToolTip.font", new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 12));
        }
//        printDiagnosticInfo();
        updateTaskPaneUI();
    }

    private void updateTaskPaneUI() {
        UIManager.put("TaskPaneContainer.useGradient", Boolean.FALSE);
        Color background = new Color(((ColorUIResource) UIManager.get("Panel.background")).getRGB());
        UIManager.put("TaskPaneContainer.background", background);
        UIManager.put("TaskPane.foreground", new ColorUIResource(Color.BLACK));
        UIManager.put("TaskPane.background", background);
        UIManager.put("TaskPane.titleForeground", new ColorUIResource(Color.BLACK));
        UIManager.put("TaskPane.titleBackgroundGradientStart", new ColorUIResource(Color.LIGHT_GRAY));
        UIManager.put("TaskPane.titleBackgroundGradientEnd", new ColorUIResource(Color.LIGHT_GRAY));
        UIManager.put("TaskPane.specialTitleForeground", new ColorUIResource(Color.BLUE));
        UIManager.put("TaskPane.specialTitleBackground", new ColorUIResource(33, 89, 201));
        UIManager.put("TaskPane.borderColor", new ColorUIResource(Color.LIGHT_GRAY));
    }

    private void printDiagnosticInfo() {
        System.out.println("user.language:" + System.getProperty("user.language"));
        System.out.println("user.region:" + System.getProperty("user.region"));
        System.out.println("file.encoding:" + System.getProperty("file.encoding"));
        Locale loc = Locale.getDefault();
        System.out.println("loc:" + loc);
        System.out.println("DisplayLanguage:" + loc.getDisplayLanguage());
        System.out.println("DisplayCountry:" + loc.getDisplayCountry());
        System.out.println(loc.getDisplayLanguage(Locale.US));
        System.out.println(loc.getDisplayCountry(Locale.US));
        String enc = new java.io.OutputStreamWriter(System.out).getEncoding();
        System.out.println("default encoding = " + enc);
        for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            System.out.println(f);
        }
    }

    /**
     * Sets the default font for all Swing components. ex.
     * <pre>
     * setUIFont(new javax.swing.plaf.FontUIResource("Serif", Font.ITALIC, 12));
     * </pre>
     *
     * @param f default font to use
     */
    private void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    private boolean isGTK() {
        return "GTK".equals(UIManager.getLookAndFeel().getID());
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }
}
