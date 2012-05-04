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
package org.mongkie.ui.visualedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.mongkie.visualedit.VisualEditController;
import org.mongkie.visualedit.spi.VisualEdit;
import org.mongkie.visualization.MongkieDisplay;
import org.mongkie.visualization.VisualizationController;
import org.mongkie.visualization.selection.SelectionListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = VisualEditController.class)
public class VisualEditControllerImpl implements VisualEditController, SelectionListener {

    public VisualEditControllerImpl() {
        Lookup.getDefault().lookup(VisualizationController.class).getSelectionManager().addSelectionListener(VisualEditControllerImpl.this);
    }

    @Override
    public void openEditor() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                VisualEditorTopComponent editor = VisualEditorTopComponent.findInstance();
                editor.open();
                editor.requestActive();
            }
        });
    }

    @Override
    public void closeEditor() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                VisualEditorTopComponent editor = VisualEditorTopComponent.findInstance();
                editor.close();
            }
        });
    }

    @Override
    public void disableEditor() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                VisualEditorTopComponent.findInstance().disableEditing();
            }
        });
    }

    @Override
    public boolean isEditorOpened() {
        VisualEditorTopComponent editor = VisualEditorTopComponent.findInstance();
        return editor.isOpened();
    }

    @Override
    public <I extends VisualItem> void edit(final I... items) {
        if (areSameTypes(items)) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    VisualEditorTopComponent.findInstance().edit(items);
                }
            });
        } else {
            disableEditor();
        }
    }

    private boolean areSameTypes(VisualItem... items) {
        if (items.length > 0) {
            VisualItem one = items[0];
            for (int i = 1; i < items.length; i++) {
                VisualItem other = items[i];
                if (!one.getClass().isAssignableFrom(other.getClass()) && !other.getClass().isAssignableFrom(one.getClass())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<VisualEdit> getVisualEdits(VisualItem[] items) {
        List<VisualEdit> edits = new ArrayList<VisualEdit>();
        VisualItem item = items[0];
        for (VisualEdit ve : Lookup.getDefault().lookupAll(VisualEdit.class)) {
            if (ve.getItemType().isAssignableFrom(item.getClass())) {
                edits.add(ve);
            }
        }
        return edits;
    }

    @Override
    public void selected(Set<VisualItem> members, VisualItem... items) {
        edit(members.toArray(new VisualItem[]{}));
    }

    @Override
    public void unselected(Set<VisualItem> members, VisualItem... items) {
        edit(members.toArray(new VisualItem[]{}));
    }

    @Override
    public MongkieDisplay getDisplay() {
        return Lookup.getDefault().lookup(VisualizationController.class).getDisplay();
    }
}
