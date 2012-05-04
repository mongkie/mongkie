/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.visualedit.editors;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.FeatureDescriptor;
import java.beans.PropertyEditor;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.util.ImageUtilities;

/**
 * A renderer for string properties, which can also delegate to the property
 * editor's
 * <code>paint()</code>method if possible.
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public final class HtmlRendererInplaceEditor extends JLabel implements InplaceEditor {

    private PropertyEditor editor = null;
    private PropertyEnv env = null;
    private boolean tableUI = false;
    private boolean enabled = true;
    private JLabel htmlLabel = HtmlRenderer.createLabel();
    private JLabel noHtmlLabel = new JLabel();
    private Object value = null;
    private static final boolean isGTK = "GTK".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    public HtmlRendererInplaceEditor(boolean tableUI) {
        this.tableUI = tableUI;
        setOpaque(true);
        ((HtmlRenderer.Renderer) htmlLabel).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
    }

    /**
     * OptimizeIt shows about 12Ms overhead calling back to Component.enable(),
     * so overriding
     */
    @Override
    public void setEnabled(boolean val) {
        enabled = val;
    }

    @Override
    public void setText(String s) {
        if (s != null) {
            if (s.length() > 512) {
                //IZ 44152 - Debugger producing 512K long strings, etc.
                super.setText(makeDisplayble(s.substring(0, 512), getFont()));
            } else {
                super.setText(makeDisplayble(s, getFont()));
            }
        } else {
            super.setText(""); //NOI18N
        }
    }

    /**
     * OptimizeIt shows about 12Ms overhead calling back to Component.enable(),
     * so overriding
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Overridden to do nothing
     */
    @Override
    protected void firePropertyChange(String name, Object old, Object nue) {
        //do nothing
    }

    @Override
    public void validate() {
        //do nothing
    }

    @Override
    public void invalidate() {
        //do nothing
    }

    @Override
    public void revalidate() {
        //do nothing
    }

    @Override
    public void repaint() {
        //do nothing
    }

    @Override
    public void repaint(long tm, int x, int y, int w, int h) {
        //do nothing
    }

    @Override
    public Dimension getPreferredSize() {
        if (getText().length() > 1024) {
            //IZ 44152, avoid excessive calculations when debugger
            //returns its 512K+ strings
            return new Dimension(4196, getMinimumPropPanelHeight());
        }

        Dimension result = super.getPreferredSize();
        result.width = Math.max(result.width, getMinimumPropPanelWidth());

        result.height = Math.max(result.height, getMinimumPropPanelHeight());

        return result;
    }

    @Override
    public void paint(Graphics g) {
        if (editor != null) {
            setEnabled(checkEnabled(this, editor, env));
        }

//        if (editor instanceof ExceptionPropertyEditor) {
//            setForeground(PropUtils.getErrorColor());
//        }

        if ((editor != null) && editor.isPaintable()) {
            delegatedPaint(g);
        } else {
            String htmlDisplayValue = (env == null) ? null
                    : (String) env.getFeatureDescriptor().getValue("htmlDisplayValue"); // NOI18N
            boolean htmlValueUsed = htmlDisplayValue != null;

            JLabel lbl = htmlValueUsed ? htmlLabel : noHtmlLabel;
            String text = htmlValueUsed ? htmlDisplayValue : getText();

            if (text == null) {
                text = ""; // NOI18N
            } else {
                text = makeDisplayble(text, getFont());
            }

            if (htmlValueUsed) {
                // > 1024 = huge strings - don't try to support this as html
                ((HtmlRenderer.Renderer) lbl).setHtml(text.length() < 1024);
            }

            lbl.setFont(getFont());
            lbl.setEnabled(isEnabled());
            lbl.setText(text); //NOI18N

            if (!htmlValueUsed) {
                lbl.putClientProperty("html", null); // NOI18N
            }

            lbl.setIcon(getIcon());
            lbl.setIconTextGap(getIconTextGap());
            lbl.setBounds(getBounds());
            lbl.setOpaque(true);
            if (isGTK) {
                //#127522 - debugger color scheme washed out, very hard to read
                Color bgColor = UIManager.getColor("Tree.textBackground"); //NOI18N
                if (null == bgColor) {
                    bgColor = Color.WHITE;
                }
                lbl.setBackground(bgColor);
            } else {
                lbl.setBackground(getBackground());
            }
            lbl.setForeground(getForeground());
            lbl.setBorder(getBorder());
            if ((isGTK || "com.sun.java.swing.plaf.windows.WindowsLabelUI".equals(lbl.getUI().getClass().getName()))
                    && !isEnabled() && !htmlValueUsed) {
                // the shadow effect from the label was making a problem
                // let's paint the text "manually" in this case
                if (isGTK) {
                    //#127522 - debugger color scheme washed out, very hard to read
                    Color bgColor = UIManager.getColor("Tree.textBackground"); //NOI18N
                    if (null == bgColor) {
                        bgColor = Color.WHITE;
                    }
                    g.setColor(bgColor);
                } else {
                    g.setColor(lbl.getBackground());
                }
                g.fillRect(0, 0, lbl.getWidth(), lbl.getHeight());
                g.setColor(lbl.getForeground());
                Icon icon = (lbl.isEnabled()) ? lbl.getIcon() : lbl.getDisabledIcon();

                FontMetrics fm = g.getFontMetrics();
                Insets insets = lbl.getInsets(paintViewInsets);

                paintViewR.x = insets.left;
                paintViewR.y = insets.top;
                paintViewR.width = lbl.getWidth() - (insets.left + insets.right);
                paintViewR.height = lbl.getHeight() - (insets.top + insets.bottom);

                paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
                paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;

                String clippedText =
                        SwingUtilities.layoutCompoundLabel(
                        lbl, fm, text, icon, lbl.getVerticalAlignment(),
                        lbl.getHorizontalAlignment(),
                        lbl.getVerticalTextPosition(),
                        lbl.getHorizontalTextPosition(),
                        paintViewR, paintIconR, paintTextR, lbl.getIconTextGap());


                if (icon != null) {
                    icon.paintIcon(lbl, g, paintIconR.x, paintIconR.y);
                }
                int textX = paintTextR.x;
                int textY = paintTextR.y + fm.getAscent();
                int mnemonicIndex = lbl.getDisplayedMnemonicIndex();
                // we are here only if the property is read-only (disabled)
                //   --> make the foreground brighter
                Color fg = lbl.getForeground();
                if (isGTK) {
                    //#127522 - debugger color scheme washed out, very hard to read
                    fg = UIManager.getColor("Tree.textForeground"); //NOI18N
                    if (null == fg) {
                        fg = Color.BLACK;
                    }
                }
                Color changedForeground = fg.brighter();
                if (Color.BLACK.equals(fg)) {
                    // for some unknown reason the code with brighter does
                    // not work for me!
                    changedForeground = Color.GRAY;
                }

                g.setColor(changedForeground);
                BasicGraphicsUtils.drawStringUnderlineCharAt(g, clippedText, mnemonicIndex,
                        textX, textY);
            } else {
                lbl.paint(g);
            }
        }

        clear();
    }
    // variables for the hack from the above method:
    private static Insets paintViewInsets = new Insets(0, 0, 0, 0);
    private static Rectangle paintIconR = new Rectangle();
    private static Rectangle paintTextR = new Rectangle();
    private static Rectangle paintViewR = new Rectangle();

    private void delegatedPaint(Graphics g) {
        Color c = g.getColor();

        try {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(getForeground());

            if (!tableUI) {
                //in the panel, give self-painting editors a lowered
                //border so they look like something
                Border b = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
                b.paintBorder(this, g, 0, 0, getWidth(), getHeight());
            }

            Rectangle r = getBounds();

            //XXX May be the source of Rochelle's multiple rows of error 
            //marking misalignment problem...(I do not jest)
//            r.x = (getWidth() > 16) ? ((editor instanceof Boolean3WayEditor) ? 0 : 3) : 0; //align text with other renderers
//            r.width -= ((getWidth() > 16) ? ((editor instanceof Boolean3WayEditor) ? 0 : 3) : 0); //align text with other renderers
            r.x = (getWidth() > 16) ? 3 : 0; //align text with other renderers
            r.width -= ((getWidth() > 16) ? 3 : 0); //align text with other renderers
            r.y = 0;
            editor.paintValue(g, r);
        } finally {
            g.setColor(c);
        }
    }

    @Override
    public void clear() {
        editor = null;
        env = null;
        setIcon(null);
        setOpaque(true);
    }

    @Override
    public void setValue(Object o) {
//        value = o;
//        setText((value instanceof String) ? (String) value : ((value != null) ? value.toString() : null));
        setText("http://" + o);
    }

    @Override
    public void connect(PropertyEditor p, PropertyEnv env) {
        editor = p;
        this.env = env;
        reset();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public KeyStroke[] getKeyStrokes() {
        return null;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return editor;
    }

    @Override
    public PropertyModel getPropertyModel() {
        return null;
    }

    @Override
    public Object getValue() {
        return editor.getValue();
//        return getText();
    }

    public void handleInitialInputEvent(java.awt.event.InputEvent e) {
        //do nothing
    }

    @Override
    public boolean isKnownComponent(Component c) {
        return false;
    }

    @Override
    public void removeActionListener(ActionListener al) {
        //do nothing
    }

    @Override
    public void reset() {
//        setText(editor.getAsText());
        setText("<html><a href=\"" + editor.getValue() + "\">http://" + editor.getValue() + "</a></html>");

        Image i = null;
        FeatureDescriptor fd;

        if (env != null) {
            if (env.getState() == PropertyEnv.STATE_INVALID) {
                setForeground(getErrorColor());
                i = ImageUtilities.loadImage("org/openide/resources/propertysheet/invalid.gif"); //NOI18N
            } else {
                fd = env.getFeatureDescriptor();
                Object o = fd == null ? null : fd.getValue("valueIcon"); //NOI18N

                if (o instanceof Icon) {
                    setIcon((Icon) o);
                } else if (o instanceof Image) {
                    i = (Image) o;
                }
            }
        }

        if (i != null) {
            setIcon(new ImageIcon(i));
        }
    }

    @Override
    public void setPropertyModel(PropertyModel pm) {
        //do nothing
    }

    @Override
    public boolean supportsTextEntry() {
        return false;
    }

    /**
     * Overridden to do nothing
     */
    protected void fireActionPerformed(ActionEvent ae) {
    }

    /**
     * Overridden to do nothing
     */
    protected void fireStateChanged() {
    }

    @Override
    public void addActionListener(ActionListener al) {
        //do nothing
    }

    /**
     * Makes the given String displayble. Probably there doesn't exists perfect
     * solution for all situation. (someone prefer display those squares for
     * undisplayable chars, someone unicode placeholders). So lets try do the
     * best compromise.
     */
    private static String makeDisplayble(String str, Font f) {
        if (null == str) {
            return str;
        }

        if (null == f) {
            f = new JLabel().getFont();
        }

        StringBuilder buf = new StringBuilder(str.length() * 6); // x -> \u1234
        char[] chars = str.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
                // label doesn't interpret tab correctly
                case '\t':
                    buf.append("        "); // NOI18N
                    break;

                case '\n':
                    break;

                case '\r':
                    break;

                case '\b':
                    buf.append("\\b");

                    break; // NOI18N

                case '\f':
                    buf.append("\\f");

                    break; // NOI18N

                default:

                    if ((null == f) || f.canDisplay(c)) {
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N

                        String hex = Integer.toHexString(c);

                        for (int j = 0; j < (4 - hex.length()); j++) {
                            buf.append('0');
                        }

                        buf.append(hex);
                    }
            }
        }

        return buf.toString();
    }

    /**
     * Minimum width for an instance of PropPanel, based on the default font
     * size
     */
    static int getMinimumPropPanelWidth() {
        if (minW == -1) {
            int base = 50;
            minW = Math.round(base * getFontSizeFactor());
        }

        return minW;
    }
    /**
     * Minimum width for a property panel
     */
    static int minW = -1;

    /**
     * Minimum height for an instance of PropPanel based on the default font
     * size
     */
    static int getMinimumPropPanelHeight() {
        if (minH == -1) {
            int base = 18;
            minH = Math.round(base * getFontSizeFactor());
        }

        return minH;
    }
    /**
     * Minimum height for a property panel
     */
    static int minH = -1;

    /**
     * Minimum size for an instance of PropPanel based on the default font size
     */
    static Dimension getMinimumPanelSize() {
        return new Dimension(getMinimumPropPanelWidth(), getMinimumPropPanelHeight());
    }

    /**
     * Get a factor of the difference between the default font size NetBeans
     * uses, and the actual font size which may be different if the -fontsize
     * argument was used on startup.
     */
    static float getFontSizeFactor() {
        if (fsfactor == -1) {
            Font f = UIManager.getFont("controlFont"); //NOI18N

            if (f == null) {
                JLabel jl = new JLabel();
                f = jl.getFont();
            }

            int baseSize = 12; //default font size
            fsfactor = baseSize / f.getSize();
        }

        return fsfactor;
    }
    /**
     * Factor by which default font is larger/smaller than 12 point, used for
     * calculating preferred sizes and compensating for larger font size
     */
    static float fsfactor = -1f;

    /**
     * Check the myriad ways in which a property may be non-editable
     */
    static boolean checkEnabled(Component c, PropertyEditor editor, PropertyEnv env) {
//        if (editor instanceof NoPropertyEditorEditor) {
//            return false;
//        }

        if (env != null) {
            Boolean canEditAsText = (Boolean) env.getFeatureDescriptor().getValue("canEditAsText"); // NOI18N

//            if (!env.isEditable() || Boolean.FALSE.equals(canEditAsText)) {
            if (Boolean.FALSE.equals(canEditAsText)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the color that should be used for text when an error or exception is
     * encountered in displaying a value. Either the look and feel or theme can
     * supply a color via the UIDefaults key nb.errorColor or a default
     * (currently Color.RED) will be used
     */
    static Color getErrorColor() {
        //allow theme.xml to override error color
        Color result = UIManager.getColor("nb.errorForeground"); //NOI18N

        if (result == null) {
            result = Color.RED;
        }

        return result;
    }
}
