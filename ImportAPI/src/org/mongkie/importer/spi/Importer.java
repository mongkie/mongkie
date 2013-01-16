/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.importer.spi;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.mongkie.importer.Container;
import org.mongkie.importer.ImportController;
import org.mongkie.importer.Report;
import org.openide.WizardDescriptor.Panel;

/**
 * Interface for classes which imports data from files, databases, streams or other sources.
 * <p>
 * Importers are built from {@link ImporterBuilder} services and can be configured
 * by {@link OptionUI} classes.
 *
 * @author Mathieu Bastian
 * @see ImportController
 */
public interface Importer<C extends Container> {

    public C createContainer();

    /**
     * Run the import processes.
     * @return          <code>true</code> if the import is successful or
     *                  <code>false</code> if it has been canceled
     */
    public boolean execute(C container);

    public boolean cancel();

    /**
     * Returns the import report, filled with logs and potential issues.
     * @return          the import report
     */
    public Report getReport();

    /**
     * Define importer settings user interface.
     * <p>
     * Declared in the system as services (i.e. singleton), the role of UI classes
     * is to provide user interface to configure importers and remember last used
     * settings if needed.
     * <p>
     * To be recognized by the system, implementations must just add the following annotation:
     * <pre>@ServiceProvider(service=Importer.OptionUI.class)</pre>
     *
     * @author Mathieu Bastian
     * @see Importer
     */
    public static interface OptionUI<I extends Importer> {

        /**
         * Link the UI to the importer and therefore to settings values. This method
         * is called after <code>getPanel()</code> to push settings.
         *
         * @param importer  the importer that settings is to be set
         */
        public void load(I importer);

        /**
         * Returns the importer settings panel.
         *
         * @return a settings panel, or <code>null</code>
         */
        public JPanel getPanel();

        /**
         * Notify UI the settings panel has been closed and that new values can be
         * written.
         *
         * @param ok    <code>true</code> if user clicked OK or <code>false</code>
         *                  if CANCEL.
         */
        public void apply(boolean ok);

        /**
         * Returns <code>true</code> if this UI belongs to the given importer.
         *
         * @param importer  the builder of the importer that has to be tested
         * @return          <code>true</code> if the UI is matching with <code>importer</code>,
         *                  <code>false</code> otherwise.
         */
        public boolean isUIForImporter(ImporterBuilder builder);
    }

    /**
     * Same with {@link org.mongkie.importer.spi.Importer.OptionUI} but this UI is in-placed into the FileChooser
     */
    public static interface SettingUI<I extends Importer> {

        /**
         * Link the UI to the importer and therefore to settings values. This method
         * is called after <code>getPanel()</code> to push settings.
         *
         * @param importer  the importer that settings is to be set
         */
        public void load(I importer);

        /**
         * Returns the importer settings panel.
         *
         * @return a settings panel, or <code>null</code>
         */
        public JPanel getPanel();

        /**
         * Notify UI the settings panel has been closed and that new values can be
         * written.
         *
         * @param ok    <code>true</code> if user clicked OK or <code>false</code>
         *                  if CANCEL.
         */
        public void apply();

        /**
         * Returns <code>true</code> if this UI belongs to the given importer.
         *
         * @param importer  the builder of the importer that has to be tested
         * @return          <code>true</code> if the UI is matching with <code>importer</code>,
         *                  <code>false</code> otherwise.
         */
        public boolean isUIForImporter(ImporterBuilder builder);
    }

    /**
     * Define importer settings wizard user interface.
     * <p>
     * Declared in the system as services (i.e. singleton), the role of UI classes
     * is to provide user interface to configure importers and remember last used
     * settings if needed. This service is designed to provide the different panels
     * part of a spigot import wizard.
     * <p>
     * To be recognized by the system, implementations must just add the following annotation:
     * <pre>@ServiceProvider(service=Importer.WizardUI.class)</pre>
     *
     * @author Mathieu Bastian
     * @see SpigotImporter
     */
    public static interface WizardUI<I extends Importer> {

        /**
         * There are two levels for wizard UIs, the category and then the display name.
         * Returns the importer category.
         * @return          the importer category
         */
        public String getCategory();

        /**
         * Returns the description for this importer
         * @return          the description test
         */
        public String getDescription();

        /**
         * Returns wizard panels.
         * @return          panels of the current importer
         */
        public Panel[] getPanels();

        /**
         * Configure <code>panel</code> with previously remembered settings. This method
         * is called after <code>getPanels()</code> to push settings.
         *
         * @param panel     the panel that settings are to be set
         */
        public void setup(Panel panel);

        /**
         * Notify UI the settings panel has been closed and that new values can be
         * written. Settings can be read in <code>panel</code> and written
         * <code>importer</code>.
         * @param importer  the importer that settings are to be written
         * @param panel     the panel that settings are read
         */
        public void apply(I importer, Panel panel);

        /**
         * Returns <code>true</code> if this UI belongs to the given importer.
         *
         * @param importer  the builder of the importer that has to be tested
         * @return          <code>true</code> if the UI is matching with <code>importer</code>,
         *                  <code>false</code> otherwise.
         */
        public boolean isUIForImporter(ImporterBuilder builder);
    }

    /**
     *
     * @author Yeongjun Jang <yjjang@kribb.re.kr>
     */
    public static interface MenuUI {

        public String getDisplayName();

        public Icon getIcon();

        public boolean isEnabled();

        public void performAction();
    }
}
