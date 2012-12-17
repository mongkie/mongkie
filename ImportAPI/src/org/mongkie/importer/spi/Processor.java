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

import javax.swing.JPanel;
import org.mongkie.importer.Container;
import org.mongkie.importer.ImportController;
import org.mongkie.visualization.MongkieDisplay;

/**
 * Interface that define the way data are <b>unloaded</b> from container and
 * appended to the display.
 * <p>
 * The purpose of processors is to unload data from the import container
 * and push it to the display, with various strategy. For instance
 * a processor could either create a new display or append data to the
 * current display, managing doubles.
 *
 * @author Mathieu Bastian
 * @see ImportController
 */
public interface Processor<C extends Container> {

    /**
     * Process data <b>from</b> the container <b>to</b> the display. This task
     * is done after an importer pushed data to the container.
     * @see Importer
     */
    public void process();

    /**
     * Sets the data container. The processor's job is to get data from the container
     * and append it to the display.
     * @param container the container where data are
     */
    public void setContainer(C container);

    /**
     * Sets the destination display for the data in the container. If no display
     * is provided, the current display will be used.
     * @param display the display where data are to be pushed
     */
    public void setDisplay(MongkieDisplay display);

    public MongkieDisplay getDisplay();

    /**
     * Returns the processor name.
     * @return the processor display name
     */
    public String getDisplayName();

    public boolean isEnabled(C container);

    /**
     * Define processor settings user interface.
     * <p>
     * Declared in the system as services (i.e. singleton), the role of UI classes
     * is to provide user interface to configure processors and remember last used
     * settings if needed. User interface for processors are shown when the import
     * report is closed and can access the container before the process started.
     * <p>
     * To be recognized by the system, implementations must just add the following annotation:
     * <pre>@ServiceProvider(service=Processor.UI.class)</pre>
     *
     * @author Mathieu Bastian
     * @see Processor
     */
    public static interface UI<C extends Container, P extends Processor<C>> {

        /**
         * Link the UI to the processor and therefore to settings values. This method
         * is called after <code>getPanel()</code> to push settings.
         *
         * @param processor  the processor that settings is to be set
         */
        public void setup(P processor);

        /**
         * Returns the processor settings panel.
         *
         * @return a settings panel, or <code>null</code>
         */
        public JPanel getPanel();

        /**
         * Returns the title of settings dialog.
         * 
         * @return the title of settings dialog
         */
        public String getTitle();

        /**
         * Notify UI the settings panel has been closed and that new values can be
         * written.
         *
         */
        public void apply(boolean update);

        /**
         * Returns <code>true</code> if this UI belongs to the given processor.
         *
         * @param processor  the processor that has to be tested
         * @return          <code>true</code> if the UI is matching with <code>processor</code>,
         *                  <code>false</code> otherwise.
         */
        public boolean isUIForProcessor(Processor processor);

        /**
         * Returns <code>true</code> if the processor this UI represents is valid for
         * the <code>container</code>. Processors could be specific to some type of data
         * and this method can provide this information.
         * @param container the container that is to be processed
         * @return          <code>true</code> if the processor this UI represents is
         *                  valid for <code>container</code>.
         */
        public boolean isValid(C container);
    }

    public static enum Hint {

        NONE, NODETABLE_NOT_AVAILABLE, EDGETABLE_NOT_AVAILABLE
    }
}
