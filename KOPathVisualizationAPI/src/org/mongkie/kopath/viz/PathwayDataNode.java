/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKIE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.kopath.viz;

import java.applet.AppletContext;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import static kobic.prefuse.Constants.NODES;
import static kobic.prefuse.Constants.PROPKEY_DATAGROUP;
import kobic.prefuse.display.DataViewSupport;
import org.mongkie.datatable.DataNode;
import static org.mongkie.kopath.Config.*;
import org.mongkie.kopath.PublicIdDatabase;
import org.mongkie.kopath.viz.pe.DescriptionPanel;
import org.openide.awt.HtmlRenderer;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Tuple;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class PathwayDataNode extends DataNode {

    private final DataViewSupport viewSupport;
    private final String propertiesName;
    private static AppletContext appletContext = null;
    private static final DescriptionPanel descriptionPanel = new DescriptionPanel();

    public static void setAppletContext(AppletContext appletContext) {
        PathwayDataNode.appletContext = appletContext;
    }

    public PathwayDataNode(VisualItem item, String labelColumn) {
        super(item.getSourceTuple(), labelColumn);
        this.viewSupport = (DataViewSupport) item.getSourceData().getClientProperty(DataViewSupport.PROP_KEY);
        if (item instanceof NodeItem) {
            this.propertiesName = "Entity";
        } else if (item instanceof EdgeItem) {
            this.propertiesName = "Relation";
        } else {
            this.propertiesName = "Properties";
        }
    }

    public PathwayDataNode(Tuple data, String labelColumn) {
        super(data, labelColumn);
        this.viewSupport = (DataViewSupport) data.getTable().getClientProperty(DataViewSupport.PROP_KEY);
        String dataGroup = (String) data.getTable().getClientProperty(PROPKEY_DATAGROUP);
        if (NODES.equals(dataGroup)) {
            this.propertiesName = "Entity";
        } else if (Graph.EDGES.equals(dataGroup)) {
            this.propertiesName = "Relation";
        } else {
            this.propertiesName = "Properties";
        }
    }

    @Override
    protected Set preparePropertySet(Tuple data) {
        Sheet.Set properties = new Sheet.Set();
        properties.setName(propertiesName);
        Property<String> p;
        Schema outline = getPropertySchema(data);
        for (int i = 0; i < outline.getColumnCount(); i++) {
            final String propertyName = outline.getColumnName(i);
            final String value = viewSupport.getStringAt(data, propertyName);
            if (value == null || value.equals("-") || value.isEmpty()) {
                continue;
            }
            p = new Property<String>(data, propertyName, getDisplayName(propertyName), String.class) {

                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    if (propertyName.equals(FIELD_PUBLICID) && getExternalLink(value) != null) {
                        setValue("htmlDisplayValue", "<html><font color=#0000ff><b><u>" + value + "</u></b></font></html>");
                        setValue("valueIcon", ImageUtilities.loadImageIcon("org/mongkie/kopath/viz/resources/external-small.png", false));
                    } else if (propertyName.equals(FIELD_PUBLICIDDBNAME)) {
                        PublicIdDatabase db = PublicIdDatabase.of(value);
                        if (db != null) {
                            setValue("htmlDisplayValue", "<html><font color=#0000ff><b><u>" + db.toString() + "</u></b></font></html>");
                            setValue("valueIcon", ImageUtilities.loadImageIcon("org/mongkie/kopath/viz/resources/external-small.png", false));
                            return db.toString();
                        }
                    } else if (appletContext == null) {
                        setValue("htmlDisplayValue", "<html><b>" + super.getShortDescription() + "</b></html>");
                    }
                    return value;
                }

                @Override
                public boolean canWrite() {
                    return (propertyName.equals(FIELD_PUBLICID) && getExternalLink(value) != null)
                            || (propertyName.equals(FIELD_PUBLICIDDBNAME) && PublicIdDatabase.of(value) != null);
                }

                @Override
                public String getShortDescription() {
                    if (propertyName.equals(FIELD_PUBLICID)) {
                        String link = getExternalLink(value);
                        if (link != null) {
                            return link;
                        }
                    } else if (propertyName.equals(FIELD_PUBLICIDDBNAME)) {
                        PublicIdDatabase db = PublicIdDatabase.of(value);
                        if (db != null) {
                            return db.getDatabaseLink();
                        }
                    }
                    return propertyName.equals(FIELD_DESCRIPTION) ? "..." : super.getShortDescription();
                }

                @Override
                public PropertyEditor getPropertyEditor() {
                    return new PropertyEditorSupport() {

                        @Override
                        public Component getCustomEditor() {
                            if (hasDescription()) {
                                descriptionPanel.setDescription(value);
                                return descriptionPanel;
                            }
                            return null;
                        }

                        @Override
                        public boolean supportsCustomEditor() {
                            return hasDescription();
                        }

                        private boolean hasDescription() {
                            return propertyName.equals(FIELD_DESCRIPTION) || propertyName.equals(FIELD_FULLNAME);
                        }
                    };
                }

                @Override
                public void setValue(String t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                }
            };
//            p.setShortDescription(StringUtilities.escapeHTML(value));
            if ((propertyName.equals(FIELD_PUBLICID) && getExternalLink(value) != null)
                    || (propertyName.equals(FIELD_PUBLICIDDBNAME) && PublicIdDatabase.of(value) != null)) {
                p.setValue("inplaceEditor", InplaceHyperlink.getInstance());
            }
            properties.put(p);
        }
        return properties;
    }

    private String getExternalLink(String id) {
        String link = getTuple().getString(FIELD_URL);
        if (link == null || id.contains(", ")) {
            PublicIdDatabase db = PublicIdDatabase.of(getTuple().getString(FIELD_PUBLICIDDBNAME));
            if (db != null) {
                link = db.getExternalLink(id);
            }
        }
        return link;
    }

    private String getDisplayName(String propertyName) {
        return viewSupport.getColumnTitle(propertyName);
    }

    @Override
    protected Schema getPropertySchema(Tuple data) {
        return viewSupport.getPropertySchema();
    }

    private static class InplaceHyperlink extends JLabel implements InplaceEditor {

        private ActionListener listener = null;
        private PropertyEditor editor = null;
        private String hyperlink = null;
        private PropertyModel model = null;
        private final JLabel htmlLabel;

        public static InplaceHyperlink getInstance() {
            return Holder.inplace;
        }

        private static class Holder {

            private static final InplaceHyperlink inplace = new InplaceHyperlink();
        }

        private InplaceHyperlink() {
            htmlLabel = HtmlRenderer.createLabel();
            ((HtmlRenderer.Renderer) htmlLabel).setHtml(true);
            ((HtmlRenderer.Renderer) htmlLabel).setRenderStyle(HtmlRenderer.STYLE_TRUNCATE);
            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    if (appletContext != null && !appletContext.toString().startsWith("sun.applet.AppletViewer")) {
                        try {
                            appletContext.showDocument(URI.create(hyperlink).toURL(), "_blank");
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else {
                        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                        if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                            try {
                                desktop.browse(URI.create(hyperlink));
                            } catch (IOException ex) {
                                Logger.getLogger(InplaceHyperlink.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            });
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIcon(ImageUtilities.loadImageIcon("org/mongkie/kopath/viz/resources/external-small.png", false));
        }

        @Override
        public void connect(PropertyEditor pe, PropertyEnv env) {
            editor = pe;
            hyperlink = env.getFeatureDescriptor().getShortDescription();
            reset();
        }

        @Override
        public JComponent getComponent() {
            return this;
        }

        @Override
        public void clear() {
            editor = null;
            model = null;
            listener = null;
        }

        @Override
        public Object getValue() {
            return editor.getValue();
        }

        @Override
        public void setValue(Object o) {
//            setText(editor.getAsText());
        }

        @Override
        public boolean supportsTextEntry() {
            return false;
        }

        @Override
        public void reset() {
            ((HtmlRenderer.Renderer) htmlLabel).reset();
            setToolTipText(hyperlink);
        }

        @Override
        public void paint(Graphics g) {
            htmlLabel.setFont(getFont());
            htmlLabel.setEnabled(isEnabled());
            htmlLabel.setText("<html><font color=#0000ff><b><u>" + getValue() + "</u></b></font></html>");
            htmlLabel.setIcon(getIcon());
            htmlLabel.setIconTextGap(getIconTextGap());
            htmlLabel.setBounds(getBounds());
            htmlLabel.setOpaque(true);
            htmlLabel.setBackground(getBackground());
            htmlLabel.setForeground(getForeground());
            htmlLabel.setBorder(getBorder());
            htmlLabel.paint(g);
        }

        @Override
        public void addActionListener(ActionListener l) {
            listener = l;
        }

        @Override
        public void removeActionListener(ActionListener l) {
            if (listener == l) {
                listener = null;
            }
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
            return model;
        }

        @Override
        public void setPropertyModel(PropertyModel pm) {
            this.model = pm;
        }

        @Override
        public boolean isKnownComponent(Component c) {
            return c == this || isAncestorOf(c);
        }
    }
}
