package prefuse.data;

import java.util.*;
import prefuse.data.column.Column;
import prefuse.data.event.ColumnListener;
import prefuse.data.event.EventConstants;
import prefuse.data.event.GraphListener;
import prefuse.data.event.TableListener;
import prefuse.data.expression.Predicate;
import prefuse.data.tuple.*;
import prefuse.data.util.Index;
import prefuse.data.util.NeighborIterator;
import prefuse.util.PrefuseConfig;
import prefuse.util.TypeLib;
import prefuse.util.collections.*;

/**
 * <p>A Graph models a network of edges connected by a collection of edges. Both
 * edges and edges can have any number of associated data fields. Additionally,
 * edges are either directed or undirected, indicating a possible directionality
 * of the connection. Each edge has both a source node and a target node, for a
 * directed edge this indicates the directionality, for an undirected edge this
 * is just an artifact of the order in which the edges were specified when
 * removed to the graph. </p>
 *
 * <p>Both edges and edges are represented by backing data {@link Table}
 * instances storing the data attributes. For edges, this table must also
 * contain a source node field and a target node field. The default column name
 * for these fields are {@link #DEFAULT_SOURCE_KEY} and
 * {@link #DEFAULT_TARGET_KEY}, but these can be configured by the graph
 * constructor. These columns should be integer valued, and contain either the
 * row number for a corresponding node in the node table, or another unique
 * identifier for a node. In this second case, the unique identifier must be
 * included as a data field in the node table. This name of this column can be
 * configured using the appropriate graph constructor. The default column name
 * for this field is {@link #DEFAULT_NODE_KEY}, which by default evaluates to
 * null, indicating that no special node key should be used, just the direct
 * node table row numbers. Though the source, target, and node key values
 * completely specify the graph linkage structure, to make graph operations more
 * efficient an additional table is maintained internally by the Graph class,
 * storing node indegree and outdegree counts and adjacency lists for the
 * inlinks and outlinks for all edges.</p>
 *
 * <p>Graph edges and edges can be accessed by application code by either using
 * the row numbers of the node and edge tables, which provide unique ids for
 * each, or using the {@link prefuse.data.Node} and {@link prefuse.data.Edge}
 * classes -- {@link prefuse.data.Tuple} instances that provide object-oriented
 * access to both node or edge data values and the backing graph structure. Node
 * and Edge tuples are maintained by special TupleManager instances contained
 * within this Graph. By default, they are not accessible from the backing node
 * or edge tables directly. The reason for this is that these tables might be
 * used in multiple graphs simultaneously. For example, a node data table could
 * be used in a number of different graphs, exploring different possible
 * linkages between those node. In short, use this Graph instance to request
 * iterators over Node or Edge tuples, not the backing data tables.</p>
 *
 * <p>All Graph instances also support an internally generated spanning tree,
 * provided by the {@link #getSpanningTree()} or {@link #getSpanningTree(Node)}
 * methods. This is particularly useful in visualization contexts that use an
 * underlying tree structure to compute a graph layout.</p>
 *
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class Graph extends CompositeTupleSet {

    /**
     * Indicates incoming edges (inlinks)
     */
    public static final int INEDGES = 0;
    /**
     * Indicates outgoing edges (outlinks)
     */
    public static final int OUTEDGES = 1;
    /**
     * Indicates all edges, regardless of direction
     */
    public static final int UNDIRECTED = 2;
    /**
     * Default data field used to label a node
     */
    public static final String DEFAULT_LABEL_COLUMN = PrefuseConfig.get("data.graph.labelColumn");
    /**
     * Default data field used to uniquely identify a node
     */
    public static final String DEFAULT_NODE_KEY = PrefuseConfig.get("data.graph.nodeKey");
    /**
     * Default data field used to denote the source node in an edge table
     */
    public static final String DEFAULT_SOURCE_KEY = PrefuseConfig.get("data.graph.sourceKey");
    /**
     * Default data field used to denote the target node in an edge table
     */
    public static final String DEFAULT_TARGET_KEY = PrefuseConfig.get("data.graph.targetKey");
    /**
     * Data group name to identify the edges of this graph
     */
    public static final String NODES = PrefuseConfig.get("data.graph.nodeGroup");
    /**
     * Data group name to identify the edges of this graph
     */
    public static final String EDGES = PrefuseConfig.get("data.graph.edgeGroup");
    public static final String GRAPH = "graph";
    // -- auxiliary data structures -----
    /**
     * Table containing the adjacency lists for the graph
     */
    protected Table m_links;
    /**
     * TupleManager for managing Node tuple instances
     */
    protected TupleManager m_nodeTuples;
    /**
     * TupleManager for managing Edge tuple instances
     */
    protected TupleManager m_edgeTuples;
    /**
     * Indicates if this graph is directed or undirected
     */
    protected boolean m_directed = false;
    /**
     * The spanning tree over this graph
     */
    protected SpanningTree m_spanning = null;
    /**
     * The node key field (for the Node table)
     */
    protected String m_nkey;
    /**
     * The source node key field (for the Edge table)
     */
    protected String m_skey;
    /**
     * The target node key field (for the Edge table)
     */
    protected String m_tkey;
    /**
     * Reference to an index over the node key field
     */
    protected Index m_nidx;
    /**
     * Indicates if the key values are of type long
     */
    protected boolean m_longKey = false;
    /**
     * Update listener
     */
    private Listener m_listener;
    /**
     * Listener list
     */
    private CopyOnWriteArrayList m_listeners = new CopyOnWriteArrayList();

    // ------------------------------------------------------------------------
    // Constructors
    /**
     * Creates a new, empty undirected Graph.
     */
    public Graph() {
        this(false);
    }

    /**
     * Creates a new, empty Graph.
     *
     * @param directed true for directed edges, false for undirected
     */
    public Graph(boolean directed) {
        this(new Table(), directed);
    }

    /**
     * Create a new Graph using the provided table of node data and an empty set
     * of edges.
     *
     * @param edges the backing table to use for node data. Node instances of
     * this graph will get their data from this table.
     * @param directed true for directed edges, false for undirected
     */
    public Graph(Table nodes, boolean directed) {
        this(nodes, directed, DEFAULT_NODE_KEY,
                DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY);
    }

    /**
     * Create a new Graph using the provided table of node data and an empty set
     * of edges.
     *
     * @param edges the backing table to use for node data. Node instances of
     * this graph will get their data from this table.
     * @param directed true for directed edges, false for undirected
     * @param nodeKey data field used to uniquely identify a node. If this field
     * is null, the node table row numbers will be used
     * @param sourceKey data field used to denote the source node in an edge
     * table
     * @param targetKey data field used to denote the target node in an edge
     * table
     */
    public Graph(Table nodes, boolean directed, String nodeKey,
            String sourceKey, String targetKey) {
        Table edges = new Table();
        edges.addColumn(sourceKey, int.class, new Integer(-1));
        edges.addColumn(targetKey, int.class, new Integer(-1));
        init(nodes, edges, directed, nodeKey, sourceKey, targetKey);
    }

    /**
     * Create a new Graph, using node table row numbers to uniquely identify
     * edges in the edge table's source and target fields.
     *
     * @param edges the backing table to use for node data. Node instances of
     * this graph will get their data from this table.
     * @param edges the backing table to use for edge data. Edge instances of
     * this graph will get their data from this table.
     * @param directed true for directed edges, false for undirected
     */
    public Graph(Table nodes, Table edges, boolean directed) {
        this(nodes, edges, directed,
                DEFAULT_NODE_KEY, DEFAULT_SOURCE_KEY, DEFAULT_TARGET_KEY);
    }

    /**
     * Create a new Graph, using node table row numbers to uniquely identify
     * edges in the edge table's source and target fields.
     *
     * @param edges the backing table to use for node data. Node instances of
     * this graph will get their data from this table.
     * @param edges the backing table to use for edge data. Edge instances of
     * this graph will get their data from this table.
     * @param directed true for directed edges, false for undirected
     * @param sourceKey data field used to denote the source node in an edge
     * table
     * @param targetKey data field used to denote the target node in an edge
     * table
     */
    public Graph(Table nodes, Table edges, boolean directed,
            String sourceKey, String targetKey) {
        init(nodes, edges, directed, DEFAULT_NODE_KEY, sourceKey, targetKey);
    }

    /**
     * Create a new Graph.
     *
     * @param edges the backing table to use for node data. Node instances of
     * this graph will get their data from this table.
     * @param edges the backing table to use for edge data. Edge instances of
     * this graph will get their data from this table.
     * @param directed true for directed edges, false for undirected
     * @param nodeKey data field used to uniquely identify a node. If this field
     * is null, the node table row numbers will be used
     * @param sourceKey data field used to denote the source node in an edge
     * table
     * @param targetKey data field used to denote the target node in an edge
     * table
     */
    public Graph(Table nodes, Table edges, boolean directed,
            String nodeKey, String sourceKey, String targetKey) {
        init(nodes, edges, directed, nodeKey, sourceKey, targetKey);
    }

    public Graph createPartial(Set<Node> nodes) {
        Table nodeTable = getNodeTable().getSchema().instantiate();
        Table edgeTable = getEdgeTable().getSchema().instantiate();
        Map<Node, Tuple> original2New = new HashMap<Node, Tuple>();
        for (Node n : nodes) {
            original2New.put(n, nodeTable.addTuple(n));
        }
        for (Iterator<Edge> edgeIter = edges(); edgeIter.hasNext();) {
            Edge e = edgeIter.next();
            Node s = e.getSourceNode();
            Node t = e.getTargetNode();
            if (nodes.contains(s) && nodes.contains(t)) {
                Tuple source = original2New.get(s);
                Tuple target = original2New.get(t);
                Tuple edge = edgeTable.addTuple(e);
                if (m_longKey) {
                    edge.setLong(m_skey, m_nkey == null ? (long) source.getRow() : source.getLong(m_nkey));
                    edge.setLong(m_tkey, m_nkey == null ? (long) target.getRow() : target.getLong(m_nkey));
                } else {
                    edge.setInt(m_skey, m_nkey == null ? source.getRow() : source.getInt(m_nkey));
                    edge.setInt(m_tkey, m_nkey == null ? target.getRow() : target.getInt(m_nkey));
                }
            }
        }
        Graph partial = new Graph(nodeTable, edgeTable, m_directed, m_nkey, m_skey, m_tkey);
        partial.m_nlabel = m_nlabel;
        return partial;
    }

    @Deprecated
    public Graph createPartial(Predicate edgesCondition) {

        Table nodeTable = getNodeTable().getSchema().instantiate();
        Table edgeTable = getEdgeTable().select(edgesCondition);

        Graph g = new Graph(nodeTable, true, m_nkey, m_skey, m_tkey);

        Iterator<Tuple> edges = edgeTable.tuples();
        while (edges.hasNext()) {
            Tuple edge = edges.next();
            int sourceKey = edge.getInt(m_skey), targetKey = edge.getInt(m_tkey);
            if (g.getNodeFromKey(sourceKey) == null) {
                g.getNodes().addTuple(getNodeFromKey(sourceKey));
            }
            if (g.getNodeFromKey(targetKey) == null) {
                g.getNodes().addTuple(getNodeFromKey(targetKey));
            }
        }

        g.setEdgeTable(edgeTable);

        g.rebuildEnclosedLinks(this);

        return g;
    }

    public Graph createEmpty() {
        Graph g = new Graph(getNodeTable().getSchema().instantiate(), isDirected(), m_nkey, m_skey, m_tkey);
//        g.addNode().setInt(g.getNodeKeyField(), 0);
        g.setEdgeTable(getEdgeTable().getSchema().instantiate());
        return g;
    }

    public Graph copy() {
        Table nodeTable = getNodeTable().getSchema().instantiate();
        nodeTable.addTuples(getNodes());
        Table edgeTable = getEdgeTable().getSchema().instantiate();
        edgeTable.addTuples(getEdges());
        return new Graph(nodeTable, edgeTable, isDirected(), getNodeKeyField(), getEdgeSourceField(), getEdgeTargetField());
    }

    // ------------------------------------------------------------------------
    // Initialization
    /**
     * Initialize this Graph instance.
     *
     * @param edges the node table
     * @param edges the edge table
     * @param directed the edge directionality
     * @param nodeKey data field used to uniquely identify a node
     * @param sourceKey data field used to denote the source node in an edge
     * table
     * @param targetKey data field used to denote the target node in an edge
     * table
     */
    protected final void init(Table nodes, Table edges, boolean directed,
            String nodeKey, String sourceKey, String targetKey) {
        // sanity check
        if ((nodeKey != null
                && !TypeLib.isIntegerType(nodes.getColumnType(nodeKey)))
                || !TypeLib.isIntegerType(edges.getColumnType(sourceKey))
                || !TypeLib.isIntegerType(edges.getColumnType(targetKey))) {
            throw new IllegalArgumentException(
                    "Incompatible column types for graph keys");
        }

        removeAllSets();
        super.addSet(EDGES, edges);
        super.addSet(NODES, nodes);

        m_directed = directed;

        // INVARIANT: these three should all reference the same type
        // currently limited to int
        m_nkey = nodeKey;
        m_nlabel = nodeKey;
        m_skey = sourceKey;
        m_tkey = targetKey;

        // set up indices
        if (nodeKey != null) {
            if (nodes.getColumnType(nodeKey) == long.class) {
                m_longKey = true;
            }
            nodes.index(nodeKey);
            m_nidx = nodes.getIndex(nodeKey);
        }

        // set up tuple manager
        if (m_nodeTuples == null) {
            m_nodeTuples = new TupleManager(nodes, this, TableNode.class);
        }
        m_edgeTuples = new TupleManager(edges, this, TableEdge.class);

        // set up node attribute optimization
        initLinkTable();

        // set up listening
        if (m_listener == null) {
            m_listener = new Listener();
        }
        nodes.addTableListener(m_listener);
        edges.addTableListener(m_listener);
        m_listener.setEdgeTable(edges);

//        empty = getNodeCount() == 0;
        empty = m_links.getTupleCount() == 0;
    }

    /**
     * Set the tuple managers used to manage the Node and Edge tuples of this
     * Graph.
     *
     * @param ntm the TupleManager to use for edges
     * @param etm the TupleManager to use for edges
     */
    public void setTupleManagers(TupleManager ntm, TupleManager etm) {
        if (!Node.class.isAssignableFrom(ntm.getTupleType())) {
            throw new IllegalArgumentException("The provided node "
                    + "TupleManager must generate tuples that implement "
                    + "the Node interface.");
        }
        if (!Edge.class.isAssignableFrom(etm.getTupleType())) {
            throw new IllegalArgumentException("The provided edge "
                    + "TupleManager must generate tuples that implement "
                    + "the Edge interface.");
        }
        m_nodeTuples = ntm;
        m_edgeTuples = etm;
    }

    /**
     * Dispose of this graph. Unregisters this graph as a listener to its
     * included tables.
     */
    public void dispose() {
        getNodeTable().removeTableListener(m_listener);
        getEdgeTable().removeTableListener(m_listener);
    }

    /**
     * Updates this graph to use a different edge structure for the same edges.
     * All other settings will remain the same (e.g., directionality, keys)
     *
     * @param edges the new edge table.
     */
    public void setEdgeTable(Table edges) {
        Table oldEdges = getEdgeTable();
        oldEdges.removeTableListener(m_listener);
        m_edgeTuples.invalidateAll();
        m_links.clear();

        init(getNodeTable(), edges, m_directed, m_nkey, m_skey, m_tkey);
    }

    // ------------------------------------------------------------------------
    // Data Access Optimization
    /**
     * Initialize the link table, which holds adjacency lists for this graph.
     */
    protected void initLinkTable() {
        // set up cache of node data
        m_links = createLinkTable();

        IntIterator edges = getEdgeTable().rows();
        while (edges.hasNext()) {
            updateDegrees(edges.nextInt(), 1);
        }
    }

    public void resetLinkTable() {
        setEdgeTable(getEdgeTable());
    }

    /**
     * Instantiate and return the link table.
     *
     * @return the created link table
     */
    protected Table createLinkTable() {
        return LINKS_SCHEMA.instantiate(getNodeTable().getMaximumRow() + 1);
    }

    /**
     * Internal method for updating the linkage of this graph.
     *
     * @param e the edge id for the updated link
     * @param incr the increment value, 1 for an removed link, -1 for a removed
     * link
     */
    protected void updateDegrees(int e, int incr) {
        if (!getEdgeTable().isValidRow(e)) {
            return;
        }
        int s = getSourceNode(e);
        int t = getTargetNode(e);
        if (s < 0 || t < 0) {
            return;
        }
        updateDegrees(e, s, t, incr);
        if (incr < 0) {
            m_edgeTuples.invalidate(e);
        }
    }

    /**
     * Internal method for updating the linkage of this graph.
     *
     * @param e the edge id for the updated link
     * @param s the source node id for the updated link
     * @param t the target node id for the updated link
     * @param incr the increment value, 1 for an removed link, -1 for a removed
     * link
     */
    protected void updateDegrees(int e, int s, int t, int incr) {
        int od = m_links.getInt(s, OUTDEGREE);
        int id = m_links.getInt(t, INDEGREE);
        // update adjacency lists
        if (incr > 0) {
            // add links
            addLink(OUTLINKS, od, s, e);
            addLink(INLINKS, id, t, e);
        } else if (incr < 0) {
            // remove links
            remLink(OUTLINKS, od, s, e);
            remLink(INLINKS, id, t, e);
        }
        // update degree counts
        m_links.setInt(s, OUTDEGREE, od + incr);
        m_links.setInt(t, INDEGREE, id + incr);
        // link structure changed, invalidate spanning tree
        m_spanning = null;
    }

    /**
     * Internal method for adding a link to an adjacency list
     *
     * @param field which adjacency list (inlinks or outlinks) to use
     * @param len the length of the adjacency list
     * @param n the node id of the adjacency list to use
     * @param e the edge to add to the list
     */
    protected void addLink(String field, int len, int n, int e) {
        int[] array = (int[]) m_links.get(n, field);
        if (array == null) {
            array = new int[]{e};
            m_links.set(n, field, array);
            return;
        } else if (len == array.length) {
            int[] narray = new int[Math.max(3 * array.length / 2, len + 1)];
            System.arraycopy(array, 0, narray, 0, array.length);
            array = narray;
            m_links.set(n, field, array);
        }
        array[len] = e;
    }

    /**
     * Internal method for removing a link from an adjacency list
     *
     * @param field which adjacency list (inlinks or outlinks) to use
     * @param len the length of the adjacency list
     * @param n the node id of the adjacency list to use
     * @param e the edge to remove from the list
     * @return true if the link was removed successfully, false otherwise
     */
    protected boolean remLink(String field, int len, int n, int e) {
        int[] array = (int[]) m_links.get(n, field);
        for (int i = 0; i < len; ++i) {
            if (array[i] == e) {
                System.arraycopy(array, i + 1, array, i, len - i - 1);
                return true;
            }
        }
        return false;
    }

    /**
     * Update the link table to accomodate an inserted or deleted node
     *
     * @param r the node id, also the row number into the link table
     * @param removed indicates if a node was removed or removed
     */
    protected void updateNodeData(int r, boolean added) {
        if (added) {
            m_links.addRow();
        } else {
            m_nodeTuples.invalidate(r);
            m_links.removeRow(r);
        }
//        empty = getNodeCount() == 0;
        empty = m_links.getTupleCount() == 0;
    }
    private transient boolean empty = true;

    public boolean isEmpty() {
        return empty;
    }
    // ------------------------------------------------------------------------
    // Key Transforms

    /**
     * Get the data field used to uniquely identify a node
     *
     * @return the data field used to uniquely identify a node
     */
    public String getNodeKeyField() {
        return m_nkey;
    }
    private String m_nlabel, m_elabel;

    public String getEdgeLabelField() {
        return m_elabel;
    }

    public void setEdgeLabelField(String elabel) {
        this.m_elabel = elabel;
    }

    public String getNodeLabelField() {
        return m_nlabel;
    }

    public void setNodeLabelField(String nlabel) {
        this.m_nlabel = nlabel;
    }

    /**
     * Get the data field used to denote the source node in an edge table.
     *
     * @return the data field used to denote the source node in an edge table.
     */
    public String getEdgeSourceField() {
        return m_skey;
    }

    /**
     * Get the data field used to denote the target node in an edge table.
     *
     * @return the data field used to denote the target node in an edge table.
     */
    public String getEdgeTargetField() {
        return m_tkey;
    }

    /**
     * Given a node id (a row number in the node table), get the value of the
     * node key field.
     *
     * @param node the node id
     * @return the value of the node key field for the given node
     */
    public long getKey(int node) {
        return m_nkey == null ? node : getNodeTable().getLong(node, m_nkey);
    }

    /**
     * Given a value of the node key field, get the node id (the row number in
     * the node table).
     *
     * @param key a node key field value
     * @return the node id (the row number in the node table)
     */
    public int getNodeIndex(long key) {
        if (m_nidx == null) {
            return (int) key;
        } else {
            int idx = m_longKey ? m_nidx.get(key) : m_nidx.get((int) key);
            return idx < 0 ? -1 : idx;
        }
    }

    // ------------------------------------------------------------------------
    // Graph Mutators
    /**
     * Add row to the node table, thereby adding a node to the graph.
     *
     * @return the node id (node table row number) of the removed node
     */
    public int addNodeRow() {
        return getNodeTable().addRow();
    }

    /**
     * Add a new node to the graph.
     *
     * @return the new Node instance
     */
    public Node addNode() {
        int nrow = addNodeRow();
        return (Node) m_nodeTuples.getTuple(nrow);
    }

    /**
     * Add an edge to the graph. Both multiple edges between two edges and edges
     * from a node to itself are allowed.
     *
     * @param s the source node id
     * @param t the target node id
     * @return the edge id (edge table row number) of the removed edge
     */
    public int addEdge(int s, int t) {
        // get keys for the edges
        long key1 = getKey(s);
        long key2 = getKey(t);

        // add edge row, set source/target fields
        Table edges = getEdgeTable();
        int r = edges.addRow();
        if (m_longKey) {
            edges.setLong(r, m_skey, key1);
            edges.setLong(r, m_tkey, key2);
        } else {
            edges.setInt(r, m_skey, (int) key1);
            edges.setInt(r, m_tkey, (int) key2);
        }
        return r;
    }

    /**
     * Add an edge to the graph.
     *
     * @param s the source Node
     * @param t the target Node
     * @return the new Edge instance
     */
    public Edge addEdge(Node s, Node t) {
        nodeCheck(s, true);
        nodeCheck(t, true);
        int e = addEdge(s.getRow(), t.getRow());
        return getEdge(e);
    }

    /**
     * Remove a node from the graph, also removing all incident edges.
     *
     * @param node the node id (node table row number) of the node to remove
     * @return true if the node was successfully removed, false if the node id
     * was not found or was not valid
     */
    public boolean removeNode(int node) {
        return removeNode(node, false);
    }

    public boolean removeNode(int node, boolean removeIsolatedNodes) {

        Table nodeTable = getNodeTable();

        if (nodeTable.isValidRow(node)) {
            removeEdges(node, removeIsolatedNodes);
        }

        boolean removed = nodeTable.removeRow(node);

        return removed;
    }

    public void removeIsolatedNodes() {
        Iterator<Integer> nodes = nodeRows();
        while (nodes.hasNext()) {
            int n = nodes.next();
            if (isIsolated(n)) {
                removeNode(n);
            }
        }
    }

    /**
     * Remove a node from the graph, also removing all incident edges.
     *
     * @param n the Node to remove from the graph
     * @return true if the node was successfully removed, false if the node was
     * not found in this graph
     */
    public boolean removeNode(Node n) {
        nodeCheck(n, true);
        return removeNode(n.getRow());
    }

    public boolean removeNode(Node n, boolean removeDegreeZeroNodes) {
        nodeCheck(n, true);
        return removeNode(n.getRow(), removeDegreeZeroNodes);
    }

    /**
     * Remove an edge from the graph.
     *
     * @param edge the edge id (edge table row number) of the edge to remove
     * @return true if the edge was successfully removed, false if the edge was
     * not found or was not valid
     */
    public boolean removeEdge(int edge) {
        return getEdgeTable().removeRow(edge);
    }

    public void removeEdges(int node, String linkDir, boolean removeIsolatedNodes) {
        int degree = (linkDir.equals(INLINKS)) ? getInDegree(node) : getOutDegree(node);
        if (degree > 0) {
            int[] links = (int[]) m_links.get(node, linkDir);
            for (int i = degree; --i >= 0;) {
                int adjacent = getAdjacentNode(links[i], node);
                removeEdge(links[i]);
                if (!removeIsolatedNodes
                        || adjacent == node) { // self linked!!
                    continue;
                }
                if (isIsolated(adjacent)) {
                    removeNode(adjacent);
                }
            }
        }
    }

    public void removeEdges(int node, boolean removeIsolatedNodes) {
        removeEdges(node, INLINKS, removeIsolatedNodes);
        removeEdges(node, OUTLINKS, removeIsolatedNodes);
    }

    /**
     * Remove an edge from the graph.
     *
     * @param e the Edge to remove from the graph
     * @return true if the edge was successfully removed, false if the edge was
     * not found in this graph
     */
    public boolean removeEdge(Edge e) {
        edgeCheck(e, true);
        return removeEdge(e.getRow());
    }

    /**
     * Internal method for clearing the edge table, removing all edges.
     */
    protected void clearEdges() {
        getEdgeTable().clear();
    }

    // ------------------------------------------------------------------------
    // Node Accessor Methods
    /**
     * Internal method for checking the validity of a node.
     *
     * @param n the Node to check for validity
     * @param throwException true if this method should throw an Exception when
     * an invalid node is encountered
     * @return true is the node is valid, false if invalid
     */
    protected boolean nodeCheck(Node n, boolean throwException) {
        if (!n.isValid()) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Node must be valid.");
            }
            return false;
        }
        Graph ng = n.getGraph();
        if (ng != this && ng.m_spanning != this) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Node must be part of this Graph.");
            }
            return false;
        }
        return true;
    }

    /**
     * Get the collection of edges as a TupleSet. Returns the same result as
     * {@link CompositeTupleSet#getSet(String)} using {@link #NODES} as the
     * parameter.
     *
     * @return the edges of this graph as a TupleSet instance
     */
    public TupleSet getNodes() {
        return getSet(NODES);
    }

    /**
     * Get the backing node table.
     *
     * @return the table of node values
     */
    public Table getNodeTable() {
        return (Table) getSet(NODES);
    }

    /**
     * Get the number of edges in this graph.
     *
     * @return the number of edges
     */
    public int getNodeCount() {
        return getNodeTable().getRowCount();
    }

    /**
     * Get the Node tuple instance corresponding to a node id.
     *
     * @param n a node id (node table row number)
     * @return the Node instance corresponding to the node id
     */
    public Node getNode(int n) {
        return (Node) m_nodeTuples.getTuple(n);
    }

    public Node getEquivalentNode(Tuple anotherNode) {

        if (anotherNode.getTable() == getNodeTable()) {
            return getNode(anotherNode.getRow());
        }

        String nodeKeyField = getNodeKeyField();
        return getNodeFromKey(anotherNode.getInt(nodeKeyField));
    }

    public Node addEquivalentNode(Tuple anotherNode) {
        Node addedNode = getEquivalentNode(anotherNode);
        return addedNode == null ? getNode(getNodes().addTuple(anotherNode).getRow()) : addedNode;
    }

    public Edge addEquivalentEdge(Edge anotherEdge) {
        Edge addedEdge =
                getEquivalentEdge(addEquivalentNode(anotherEdge.getSourceNode()), addEquivalentNode(anotherEdge.getTargetNode()));
        return addedEdge == null ? getEdge(getEdges().addTuple(anotherEdge).getRow()) : addedEdge;
    }

    private Edge addEnclosedEdge(Edge anotherEdge) {

        Node source = getEquivalentNode(anotherEdge.getSourceNode());
        Node target = getEquivalentNode(anotherEdge.getTargetNode());
        if (source == null || target == null) {
            return null;
        }

        Edge addedEdge = getEquivalentEdge(source, target);
        return addedEdge == null ? getEdge(getEdges().addTuple(anotherEdge).getRow()) : addedEdge;
    }

    /**
     * Get the Node tuple corresponding to the input node key field value. The
     * node key field is used to find the node id (node table row number), which
     * is then used to retrieve the Node tuple.
     *
     * @param key a node key field value
     * @return the requested Node instance
     */
    public Node getNodeFromKey(long key) {
        int n = getNodeIndex(key);
        return (n < 0 ? null : getNode(n));
    }

    public Node getNodeFrom(String field, int value) {
        Tuple t = getNodeTable().getTuple(field, value);
        if (t == null) {
            return null;
        }
        return getNode(t.getRow());
    }

    public Set<Node> getNodesFrom(String field, int value) {
        Set<Node> nodes = new HashSet<Node>();
        Set<Tuple> tuples = getNodeTable().getTuples(field, value);
        for (Tuple t : tuples) {
            nodes.add(getNode(t.getRow()));
        }
        return nodes;
    }

    public Node getNodeFrom(String field, Object value) {
        Tuple t = getNodeTable().getTuple(field, value);
        if (t == null) {
            return null;
        }
        return getNode(t.getRow());
    }

    public Set<Node> getNodesFrom(String field, Object value) {
        Set<Node> nodes = new HashSet<Node>();
        Set<Tuple> tuples = getNodeTable().getTuples(field, value);
        for (Tuple t : tuples) {
            nodes.add(getNode(t.getRow()));
        }
        return nodes;
    }

    public Edge getEdgeFrom(String field, Object value) {
        Tuple t = getEdgeTable().getTuple(field, value);
        if (t == null) {
            return null;
        }
        return getEdge(t.getRow());
    }

    public Set<Edge> getEdgesFrom(String field, int value) {
        Set<Edge> edges = new HashSet<Edge>();
        Set<Tuple> tuples = getEdgeTable().getTuples(field, value);
        for (Tuple t : tuples) {
            edges.add(getEdge(t.getRow()));
        }
        return edges;
    }

    public Set<Edge> getEdgesFrom(String field, Object value) {
        Set<Edge> edges = new HashSet<Edge>();
        Set<Tuple> tuples = getEdgeTable().getTuples(field, value);
        for (Tuple t : tuples) {
            edges.add(getEdge(t.getRow()));
        }
        return edges;
    }

    /**
     * Get the in-degree of a node, the number of edges for which the node is
     * the target.
     *
     * @param node the node id (node table row number)
     * @return the in-degree of the node
     */
    public int getInDegree(int node) {
        return m_links.getInt(node, INDEGREE);
    }

    /**
     * Get the in-degree of a node, the number of edges for which the node is
     * the target.
     *
     * @param n the Node instance
     * @return the in-degree of the node
     */
    public int getInDegree(Node n) {
        nodeCheck(n, true);
        return getInDegree(n.getRow());
    }

    /**
     * Get the out-degree of a node, the number of edges for which the node is
     * the source.
     *
     * @param node the node id (node table row number)
     * @return the out-degree of the node
     */
    public int getOutDegree(int node) {
        return m_links.getInt(node, OUTDEGREE);
    }

    /**
     * Get the out-degree of a node, the number of edges for which the node is
     * the source.
     *
     * @param n the Node instance
     * @return the out-degree of the node
     */
    public int getOutDegree(Node n) {
        nodeCheck(n, true);
        return getOutDegree(n.getRow());
    }

    /**
     * Get the degree of a node, the number of edges for which a node is either
     * the source or the target.
     *
     * @param node the node id (node table row number)
     * @return the total degree of the node
     */
    public int getDegree(int node) {
        return getInDegree(node) + getOutDegree(node);
    }

    public boolean isIsolated(int node) {
        int degree = getDegree(node);
        return degree == 0 || (degree == 2 && isSelfLinked(node));
    }

    public boolean isSelfLinked(int node) {
        return getEdge(node, node) != -1;
    }

    /**
     * Get the degree of a node, the number of edges for which a node is either
     * the source or the target.
     *
     * @param n the Node instance
     * @return the total degree of the node
     */
    public int getDegree(Node n) {
        nodeCheck(n, true);
        return getDegree(n.getRow());
    }

    public int getDistinctDegree(Node n) {
        return isSelfLinked(n.getRow()) ? getDegree(n) - 1 : getDegree(n);
    }

    // ------------------------------------------------------------------------
    // Edge Accessor Methods
    /**
     * Indicates if the edges of this graph are directed or undirected.
     *
     * @return true if directed edges, false if undirected edges
     */
    public boolean isDirected() {
        return m_directed;
    }

    public boolean setDirected(boolean directed) {
        boolean old = m_directed;
        m_directed = directed;
        return old;
    }

    /**
     * Internal method for checking the validity of an edge.
     *
     * @param e the Edge to check for validity
     * @param throwException true if this method should throw an Exception when
     * an invalid node is encountered
     * @return true is the edge is valid, false if invalid
     */
    protected boolean edgeCheck(Edge e, boolean throwException) {
        if (!e.isValid()) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Edge must be valid.");
            }
            return false;
        }
        if (e.getGraph() != this) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Edge must be part of this Graph.");
            }
            return false;
        }
        return true;
    }

    /**
     * Get the collection of edges as a TupleSet. Returns the same result as
     * {@link CompositeTupleSet#getSet(String)} using {@link #EDGES} as the
     * parameter.
     *
     * @return the edges of this graph as a TupleSet instance
     */
    public TupleSet getEdges() {
        return getSet(EDGES);
    }

    /**
     * Get the backing edge table.
     *
     * @return the table of edge values
     */
    public Table getEdgeTable() {
        return (Table) getSet(EDGES);
    }

    /**
     * Get the number of edges in this graph.
     *
     * @return the number of edges
     */
    public int getEdgeCount() {
        return getEdgeTable().getRowCount();
    }

    /**
     * Get the Edge tuple instance corresponding to an edge id.
     *
     * @param e an edge id (edge table row number)
     * @return the Node instance corresponding to the node id
     */
    public Edge getEdge(int e) {
        return (e < 0 ? null : (Edge) m_edgeTuples.getTuple(e));
    }

    /**
     * Returns an edge from the source node to the target node. This method
     * returns the first such edge found; in the case of multiple edges there
     * may be more.
     */
    public int getEdge(int source, int target) {
        int outd = getOutDegree(source);
        if (outd > 0) {
            int[] edges = (int[]) m_links.get(source, OUTLINKS);
            for (int i = 0; i < outd; ++i) {
                if (getTargetNode(edges[i]) == target) {
                    return edges[i];
                }
            }
        }
        return -1;
    }

    public List<Integer> getEdges(int source, int target) {
        List<Integer> edgeRowList = new ArrayList<Integer>();
        int outd = getOutDegree(source);
        if (outd > 0) {
            int[] edges = (int[]) m_links.get(source, OUTLINKS);
            for (int i = 0; i < outd; ++i) {
                if (getTargetNode(edges[i]) == target) {
                    edgeRowList.add(edges[i]);
                }
            }
        }
        return edgeRowList;
    }

    /**
     * Get an Edge with given source and target Nodes. There may be times where
     * there are multiple edges between two edges; in those cases this method
     * returns the first such edge found.
     *
     * @param source the source Node
     * @param target the target Node
     * @return an Edge with given source and target edges, or null if no such
     * edge is found.
     */
    public Edge getEdge(Node source, Node target) {
        if (source == null || target == null) {
            return null;
        }
        nodeCheck(source, true);
        nodeCheck(target, true);
        return getEdge(getEdge(source.getRow(), target.getRow()));
    }

    public List<Edge> getEdges(Node source, Node target) {
        List<Edge> edgeList = new ArrayList<Edge>();
        if (source == null || target == null) {
            return edgeList;
        }
        nodeCheck(source, true);
        nodeCheck(target, true);
        for (int row : getEdges(source.getRow(), target.getRow())) {
            edgeList.add(getEdge(row));
        }
        return edgeList;
    }

    public Edge getEquivalentEdge(Tuple source, Tuple target) {
        if (source == null || target == null) {
            return null;
        }
        String nodeKeyField = getNodeKeyField();
        return getEdge(getNodeFromKey(source.getInt(nodeKeyField)), getNodeFromKey(target.getInt(nodeKeyField)));
    }

    public int getNeighborCount(Node n) {
        Iterator<Node> neighbors = neighbors(n);
        int count = 0;
        while (neighbors.hasNext()) {
            neighbors.next();
            count++;
        }
        return count;
    }

    public int getIncomingNeighborCount(Node n) {
        Iterator<Node> neighbors = inNeighbors(n);
        int count = 0;
        while (neighbors.hasNext()) {
            neighbors.next();
            count++;
        }
        return count;
    }

    public int getOutgoingNeighborCount(Node n) {
        Iterator<Node> neighbors = outNeighbors(n);
        int count = 0;
        while (neighbors.hasNext()) {
            neighbors.next();
            count++;
        }
        return count;
    }

    @Deprecated
    public int getNeighborCount(Node n, boolean includeSelf) {
        Iterator<Node> neighbors = neighbors(n);
        int count = 0;
        Node neighbor;
        while (neighbors.hasNext()) {
            neighbor = neighbors.next();
            if (!includeSelf && neighbor == n) {
                continue;
            }
            count++;
        }
        return count;
    }

    public int getNeighborCount(int n) {
        return getNeighborCount(getNode(n));
    }

    private void rebuildEnclosedLinks(Graph from) {
        IntIterator rowsIter = getNodeTable().rows();
        while (rowsIter.hasNext()) {
            link(getNode(rowsIter.nextInt()), from);
        }
    }

    public Set<Node> link(Collection<? extends Tuple> nodes, Graph from) {

        Set<Node> addedNodes = new HashSet();
        for (Tuple node : nodes) {
            Node added = addEquivalentNode(node);
            link(added, from);
            addedNodes.add(added);
        }

        return addedNodes;
    }

    public void link(Node n, Graph from) {
        expand(n, from, true);
    }

    public void expand(Node n, Graph from) {
        expand(n, from, false);
    }

    private void expand(Node n, Graph from, boolean enclosed) {
        if (getNodeTable() != n.getTable()) {
            throw new IllegalArgumentException("a Graph must contain the node to expand.");
        }

        Node N = from.getNodeFromKey(n.getInt(getNodeKeyField()));

        Iterator<Edge> edges = from.edges(N);
        while (edges.hasNext()) {
            if (enclosed) {
                addEnclosedEdge(edges.next());
            } else {
                addEquivalentEdge(edges.next());
            }
        }

        if (!enclosed) {
            rebuildEnclosedLinks(from);
        }
    }

    public void expand(Node n, Graph from, String filter, String value) {

        if (getNodeTable() != n.getTable()) {
            throw new IllegalArgumentException("a Graph must contain the node to expand.");
        }

        Node N = from.getNodeFromKey(n.getInt(getNodeKeyField()));

        Set<Edge> equivalentEdges = from.filteredEdgeSet(N, filter, value);

        for (Edge e : equivalentEdges) {
            addEquivalentEdge(e);
        }

        rebuildEnclosedLinks(from);
    }

    public void expand(Set<Node> nodes, Graph from, boolean intersectedOnly) {

        Set<Node> resultEqNodes = null;

        for (Node n : nodes) {
            Node N = from.getNodeFromKey(n.getInt(getNodeKeyField()));
            if (resultEqNodes == null) {
                resultEqNodes = from.neighborSet(N);
            } else if (intersectedOnly) {
                resultEqNodes.retainAll(from.neighborSet(N));
            } else {
                resultEqNodes.addAll(from.neighborSet(N));
            }
        }

        for (Node N : resultEqNodes) {
            Node added = addEquivalentNode(N);
            link(added, from);
        }
    }

    public void expand(Set<Node> nodes, Graph from, String filter, String value, boolean intersectedOnly) {
        Set<Node> resultEqNodes = null;

        for (Node n : nodes) {
            Node N = from.getNodeFromKey(n.getInt(getNodeKeyField()));
            if (resultEqNodes == null) {
                resultEqNodes = from.filteredNeighborSet(N, filter, value);
            } else if (intersectedOnly) {
                resultEqNodes.retainAll(from.filteredNeighborSet(N, filter, value));
            } else {
                resultEqNodes.addAll(from.filteredNeighborSet(N, filter, value));
            }
        }

        for (Node N : resultEqNodes) {
            Node added = addEquivalentNode(N);
            link(added, from);
        }
    }

    /**
     * Get the source node id (node table row number) for the given edge id
     * (edge table row number).
     *
     * @param edge an edge id (edge table row number)
     * @return the source node id (node table row number)
     */
    public int getSourceNode(int edge) {
        return getNodeIndex(getEdgeTable().getLong(edge, m_skey));
    }

    /**
     * Get the source Node for the given Edge instance.
     *
     * @param e an Edge instance
     * @return the source Node of the edge
     */
    public Node getSourceNode(Edge e) {
        edgeCheck(e, true);
        return getNode(getSourceNode(e.getRow()));
    }

    /**
     * Get the target node id (node table row number) for the given edge id
     * (edge table row number).
     *
     * @param edge an edge id (edge table row number)
     * @return the target node id (node table row number)
     */
    public int getTargetNode(int edge) {
        return getNodeIndex(getEdgeTable().getLong(edge, m_tkey));
    }

    /**
     * Get the target Node for the given Edge instance.
     *
     * @param e an Edge instance
     * @return the target Node of the edge
     */
    public Node getTargetNode(Edge e) {
        edgeCheck(e, true);
        return getNode(getTargetNode(e.getRow()));
    }

    /**
     * Given an edge id and an incident node id, return the node id for the
     * other node connected to the edge.
     *
     * @param edge an edge id (edge table row number)
     * @param node a node id (node table row number). This node id must be
     * connected to the edge
     * @return the adjacent node id
     */
    public int getAdjacentNode(int edge, int node) {
        int s = getSourceNode(edge);
        int d = getTargetNode(edge);

        if (s == node) {
            return d;
        } else if (d == node) {
            return s;
        } else {
            throw new IllegalArgumentException(
                    "Edge is not incident on the input node.");
        }
    }

    /**
     * Given an Edge and an incident Node, return the other Node connected to
     * the edge.
     *
     * @param e an Edge instance
     * @param n a Node instance. This node must be connected to the edge
     * @return the adjacent Node
     */
    public Node getAdjacentNode(Edge e, Node n) {
        edgeCheck(e, true);
        nodeCheck(n, true);
        return getNode(getAdjacentNode(e.getRow(), n.getRow()));
    }

    // ------------------------------------------------------------------------
    // Iterators
    // -- table row iterators ----
    /**
     * Get an iterator over all node ids (node table row numbers).
     *
     * @return an iterator over all node ids (node table row numbers)
     */
    public IntIterator nodeRows() {
        return getNodeTable().rows();
    }

    /**
     * Get an iterator over all edge ids (edge table row numbers).
     *
     * @return an iterator over all edge ids (edge table row numbers)
     */
    public IntIterator edgeRows() {
        return getEdgeTable().rows();
    }

    /**
     * Get an iterator over all edge ids for edges incident on the given node.
     *
     * @param node a node id (node table row number)
     * @return an iterator over all edge ids for edges incident on the given
     * node
     */
    public IntIterator edgeRows(int node) {
        return edgeRows(node, UNDIRECTED);
    }

    /**
     * Get an iterator edge ids for edges incident on the given node.
     *
     * @param node a node id (node table row number)
     * @param direction the directionality of the edges to include. One of
     * {@link #INEDGES} (for in-linking edges), {@link #OUTEDGES} (for
     * out-linking edges), or {@link #UNDIRECTED} (for all edges).
     * @return an iterator over all edge ids for edges incident on the given
     * node
     */
    public IntIterator edgeRows(int node, int direction) {
        if (direction == OUTEDGES) {
            int[] outedges = (int[]) m_links.get(node, OUTLINKS);
            return new IntArrayIterator(outedges, 0, getOutDegree(node));
        } else if (direction == INEDGES) {
            int[] inedges = (int[]) m_links.get(node, INLINKS);
            return new IntArrayIterator(inedges, 0, getInDegree(node));
        } else if (direction == UNDIRECTED) {
            return new CompositeIntIterator(
                    edgeRows(node, OUTEDGES), edgeRows(node, INEDGES));
        } else {
            throw new IllegalArgumentException("Unrecognized edge type: " + direction + ". Type should be one of Graph.OUTEDGES, " + "Graoh.INEDGES, or Graph.ALL");
        }
    }

    /**
     * Get an iterator over all edges that have the given node as a target. That
     * is, edges that link into the given target node.
     *
     * @param node a node id (node table row number)
     * @return an iterator over all edges that have the given node as a target
     */
    public IntIterator inEdgeRows(int node) {
        return edgeRows(node, INEDGES);
    }

    /**
     * Get an iterator over all edges that have the given node as a source. That
     * is, edges that link out from the given source node.
     *
     * @param node a node id (node table row number)
     * @return an iterator over all edges that have the given node as a source
     */
    public IntIterator outEdgeRows(int node) {
        return edgeRows(node, OUTEDGES);
    }

    // -- tuple iterators --
    /**
     * Get an iterator over all edges in the graph.
     *
     * @return an iterator over Node instances
     */
    public Iterator nodes() {
        return m_nodeTuples.iterator(nodeRows());
    }

    /**
     * Get an iterator over all neighbor edges for the given Node in the graph.
     *
     * @param n a Node in the graph
     * @return an iterator over all Nodes connected to the input node
     */
    public Iterator neighbors(Node n) {
        return new NeighborIterator(n, edges(n));
    }

    public Set<Node> neighborSet(Node n, boolean includeSelf) {
        Set<Node> neighborSet = new HashSet();
        Iterator<Node> neighbors = neighbors(n);
        while (neighbors.hasNext()) {
            neighborSet.add(neighbors.next());
        }
        if (!includeSelf && neighborSet.contains(n)) {
            neighborSet.remove(n);
        }
        return neighborSet;
    }

    public Set<Node> neighborSet(Node n) {
        return neighborSet(n, true);
    }

    public Set<Node> filteredNeighborSet(Node n, String field, Object value, boolean includeSelf) {
        Set<Node> neighborSet = new HashSet();
        Iterator<Node> neighborsIter = neighbors(n);
        while (neighborsIter.hasNext()) {
            Node ne = neighborsIter.next();
            if (ne.canGet(field, value.getClass()) && ne.get(field).equals(value)) {
                neighborSet.add(ne);
            }
        }
        if (!includeSelf && neighborSet.contains(n)) {
            neighborSet.remove(n);
        }
        return neighborSet;
    }

    public Set<Node> filteredNeighborSet(Node n, String field, Object value) {
        return filteredNeighborSet(n, field, value, true);

    }

    public Set<Edge> filteredEdgeSet(Node n, String field, Object value, boolean includeSelfLink) {
        Set<Edge> edgeSet = new HashSet();
        Iterator<Edge> edgesIter = edges(n);
        while (edgesIter.hasNext()) {
            Edge edgeIt = edgesIter.next();
            Node target = edgeIt.getAdjacentNode(n);
            if (!includeSelfLink && target == n) {
                continue;
            }
            if (target.canGet(field, value.getClass()) && target.get(field).equals(value)) {
                edgeSet.add(edgeIt);
            }
        }
        return edgeSet;
    }

    public Set<Edge> filteredEdgeSet(Node n, String field, Object value) {
        return filteredEdgeSet(n, field, value, true);
    }

    /**
     * Get an iterator over all in-linking neighbor edges for the given Node.
     *
     * @param n a Node in the graph
     * @return an iterator over all Nodes that point to the input target node
     */
    public Iterator inNeighbors(Node n) {
        return new NeighborIterator(n, inEdges(n));
    }

    /**
     * Get an iterator over all out-linking neighbor edges for the given Node.
     *
     * @param n a Node in the graph
     * @return an iterator over all Nodes pointed to by the input source node
     */
    public Iterator outNeighbors(Node n) {
        return new NeighborIterator(n, outEdges(n));
    }

    /**
     * Get an iterator over all edges in the graph.
     *
     * @return an iterator over Edge instances
     */
    public Iterator edges() {
        return m_edgeTuples.iterator(edgeRows());
    }

    /**
     * Get an iterator over all Edges connected to the given Node in the graph.
     *
     * @param node a Node in the graph
     * @return an iterator over all Edges connected to the input node
     */
    public Iterator edges(Node node) {
        nodeCheck(node, true);
        return m_edgeTuples.iterator(edgeRows(node.getRow(), UNDIRECTED));
    }

    /**
     * Get an iterator over all in-linking edges to the given Node.
     *
     * @param node a Node in the graph
     * @return an iterator over all in-linking edges to the input target node
     */
    public Iterator inEdges(Node node) {
        nodeCheck(node, true);
        return m_edgeTuples.iterator(inEdgeRows(node.getRow()));
    }

    /**
     * Get an iterator over all out-linking edges from the given Node.
     *
     * @param node a Node in the graph
     * @return an iterator over all out-linking edges from the input source node
     */
    public Iterator outEdges(Node node) {
        nodeCheck(node, true);
        return m_edgeTuples.iterator(outEdgeRows(node.getRow()));
    }

    // ------------------------------------------------------------------------
    // TupleSet Interface
    /**
     * Clear this graph, removing all edges and edges.
     *
     * @see prefuse.data.tuple.TupleSet#clear()
     */
    @Override
    public void clear() {
        m_nodeTuples.invalidateAll();
        m_edgeTuples.invalidateAll();
        super.clear();
        m_links.clear();
    }

    /**
     * If the given tuple is a Node or Edge in this graph, remove it.
     *
     * @see prefuse.data.tuple.TupleSet#removeTuple(prefuse.data.Tuple)
     */
    @Override
    public boolean removeTuple(Tuple t) {
        // TODO: check underlying table tuples as well?
        if (t instanceof Node) {
            return removeNode((Node) t);
        } else if (t instanceof Edge) {
            return removeEdge((Edge) t);
        } else {
            throw new IllegalArgumentException(
                    "Input tuple must be part of this graph");
        }
    }

    /**
     * Get a filtered iterator over the edges and edges of this graph.
     *
     * @see
     * prefuse.data.tuple.TupleSet#tuples(prefuse.data.expression.Predicate)
     */
    @Override
    public Iterator tuples(Predicate filter) {
        if (filter == null) {
            return tuples();
        } else {
            return new CompositeIterator(
                    m_edgeTuples.iterator(getEdgeTable().rows(filter)),
                    m_nodeTuples.iterator(getNodeTable().rows(filter)));
        }
    }

    /**
     * Get an iterator over all the edges and edges of this graph. The iterator
     * will return all edges first, then all edges.
     *
     * @see prefuse.data.tuple.TupleSet#tuples()
     */
    @Override
    public Iterator tuples() {
        return new CompositeIterator(edges(), nodes());
    }

    // ------------------------------------------------------------------------
    // Spanning Tree Methods
    /**
     * Return the current spanning tree over this graph. If no spanning tree has
     * been constructed, a SpanningTree rooted at the first valid node found in
     * the node table will be generated.
     *
     * Spanning trees are generated using an unweighted breadth first search
     * over the graph structure.
     *
     * @return a spanning tree over this graph
     * @see #getSpanningTree(Node)
     * @see #clearSpanningTree()
     */
    public Tree getSpanningTree() {
        if (m_spanning == null) {
            return getSpanningTree((Node) nodes().next());
        } else {
            return m_spanning;
        }
    }

    /**
     * Returns a spanning tree rooted at the specified node. If the current
     * spanning tree is alrady rooted at the given node, it is simply returned.
     * Otherwise, the tree is reconstructed at the new root and made the current
     * spanning tree for this Graph instance.
     *
     * Spanning trees are generated using an unweighted breadth first search
     * over the graph structure.
     *
     * @param root the node at which to root the spanning tree.
     * @return a spanning tree over this graph, rooted at the given root
     * @see #getSpanningTree()
     * @see #clearSpanningTree()
     */
    public Tree getSpanningTree(Node root) {
        nodeCheck(root, true);
        if (m_spanning == null) {
            m_spanning = new SpanningTree(this, root);
        } else if (m_spanning.getRoot() != root) {
            m_spanning.buildSpanningTree(root);
        }
        return m_spanning;
    }

    /**
     * Clear the internally stored spanning tree. Any new calls to a
     * getSpanningTree() method will generate a new spanning tree instance as
     * needed.
     *
     * This method is primarily useful for subclasses. For example, calling this
     * method on a Tree instance will revert the state to the original rooted
     * tree such that a sbusequent call to getSpanningTree() will return the
     * backing Tree itself.
     *
     * @see #getSpanningTree()
     * @see #getSpanningTree(Node)
     * @see Tree#getSpanningTree(Node)
     */
    public void clearSpanningTree() {
        m_spanning = null;
    }

    // ------------------------------------------------------------------------
    // Graph Listeners
    /**
     * Add a listener to be notified of changes to the graph.
     *
     * @param listnr the listener to add
     */
    public void addGraphModelListener(GraphListener listnr) {
        if (!m_listeners.contains(listnr)) {
            m_listeners.add(listnr);
        }
    }

    /**
     * Remove a listener from this graph.
     *
     * @param listnr the listener to remove
     */
    public void removeGraphModelListener(GraphListener listnr) {
        m_listeners.remove(listnr);
    }

    /**
     * Removes all listeners on this graph
     */
    public void removeAllGraphModelListeners() {
        m_listeners.clear();
    }

    /**
     * Fire a graph change event
     *
     * @param t the backing table where the change occurred (either a node table
     * or an edge table)
     * @param first the first modified table row
     * @param last the last (inclusive) modified table row
     * @param col the number of the column modified, or
     * {@link prefuse.data.event.EventConstants#ALL_COLUMNS} for operations
     * affecting all columns
     * @param type the type of modification, one of null     {@link prefuse.data.event.EventConstants#INSERT},
     * {@link prefuse.data.event.EventConstants#DELETE}, or
     * {@link prefuse.data.event.EventConstants#UPDATE}.
     */
    protected void fireGraphEvent(Table t,
            int first, int last, int col, int type) {
        String table = (t == getNodeTable() ? NODES : EDGES);

        if (type != EventConstants.UPDATE && type != EventConstants.INSERTED) {
            // fire event to all tuple set listeners
            fireTupleEvent(t, first, last, type);
        }

        if (!m_listeners.isEmpty()) {
            // fire event to all listeners
            Object[] lstnrs = m_listeners.getArray();
            for (int i = 0; i < lstnrs.length; ++i) {
                ((GraphListener) lstnrs[i]).graphChanged(
                        this, table, first, last, col, type);
            }
        }
    }

    public void addGraph(Graph g) {

        if (!getNodeTable().getSchema().isAssignableFrom(g.getNodeTable().getSchema())
                || !getEdgeTable().getSchema().isAssignableFrom(g.getEdgeTable().getSchema())) {
            throw new IllegalArgumentException("Incompatible graph format.");
        }

        Iterator<Edge> edges = g.edges();
        while (edges.hasNext()) {
            addEquivalentEdge(edges.next());
        }
    }

    @Deprecated
    public void addGraphX(Graph g) {

        if (!getNodeTable().getSchema().isAssignableFrom(g.getNodeTable().getSchema())
                || !getEdgeTable().getSchema().isAssignableFrom(g.getEdgeTable().getSchema())) {
            return;
        }

        Iterator<Tuple> tuples = g.getNodes().tuples();
        String nodeKeyField = g.getNodeKeyField();
        while (tuples.hasNext()) {
            Tuple t = tuples.next();
            Node n = getNodeFromKey(t.getInt(nodeKeyField));
            if (n == null) {
                getNodes().addTuple(t);
            }
        }

        tuples = g.getEdges().tuples();
        String sourceKeyField = g.getEdgeSourceField(), targetKeyField = g.getEdgeTargetField();
        while (tuples.hasNext()) {
            Tuple t = tuples.next();
            Edge e = getEdge(getNodeFromKey(t.getInt(sourceKeyField)), getNodeFromKey(t.getInt(targetKeyField)));
            if (e == null) {
                getEdges().addTuple(t);
            }
        }
    }

    public double getWeight(Edge e) {
        return 1.0D;
    }

    public List<Edge> getEdges(List<Node> nodes) {
        List<Edge> edges = new ArrayList<Edge>();
        for (Iterator<Edge> edgeIter = edges(); edgeIter.hasNext();) {
            Edge e = edgeIter.next();
            if (nodes.contains(e.getSourceNode()) && nodes.contains(e.getTargetNode())) {
                edges.add(e);
            }
        }
        return edges;
    }

    public List<Edge> getEdges(Node... nodes) {
        return getEdges(Arrays.asList(nodes));
    }
    // ------------------------------------------------------------------------
    // Table and Column Listener

    /**
     * Listener class for tracking updates from node and edge tables, and their
     * columns that determine the graph linkage structure.
     */
    protected class Listener implements TableListener, ColumnListener {

        private Table m_edges;
        private Column m_scol, m_tcol;
        private int m_sidx, m_tidx;

        public void setEdgeTable(Table edges) {
            // remove any previous listeners
            if (m_scol != null) {
                m_scol.removeColumnListener(this);
            }
            if (m_tcol != null) {
                m_tcol.removeColumnListener(this);
            }
            m_scol = m_tcol = null;
            m_sidx = m_tidx = -1;

            m_edges = edges;

            // register listeners
            if (m_edges != null) {
                m_sidx = edges.getColumnNumber(m_skey);
                m_tidx = edges.getColumnNumber(m_tkey);
                m_scol = edges.getColumn(m_sidx);
                m_tcol = edges.getColumn(m_tidx);
                m_scol.addColumnListener(this);
                m_tcol.addColumnListener(this);
            }
        }

        @Override
        public void tableChanged(Table t, int start, int end, int col, int type) {
            if (!containsSet(t)) {
                throw new IllegalStateException(
                        "Graph shouldn't be listening to an unrelated table");
            }

            if (type != EventConstants.UPDATE && type != EventConstants.INSERTED) {
                if (t == getNodeTable()) {
                    // update the linkage structure table
                    if (col == EventConstants.ALL_COLUMNS) {
                        boolean added = type == EventConstants.INSERT;
                        for (int r = start; r <= end; ++r) {
                            updateNodeData(r, added);
                        }
                    }
                } else {
                    // update the linkage structure table
                    if (col == EventConstants.ALL_COLUMNS) {
                        boolean added = type == EventConstants.INSERT;
                        for (int r = start; r <= end; ++r) {
                            updateDegrees(start, added ? 1 : -1);
                        }
                    }
                }
                // clear the spanning tree reference
                m_spanning = null;
            }
            fireGraphEvent(t, start, end, col, type);
        }

        @Override
        public void columnChanged(Column src, int idx, int prev) {
            columnChanged(src, idx, (long) prev);
        }

        @Override
        public void columnChanged(Column src, int idx, long prev) {
            if (src == m_scol || src == m_tcol) {
                boolean isSrc = src == m_scol;
                int e = m_edges.getTableRow(idx, isSrc ? m_sidx : m_tidx);
                if (e == -1) {
                    return; // edge not in this graph
                }
                int s = getSourceNode(e);
                int t = getTargetNode(e);
                int p = getNodeIndex(prev);
                if (p > -1 && ((isSrc && t > -1) || (!isSrc && s > -1))) {
                    updateDegrees(e, isSrc ? p : s, isSrc ? t : p, -1);
                }
                if (s > -1 && t > -1) {
                    updateDegrees(e, s, t, 1);
                }
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void columnChanged(Column src, int type, int start, int end) {
            // should never be called
            throw new IllegalStateException();
        }

        @Override
        public void columnChanged(Column src, int idx, float prev) {
            // should never be called
            throw new IllegalStateException();
        }

        @Override
        public void columnChanged(Column src, int idx, double prev) {
            // should never be called
            throw new IllegalStateException();
        }

        @Override
        public void columnChanged(Column src, int idx, boolean prev) {
            // should never be called
            throw new IllegalStateException();
        }

        @Override
        public void columnChanged(Column src, int idx, Object prev) {
            // should never be called
            throw new IllegalStateException();
        }
    } // end of inner class Listener
    // ------------------------------------------------------------------------
    // Graph Linkage Schema
    /**
     * In-degree data field for the links table
     */
    protected static final String INDEGREE = "_indegree";
    /**
     * Out-degree data field for the links table
     */
    protected static final String OUTDEGREE = "_outdegree";
    /**
     * In-links adjacency list data field for the links table
     */
    protected static final String INLINKS = "_inlinks";
    /**
     * Out-links adjacency list data field for the links table
     */
    protected static final String OUTLINKS = "_outlinks";
    /**
     * Schema used for the internal graph linkage table
     */
    protected static final Schema LINKS_SCHEMA = new Schema();

    static {
        Integer defaultValue = new Integer(0);
        LINKS_SCHEMA.addColumn(INDEGREE, int.class, defaultValue);
        LINKS_SCHEMA.addColumn(OUTDEGREE, int.class, defaultValue);
        LINKS_SCHEMA.addColumn(INLINKS, int[].class);
        LINKS_SCHEMA.addColumn(OUTLINKS, int[].class);
        LINKS_SCHEMA.lockSchema();
    }
} // end of class Graph

