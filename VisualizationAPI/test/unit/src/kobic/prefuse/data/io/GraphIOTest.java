/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2012 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package kobic.prefuse.data.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import static org.junit.Assert.*;
import org.junit.*;
import prefuse.data.Graph;
import prefuse.data.io.GraphMLReader;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GraphIOTest {

    static Graph G;
    static final File seg = new File("test.seg");

    public GraphIOTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        G = new GraphMLReader().readGraph(GraphIOTest.class.getResourceAsStream("/kobic/prefuse/data/io/resources/sox2.graphml"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        G.clear();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of writeGraph method, of class GraphIO.
     */
    @Test
    public void testWriteGraph() throws FileNotFoundException {
        seg.delete();
        assertTrue(!seg.exists());
        if (!GraphIO.writeSerializableGraph(G, new FileOutputStream(seg))) {
            seg.delete();
            fail();
        }
        assertTrue(seg.exists());
    }

    /**
     * Test of readGraph method, of class GraphIO.
     */
    @Test
    public void testReadGraph() throws FileNotFoundException {
        assertTrue(seg.exists());
        Graph g = GraphIO.readSerializableGraph(new FileInputStream(seg));
        assertNotNull(g);
        assertEquals(G.getNodeCount(), g.getNodeCount());
        assertEquals(G.getEdgeCount(), g.getEdgeCount());
        assertTrue(seg.delete());
    }
}
