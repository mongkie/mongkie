package org.mongkie.ui.enrichment.go.util;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter extends FileFilter {

    private final String description;
    private final String extension;

    public ExtensionFileFilter(String description, String extension) {
        this.description = description;
        this.extension = extension;
    }

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        return file.getName().toLowerCase().endsWith("." + extension);
    }

    @Override
    public String getDescription() {
        return description;
    }
}
