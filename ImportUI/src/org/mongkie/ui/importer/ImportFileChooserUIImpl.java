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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.mongkie.importer.ImportController;
import org.mongkie.importer.ImportFileChooserUI;
import org.mongkie.importer.spi.FileImporter;
import org.mongkie.importer.spi.FileImporterBuilder;
import org.mongkie.importer.spi.Importer;
import org.mongkie.lib.widgets.TopDialog;
import org.mongkie.util.io.DialogFileFilter;
import org.mongkie.util.io.FileType;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@NbBundle.Messages({
    "ImportFileChooserUIImpl.fileImporter.fileChooser.optionButton.name=Options...",
    "ImportFileChooserUIImpl.fileImporter.settingUI.title={0} Settings",
    "ImportFileChooserUIImpl.fileImporter.fileChooser.title=Import"
})
class ImportFileChooserUIImpl<I extends FileImporter> implements ImportFileChooserUI<I> {

    private JFileChooser fileChooser;
    private File selectedFile;
    private DialogFileFilter selectedFilter;
    private JDialog dialog;
    private Importer.SettingUI selectedSettingUI;
    private final ImportController controller = Lookup.getDefault().lookup(ImportController.class);

    ImportFileChooserUIImpl(final Class<? extends FileImporterBuilder<I>> builderClass, String lastPath) {

        //Options panel
        JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton optionsButton = new JButton(NbBundle.getMessage(ImportControllerUIImpl.class, "ImportFileChooserUIImpl.fileImporter.fileChooser.optionButton.name"));
        optionsPanel.add(optionsButton);
        optionsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                FileImporterBuilder<I> builder = getFileImporterBuilder(builderClass, selectedFilter);
                Importer.OptionUI optionUI = controller.getOptionUI(builder);
                if (optionUI != null) {
                    JPanel optionPanel = optionUI.getPanel();
                    optionUI.setup(controller.getImporter(builder));
                    final DialogDescriptor optionDescriptor = new DialogDescriptor(optionPanel,
                            NbBundle.getMessage(ImportControllerUIImpl.class, "ImportFileChooserUIImpl.fileImporter.settingUI.title", builder.getName()));
                    if (optionPanel instanceof ValidationPanel) {
                        ValidationPanel vp = (ValidationPanel) optionPanel;
                        vp.addChangeListener(new ChangeListener() {

                            @Override
                            public void stateChanged(ChangeEvent e) {
                                optionDescriptor.setValid(!((ValidationPanel) e.getSource()).isFatalProblem());
                            }
                        });
                    }
                    TopDialog optionDialog = new TopDialog(dialog, optionDescriptor.getTitle(), optionDescriptor.isModal(), optionDescriptor, optionDescriptor.getClosingOptions(), optionDescriptor.getButtonListener());
                    optionDialog.setVisible(true);
                    Object result = (optionDescriptor.getValue() != null) ? optionDescriptor.getValue() : NotifyDescriptor.CLOSED_OPTION;
                    optionUI.apply(result == NotifyDescriptor.OK_OPTION);
                }
            }
        });

        //Graph Settings Panel
        final JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(optionsPanel, BorderLayout.NORTH);

        //Optionable file fileChooser
        fileChooser = new JFileChooser(lastPath) {

            @Override
            protected JDialog createDialog(Component parent) throws HeadlessException {
                dialog = super.createDialog(parent);
                dialog.setSize(640, 480);
                Component c = dialog.getContentPane().getComponent(0);
                if (c != null && c instanceof JComponent) {
                    Insets insets = ((JComponent) c).getInsets();
                    southPanel.setBorder(BorderFactory.createEmptyBorder(0, insets.left, insets.bottom, insets.right));
                } else {
                    southPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 10, 2));
                }
                dialog.getContentPane().add(southPanel, BorderLayout.SOUTH);
                return dialog;
            }

            @Override
            public void approveSelection() {
                if (selectedSettingUI != null) {
                    selectedSettingUI.apply(true);
                }
                super.approveSelection();
            }
        };
        fileChooser.setDialogTitle(NbBundle.getMessage(ImportControllerUIImpl.class, "ImportFileChooserUIImpl.fileImporter.fileChooser.title"));
        fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DialogFileFilter fileFilter = (DialogFileFilter) evt.getNewValue();

                FileImporterBuilder<I> builder = getFileImporterBuilder(builderClass, fileFilter);
                //Options panel enabling
                optionsButton.setEnabled(fileFilter != null && controller.getOptionUI(builder) != null);
                //Settings panel enabling
                Importer.SettingUI settingUI = controller.getSettingUI(builder);
                if (selectedSettingUI != null) {
                    southPanel.remove(selectedSettingUI.getPanel());
                }
                if (settingUI != null) {
                    settingUI.setup(controller.getImporter(builder));
                    southPanel.add(settingUI.getPanel(), BorderLayout.CENTER);
                    southPanel.revalidate();
                    southPanel.repaint();
                } else if (selectedSettingUI != null) {
                    southPanel.revalidate();
                    southPanel.repaint();
                }
                selectedSettingUI = settingUI;

                //Selected file extension change
                selectedFilter = fileFilter;
                if (selectedFile != null && selectedFilter != null) {
                    String fileName = selectedFile.getName();
                    String directoryPath = fileChooser.getCurrentDirectory().getAbsolutePath();
                    if (fileName.lastIndexOf(".") != -1) {
                        fileName = fileName.substring(0, fileName.lastIndexOf("."));
                        fileName = fileName.concat(selectedFilter.getExtensions().get(0));
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
        for (FileType fileType : controller.getFileTypes(builderClass)) {
            DialogFileFilter fileFilter = new DialogFileFilter(fileType.getName());
            fileFilter.addExtensions(fileType.getExtensions());
            if (defaultFilter == null) {
                defaultFilter = fileFilter;
            }
            fileChooser.addChoosableFileFilter(fileFilter);
        }
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(defaultFilter);
    }

    private FileImporterBuilder<I> getFileImporterBuilder(Class<? extends FileImporterBuilder<I>> builderClass, DialogFileFilter fileFilter) {
        for (FileImporterBuilder<I> fileImporterBuilder : Lookup.getDefault().lookupAll(builderClass)) {
            for (FileType fileType : fileImporterBuilder.getFileTypes()) {
                DialogFileFilter tempFilter = new DialogFileFilter(fileType.getName());
                tempFilter.addExtensions(fileType.getExtensions());
                if (tempFilter.equals(fileFilter)) {
                    return fileImporterBuilder;
                }
            }
        }
        return null;
    }

    @Override
    public int showOpenDialog(Component parent) {
        return fileChooser.showOpenDialog(parent);
    }

    @Override
    public File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }

    @Override
    public JFileChooser getFileChooser() {
        return fileChooser;
    }
}
