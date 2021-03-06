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
package org.mongkie.perspective.spi.impl;

import javax.swing.Icon;
import org.mongkie.perspective.spi.Perspective;
import static org.mongkie.visualization.Config.ROLE_NETWORK;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = Perspective.class, position = 100)
public class NetworkPerspective implements Perspective {

    @Override
    public String getRole() {
        return ROLE_NETWORK;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NetworkPerspective.class, "NetworkPerspective.displayName");
    }

    @Override
    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/mongkie/ui/visualization/resources/network.png", false);
    }
}
