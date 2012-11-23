/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.mongkie.layout.spi;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * A <code>LayoutBuilder</code> provides a specific {@link Layout} instance. The
 * Builder pattern is more suitable for the Layout instantiation to allow
 * simpler reusability of Layout's code.
 *<p>
 * Only the LayoutBuilder of a given layout algorithm is exposed,
 * this way, one can devise different layout algorithms (represented by their
 * respective LayoutBuilder) that uses a same underlying Layout implementation,
 * but that differs only by an aggregation, composition or a property that is
 * set only during instantiation time.
 *<p>
 * See <code>ClockwiseRotate</code> and <code>CounterClockwiseRotate</code> for
 * a simple example of this pattern. Both are LayoutBuilders that instanciate
 * Layouts with a different behavior (the direction of rotation), but both uses
 * the RotateLayout class. The only difference is the angle provided by the
 * LayoutBuilder on the time of instantiation of the RotateLayout object.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 * @author Youngjun Jang <yjjang@kribb.re.kr>
 */
public abstract class LayoutBuilder<L extends Layout> {

    protected L layout;

    /**
     * The name of the behavior of the Layout's provided by this Builder.
     * @return  the display name of the layout algorithm
     */
    public abstract String getName();

    /**
     * User interface attributes (name, description, icon...) for all Layouts
     * built by this builder.
     * @return a <code>LayoutUI</code> instance
     */
    public abstract UI<L> getUI();

    /**
     *
     * @return a Layout singleton instance with lazy instantiation
     */
    public final L getLayout() {
        if (layout == null) {
            layout = buildLayout();
        }
        return layout;
    }

    /**
     * Builds an instance of the Layout.
     * @return  a new <code>Layout</code> instance
     * @author Youngjun Jang <yjjang@kribb.re.kr>
     */
    protected abstract L buildLayout();

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Various information about a layout algorithm that allows UI integration.
     *
     * @author Mathieu Bastian
     * @author Youngjun Jang <yjjang@kribb.re.kr>
     */
    public static interface UI<L extends Layout> {

        /**
         * The description of the layout algorithm purpose.
         * @return  a description snippet for the algorithm
         */
        public String getDescription();

        /**
         * The icon that represents the layout action.
         * @return  a icon for this particular layout
         */
        public Icon getIcon();

        /**
         * A <code>LayoutUI</code> can have a optional settings panel, that will be
         * displayed instead of the property sheet.
         * @param layout the layout that require a setting panel
         * @return A settings panel for <code>layout</code> or
         * <code>null</code>
         * @author Youngjun Jang <yjjang@kribb.re.kr>
         */
        public JPanel getSettingPanel(L layout);

        /**
         * An appraisal of quality for this algorithm. The rank must be between 1 and
         * 5. The rank will be displayed to users to help them to choose a suitable
         * algorithm. Return -1 if you don't want to display a rank.
         * @return an integer between 1 and 5 or -1 if you don't want to show a rank
         */
        public int getQualityRank();

        /**
         * An appraisal of speed for this algorithm. The rank must be between 1 and
         * 5. The rank will be displayed to users to help them to choose a suitable
         * algorithm. Return -1 if you don't want to display a rank.
         * @return an integer between 1 and 5 or -1 if you don't want to show a rank
         */
        public int getSpeedRank();
    }
}
