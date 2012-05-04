/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.importer.plugins.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import kobic.prefuse.data.io.SerializableTable;
import org.mongkie.importer.ContainerFactory;
import org.mongkie.importer.Report;
import org.mongkie.importer.VizGraphContainer;
import org.mongkie.importer.spi.FileImporter;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ImporterVizGraph implements FileImporter<VizGraphContainer> {

    protected InputStream in;

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public VizGraphContainer createContainer() {
        return Lookup.getDefault().lookup(ContainerFactory.class).createVizGraphContainer();
    }

    @Override
    public boolean execute(VizGraphContainer container) {
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(in);
            Graph g = new Graph(((SerializableTable) ois.readObject()).getTable(), ((SerializableTable) ois.readObject()).getTable(),
                    ois.readBoolean(), (String) ois.readObject(), (String) ois.readObject(), (String) ois.readObject());
            g.setNodeLabelField((String) ois.readObject());
            g.setEdgeLabelField((String) ois.readObject());
            container.setResult(g);
            container.setNodeVisualProperties(((SerializableTable) ois.readObject()).getTable());
            container.setEdgeVisualProperties(((SerializableTable) ois.readObject()).getTable());
            container.setAggregateVisualProperties(((SerializableTable) ois.readObject()).getTable());
            container.setAggregateId2NodeItemRows((Map<Integer, List<Integer>>) ois.readObject());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            throw new RuntimeException("An error occured while importing a visual graph", ex);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                    return false;
                }
            }
        }
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Visual graph is imported successfully");
        return true;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public Report getReport() {
        return null;
    }
}
