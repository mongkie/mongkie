/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.layout;

import java.beans.PropertyEditor;
import org.mongkie.layout.spi.Layout;
import org.openide.nodes.PropertySupport;

/**
 * Properties for layout algorithms that are used by the UI to fill the property
 * sheet and thus allow user edit.
 *
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class LayoutProperty<T extends Object> extends PropertySupport.Reflection<T> {

    public static final String ALL = "Layout Properties";
    protected final String category;

    /**
     * Create a property.
     *
     * @param name        The display name of the property
     * @param description A description string for the property
     * @param category    A category string or <code>null</code> for using default
     *                    category
     * @param layout      The layout instance
     * @param valueType   The type of the property value, * ex: <code>Double.class</code>
     * @param getter      The name of the get method for this property, must exist to
     *                    make Java reflexion working.
     * @param setter      The name of the set method for this property, must exist to
     *                    make Java reflexion working.
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be
     *                               found
     */
    public LayoutProperty(String name, String description, String category,
            Layout layout, Class valueType, String getter, String setter) throws NoSuchMethodException {
        super(layout, valueType, getter, setter);
        setName(name);
        setShortDescription(description);
        this.category = category;
    }

    /**
     * Create a property, with a particular {@link PropertyEditor}. A particular
     * editor must be specified when the property type don't have a registered
     * editor class.
     *
     * @param name        The display name of the property
     * @param description A description string for the property
     * @param category    A category string or <code>null</code> for using default
     *                    category
     * @param layout      The layout instance
     * @param valueType   The type of the property value, * ex: <code>Double.class</code>
     * @param getter      The name of the get method for this property, must exist to
     *                    make Java reflexion working.
     * @param setter      The name of the set method for this property, must exist to
     *                    make Java reflexion working.
     * @param editorClass A <code>PropertyEditor</code> class for the given type
     * @return the created property
     * @throws NoSuchMethodException if the getter or setter methods cannot be
     *                               found
     */
    public LayoutProperty(String name, String description, String category,
            Layout layout, Class valueType, String getter, String setter, Class<? extends PropertyEditor> editorClass) throws NoSuchMethodException {
        super(layout, valueType, getter, setter);
        setName(name);
        setShortDescription(description);
        setPropertyEditorClass(editorClass);
        this.category = category;
    }

    /**
     * Return the category of the property
     */
    public String getCategory() {
        return category;
    }
}
