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
import static org.mongkie.exporter.spi.Exporter.*;
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
    private GlobalSettingUI<E> globalSettingUI;
    private SettingUI<E> selectedSettingUI;

    ExportFileChooserUIImpl(final Class<? extends FileExporterBuilder<E>> builderClass, String lastPath, final GlobalSettingUI<E> globalSettingUI) {
        final ExportControllerUI controllerUI = Lookup.getDefault().lookup(ExportControllerUI.class);

        // Option UI shown when option bution clicked
        JPanel optionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        final JButton optionsButton = new JButton(NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.optionsButton.text"));
        optionButtonPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OptionUI optionUI = controllerUI.getExportController().getOptionUI(selectedExporter);
                if (optionUI != null) {
                    JPanel optionPanel = optionUI.getPanel();
                    optionUI.load(selectedExporter);
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
                    optionUI.apply(result == NotifyDescriptor.OK_OPTION);
                }
            }
        });

        // Option button and global setting panel
        final JPanel optionAndGlobalSettingPanel = new JPanel(new BorderLayout());
        optionAndGlobalSettingPanel.add(optionButtonPanel, BorderLayout.NORTH);
        this.globalSettingUI = globalSettingUI;
        if (globalSettingUI != null) {
            JPanel globalSettingPanel = new JPanel(new BorderLayout());
            globalSettingPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 0, 0, 0), BorderFactory.createEtchedBorder()));
            globalSettingPanel.add(globalSettingUI.getPanel(), BorderLayout.CENTER);
            optionAndGlobalSettingPanel.add(globalSettingPanel, BorderLayout.CENTER);
        }
        // Selected exporter setting panel
        final JPanel exportSettingPanel = new JPanel(new BorderLayout());

        fileChooser = new JFileChooser(lastPath) {
            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                dialog = super.createDialog(parent);
                dialog.setResizable(true);
                Component c = dialog.getContentPane().getComponent(0);
                if (c != null && c instanceof JComponent) {
                    Insets insets = ((JComponent) c).getInsets();
                    optionAndGlobalSettingPanel.setBorder(BorderFactory.createEmptyBorder(0, insets.left, insets.bottom, insets.right));
                    exportSettingPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(insets.top, 2, insets.bottom, insets.right), BorderFactory.createTitledBorder("Export Settings")));
                } else {
                    optionAndGlobalSettingPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 10, 2));
                    exportSettingPanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createEmptyBorder(10, 2, 10, 2), BorderFactory.createTitledBorder("Export Settings")));
                }
                dialog.getContentPane().add(optionAndGlobalSettingPanel, BorderLayout.SOUTH);
                if (selectedSettingUI != null) {
                    dialog.getContentPane().add(exportSettingPanel, BorderLayout.EAST);
                }
                dialog.pack();
                return dialog;
            }

            @Override
            public void approveSelection() {
                if (canExport()) {
                    if (selectedSettingUI != null) {
                        selectedSettingUI.apply();
                    }
                    if (globalSettingUI != null && selectedExporter != null) {
                        globalSettingUI.apply();
                    }
                    super.approveSelection();
                }
            }

            @Override
            public void cancelSelection() {
                // Also apply settings even if file chooser canceled
                if (selectedSettingUI != null) {
                    selectedSettingUI.apply();
                }
                if (globalSettingUI != null && selectedExporter != null) {
                    globalSettingUI.apply();
                }
                super.cancelSelection();
            }
        };

        fileChooser.setDialogTitle(NbBundle.getMessage(ExportFileChooserUIImpl.class, "ExportFileChooserUIImpl.filechooser.title"));
        fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();
                selectedFilter = fileFilter;

                // Refresh option and settings UI
                selectedBuilder = getFileExporterBuilder(builderClass, fileFilter);
                if (selectedSettingUI != null) {
                    selectedSettingUI.apply();
                    dialog.remove(exportSettingPanel);
                    exportSettingPanel.remove(selectedSettingUI.getPanel());
//                    dialog.revalidate();
//                    dialog.repaint();
                    dialog.pack();
                    selectedSettingUI = null;
                }
                if (globalSettingUI != null && selectedExporter != null) {
                    globalSettingUI.apply();
                }
                if (selectedBuilder != null) {
                    selectedExporter = controllerUI.getExportController().getExporter(selectedBuilder);
                    optionsButton.setEnabled(controllerUI.getExportController().getOptionUI(selectedExporter) != null);
                    if ((selectedSettingUI = controllerUI.getExportController().getSettingUI(selectedExporter)) != null) {
                        selectedSettingUI.load(selectedExporter);
                        exportSettingPanel.add(selectedSettingUI.getPanel(), BorderLayout.CENTER);
                        if (dialog != null) {
                            dialog.getContentPane().add(exportSettingPanel, BorderLayout.EAST);
//                            dialog.revalidate();
//                            dialog.repaint();
                            dialog.pack();
                        }
                    }
                    if (globalSettingUI != null) {
                        globalSettingUI.load(selectedExporter);
                    }
                } else {
                    optionsButton.setEnabled(false);
                }

                // Selected file extension change
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

        // File filters
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
    public GlobalSettingUI<E> getGlobalSettingUI() {
        return globalSettingUI;
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
