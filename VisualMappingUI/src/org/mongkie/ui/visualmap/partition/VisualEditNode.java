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
package org.mongkie.ui.visualmap.partition;

import java.awt.Color;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Action;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.mongkie.visualedit.VisualEditor;
import org.mongkie.visualedit.spi.VisualEdit;
import org.mongkie.visualmap.spi.partition.Part;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class VisualEditNode extends AbstractNode {

    private final Part p;
    private final VisualEdit edit;

    public VisualEditNode(Part p, VisualEdit edit) {
        super(Children.LEAF);
        this.p = p;
        this.edit = edit;
        setName(edit.getName());
        setDisplayName(edit.getDisplayName());
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        VisualEditor.Reflection prop;
        try {
            prop = new VisualEditor.Reflection(edit, p.getVisualItems().toArray(new VisualItem[]{}));
            prop.setName(NbBundle.getMessage(VisualEditNode.class, "PartNode.column.visual.name"));
            prop.setDisplayName(NbBundle.getMessage(VisualEditNode.class, "PartNode.column.visual.displayName"));
            set.put(prop);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        }
        Property<Color> color = new PropertySupport.ReadOnly<Color>(
                NbBundle.getMessage(VisualEditNode.class, "PartNode.column.color.name"), Color.class,
                NbBundle.getMessage(VisualEditNode.class, "PartNode.column.color.displayName"), null) {

            @Override
            public Color getValue() throws IllegalAccessException, InvocationTargetException {
                return null;
            }
        };
        color.setValue("suppressCustomEditor", true);
        set.put(color);

        sheet.put(set);
        return sheet;
    }

    public Part getPart() {
        return p;
    }

    public VisualEdit getVisualEdit() {
        return edit;
    }

    @Override
    public Image getIcon(int type) {
        Image icon = edit.getIcon();
        return icon == null ? BULLET_ICON : icon;
    }

    @Override
    public Image getOpenedIcon(int type) {
        Image icon = edit.getIcon();
        return icon == null ? BULLET_ICON : icon;
    }
    private static final Image BULLET_ICON = ImageUtilities.icon2Image(new EmptyIcon(1, 1));

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{};
    }
}
