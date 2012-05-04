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
package org.mongkie.importer.plugins.graph;

import org.mongkie.importer.GraphContainer;
import org.mongkie.importer.Report;
import org.mongkie.importer.spi.GraphFileImporter;
import org.openide.util.NbBundle;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ImporterGraphML extends GraphFileImporter {

    private final GraphMLReader reader;

    public ImporterGraphML() {
        reader = new GraphMLReader();
    }

    @Override
    public boolean execute(GraphContainer container) {
        try {
            container.setResult(reader.readGraph(in));
        } catch (DataIOException ex) {
            throw new RuntimeException(NbBundle.getMessage(ImporterGraphML.class, "ImporterGraphML.readGraph.error"), ex);
        }
        return true;
    }

    @Override
    public Report getReport() {
        return null;
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
