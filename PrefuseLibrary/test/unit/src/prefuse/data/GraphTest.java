package prefuse.data;

import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.RegexSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.util.GraphLib;
import static org.junit.Assert.*;

/**
 *
 * @author i-pharm
 */
public class GraphTest {

    private static Graph G;
    private static SearchTupleSet proxy;

    public GraphTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        G = CSVTableReaderTest.readGraph();
        proxy = new RegexSearchTupleSet();
        new SearchQueryBinding(G.getNodes(), CSVTableReaderTest.NODE_NAME, proxy);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        GraphLib.disposeAndClear(G);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testExpansionAndRemoval() {

        Graph g = CSVTableReaderTest.makeCancerPartialGraph(G);
        Iterator<Node> nodes = g.nodes();
        for (int i = 0; i < 3; i++) {
            nodes.next();
        }
        Node gars = nodes.next(); // GARS
        assertEquals(18, g.getNodeCount());
//        assertEquals(26, g.getEdgeCount());
        assertEquals(39, g.getEdgeCount());

        g.expand(gars, G);
        assertEquals(54, g.getNodeCount());
//        assertEquals(69, g.getEdgeCount());
        assertEquals(162, g.getEdgeCount());

        g.removeNode(gars, true);
//        assertEquals(17, g.getNodeCount());
        assertEquals(43, g.getNodeCount());
//        assertEquals(24, g.getEdgeCount());
        assertEquals(117, g.getEdgeCount());

        proxy.search("NARS");
        SearchQueryTest.printResultSet(proxy.resultSet());
        Tuple narsTuple = proxy.resultArray()[0];
        assertEquals(g.getEquivalentNode(narsTuple), g.addEquivalentNode(narsTuple));
//        assertEquals(17, g.getNodeCount());
        assertEquals(43, g.getNodeCount());

        Node nars = g.getNode(g.getEquivalentNode(narsTuple).getRow());
        g.expand(nars, G);
//        assertEquals(26, g.getNodeCount());
        assertEquals(52, g.getNodeCount());
//        assertEquals(33, g.getEdgeCount());
        assertEquals(139, g.getEdgeCount());
        g.removeNode(nars);
//        assertEquals(25, g.getNodeCount());
        assertEquals(51, g.getNodeCount());
//        assertEquals(23, g.getEdgeCount());
        assertEquals(129, g.getEdgeCount());
        g.removeIsolatedNodes();
//        assertEquals(16, g.getNodeCount());
        assertEquals(47, g.getNodeCount());
//        assertEquals(23, g.getEdgeCount());
        assertEquals(129, g.getEdgeCount());

        proxy.search("AARS");
        SearchQueryTest.printResultSet(proxy.resultSet());
        Tuple aarsTuple = proxy.resultArray()[0];
//        assertEquals(null, g.getEquivalentNode(aarsTuple));
        g.addEquivalentNode(aarsTuple);
//        assertEquals(17, g.getNodeCount());
        assertEquals(47, g.getNodeCount());
//        assertEquals(23, g.getEdgeCount());
        assertEquals(129, g.getEdgeCount());

        Node aars = g.getNode(g.getEquivalentNode(aarsTuple).getRow());
        g.expand(aars, G);
//        assertEquals(28, g.getNodeCount());
        assertEquals(56, g.getNodeCount());
//        assertEquals(36, g.getEdgeCount());
        assertEquals(202, g.getEdgeCount());
        g.removeNode(aars);
        g.removeIsolatedNodes();
//        assertEquals(16, g.getNodeCount());
        assertEquals(53, g.getNodeCount());
//        assertEquals(23, g.getEdgeCount());
        assertEquals(189, g.getEdgeCount());
        g.addEquivalentNode(aarsTuple);
//        assertEquals(17, g.getNodeCount());
        assertEquals(54, g.getNodeCount());
//        assertEquals(23, g.getEdgeCount());
        assertEquals(189, g.getEdgeCount());
        aars = g.getNode(g.getEquivalentNode(aarsTuple).getRow());
        g.link(aars, G);
//        assertEquals(17, g.getNodeCount());
        assertEquals(54, g.getNodeCount());
//        assertEquals(24, g.getEdgeCount());
        assertEquals(200, g.getEdgeCount());
        g.expand(aars, G);
//        assertEquals(28, g.getNodeCount());
        assertEquals(56, g.getNodeCount());
//        assertEquals(36, g.getEdgeCount());
        assertEquals(202, g.getEdgeCount());
    }

    @Test
    public void testAddGraph() {
        Graph g = CSVTableReaderTest.makeCancerPartialGraph(G);
        Graph qarsGraph = CSVTableReaderTest.makeQARSPartialGraph(G);
        g.addGraph(qarsGraph);
        assertEquals(18 + 6, g.getNodeCount());
//        assertEquals(26 + 8, g.getEdgeCount());
        assertEquals(47, g.getEdgeCount());
    }

    @Test
    public void testClearGraph() {
        Graph g = CSVTableReaderTest.makeCancerPartialGraph(G);
        Graph qarsGraph = CSVTableReaderTest.makeQARSPartialGraph(G);
        g.addGraph(qarsGraph);

        g.clear();

        assertEquals(0, g.getTupleCount());
        assertEquals(0, g.getNodeCount());
        assertEquals(0, g.getEdgeCount());
        assertEquals(0, g.getNodeTable().getTupleCount());
        assertEquals(0, g.getEdgeTable().getTupleCount());
    }
}
