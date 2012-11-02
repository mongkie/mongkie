/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Visit <http://www.mongkie.org> for details about MONGKIE.
 * Copyright (C) 2012 Korean Bioinformation Center (KOBIC)
 *
 * MONGKIE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MONGKIE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.mongkie.mimi.official;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import kobic.prefuse.data.Attribute;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mongkie.mimi.official.MiMI.AnnotationElement.*;
import org.mongkie.mimi.official.MiMI.PPI;
import org.xml.sax.SAXException;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class MiMITest {

    private static MiMI mimi;

    public MiMITest() {
    }

    @BeforeClass
    public static void setUpClass() {
        mimi = new MiMI();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testQuery() throws JAXBException, MalformedURLException {
        System.out.println("testQuery");
        System.out.println();
        Integer[] geneIds = new Integer[]{1436, 8563};
        Map<Integer, Set<PPI>> results = mimi.query(geneIds);
        for (Integer sourceGeneId : results.keySet()) {
            System.out.println("\tSource gene id: " + sourceGeneId);
            System.out.println();
            for (PPI i : results.get(sourceGeneId)) {
                for (Attribute a : i.getInteractor().getAttributes()) {
                    System.out.println("\t" + a.getName() + ": " + a.getValue());
                }
                for (Attribute a : i.getAttributeSet().getList()) {
                    System.out.println("\t" + a.getName() + ": " + a.getValue());
                }
                System.out.println();
            }
        }
        assertEquals(18, results.get(1436).size());
        assertEquals(2, results.get(8563).size());
    }

    @Test
    public void testAnnotate() throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        System.out.println("testAnnotate");
        System.out.println();
        Integer[] geneIds = new Integer[]{1436, 8563};
        Map<Integer, Attribute.Set> annotate = mimi.annotate(geneIds);
        for (Attribute.Set attributes : annotate.values()) {
            for (Attribute a : attributes.getList()) {
                System.out.println("\t" + a.getName() + ": " + a.getValue());
            }
            System.out.println();
        }
        Attribute.Set attrs = annotate.get(1436);
        assertEquals(1436, attrs.getValue(GeneID.name()));
        assertEquals("CSF1R", attrs.getValue(GeneSymbol.name()));
        assertEquals(9606, attrs.getValue(TaxonomyID.name()));
        assertEquals("Homo sapiens", attrs.getValue(Organism.name()));
        assertEquals("colony stimulating factor 1 receptor, formerly McDonough feline sarcoma viral (v-fms) oncogene homolog",
                attrs.getValue(GeneDescription.name()));
        assertEquals("protein-coding", attrs.getValue(GeneType.name()));
        System.out.println();
    }
}
