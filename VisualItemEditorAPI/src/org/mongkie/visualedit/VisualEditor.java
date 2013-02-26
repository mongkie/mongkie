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
package org.mongkie.visualedit;

import java.lang.reflect.InvocationTargetException;
import org.mongkie.visualedit.spi.VisualEdit;
import org.mongkie.visualization.MongkieDisplay;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import prefuse.Visualization;
import prefuse.visual.AggregateItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class VisualEditor<I extends VisualItem> extends AbstractNode {

    private PropertySet[] propertySets;
    private final I[] items;
    private final String labelField;
    private final boolean isGroup;

    public VisualEditor(I... items) {
        super(Children.LEAF);
        this.items = items;
        labelField = getLabelField();
        isGroup = items[0] instanceof AggregateItem;
        setName(items.length > 1 || !items[0].canGetString(labelField) ? "?" : items[0].getString(labelField));
        if (items.length > 1) {
            setShortDescription("Multiple items");
        }
    }

    private String getLabelField() {
        I item = items[0];
        if (item instanceof NodeItem) {
            return ((MongkieDisplay) item.getVisualization().getDisplay(0)).getGraph().getNodeLabelField();
        } else if (item instanceof EdgeItem) {
            return ((MongkieDisplay) item.getVisualization().getDisplay(0)).getGraph().getEdgeLabelField();
        } else if (item instanceof AggregateItem) {
            return AggregateItem.AGGR_NAME;
        } else {
            throw new IllegalArgumentException(item.getClass().getName() + " IS not A VisualItem");
        }
    }

    @Override
    public PropertySet[] getPropertySets() {
        if (propertySets == null) {
            propertySets = isGroup
                    ? new PropertySet[]{
                prepareVisualProperties(NbBundle.getMessage(VisualEditor.class, "EditItems.group.properties.displayName"), items),
                prepareVisualProperties(NbBundle.getMessage(VisualEditor.class, "EditItems.member.properties.displayName"), ((AggregateItem) items[0]).toArray())}
                    : new PropertySet[]{prepareVisualProperties(NbBundle.getMessage(VisualEditor.class, "EditItems.visual.properties.displayName"), items)};
        }
        return propertySets;
    }

    private Sheet.Set prepareVisualProperties(String name, VisualItem[] items) {
        Sheet.Set properties = new Sheet.Set();
        properties.setName(name);
        try {
            Property<String> label = new PropertySupport.ReadOnly<String>("label",
                    String.class, NbBundle.getMessage(VisualEditor.class, "EditItems.label.displayName"), null) {
                @Override
                public String getValue() throws IllegalAccessException, InvocationTargetException {
                    return VisualEditor.this.getName();
                }
            };
            label.setValue("nameIcon", ImageUtilities.loadImage("org/mongkie/visualedit/resources/label_16.png"));
            properties.put(label);
            for (VisualEdit edit : Lookup.getDefault().lookup(VisualEditController.class).getVisualEdits(items)) {
                Reflection p = new Reflection(edit, items);
                p.setValue("nameIcon", ImageUtilities.loadImage("org/mongkie/visualedit/resources/bullet_red.png"));
                properties.put(p);
            }
            return properties;
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static class Reflection extends PropertySupport.Reflection {

        public Reflection(VisualEdit edit, VisualItem... items) throws NoSuchMethodException {
            super(new Bean(edit, items), edit.getValueType(),
                    Bean.class.getMethod("getVisualProperty"),
                    Bean.class.getMethod("setVisualProperty", Object.class));
            setName(edit.getName());
            setDisplayName(edit.getDisplayName());
            if (!edit.canEditAsText()) {
                setValue("canEditAsText", Boolean.FALSE);
            }
            if (edit.supportsPropertyEditor()) {
                setPropertyEditorClass(edit.getPropertyEditorClass());
            }
        }
    }

    public static class Bean<T> {

        private final VisualEdit<T> edit;
        private final VisualItem[] items;
        private T value;
        private final Object[] initials;

        public Bean(VisualEdit<T> edit, VisualItem... items) {
            this.edit = edit;
            this.items = items;
//            value = items.length > 1 ? null : ve.getValue(items[0]);
            value = getInitProperty();
            initials = new Object[items.length];
            for (int i = 0; i < items.length; i++) {
                initials[i] = edit.getValue(items[i]);
            }
        }

        private T getInitProperty() {
            T property = edit.getValue(items[0]);
            if (items.length > 1) {
                for (int i = 1; i < items.length; i++) {
                    if (!property.equals(edit.getValue(items[i]))) {
                        return null;
                        //TODO: Returns default value instead of null?
//                        return edit.getDefaultValue();
                    }
                }
            }
            return property;
        }

        public void setVisualProperty(T value) {
            if (value != null) {
                for (VisualItem item : items) {
                    edit.setValue(item, value);
                }
            } else if (items.length > 1) { //TODO: Should be revised if the value of cancel is not null...
                // After cancelling the first edit of multiple items,
                // those items need to be restored to initial values
                for (int i = 0; i < items.length; i++) {
                    edit.setValue(items[i], (T) initials[i]);
                }
            }
            items[0].getVisualization().rerun(Visualization.DRAW);
            this.value = value;
        }

        public T getVisualProperty() {
            return value;
        }
    }
}
