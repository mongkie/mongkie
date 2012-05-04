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
package org.mongkie.kopath.spi;

import java.util.Collection;
import java.util.List;
import org.mongkie.kopath.Pathway;
import org.openide.util.lookup.Lookups;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public interface PathwayDatabase {

    public int getCode();

    public String getName();

    public int getSize();

    public List<Pathway> getPathways();

    public int countPathway(String... genes);

    public int countPathway(String pathway, boolean like);

    public List<Pathway> searchPathway(String... genes);

    public List<Pathway> searchPathway(String pathway, boolean like);

    public static final class Lookup {

        private static int size = values().size();

        public static Collection<? extends PathwayDatabase> values() {
//            return org.openide.util.Lookup.getDefault().lookupAll(PathwayDatabase.class);
            return Lookups.metaInfServices(Thread.currentThread().getContextClassLoader()).lookupAll(PathwayDatabase.class);
        }

        public static PathwayDatabase valueOf(int code) {
            for (PathwayDatabase pdb : values()) {
                if (pdb.getCode() == code) {
                    return pdb;
                }
            }
            throw new IllegalArgumentException("Can not find a pathway database for the code: " + code);
        }

        public static PathwayDatabase valueOf(String name) {
            for (PathwayDatabase pdb : values()) {
                if (pdb.getName().equals(name)) {
                    return pdb;
                }
            }
            throw new IllegalArgumentException("Can not find a pathway database for the name: " + name);
        }

        public static int size() {
            return size;
        }

        public static String[] names() {
            String[] names = new String[size];
            int i = 0;
            for (PathwayDatabase pdb : values()) {
                names[i++] = pdb.getName();
            }
            return names;
        }
    }
}
