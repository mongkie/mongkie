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

import org.mongkie.importer.ImportController;

/**
 * Factory class for building importer instances. Declared in the system as
 * services (i.e. singleton), the role of builders is simply the create new
 * instances of particular importer on demand.
 * <p>
 * To be recognized by the system, implementations must just add the following annotation:
 * <pre>@ServiceProvider(service=ImporterBuilder.class)</pre>
 *
 * @author Mathieu Bastian
 * @see ImportController
 */
public interface ImporterBuilder<I extends Importer> {

    /**
     * Builds a new importer instance, ready to be used.
     * DO NOT CALL THIS METHOD DIRECTLY, USE {@link ImportController#getImporter(org.mongkie.importer.spi.ImporterBuilder)} INSTEAD.
     * 
     * @return  a new importer
     * @see ImportController#getImporter(org.mongkie.importer.spi.ImporterBuilder) 
     */
    public I buildImporter();

    /**
     * Returns the name of this builder
     * @return  the name of this importer
     */
    public String getName();
}
