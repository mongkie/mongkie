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
package org.mongkie.clustering.plugins.clustermaker.mcl;

import org.mongkie.clustering.spi.Clustering;
import org.mongkie.clustering.spi.ClusteringBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ClusteringBuilder.class)
public class MCLBuilder implements ClusteringBuilder {

    private final MCL mcl = new MCL(this);

    @Override
    public Clustering getClustering() {
        return mcl;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MCLBuilder.class, "name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MCLBuilder.class, "description");
    }

    @Override
    public SettingUI getSettingUI() {
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }
}
