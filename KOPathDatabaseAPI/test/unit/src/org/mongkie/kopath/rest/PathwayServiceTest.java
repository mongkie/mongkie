package org.mongkie.kopath.rest;

import org.mongkie.kopath.ControlType;
import prefuse.data.Tuple;
import prefuse.data.expression.AbstractPredicate;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongkie.kopath.Pathway;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;
import static org.junit.Assert.*;
import static org.mongkie.kopath.Config.*;

/**
 *
 * @author yjjang
 */
public class PathwayServiceTest {

    public PathwayServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getGraphML method, of class PathwayService.
     */
    @Test
    public void testGetGraphML() throws Exception {
        System.out.println("getGraphML");
        String graphML = PathwayService.getGraphML(1, "path:hsa04210");
//        System.out.println(graphML);
        Graph g = new GraphMLReader().readGraph(new ByteArrayInputStream(graphML.getBytes()));
        System.out.println("Node count : " + g.getNodeCount());
        System.out.println("Edge count : " + g.getEdgeCount());

        graphML = PathwayService.getGraphML(2, "pid_p_100134_il10pathway");
        g = new GraphMLReader().readGraph(new ByteArrayInputStream(graphML.getBytes()));
        assertEquals(33, g.getNodeCount());
        assertEquals(34, g.getEdgeCount());
        assertEquals(2, g.getNodeTable().getTupleCount(FIELD_LOCATIONCHANGED));

        graphML = PathwayService.getGraphML(2, "pid_p_100088_nthipathway");
        g = new GraphMLReader().readGraph(new ByteArrayInputStream(graphML.getBytes()));
        // Unify the duplicate nodes(with different node ids) which have a same local id
        assertEquals(104 - 3, g.getNodeCount());
        assertEquals(1, g.getNodeTable().getTupleCount(FIELD_ISDIMER));
        assertEquals(86, g.getEdgeCount());
        assertEquals(6, g.getNodeTable().getTupleCount(FIELD_LOCATIONCHANGED));
    }

    @Test
    public void testCountPathway() throws IOException {
        int count = PathwayService.countPathway(1, "7124", "672");
        assertEquals(12, count);
        count = PathwayService.countPathway(0, "7124", "672");
        assertEquals(51, count);

        count = PathwayService.countPathway(0);
        assertEquals(0, count);
        count = PathwayService.countPathway(1);
        assertEquals(187, count);
        count = PathwayService.countPathway(2);
        assertEquals(254, count);
        count = PathwayService.countPathway(3);
        assertEquals(167, count);
        count = PathwayService.countPathway(4);
        assertEquals(796, count);

        count = PathwayService.countPathway(0, "tnf", true);
        assertEquals(94, count);
        count = PathwayService.countPathway(0, "tnf");
        assertEquals(94, count);
        count = PathwayService.countPathway(3, "tnf", true);
        assertEquals(2, count);
        count = PathwayService.countPathway(3, "tnf");
        assertEquals(0, count);
        count = PathwayService.countPathway(3, "TNF receptor signaling pathway");
        assertEquals(1, count);
        count = PathwayService.countPathway(3, "TNF receptor signaling pathway", true);
        assertEquals(1, count);
    }

    @Test
    public void testSearchPathway() throws IOException {
        List<Pathway> pathways = PathwayService.searchPathway(1, "7124", "672");
        for (Pathway p : pathways) {
            System.out.println("> " + p);
        }
        assertEquals(12, pathways.size());

        pathways = PathwayService.searchPathway(0, "7124", "672");
        for (Pathway p : pathways) {
            System.out.println("< " + p);
        }
        assertEquals(51, pathways.size());

        pathways = PathwayService.searchPathway(0);
        for (Pathway p : pathways) {
            System.out.println("<< " + p);
        }
        assertEquals(0, pathways.size());
        pathways = PathwayService.searchPathway(1);
        for (Pathway p : pathways) {
            System.out.println(">> " + p);
        }
        assertEquals(187, pathways.size());
        pathways = PathwayService.searchPathway(2);
        for (Pathway p : pathways) {
            System.out.println(">> " + p);
        }
        assertEquals(254, pathways.size());
        pathways = PathwayService.searchPathway(3);
        for (Pathway p : pathways) {
            System.out.println(">> " + p);
        }
        assertEquals(167, pathways.size());
        pathways = PathwayService.searchPathway(4);
        for (Pathway p : pathways) {
            System.out.println(">> " + p);
        }
        assertEquals(796, pathways.size());

        pathways = PathwayService.searchPathway(0, "tnf", true);
        for (Pathway p : pathways) {
            System.out.println("<<< " + p);
        }
        assertEquals(94, pathways.size());
        pathways = PathwayService.searchPathway(0, "tnf");
        assertEquals(94, pathways.size());

        pathways = PathwayService.searchPathway(3, "tnf", true);
        for (Pathway p : pathways) {
            System.out.println(">>> " + p);
        }
        assertEquals(2, pathways.size());
        pathways = PathwayService.searchPathway(3, "tnf");
        for (Pathway p : pathways) {
            System.out.println(">>> " + p);
        }
        assertEquals(0, pathways.size());
        pathways = PathwayService.searchPathway(3, "TNF receptor signaling pathway");
        for (Pathway p : pathways) {
            System.out.println(">>>> " + p);
        }
        assertEquals(1, pathways.size());
        pathways = PathwayService.searchPathway(3, "TNF receptor signaling pathway", true);
        for (Pathway p : pathways) {
            System.out.println(">>>>> " + p);
        }
    }

    @Test
    public void testSuperPathway() throws IOException, DataIOException {
        Graph g = PathwayService.getGraphFromSuperPathway("entrezgene2hgncsymbol",
                "TNF,BRCA1".split(","),
                "278:1,281:1,294:1,301:1,311:1,313:1,319:1,322:1,338:1,339:1,348:1,357:1,37:2,45:2,174:2,220:2,2:2,187:2,177:2,189:2,249:2,170:2,155:2,213:2,169:2,66:2,25:2,159:2,78:2,54:2,137:2,403:3,506:3,409:3,560:3,468:3,391:3,471:3,445:3,412:3,511:3,421:3,539:3,475:3,470:3,449:3,399:3,433:3,551:3,493:3,500:3,1526:4,1527:4,841:4".split(","));
        assertEquals(287, g.getNodeCount());
        assertEquals(533, g.getEdgeCount());
        g = PathwayService.getGraphFromSuperPathway("entrezgene2hgncsymbol",
                "JUN".split(","),
                "320:1,321:1".split(","));
        assertEquals(11, g.getNodeCount());
        assertEquals(12, g.getEdgeCount());
        int count = g.getEdgeTable().getTupleCount(FIELD_ISINCLUDE);
        assertEquals(7, count);
        count = g.getEdgeTable().getTupleCount(FIELD_ISCONTROL);
        assertEquals(5, count);
        assertEquals(3, g.getEdgeTable().getTupleCount(new AbstractPredicate() {

            @Override
            public boolean getBoolean(Tuple t) {
                String eventSymbol = t.getString(FIELD_MOLECULAREVENT);
                return eventSymbol != null && eventSymbol.equals(ControlType.PHOSPHORYLATION.getSymbol());
            }
        }));
    }
}
