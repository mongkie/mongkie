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

import java.io.*;
import java.util.Iterator;
import static org.junit.Assert.*;
import org.junit.*;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.io.GraphMLReader;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class SerializableTableTest {

    static Graph g;

    public SerializableTableTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        g = new GraphMLReader().readGraph(SerializableTableTest.class.getResourceAsStream("/kobic/prefuse/data/io/resources/sox2.graphml"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        g.clear();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstructor() {
        Table nodeTable = g.getNodeTable();
        SerializableTable serializableNodeTable = new SerializableTable(nodeTable);
        assertTableEquals(nodeTable, serializableNodeTable);

        Table edgeTable = g.getEdgeTable();
        SerializableTable serializableEdgeTable = new SerializableTable(edgeTable);
        assertTableEquals(edgeTable, serializableEdgeTable);
    }

    @Test
    public void testIsSerializable()
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(new SerializableTable(g.getNodeTable()));
        out.flush();
        out.close();
        assertTrue(baos.toByteArray().length > 0);
        baos.close();
    }

    @Test
    public void testNodeTableSerialization()
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(new SerializableTable(g.getNodeTable()));
        out.flush();
        out.close();
        byte[] pickled = baos.toByteArray();
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(pickled);
        ObjectInputStream in = new ObjectInputStream(bais);
        SerializableTable serializedNodeTable = (SerializableTable) in.readObject();
        in.close();
        bais.close();
        assertTableEquals(g.getNodeTable(), serializedNodeTable);
    }

    @Test
    public void testEdgeTableSerialization()
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(new SerializableTable(g.getEdgeTable()));
        out.flush();
        out.close();
        byte[] pickled = baos.toByteArray();
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(pickled);
        ObjectInputStream in = new ObjectInputStream(bais);
        SerializableTable serializedEdgeTable = (SerializableTable) in.readObject();
        in.close();
        bais.close();
        assertTableEquals(g.getEdgeTable(), serializedEdgeTable);
    }

    private void assertTableEquals(Table one, SerializableTable serialized) {
        Table another = serialized.getTable();
        assertTrue(one.getSchema().isAssignableFrom(another.getSchema()));
        assertEquals(one.getColumnCount(), another.getColumnCount());
        assertEquals(one.getTupleCount(), another.getTupleCount());
        assertEquals(one.getRowCount(), another.getRowCount());
        Iterator<Tuple> oneTuples = one.tuples();
        Iterator<Tuple> anotherTuples = another.tuples();
        while (oneTuples.hasNext()) {
            Tuple oneTuple = oneTuples.next();
            Tuple anotherTuple = anotherTuples.next();
            for (int col = 0; col < one.getColumnCount(); col++) {
                Object oneVal = oneTuple.get(col);
                Object anotherVal = anotherTuple.get(col);
                if (oneVal == null) {
                    assertNull(anotherVal);
                    continue;
                }
                assertTrue(oneVal.equals(anotherVal));
            }
        }
    }
}
