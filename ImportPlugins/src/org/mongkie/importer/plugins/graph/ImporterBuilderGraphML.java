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
package org.mongkie.importer.plugins.graph;

import org.mongkie.importer.GraphContainer;
import org.mongkie.importer.spi.GraphFileImporterBuilder;
import org.mongkie.util.io.FileType;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = GraphFileImporterBuilder.class, position = 100)
public class ImporterBuilderGraphML implements GraphFileImporterBuilder<GraphContainer> {

    @Override
    public ImporterGraphML buildImporter() {
        return new ImporterGraphML();
    }

    @Override
    public String getName() {
        return "GraphML";
    }

    @Override
    public FileType[] getFileTypes() {
        return new FileType[]{new FileType("GraphML Files", ".graphml")};
    }

    @Override
    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("graphml");
    }
}