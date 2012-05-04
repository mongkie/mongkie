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
package org.mongkie.series;

import java.io.InputStream;
import org.mongkie.importer.Container;
import org.mongkie.importer.Report;
import org.mongkie.importer.spi.FileImporter;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SeriesImporter implements FileImporter {

    private boolean hasHeaderRecord = true;
    private String keyField;
    private InputStream in;
    private String title = "UNTITLED";

    public boolean hasHeaderRecord() {
        return hasHeaderRecord;
    }

    public void setHasHeaderRecord(boolean hasHeaderRecord) {
        this.hasHeaderRecord = hasHeaderRecord;
    }

    public String getKeyField() {
        return keyField;
    }

    public void setKeyField(String keyField) {
        this.keyField = keyField;
    }

    @Override
    public Container createContainer() {
        return null;
    }

    @Override
    public boolean execute(Container container) {
        // Delegated to SeriesController
        return false;
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public Report getReport() {
        return null;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
