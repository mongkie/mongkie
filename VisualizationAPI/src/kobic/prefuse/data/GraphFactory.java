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
package kobic.prefuse.data;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.Table;
import static prefuse.data.Graph.*;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GraphFactory {

    public static Graph createDefault() {
        Graph g = new Graph(false);
        g.setNodeLabelField(null);
        g.setEdgeLabelField(null);
        return g;
    }

    public static Graph create(String nodeLabel) {
        Graph g = new Graph(
                new Schema(new String[]{nodeLabel},
                new Class[]{String.class},
                new Object[]{null}).instantiate(),
                new Schema(new String[]{DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY},
                new Class[]{int.class, int.class},
                new Object[]{-1, -1}).instantiate(),
                false, null, DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY);
        g.setNodeLabelField(nodeLabel);
        g.setEdgeLabelField(null);
        return g;
    }

    public static Graph create(String nodeLabel, String sourceKey, String targetKey, String edgeLabel, boolean directed) {
        Graph g = new Graph(
                new Schema(new String[]{nodeLabel},
                new Class[]{String.class},
                new Object[]{null}).instantiate(),
                new Schema(new String[]{sourceKey, targetKey, edgeLabel},
                new Class[]{int.class, int.class, String.class},
                new Object[]{-1, -1, null}).instantiate(),
                false, null, sourceKey, targetKey);
        g.setNodeLabelField(nodeLabel);
        g.setEdgeLabelField(edgeLabel);
        return g;
    }

    public static Graph create(String nodeLabel, String sourceKey, String targetKey, boolean directed) {
        Graph g = new Graph(
                new Schema(new String[]{nodeLabel},
                new Class[]{String.class},
                new Object[]{null}).instantiate(),
                new Schema(new String[]{sourceKey, targetKey},
                new Class[]{int.class, int.class},
                new Object[]{-1, -1}).instantiate(),
                false, null, sourceKey, targetKey);
        g.setNodeLabelField(nodeLabel);
        g.setEdgeLabelField(null);
        return g;
    }

    public static Graph create(Table nodeTable, Table edgeTable,
            String nodeKey, String nodeLabel, String sourceKey, String targetKey, String edgeLabel, boolean directed) {
        Graph g = new Graph(nodeTable, edgeTable, directed, nodeKey, sourceKey, targetKey);
        g.setNodeLabelField(nodeLabel);
        g.setEdgeLabelField(edgeLabel);
        return g;
    }

    public static Graph create(Schema nodeSchema, Schema edgeSchema,
            String nodeKey, String nodeLabel, String sourceKey, String targetKey, String edgeLabel, boolean directed) {
        return create(nodeSchema.instantiate(), edgeSchema.instantiate(),
                nodeKey, nodeLabel, sourceKey, targetKey, edgeLabel, directed);
    }

    public static Graph create(Table nodeTable, String nodeKey, String nodeLabel, boolean directed) {
        Graph g = new Graph(nodeTable, directed, nodeKey, DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY);
        g.setNodeLabelField(nodeLabel);
        g.setEdgeLabelField(null);
        return g;
    }
}
