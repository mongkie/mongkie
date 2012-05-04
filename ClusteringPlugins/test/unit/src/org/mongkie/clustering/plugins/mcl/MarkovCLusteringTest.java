/*
 *  This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 *  Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
 * 
 *  MONGKIE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  MONGKIE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.clustering.plugins.mcl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import prefuse.data.Graph;
import prefuse.data.io.DataIOException;
import prefuse.data.io.GraphMLReader;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MarkovCLusteringTest {

    public MarkovCLusteringTest() {
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
     * Test the algorithm of MarkovCLustering.
     */
    @Test
    public void testMCL() throws DataIOException {
        Graph g = new GraphMLReader().readGraph(MarkovCLusteringTest.class.getResourceAsStream("resources/sox2.graphml"));
        System.out.println("Nodes: " + g.getNodeCount());
        System.out.println("Edges: " + g.getEdgeCount());
        MarkovCLustering mcl = new MarkovCLustering(null);
        mcl.execute(g);
        assertEquals(1, mcl.getClusters().size()); //TODO: fake success
    }
}
