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
package org.mongkie.exporter.plugins.graph;

import org.mongkie.exporter.spi.AbstractGraphExporter;
import prefuse.data.io.GraphMLWriter;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class ExporterGraphML extends AbstractGraphExporter {

    private final GraphMLWriter writer = new GraphMLWriter();

    @Override
    public boolean execute() {
        try {
            writer.writeGraph(display.getGraph(), out);
        } catch (Exception ex) {
            throw new RuntimeException("An error happened when writing to the GraphML file", ex);
        }
        return true;
    }

    @Override
    public boolean cancel() {
        return false;
    }
}
