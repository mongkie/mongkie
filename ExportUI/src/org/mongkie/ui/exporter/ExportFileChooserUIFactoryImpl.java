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
package org.mongkie.ui.exporter;

import org.mongkie.exporter.ExportFileChooserUI;
import org.mongkie.exporter.ExportFileChooserUIFactory;
import org.mongkie.exporter.spi.Exporter.GlobalSettingUI;
import org.mongkie.exporter.spi.FileExporter;
import org.mongkie.exporter.spi.FileExporterBuilder;
import org.mongkie.exporter.spi.GraphExporter;
import org.mongkie.exporter.spi.GraphExporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ExportFileChooserUIFactory.class)
public class ExportFileChooserUIFactoryImpl implements ExportFileChooserUIFactory {

    @Override
    public <E extends FileExporter, B extends FileExporterBuilder<E>> ExportFileChooserUI<E> createUI(Class<B> builderClass, String lastPath) {
        return new ExportFileChooserUIImpl<E>(builderClass, lastPath, null);
    }

    @Override
    public <E extends FileExporter, B extends FileExporterBuilder<E>> ExportFileChooserUI<E> createUI(Class<B> builderClass, String lastPath, GlobalSettingUI<E> globalSettings) {
        return new ExportFileChooserUIImpl<E>(builderClass, lastPath, globalSettings);
    }

    @Override
    public ExportFileChooserUI<GraphExporter> createUIForGraphExporter(String lastPath) {
        return new ExportFileChooserUIImpl(GraphExporterBuilder.class, lastPath, new GraphExportGlobalSettingUI());
    }

    @Override
    public <E extends GraphExporter, B extends FileExporterBuilder<E>> ExportFileChooserUI<E> createUIForGraphExporter(Class<B> builderClass, String lastPath) {
        return new ExportFileChooserUIImpl(builderClass, lastPath, new GraphExportGlobalSettingUI());
    }
}
