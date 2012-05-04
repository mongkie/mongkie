package org.mongkie.ui.enrichment.go.util;

import org.mongkie.lib.widgets.file.SingleFileChooserBuilder;
import gobean.GoProjectSetting;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.filechooser.FileFilter;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class UIUtilities {

    public static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("0.####E00");

//    public static void colorLog(String tabTitle, Color color, String text) {
//        if (isRcpMode()) {
//            InputOutput io = IOProvider.getDefault().getIO(tabTitle, false);
//            try {
//                IOColorLines.println(io, text, color);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
//        } else {
//            System.out.println(text);
//        }
//    }
    public void copySelectedTableToClipboard(JTable table) {
        TransferHandler handler = table.getTransferHandler();
        if (handler != null) {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            handler.exportToClipboard(table, cb, TransferHandler.COPY);
        }
    }

//    public static FileSystem getApplicationRootFileSystem() {
//        return new LocalFileSystem();
//    }
//
//    public static FileObject getFileObjectFromUserDirectory(String filePath) throws PropertyVetoException, IOException {
//        LocalFileSystem fileSystem = new LocalFileSystem();
//        fileSystem.setRootDirectory(new File(System.getProperty("user.home")));
//        return fileSystem.findResource(filePath);
//    }
    public static String getInputLine(String text, String title) {
        NotifyDescriptor.InputLine d = new NotifyDescriptor.InputLine(text, title);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
            return d.getInputText();
        } else {
            return null;
        }
    }

//    public static PrintWriter getOutputWriter(String tabTitle, boolean flag) {
//        if (isRcpMode()) {
//            InputOutput io = IOProvider.getDefault().getIO(tabTitle, flag);
//            OutputWriter writer = io.getOut();
//            return writer;
//        } else {
//            return new PrintWriter(System.out);
//        }
//    }
    public static float getSaturationFromP(Double p) {
        if (null == p) {
            return Float.MIN_VALUE;
        }
        if (Double.NaN == p) {
            return Float.MIN_VALUE;
        }
        float logP = (float) Math.log10(p);
        if (logP < MIN_LOG10_P) {
            return 1.f;
        } else {
            return 1.f - (MIN_LOG10_P - logP) / MIN_LOG10_P;
        }
    }

//    public static FileSystem getUserHomeFileSystem() {
//        try {
//            LocalFileSystem fileSystem = new LocalFileSystem();
//            fileSystem.setRootDirectory(new File(System.getProperty("user.home")));
//            return fileSystem;
//        } catch (Exception ex) {
//            Logger.getLogger(UIUtilities.class.getName()).log(Level.SEVERE, null, ex);
//            throw new RuntimeException(ex);
//        }
//    }
    public static boolean isRcpMode() {
        Properties props = System.getProperties();
        if (props.containsKey("netbeans.user")) {
            return true;
        } else {
            return false;
        }
    }

    public static void viewComponentInJDialog(JComponent component) {
        JDialog f = new JDialog(null, ModalityType.APPLICATION_MODAL);
        f.setLayout(new BorderLayout());
        f.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        f.add(component, BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
    }

    public static void writeToFile(File file, CharSequence contents) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.append(contents);
            StatusDisplayer.getDefault().setStatusText("Saved to \"" + file + "\".");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    public static String readFromFile(File file) {
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            StatusDisplayer.getDefault().setStatusText("Read from \"" + file + "\".");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return result.toString();
    }

    public static List<Action> actionsForPath(String path) {
        List<Action> actions = new ArrayList<Action>();
        for (Lookup.Item<Object> item : Lookups.forPath(path).lookupResult(Object.class).allItems()) {
            if (Action.class.isAssignableFrom(item.getType())) {
                Object instance = item.getInstance();
                if (instance != null) {
                    actions.add((Action) instance);
                }
            } else if (JSeparator.class.isAssignableFrom(item.getType())) {
                actions.add(null);
            } else {
                Logger.getLogger(UIUtilities.class.getName()).log(Level.WARNING, "Unrecognized object of {0} found in actions path {1}", new Object[]{item.getType(), path});
            }
        }
        return actions;
    }
//    public static JPopupMenu getTopComponentPopupMenu(JComponent component) {
//        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, component);
//        JPopupMenu popup = new JPopupMenu();
//        boolean separatorJustAdded = false;
//        for (Action action : tc.getActions()) {
//            if (action == null) {
//                if (!separatorJustAdded) {
//                    popup.addSeparator();
//                    separatorJustAdded = true;
//                }
//            } else {
//                JMenuItem wrappedAction = new JMenuItem(action);
//                wrappedAction.setText(removeAmpersand(action.getValue(Action.NAME)));
//                popup.add(wrappedAction);
//                separatorJustAdded = false;
//            }
//        }
//        return popup;
//    }
    public static final SingleFileChooserBuilder.SelectionApprover OVERWRITE_APPROVER = new SingleFileChooserBuilder.SelectionApprover() {

        @Override
        public boolean approve(File selected) {
            if (selected.isFile()) {
                NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation("Overwrite file: " + selected + " ?", NotifyDescriptor.YES_NO_OPTION);
                Object response = DialogDisplayer.getDefault().notify(descriptor);
                if (response.equals(NotifyDescriptor.YES_OPTION)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    };
//    private static String removeAmpersand(Object actionName) {
//        return ((String) actionName).replaceAll("&", "");
//    }
    private static final float MIN_LOG10_P = -7.f;

    public static String getProjectDirectoryPath() {
        GoProjectSetting projectSetting = Lookup.getDefault().lookup(GoProjectSetting.class);
        if (projectSetting != null) {
            String directory = projectSetting.getProjectDirectoryPath();
            File directoryFile = new File(directory);
            if (directoryFile.isDirectory()) {
                return directory;
            } else {
                return System.getProperty("user.home");
            }
        } else {
            return System.getProperty("user.home");
        }
    }

    public static File getProjectDirectory() {
        return new File(getProjectDirectoryPath());
    }

    public static File getOpenFile(String builderID, String title, File workingDirectory, FileFilter filter) {
        File openFile = new SingleFileChooserBuilder(builderID).setTitle(title).
                setFileFilter(filter).
                setDefaultWorkingDirectory(workingDirectory).
                setApproveText("Open").
                showOpenDialog();
        return openFile;
    }

    public static File getOpenFile(String builderID, String title, String extension, String extensionDescription, File workingDirectory) {
        File openFile = new SingleFileChooserBuilder(builderID).setTitle(title).
                setFileFilter(new ExtensionFileFilter(extensionDescription, extension)).
                setDefaultWorkingDirectory(workingDirectory).
                setApproveText("Open").
                showOpenDialog();
        return openFile;
    }

    public static File getOpenFile(String builderID, String title, String extension, String extensionDescription) {
        return getOpenFile(builderID, title, extension, extensionDescription, getProjectDirectory());
    }

    public static File getSaveFile(String builderID, String title, String extension, String extensionDescription) {
        File saveFile = new SingleFileChooserBuilder(builderID).setTitle(title).
                setFileFilter(new ExtensionFileFilter(extensionDescription, extension)).
                setDefaultWorkingDirectory(getProjectDirectory()).
                setSelectionApprover(OVERWRITE_APPROVER).
                setApproveText("SAVE").
                showSaveDialog();
        return saveFile;
    }
}
