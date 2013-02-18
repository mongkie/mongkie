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
        } else if (isWindows()) {
            UIManager.put("windowTitleFont", new javax.swing.plaf.FontUIResource("Segoe UI", Font.BOLD, 11));
            UIManager.put("controlFont", new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 12));
            if (System.getProperty("user.language").equals("ko")) {
                setUIFont(new javax.swing.plaf.FontUIResource("Malgun Gothic", Font.PLAIN, 12));
            }
            UIManager.put("Button.font", new javax.swing.plaf.FontUIResource("Tahoma", Font.PLAIN, 12));
            UIManager.put("ToolTip.font", new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 12));
            UIManager.put("TabbedPane.font", new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 12));
            UIManager.put("windowTitleFont", new javax.swing.plaf.FontUIResource("Segoe UI", Font.BOLD, 11));
            UIManager.put("controlFont", new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 12));
        }
//        printDiagnosticInfo();
        updateTaskPaneUI();
    }

    private void updateTaskPaneUI() {
        UIManager.put("TaskPaneContainer.useGradient", Boolean.FALSE);
        UIManager.put("TaskPaneContainer.background", new Color(((ColorUIResource) UIManager.get("Panel.background")).getRGB()));
        UIManager.put("TaskPane.foreground", UIManager.getColor("Panel.foreground"));
        UIManager.put("TaskPane.background", UIManager.getColor("Panel.background"));
        UIManager.put("TaskPane.titleForeground", UIManager.getColor("Panel.foreground"));
        UIManager.put("TaskPane.titleBackgroundGradientStart", UIManager.getColor("InternalFrame.borderShadow"));
        UIManager.put("TaskPane.titleBackgroundGradientEnd", UIManager.getColor("InternalFrame.borderShadow"));
        UIManager.put("TaskPane.titleOver", UIManager.getColor("InternalFrame.borderHighlight"));
        UIManager.put("TaskPane.specialTitleForeground", new ColorUIResource(Color.ORANGE));
        UIManager.put("TaskPane.specialTitleBackground", UIManager.getColor("InternalFrame.borderShadow"));
        UIManager.put("TaskPane.specialTitleOver", UIManager.getColor("InternalFrame.borderHighlight"));
        UIManager.put("TaskPane.borderColor", UIManager.getColor("InternalFrame.borderShadow"));
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
