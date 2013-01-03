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
package org.mongkie.ui.im;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.mongkie.im.InteractionController;
import org.mongkie.im.spi.InteractionSource;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.util.VisualStyle;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import prefuse.data.Table;
import prefuse.util.DataLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SettingsPanel extends javax.swing.JPanel {

    private final InteractionSource is;
    private final VisualStyle.UI<EdgeItem> edgeStyleUI;
    private final InteractionSource.SettingUI settings;
    private Map<VisualStyle<EdgeItem>, List<EdgeItem>> edgeOldStyles;
    private static final Iterator<VisualItem> NULL_ITEMS = new Iterator<VisualItem>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public VisualItem next() {
            throw new IllegalStateException();
        }

        @Override
        public void remove() {
        }
    };

    /**
     * Creates new form SettingsPanel
     */
    SettingsPanel(final MongkieDisplay d, final InteractionSource is) {
        initComponents();
        this.is = is;
        settings = is.getSettingUI();
        if (settings != null) {
            tabbedPane.addTab("Settings", ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/settings.png", false), settings.getPanel());
        }
        edgeStyleUI = Lookup.getDefault().lookup(VisualStyle.Edge.UIFactory.class).createUI(d,
                new Iterable<EdgeItem>() {
                    @Override
                    public Iterator<EdgeItem> iterator() {
                        Table edges = d.getVisualGraph().getEdgeTable();
                        return edges.getColumnNumber(InteractionController.FIELD_INTERACTION_SOURCE) < 0 ? NULL_ITEMS
                                : edges.tuples(DataLib.rows(edges, InteractionController.FIELD_INTERACTION_SOURCE, is.getName()));
                    }
                });
        tabbedPane.addTab("Visual Styles", ImageUtilities.loadImageIcon("org/mongkie/ui/im/resources/styleedit.png", false), edgeStyleUI.getComponent());
    }

    void load() {
        if (settings != null) {
            settings.load(is);
        }
        // Initialize the UI style using the model's style
        edgeStyleUI.loadVisualStyle(Lookup.getDefault().lookup(InteractionController.class).getEdgeVisualStyle(is), false);
        // Store current styles of visual items to revert when the UI is canceled
        edgeOldStyles = VisualStyle.valuesOf(edgeStyleUI.getVisualItems());
    }

    void apply(boolean ok) {
        if (settings != null && ok) {
            settings.apply(is);
        }
        if (ok) {
            // Load the UI style into the model's style
            Lookup.getDefault().lookup(InteractionController.class).getEdgeVisualStyle(is).load(edgeStyleUI.getVisualStyle());
            // Then, apply the style to the visual items
            edgeStyleUI.apply();
        } else { // cancel
            // Revert any styles changed in the UI
            for (VisualStyle<EdgeItem> style : edgeOldStyles.keySet()) {
                style.apply(edgeOldStyles.get(style).toArray(new EdgeItem[]{}));
            }
        }
        edgeOldStyles.clear();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.BorderLayout());

        tabbedPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        add(tabbedPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
