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
package org.mongkie.kopath;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import org.mongkie.kopath.spi.PathwayDatabase;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
@XmlRootElement
public class Pathway implements Transferable {

    private int db;
    private String name;
    private String id;
    public static final DataFlavor DATA_FLAVOR = new DataFlavor(Pathway.class, "pathway");

    public void setDb(int db) {
        this.db = db;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDb() {
        return db;
    }

    public Pathway() {
    }

    public Pathway(int db, String name, String id) {
        this.db = db;
        this.name = name;
        this.id = id;
    }

    public PathwayDatabase getDatabase() {
        return PathwayDatabase.Lookup.valueOf(db);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor == DATA_FLAVOR) {
            return this;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    @Override
    public String toString() {
        return "Pathway{" + "db=" + PathwayDatabase.Lookup.valueOf(db).getName() + ",name=" + name + ",id=" + id + '}';
    }
}
