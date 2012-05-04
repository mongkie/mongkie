package prefuse.data;

import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import prefuse.data.query.SearchQueryBinding;
import prefuse.data.search.PrefixSearchTupleSet;
import prefuse.data.search.RegexSearchTupleSet;
import prefuse.data.search.SearchTupleSet;
import prefuse.util.GraphLib;

/**
 *
 * @author lunardo
 */
public class SearchQueryTest {

    private static Graph G, cancerGraph;

    public SearchQueryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        G = CSVTableReaderTest.readGraph();
        cancerGraph = CSVTableReaderTest.makeCancerPartialGraph(G);
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
    public void testPrefixSearch() {
        SearchTupleSet proxy = new PrefixSearchTupleSet();
        new SearchQueryBinding(cancerGraph.getNodes(), CSVTableReaderTest.NODE_NAME, proxy);
        proxy.search("cancer");
        printResultSet(proxy.resultSet());
        assertEquals(1, proxy.getTupleCount());
        proxy.search("e");
        printResultSet(proxy.resultSet());
        assertEquals(2, proxy.getTupleCount());
    }

    @Test
    public void testRegexSearch() {
        SearchTupleSet proxy = new RegexSearchTupleSet();
        new SearchQueryBinding(cancerGraph.getNodes(), CSVTableReaderTest.NODE_NAME, proxy);
        proxy.search("cancer");
        printResultSet(proxy.resultSet());
        assertEquals(1, proxy.getTupleCount());
        proxy.search("^e.*");
        printResultSet(proxy.resultSet());
        assertEquals(2, proxy.getTupleCount());
    }

    public static void printResultSet(Set<Tuple> results) {
        System.out.println("-------------------------------------------------");
        for (Tuple t : results) {
            System.out.println(t);
        }
        System.out.println("-------------------------------------------------");
    }
}
