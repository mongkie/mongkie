/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.clustering.plugins.mcode;

import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ClusteringBuilder.class)
public class MCODEBuilder implements ClusteringBuilder {

    private final MCODE mcode = new MCODE(this);
    private final SettingUI settings = new MCODESettingUI();

    @Override
    public Clustering getClustering() {
        return mcode;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MCODEBuilder.class, "name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MCODEBuilder.class, "description");
    }

    @Override
    public SettingUI getSettingUI() {
        return settings;
    }

    @Override
    public String toString() {
        return getName();
    }
}
