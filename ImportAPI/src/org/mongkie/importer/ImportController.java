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
package org.mongkie.importer;

import java.io.File;
import java.io.InputStream;
import org.mongkie.importer.spi.FileImporter;
import org.mongkie.importer.spi.FileImporterBuilder;
import org.mongkie.importer.spi.Importer;
import org.mongkie.importer.spi.ImporterBuilder;
import org.mongkie.importer.spi.Processor;
import org.mongkie.util.io.FileType;
import prefuse.data.Table;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface ImportController {

    public <C extends Container> C importFile(InputStream stream, FileImporter<C> importer);

    public <I extends FileImporter, B extends FileImporterBuilder<I>> I getFileImporter(Class<B> builderClass, File file);

    public FileType[] getFileTypes(Class<? extends FileImporterBuilder> builderClass);

    public Importer.OptionUI getOptionUI(ImporterBuilder builder);

    public Importer.SettingUI getSettingUI(ImporterBuilder builder);

    public Importer.WizardUI getWizardUI(ImporterBuilder builder);

    public Processor.UI getProcessorUI(Processor processor);

    public <I extends Importer> I getImporter(ImporterBuilder<I> builder);

    public <I extends Importer> ImporterBuilder<I> getBuilder(I importer);

    public GraphContainer importCSV(InputStream nodesInputStream, InputStream edgesInputStream,
            String nodeId, String nodeLabel, String edgeSource, String edgeTarget, String edgeLabel, boolean directed);

    public GraphContainer importCSV(InputStream nodesInputStream, InputStream edgesInputStream,
            String[] nodeHeaderNames, boolean hasNodeHeader, String[] edgeHeaderNames, boolean hasEdgeHeader,
            String nodeId, String nodeLabel, String sourceId, String targetId, String edgeLabel, boolean directed);

    public void importAttributes(InputStream is, Table to, String[] headerNames, boolean hasHeader, boolean multipleValue, String attributeKey, String networkKey);
}
