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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mongkie.exporter.ExportControllerUI;
import org.mongkie.exporter.ExportFileChooserUI;
import org.mongkie.exporter.spi.Exporter;
import org.mongkie.exporter.spi.FileExporter;
import org.mongkie.exporter.spi.FileExporterBuilder;
import org.mongkie.lib.widgets.TopDialog;
import org.mongkie.util.io.DialogFileFilter;
import org.mongkie.util.io.FileType;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@NbBundle.Messages({
    "ExportFileChooserUIImpl.optionsButton.text=Options...",
    "ExportFileChooserUIImpl.optionsDialog.title={0} Options",
    "ExportFileChooserUIImpl.filechooser.title=Export",
    "ExportFileChooserUIImpl.error.createFile.failed=Could not create the file: {0}",
    "ExportFileChooserUIImpl.overwriteDialog.msg={0} exists.\n Overwrite it?",
    "ExportFileChooserUIImpl.overwriteDialog.title=Confirmation"
})
final class ExportFileChooserUIImpl<E extends FileExporter> implements ExportFileChooserUI<E> {

    private JFileChooser fileChooser;
    private DialogFileFilter selectedFilter;
    private File selectedFile;
    private JDialog dialog;
    private FileExporterBuilder<E> selectedBuilder;
    private E selectedExporter;
    private GraphExportSettingUI exportSettings;

    ExportFileChooserUIImpl(final Class<? extends FileExporterBuilder<E>> builderClass, String lastPath, GraphExportSettingUI exportSettings) {
        final ExportControllerUI controllerUI = Lookup.getDefault().lookup(ExportControllerUI.class);

        //Options optionPanel
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton optionsButton = new JButton(NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.optionsButton.text"));
        optionsPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Exporter.OptionUI optionUI = controllerUI.getExportController().getOptionUI(selectedExporter);
                if (optionUI != null) {
                    JPanel optionPanel = optionUI.getPanel();
                    optionUI.setup(selectedExporter);
                    final DialogDescriptor dd = new DialogDescriptor(optionPanel,
                            NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.optionsDialog.title", selectedBuilder.getName()));
                    if (optionPanel instanceof ValidationPanel) {
                        ValidationPanel vp = (ValidationPanel) optionPanel;
                        vp.addChangeListener(new ChangeListener() {

                            @Override
                            public void stateChanged(ChangeEvent e) {
                                dd.setValid(!((ValidationPanel) e.getSource()).isFatalProblem());
                            }
                        });
                    }
                    TopDialog topDialog = new TopDialog(dialog, dd.getTitle(), dd.isModal(), dd, dd.getClosingOptions(), dd.getButtonListener());
                    topDialog.setVisible(true);
                    Object result = (dd.getValue() != null) ? dd.getValue() : NotifyDescriptor.CLOSED_OPTION;
//                    Object result = DialogDisplayer.getDefault().notify(dd);
                    optionUI.apply(result == NotifyDescriptor.OK_OPTION);
                }
            }
        });

        //Graph Settings Panel
        final JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(optionsPanel, BorderLayout.NORTH);
        this.exportSettings = exportSettings;
        if (exportSettings != null) {
            southPanel.add(exportSettings, BorderLayout.CENTER);
        }

        //Optionable file fileChooser
        fileChooser = new JFileChooser(lastPath) {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                dialog = super.createDialog(parent);
                dialog.setSize(640, 480);
                dialog.setResizable(true);
                Component c = dialog.getContentPane().getComponent(0);
                if (c != null && c instanceof JComponent) {
                    Insets insets = ((JComponent) c).getInsets();
                    southPanel.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.right));
                }
                dialog.getContentPane().add(southPanel, BorderLayout.SOUTH);
                return dialog;
            }

            @Override
            public void approveSelection() {
                if (canExport()) {
                    super.approveSelection();
                }
            }
        };

        fileChooser.setDialogTitle(NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.filechooser.title"));
        fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();
                selectedFilter = fileFilter;

                //Options optionPanel enabling
                selectedBuilder = getFileExporterBuilder(builderClass, fileFilter);
                if (selectedBuilder != null) {
                    selectedExporter = controllerUI.getExportController().getExporter(selectedBuilder);
                }
                if (selectedBuilder != null && controllerUI.getExportController().getOptionUI(selectedExporter) != null) {
                    optionsButton.setEnabled(true);
                } else {
                    optionsButton.setEnabled(false);
                }

                //Selected file extension change
                if (selectedFile != null && fileFilter != null) {
                    String fileName = selectedFile.getName();
                    String directoryPath = fileChooser.getCurrentDirectory().getAbsolutePath();
                    if (fileName.lastIndexOf(".") != -1) {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        fileName = fileName.concat(fileFilter.getExtensions().get(0));
                        selectedFile = new File(directoryPath, fileName);
                        fileChooser.setSelectedFile(selectedFile);
                    }
                }
            }
        });
        fileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getNewValue() != null) {
                    selectedFile = (File) evt.getNewValue();
                }
            }
        });

        //File filters
        DialogFileFilter defaultFilter = null;
        for (FileExporterBuilder builder : Lookup.getDefault().lookupAll(builderClass)) {
            for (FileType fileType : builder.getFileTypes()) {
                DialogFileFilter dialogFileFilter = new DialogFileFilter(fileType.getName());
                dialogFileFilter.addExtensions(fileType.getExtensions());
                if (defaultFilter == null) {
                    defaultFilter = dialogFileFilter;
                }
                fileChooser.addChoosableFileFilter(dialogFileFilter);
            }
        }
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(defaultFilter);
        selectedFile = new File(fileChooser.getCurrentDirectory(), "Untitled" + defaultFilter.getExtensions().get(0));
        fileChooser.setSelectedFile(selectedFile);
    }

    private boolean canExport() {
        File file = fileChooser.getSelectedFile();

        try {
            if (!checkExtension(file)) {
                file = new File(file.getPath() + selectedFilter.getExtensions().get(0));
                selectedFile = file;
                fileChooser.setSelectedFile(file);
            }
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.error.createFile.failed", new Object[]{file.getPath()}),
                            NotifyDescriptor.ERROR_MESSAGE));
                    return false;
                }
            } else {
                if (DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.overwriteDialog.msg", new Object[]{file.getPath()}),
                        NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.overwriteDialog.title"),
                        NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE)) != NotifyDescriptor.OK_OPTION) {
                    return false;
                }
            }
        } catch (IOException ex) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.WARNING_MESSAGE));
            return false;
        }

        return true;
    }

    private boolean checkExtension(File file) {
        for (String extension : selectedFilter.getExtensions()) {
            if (file.getPath().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    private FileExporterBuilder<E> getFileExporterBuilder(Class<? extends FileExporterBuilder<E>> builderClass, DialogFileFilter fileFilter) {
        //Find fileFilter
        for (FileExporterBuilder<E> builder : Lookup.getDefault().lookupAll(builderClass)) {
            for (FileType fileType : builder.getFileTypes()) {
                DialogFileFilter tempFilter = new DialogFileFilter(fileType.getName());
                tempFilter.addExtensions(fileType.getExtensions());
                if (tempFilter.equals(fileFilter)) {
                    return builder;
                }
            }
        }
        return null;
    }

    @Override
    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    @Override
    public boolean isExportSelectedOnly() {
        return exportSettings != null ? exportSettings.isExportSelectedOnly() : false;
    }

    @Override
    public E getSelectedExporter() {
        return selectedExporter;
    }

    @Override
    public int showSaveDialog(Component parent) {
        return fileChooser.showSaveDialog(parent);
    }

    @Override
    public File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }
}
