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

import java.io.File;
import javax.swing.JFileChooser;
import org.mongkie.exporter.ExportController;
import org.mongkie.exporter.ExportControllerUI;
import org.mongkie.exporter.ExportFileChooserUI;
import org.mongkie.exporter.ExportFileChooserUIFactory;
import org.mongkie.exporter.spi.FileExporter;
import org.mongkie.exporter.spi.GraphExporter;
import org.mongkie.exporter.spi.ImageExporter;
import org.mongkie.exporter.spi.ImageExporterBuilder;
import org.mongkie.exporter.spi.TableExporterBuilder;
import org.mongkie.longtask.LongTaskErrorHandler;
import org.mongkie.longtask.LongTaskExecutor;
import org.mongkie.longtask.progress.Progress;
import org.mongkie.longtask.progress.ProgressTask;
import org.mongkie.longtask.progress.ProgressTicket;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@NbBundle.Messages({
    "ExportControllerUIImpl.exportTask.name=Export to {0}",
    "ExportControllerUIImpl.error.noMatchingFileExporter=Impossible to obtain an instance of FileExporter",
    "ExportControllerUIImpl.status.exportSuccess={0} exported successfully"
})
@ServiceProvider(service = ExportControllerUI.class)
public class ExportControllerUIImpl implements ExportControllerUI {

    private final ExportController controller;
    private final LongTaskExecutor executor;

    public ExportControllerUIImpl() {
        controller = Lookup.getDefault().lookup(ExportController.class);
        executor = new LongTaskExecutor(true, "Exporter");
        executor.setDefaultErrorHandler(new LongTaskErrorHandler() {
            @Override
            public void fatalError(Throwable t) {
                String message = t.getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getCause().getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        });
    }

    @Override
    public ExportController getExportController() {
        return controller;
    }

    @Override
    public void exportGraph() {
        final String LAST_PATH = "ExportGraph_Last_Path";
        String lastPath = NbPreferences.forModule(ExportControllerUIImpl.class).get(LAST_PATH, null);
        ExportFileChooserUI<GraphExporter> chooserUI = Lookup.getDefault().lookup(ExportFileChooserUIFactory.class).createUIForGraphExporter(lastPath);
        int state = chooserUI.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = FileUtil.normalizeFile(chooserUI.getSelectedFile());
            NbPreferences.forModule(ExportControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());
            GraphExporter selectedExporter = chooserUI.getSelectedExporter();
            exportFile(file, selectedExporter);
        }
    }

    @Override
    public void exportNodeTable() {
        exportTable(TableExporterBuilder.Node.class);
    }

    @Override
    public void exportEdgeTable() {
        exportTable(TableExporterBuilder.Edge.class);
    }

    private void exportTable(Class<? extends TableExporterBuilder> builderClass) {
        final String LAST_PATH = "ExportCSV_Last_Path";
        String lastPath = NbPreferences.forModule(ExportControllerUIImpl.class).get(LAST_PATH, null);
        ExportFileChooserUI<GraphExporter> chooserUI =
                Lookup.getDefault().lookup(ExportFileChooserUIFactory.class).createUIForGraphExporter(builderClass, lastPath);
        int state = chooserUI.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = FileUtil.normalizeFile(chooserUI.getSelectedFile());
            NbPreferences.forModule(ExportControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());
            GraphExporter selectedExporter = chooserUI.getSelectedExporter();
            exportFile(file, selectedExporter);
        }
    }

    @Override
    public void exportImage() {
        final String LAST_PATH = "ExportImage_Last_Path";
        String lastPath = NbPreferences.forModule(ExportControllerUIImpl.class).get(LAST_PATH, null);
        ExportFileChooserUI<ImageExporter> chooserUI = Lookup.getDefault().lookup(ExportFileChooserUIFactory.class).createUI(ImageExporterBuilder.class, lastPath);
        int state = chooserUI.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            File file = FileUtil.normalizeFile(chooserUI.getSelectedFile());
            NbPreferences.forModule(ExportControllerUIImpl.class).put(LAST_PATH, file.getAbsolutePath());
            ImageExporter selectedExporter = chooserUI.getSelectedExporter();
            selectedExporter.setFileExtension(FileUtil.toFileObject(file).getExt());
            exportFile(file, selectedExporter);
        }
    }

    private void exportFile(final File file, final FileExporter exporter) {
        if (exporter == null) {
            throw new RuntimeException(NbBundle.getMessage(ExportControllerUIImpl.class, "ExportControllerUIImpl.error.noMatchingFileExporter"));
        }
        final String fileName = FileUtil.toFileObject(file).getNameExt();

        //Export Task
        final FileExportTask task = new FileExportTask(exporter);
        executor.execute(task, new Runnable() {
            @Override
            public void run() {
                ProgressTicket ticket = task.getProgressTicket();
                Progress.setDisplayName(ticket, NbBundle.getMessage(ExportControllerUIImpl.class, "ExportControllerUIImpl.exportTask.name", fileName));
                Progress.start(ticket);
                try {
                    controller.exportFile(file, exporter);
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ExportControllerUIImpl.class, "ExportControllerUIImpl.status.exportSuccess", fileName));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                } finally {
                    Progress.finish(ticket);
                }
            }
        });
    }

    private static class FileExportTask extends ProgressTask {

        private final FileExporter exporter;

        public FileExportTask(FileExporter exporter) {
            this.exporter = exporter;
        }

        @Override
        public boolean cancel() {
            return exporter.cancel();
        }
    }
}
