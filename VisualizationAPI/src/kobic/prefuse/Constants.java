/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse;

import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

/**
 * 
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public abstract class Constants implements Config {

    public static final String GRAPH = Graph.GRAPH;
    public static final String NODES = PrefuseLib.getGroupName(GRAPH, Graph.NODES);
    public static final String EDGES = PrefuseLib.getGroupName(GRAPH, Graph.EDGES);
    public static final String NODE_LABEL = "decorators_node_label";
    public static final Schema DEFAULT_NODE_DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();

    static {
        DEFAULT_NODE_DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        DEFAULT_NODE_DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, Config.COLOR_DEFAULT_NODE_TEXT);
        DEFAULT_NODE_DECORATOR_SCHEMA.setDefault(VisualItem.FONT, Config.FONT_DEFAULT_NODETEXT);
    }
    public static final String EDGE_LABEL = "decorators_edge_label";
    public static final Schema DEFAULT_EDGE_DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();

    static {
        DEFAULT_EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        DEFAULT_EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, Config.COLOR_DEFAULT_EDGE_TEXT);
        DEFAULT_EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.FONT, Config.FONT_DEFAULT_EDGETEXT);
//        DEFAULT_EDGE_DECORATOR_SCHEMA.setDefault(VisualItem.VISIBLE, false);
    }
    public static final String AGGREGATE_LABEL = "decorators_aggregate_label";
    public static final Schema DEFAULT_AGGR_DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();

    static {
        DEFAULT_AGGR_DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        DEFAULT_AGGR_DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR, Config.COLOR_DEFAULT_AGGR_TEXT);
        DEFAULT_AGGR_DECORATOR_SCHEMA.setDefault(VisualItem.FONT, Config.FONT_DEFAULT_AGGRTEXT);
    }
    public static final String IMAGE_PATH = "kobic/prefuse/resources/";
}
