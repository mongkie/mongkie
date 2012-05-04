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
package org.mongkie.importer;

import org.mongkie.importer.spi.Importer;
import org.mongkie.importer.spi.Processor;
import org.mongkie.importer.spi.Processor.Hint;
import prefuse.data.Graph;

/**
 * A container is created each time data are imported by <b>importers</b>. Its role is to host all data
 * collected by importers during import process. After pushing data in the container, its content can be
 * analyzed to verify its validity and then be processed by <b>processors</b>. Thus containers are
 * <b>created</b> and <b>returned as a result</b> by importers and <b>processed</b> by processors.
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 * @see Importer
 * @see Processor
 */
public interface Container<T> {

    public void setResult(T result);

    public T getResult();

    /**
     * Set the source of the data put in the container. Could be a file name.
     * @param source the original source of data.
     * @throws NullPointerException if <code>source</code> is <code>null</code>
     */
    public void setSource(String source);

    /**
     * If exists, returns the source of the data.
     * @return the source of the data, or <code>null</code> if source is not defined.
     */
    public String getSource();

    /**
     * Set a report this container can use to report issues detected when loading the container. Report
     * are used to log info and issues during import process. Only one report can be associated to a
     * container.
     * @param report set <code>report</code> as the default report for this container
     * @throws NullPointerException if <code>report</code> is <code>null</code>
     */
    public void setReport(Report report);

    /**
     * Returns the report associated to this container, if exists.
     * @return the report set for this container or <code>null</code> if no report is defined
     */
    public Report getReport();

    /**
     * This method must be called after the loading is complete and before processing. Its aim is to verify data consistency as a whole.
     * @return <code>true</code> if container data is consistent, <code>false</code> otherwise
     */
    public boolean verify();

    /**
     * Add hints for processing the result, a {@link Graph} instance from the importer.
     * 
     * @param hints hints for processors,
     * <b>if the hints is <code>empty</code> or <code>null</code> then the hint list will be cleared</b>
     * @see #isProcessorHinted(org.mongkie.importer.spi.Processor.Hint[]) 
     * @see Processor
     */
    public void addProcessorHint(Hint... hints);

    /**
     * Returns true if this container hinted by specified hints.
     * 
     * @param hints hints for processors
     * @return true if this container hinted by specified hints
     * @see #addProcessorHint(org.mongkie.importer.spi.Processor.Hint[]) 
     * @see Processor
     */
    public boolean isProcessorHinted(Hint... hints);
}
