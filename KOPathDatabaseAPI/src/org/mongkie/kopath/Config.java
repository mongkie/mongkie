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

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class Config {

    public static final String FIELD_UID = "UID";
    public static final String FIELD_NODEID = "NodeId";
    public static final String FIELD_PUBLICID = "PublicId";
    public static final String FIELD_PUBLICIDDBNAME = "PublicIdDB";
    public static final String FIELD_SYMBOL = "symbol";
    public static final String FIELD_FULLNAME = "fullName";
    public static final String FIELD_GENETYPE = "geneType";
    public static final String FIELD_URL = "url";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_NAME = "Name";
    public static final String FIELD_LOCALID = "LocalId";
    public static final String FIELD_TYPE = "Type";
    public static final String FIELD_ITYPE = "IType";
    public static final String FIELD_LOCATION = "Location";
    public static final String FIELD_FEATURE = "State";
    public static final String FIELD_DATABASE = "Database";
    public static final String FIELD_SUBNODES = "SubNodes";
    public static final String FIELD_LOCATIONCHANGED = "locationChanged";
    public static final String FIELD_INLAYOUT = "InLayout";
    public static final String FIELD_CONTROLLIE_EDGEROW = "ControllieEdgeRow";
    public static final String FIELD_ISDIMER = "isDimer";
    public static final String FIELD_ISFAMILY = "isFamily";
    public static final Schema SCHEMA_NODETABLE = new Schema(
            new String[]{FIELD_UID, FIELD_NODEID, FIELD_PUBLICID, FIELD_PUBLICIDDBNAME, FIELD_SYMBOL, FIELD_FULLNAME, FIELD_GENETYPE, FIELD_URL, FIELD_DESCRIPTION, FIELD_NAME, FIELD_LOCALID, FIELD_TYPE, FIELD_ITYPE, FIELD_LOCATION, FIELD_FEATURE, FIELD_DATABASE, FIELD_SUBNODES, FIELD_LOCATIONCHANGED, FIELD_CONTROLLIE_EDGEROW, FIELD_INLAYOUT, FIELD_ISDIMER, FIELD_ISFAMILY},
            new Class[]{int.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, int[].class, boolean.class, int.class, boolean.class, boolean.class, boolean.class},
            new Object[]{-1, null, null, null, null, null, null, null, null, null, null, null, NodeType.MOLECULE.toString(), null, null, null, null, false, -1, true, false, false});
    public static final String FIELD_SOURCEKEY = Graph.DEFAULT_SOURCE_KEY;
    public static final String FIELD_TARGETKEY = Graph.DEFAULT_TARGET_KEY;
    public static final String FIELD_PATHWAYID = "pathwayId";
    public static final String FIELD_PATHWAYNAME = "pathwayName";
    public static final String FIELD_INTERACTIONID = "interactionId";
    public static final String FIELD_INTERACTIONTYPE = "interactionType";
    public static final String FIELD_NODEA = "nodeA";
    public static final String FIELD_NODEB = "nodeB";
    public static final String FIELD_NODENAMEA = "nodeNameA";
    public static final String FIELD_NODENAMEB = "nodeNameB";
    public static final String FIELD_CONTROLTYPE = "controlType";
    public static final String FIELD_CONTROLLER = "controller";
    public static final String FIELD_NODENAMEC = "nodeNameC";
    public static final String FIELD_ISINCLUDE = "IsInclude";
    public static final String FIELD_ISVIRTUAL = "IsVirtual";
    public static final String FIELD_ISCONTROL = "IsControl";
    public static final String FIELD_MOLECULAREVENT = "MolecularEvent";
    public static final Schema SCHEMA_EDGETABLE = new Schema(
            new String[]{FIELD_SOURCEKEY, FIELD_TARGETKEY, FIELD_PATHWAYNAME, FIELD_PATHWAYID, FIELD_INTERACTIONID, FIELD_INTERACTIONTYPE, FIELD_NODENAMEA, FIELD_NODENAMEB, FIELD_CONTROLTYPE, FIELD_CONTROLLER, FIELD_NODENAMEC, FIELD_DATABASE, FIELD_ISINCLUDE, FIELD_ISVIRTUAL, FIELD_ISCONTROL, FIELD_CONTROLLIE_EDGEROW, FIELD_MOLECULAREVENT},
            new Class[]{int.class, int.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, boolean.class, boolean.class, boolean.class, int.class, String.class},
            new Object[]{-1, -1, null, null, null, null, null, null, "-", "-", null, null, false, false, false, -1, null});
    /* Column names in database */
    public static final String RAW_PATHWAYDBID = "pathwayDbId"; // Int type
    public static final String RAW_SUPERNODEID = "superNodeId";
    public static final String RAW_ENTITYID = "participantId";
    public static final String RAW_ENTITYID2 = "entityId";
    public static final String RAW_ENTITYLOCATION = "entityLocation";
    public static final String RAW_ENTITYFEATURE = "entityFeature";
    public static final String RAW_ENTITYNAME = "entityName";
    public static final String RAW_ENTITYTYPE = "entityType";
    public static final String RAW_PUBLICID = "publicId";
    public static final String RAW_PUBLICIDDBNAME = "publicIdDbName";
    public static final String RAW_SUBENTITYID = "subcomponentId";
    public static final String RAW_SUBENTITYID2 = "subentityId";
    public static final String RAW_SUBENTITYLOCATION = "subentityLocation";
    public static final String RAW_SUBENTITYFEATURE = "subentityFeature";
    public static final String RAW_SUBENTITYNAME = "subentityName";
    public static final String RAW_SUBENTITYTYPE = "subentityType";
    public static final String RAW_PATHWAYID = "pathwayId";
    public static final String RAW_PATHWAYNAME = "pathwayName";

    public static Graph createEmptyEachGraph() {
        Graph g = new Graph(SCHEMA_NODETABLE.instantiate(), SCHEMA_EDGETABLE.instantiate(), true, FIELD_UID, FIELD_SOURCEKEY, FIELD_TARGETKEY);
        g.setNodeLabelField(FIELD_NAME);
        g.setEdgeLabelField(null);
        return g;
    }

    public static Graph createEmptyIntegratedGraph() {
        Graph g = new Graph(SCHEMA_NODETABLE.instantiate(), SCHEMA_EDGETABLE.instantiate(), true, FIELD_UID, FIELD_SOURCEKEY, FIELD_TARGETKEY);
        g.setNodeLabelField(FIELD_NAME);
        g.setEdgeLabelField(null);
        return g;
    }

    public static Graph createPathwayGraph(Table nodeTable, Table edgeTable) {
        Graph g = new Graph(nodeTable, edgeTable, true, FIELD_UID, FIELD_SOURCEKEY, FIELD_TARGETKEY);
        g.setNodeLabelField(FIELD_NAME);
        g.setEdgeLabelField(null);
        return g;
    }
}
