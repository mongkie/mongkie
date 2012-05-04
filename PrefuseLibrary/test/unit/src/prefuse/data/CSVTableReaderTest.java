package prefuse.data;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.GraphLib;
import prefuse.util.io.IOLib;
import static org.junit.Assert.*;

/**
 *
 * @author lunardo
 */
public class CSVTableReaderTest {

    private static final Logger prefuseLogger = Logger.getLogger(ExpressionParser.class.getName());

    static {
        prefuseLogger.setLevel(Level.WARNING);
    }
    private static final String NODE_TABLE_CSV = "resources/2009-06-18_Pathway_Studio_-_ARS_nework_entities.csv";
    private static final String EDGE_TABLE_CSV = "resources/2009-06-18_Pathway_Studio_-_ARS_nework_relations_split.csv";
    private static final String NODE_KEY = "IDX";
    private static final String EDGE_KEY = "IDX";
    private static final String SOURCE_KEY = "sourceIdx";
    private static final String TARGET_KEY = "targetIdx";
    public static final String NODE_NAME = "Name", SOURCE_NAME = "Source", TARGET_NAME = "Target";
    private static Graph G;

    public CSVTableReaderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        G = readGraph();
        assertEquals(1480, G.getNodeCount());
        assertEquals(2669, G.getEdgeCount());
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

    public static Graph readGraph() throws Exception {
        return IOLib.readGraphFromCSV(NODE_TABLE_CSV, NODE_KEY, NODE_NAME, EDGE_TABLE_CSV, EDGE_KEY, SOURCE_KEY, SOURCE_NAME, TARGET_KEY, TARGET_NAME);
    }

    @Test
    public void testCancerPartialGraph() {

        Graph g = makeCancerPartialGraph(G);

        GraphLib.printDegrees(g, NODE_NAME);

        assertEquals(g.getNodeTable().getTupleCount(), g.getNodeCount());
        assertEquals(g.getEdgeTable().getTupleCount(), g.getEdgeCount());
        assertEquals(18, g.getNodeCount());
//        assertEquals(26, g.getEdgeCount());
        assertEquals(39, g.getEdgeCount());
    }

    public static Graph makeCancerPartialGraph(Graph from) {

        final String name = "cancer";

        Predicate predicate = ExpressionParser.predicate(
                SOURCE_NAME + " == \"" + name + "\"" + " OR " + TARGET_NAME + " == \"" + name + "\"");

        return from.createPartial(predicate);
    }

    @Test
    public void testELA2PartialGraph() {

        Graph g = makeELA2PartialGraph(G);

//        GraphLib.printDegrees(g, NODE_NAME);

        assertEquals(g.getNodeTable().getTupleCount(), g.getNodeCount());
        assertEquals(g.getEdgeTable().getTupleCount(), g.getEdgeCount());
        assertEquals(512, g.getNodeCount());
//        assertEquals(704, g.getEdgeCount());
        assertEquals(1134, g.getEdgeCount());
    }

    public static Graph makeELA2PartialGraph(Graph from) {

        final String name = "ELA2";

        Predicate predicate = ExpressionParser.predicate(
                SOURCE_NAME + " == \"" + name + "\"" + " OR " + TARGET_NAME + " == \"" + name + "\"");

        return from.createPartial(predicate);
    }

    @Test
    public void testQARSPartialGraph() {

        Graph g = makeQARSPartialGraph(G);

        GraphLib.printDegrees(g, NODE_NAME);

        assertEquals(g.getNodeTable().getTupleCount(), g.getNodeCount());
        assertEquals(g.getEdgeTable().getTupleCount(), g.getEdgeCount());
        assertEquals(8, g.getNodeCount());
        assertEquals(8, g.getEdgeCount());
    }

    public static Graph makeQARSPartialGraph(Graph from) {

        final String name = "QARS";

        Predicate predicate = ExpressionParser.predicate(
                SOURCE_NAME + " == \"" + name + "\"" + " OR " + TARGET_NAME + " == \"" + name + "\"");

        return from.createPartial(predicate);
    }
}
