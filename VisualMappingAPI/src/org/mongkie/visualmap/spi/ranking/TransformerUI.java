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
package org.mongkie.visualmap.spi.ranking;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Transformer user interface. Implement this interface to create panels associated
 * to a particular transformer.
 * <p>
 * The icon and display name are used to create the transformer button in the UI.
 * <p>
 * Implementors should add the <code>@ServiceProvider</code> annotation to be
 * registered by the system.
 * 
 * @see Transformer
 * @author Mathieu Bastian <mathieu.bastian@gephi.org>
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface TransformerUI {

    /**
     * Returns the transformer's icon
     * @return the icon of this transformer
     */
    public Icon getIcon();

    /**
     * Returns the display name of the transformer
     * @return the display name of this transformer
     */
    public String getDisplayName();

    /**
     * Returns the panel associated to this transformer.
     * @param transformer the transformer to build the panel for
     * @param ranking the ranking to be used by the transformer
     * @return the panel of this transformer
     */
    public JPanel getPanel(Transformer transformer, Ranking ranking);

    /**
     * Returns <code>true</code> if this UI is built for <code>transformer</code>.
     * @param transformer the transformer to test ownership
     * @return <code>true</code> if this UI is associated to <code>transformer</code>,
     * <code>false</code> otherwise
     */
    public boolean isUIForTransformer(Transformer transformer);
}
