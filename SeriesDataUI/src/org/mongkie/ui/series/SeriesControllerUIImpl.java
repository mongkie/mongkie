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
package org.mongkie.ui.series;

import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.JFileChooser;
import org.mongkie.importer.ImportController;
import org.mongkie.importer.ImportFileChooserUI;
import org.mongkie.importer.ImportFileChooserUIFactory;
import org.mongkie.series.SeriesController;
import org.mongkie.series.SeriesControllerUI;
import org.mongkie.series.SeriesImporter;
import org.mongkie.ui.series.importer.SeriesImporterBuilder;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@ServiceProvider(service = SeriesControllerUI.class)
public class SeriesControllerUIImpl implements SeriesControllerUI {
    
    @Override
    public void loadSeries() {
        final String LAST_PATH = "ImportSeries_Last_Path";
        final String LAST_PATH_DEFAULT = "ImportSeries_Last_Path_Default";
        String lastPathDefault = NbPreferences.forModule(SeriesControllerUIImpl.class).get(LAST_PATH_DEFAULT, null);
        String lastPath = NbPreferences.forModule(SeriesControllerUIImpl.class).get(LAST_PATH, lastPathDefault);
        
        ImportFileChooserUI chooserUI = Lookup.getDefault().lookup(ImportFileChooserUIFactory.class).createUI(SeriesImporterBuilder.class, lastPath);
        int state = chooserUI.showOpenDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = chooserUI.getSelectedFile();
            NbPreferences.forModule(SeriesControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());
            file = FileUtil.normalizeFile(file);
            FileObject fileObject = FileUtil.toFileObject(file);
            try {
                SeriesImporter importer = Lookup.getDefault().lookup(ImportController.class).getFileImporter(SeriesImporterBuilder.class, file);
                importer.setInputStream(fileObject.getInputStream());
                importer.setTitle(fileObject.getName().toUpperCase());
                Lookup.getDefault().lookup(SeriesController.class).loadSeries(importer);
            } catch (FileNotFoundException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
}
