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
package org.mongkie.kopath.util;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.mongkie.kopath.Config.FIELD_NAME;
import static org.mongkie.kopath.Config.FIELD_UID;
import prefuse.data.Graph;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class Utilities {

    public static String getCsvStringFrom(String[] values) {
        String csv = Arrays.toString(values);
        return csv.substring(1, csv.length() - 1).replaceAll(", ", ",");
    }

    public static String[] trim(String[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    public static String[] getEntitiesAsStringArrayFromUIDs(Graph g, int... uids) {
        if (uids == null || uids.length == 0) {
            return new String[]{};
        }
        String[] names = new String[uids.length];
        for (int i = 0; i < names.length; i++) {
            int row = -1;
            try {
                row = g.getNodeTable().getIndex(FIELD_UID).get(uids[i]);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Utilities.class.getName()).severe(ex.getMessage());
                Logger.getLogger(Utilities.class.getName()).log(Level.SEVERE, "Invalid UID: {0}", uids[i]);
            }
            names[i] = (row < 0) ? "NA" : g.getNode(row).getString(FIELD_NAME);
        }
        return names;
    }

    public static String getEntitiesAsStringFromUIDs(Graph g, int... uids) {
        String[] names = getEntitiesAsStringArrayFromUIDs(g, uids);
        if (names.length > 0) {
            String entities = Arrays.toString(names);
            return entities.substring(1, entities.length() - 1);
        } else {
            return "EMPTY";
        }
    }
}
