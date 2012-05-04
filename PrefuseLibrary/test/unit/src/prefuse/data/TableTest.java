package prefuse.data;

import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.RegexSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.data.tuple.TupleSet;
import prefuse.util.GraphLib;

/**
 *
 * @author lunardo
 */
public class TableTest {

    private static Graph G, g;
    private static SearchTupleSet proxy;
    private static final Schema NODE_FILTER_TABLE = new Schema(
            new String[]{"Name", "Type", "Description", "Notes", "Owner"},
            new Class[]{String.class, String.class, String.class, String.class, String.class});

    public TableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        G = CSVTableReaderTest.readGraph();
        g = CSVTableReaderTest.makeCancerPartialGraph(G);
        proxy = new RegexSearchTupleSet();
        new SearchQueryBinding(G.getNodes(), CSVTableReaderTest.NODE_NAME, proxy);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        GraphLib.disposeAndClear(G);
        GraphLib.disposeAndClear(g);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testTableFromTupleSet() {
        TupleSet nodes = g.getNodes();
        Table table = g.getNodeTable().getSchema().instantiate();
        table.addTuples(nodes);
        assertEquals(nodes.getTupleCount(), table.getRowCount());
    }

    @Test
    public void testAddSuple() {
        Table table = NODE_FILTER_TABLE.instantiate();
        TupleSet nodes = g.getNodes();
        Iterator<Tuple> nodeTuples = nodes.tuples();
        while (nodeTuples.hasNext()) {
            table.addSuple(nodeTuples.next());
        }
        assertEquals(nodes.getTupleCount(), table.getRowCount());
        nodeTuples = nodes.tuples();
        while (nodeTuples.hasNext()) {
            Tuple suple = nodeTuples.next();
            Tuple tuple = table.getTuple(suple);
            System.out.println("suple : " + suple);
            System.out.println("tuple : " + tuple);
            assertEquals(suple.getRow(), tuple.getRow());
        }
    }
}
