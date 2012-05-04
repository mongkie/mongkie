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
package org.mongkie.ui.importer;

import org.mongkie.importer.ImportFileChooserUI;
import org.mongkie.importer.ImportFileChooserUIFactory;
import org.mongkie.importer.spi.FileImporter;
import org.mongkie.importer.spi.FileImporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = ImportFileChooserUIFactory.class)
public class ImportFileChooserUIFactoryImpl implements ImportFileChooserUIFactory {

    @Override
    public <I extends FileImporter, B extends FileImporterBuilder<I>> ImportFileChooserUI<I> createUI(Class<B> builderClass, String lastPath) {
        return new ImportFileChooserUIImpl<I>(builderClass, lastPath);
    }
}
