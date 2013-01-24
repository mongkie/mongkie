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
package org.mongkie.kopath.viz;

import java.util.HashMap;
import java.util.Map;
import static org.mongkie.kopath.Config.*;
import prefuse.data.Schema;
import prefuse.util.ColorLib;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class Config {

    public static final String ROLE_PATHWAY = "PathwayRole";
    public static final Schema NODEOUTLINE_FOR_PATHWAYLEVEL = new Schema(
            new String[]{FIELD_NAME, FIELD_PUBLICID, FIELD_TYPE, FIELD_LOCATION, FIELD_FEATURE, FIELD_SUBNODES},
            new Class[]{String.class, String.class, String.class, String.class, String.class, int[].class},
            new Object[]{null, null, null, null, null, null}).lockSchema();
    public static final Schema NODEPROPERTIES_FOR_PATHWAYLEVEL = new Schema(
            new String[]{FIELD_NAME, FIELD_TYPE, FIELD_PUBLICID, FIELD_PUBLICIDDBNAME, FIELD_SYMBOL, FIELD_FULLNAME, FIELD_GENETYPE, FIELD_DESCRIPTION, FIELD_LOCATION, FIELD_FEATURE, FIELD_SUBNODES},
            new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class},
            new Object[]{null, null, null, null, null, null, null, null, null, null, null}).lockSchema();
    public static final Schema EDGEOUTLINE_FOR_PATHWAYLEVEL = new Schema(
            new String[]{FIELD_INTERACTIONID, FIELD_INTERACTIONTYPE, FIELD_NODENAMEA, FIELD_NODENAMEB, FIELD_CONTROLTYPE, FIELD_NODENAMEC},
            new Class[]{String.class, String.class, String.class, String.class, String.class, String.class},
            new Object[]{null, null, null, null, null, null}).lockSchema();
    public static final Schema NODEOUTLINE_FOR_ENTITYLEVEL = new Schema(
            new String[]{FIELD_NAME, FIELD_PUBLICID, FIELD_PUBLICIDDBNAME, FIELD_TYPE, FIELD_SUBNODES},
            new Class[]{String.class, String.class, String.class, String.class, int[].class},
            new Object[]{null, null, null, null, null}).lockSchema();
    public static final Schema NODEPROPERTIES_FOR_ENTITYLEVEL = new Schema(
            new String[]{FIELD_NAME, FIELD_TYPE, FIELD_PUBLICID, FIELD_PUBLICIDDBNAME, FIELD_SYMBOL, FIELD_FULLNAME, FIELD_GENETYPE, FIELD_DESCRIPTION, FIELD_SUBNODES},
            new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class},
            new Object[]{null, null, null, null, null, null, null, null, null}).lockSchema();
    public static final Schema EDGEOUTLINE_FOR_ENTITYLEVEL = new Schema(
            new String[]{FIELD_PATHWAYNAME, FIELD_INTERACTIONID, FIELD_INTERACTIONTYPE, FIELD_NODENAMEA, FIELD_NODENAMEB, FIELD_CONTROLTYPE, FIELD_DATABASE},
            new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class},
            new Object[]{null, null, null, null, null, null, null}).lockSchema();
    public static final Map<String, String> NODEOUTLINE_COLUMN_NAMES = new HashMap<String, String>();

    static {
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_PUBLICID, "Public ID");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_PUBLICIDDBNAME, "ID Database");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_LOCALID, "Local ID");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_SUBNODES, "Sub Entities");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_DATABASE, "Pathway Database");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_SYMBOL, "Symbol");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_GENETYPE, "Gene type");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_FULLNAME, "Full name");
        NODEOUTLINE_COLUMN_NAMES.put(FIELD_DESCRIPTION, "Description");
    }
    public static final Map<String, String> EDGEOUTLINE_COLUMN_NAMES = new HashMap<String, String>();

    static {
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_PATHWAYID, "Pathway ID");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_PATHWAYNAME, "Pathway Name");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_INTERACTIONID, "Interaction ID");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_INTERACTIONTYPE, "Interaction Type");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_NODENAMEA, "Node A");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_NODENAMEB, "Node B");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_CONTROLTYPE, "Control Type");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_NODENAMEC, "Controller");
        EDGEOUTLINE_COLUMN_NAMES.put(FIELD_MOLECULAREVENT, "Molecular Event");
    }

    public static String getNodeOutlineColumnName(String name) {
        return NODEOUTLINE_COLUMN_NAMES.containsKey(name) ? NODEOUTLINE_COLUMN_NAMES.get(name) : name;
    }

    public static String getEdgeOutlineColumnName(String name) {
        return EDGEOUTLINE_COLUMN_NAMES.containsKey(name) ? EDGEOUTLINE_COLUMN_NAMES.get(name) : name;
    }
    public static final int[] PATHWAYDB_EDGECOLORS = new int[]{
        ColorLib.rgb(70, 84, 80), ColorLib.rgb(255, 80, 0), ColorLib.rgb(27, 215, 23), ColorLib.rgb(151, 74, 186), ColorLib.rgb(23, 152, 255)
    };
    public static String FIELD_ISEXPANDING = "IsExpanding";
}
