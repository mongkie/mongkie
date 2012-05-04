/*
 * This file is part of MONGKIE. Visit <http://www.mongkie.org/> for details.
 * Copyright (C) 2011 Korean Bioinformation Center(KOBIC)
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
package org.mongkie.gobean.rest;

import gobean.GoId;
import gobean.calculation.EnrichmentMethod;
import gobean.statistics.MultipleTestCorrectionMethod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mongkie.gobean.EnrichedResult;

/**
 *
 * @author Yeongjun Jang <yjjang@kribb.re.kr>
 */
public class GoBeanServiceTest {

    public GoBeanServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        GoBeanService.getDefault();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        GoBeanService.getDefault().close();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getEnrichedResult method, of class GoBeanService.
     */
    @Test
    public void testGetEnrichedResult() {
        EnrichedResult result = GoBeanService.getDefault().getEnrichedResult(
                EnrichmentMethod.Intersection,
                MultipleTestCorrectionMethod.BenjaminiHochberg,
                "Q92963", "Q16690", "O95271", "P17066", "Q9H4X1", "O75807", "Q16875", "P18847", "Q9UBD9", "Q9NX09", "P35398", "P10145", "Q86YR5", "P01100", "P29459", "Q9Y2D0", "P13995", "P98155", "P35638", "P08195", "Q8NHU3", "Q9C004", "P78504", "P15692", "P54274", "P49589", "Q9H4Q3", "P35318", "Q86YR5", "Q86UZ6", "Q96BD6", "Q15327", "O75293", "P26651", "Q17RY0", "O00257", "Q14511", "P08195", "P81172", "P48506", "P15018", "Q9Y272", "Q9C004", "Q9HD43", "P05121", "Q9H422", "P58004", "P53539", "Q86YR5", "Q14511", "Q8N9B5", "P18146", "Q9UJT0", "P78545", "Q7Z591", "P15407", "Q9UBS3", "P80297", "Q86YB8", "Q8IUR6", "P32929", "P28562", "P20809", "Q9Y5J3", "P48507", "P17735", "Q53EU6", "O14782", "Q9C004", "Q8N6G5", "P09601", "Q9BYN0", "P05412", "P17066", "P49910", "Q9HBL0", "Q6ZRY4", "Q96DZ7", "O60356", "P58166", "Q9UDY4", "P11309", "P17275", "Q9C004", "Q13794", "Q99075", "Q9UJT0", "P04732", "Q93083", "P10145", "Q16875", "O00622", "Q8IUR6", "Q96B67", "Q96B67", "Q8WV93", "Q05195", "P48506", "Q9UDY4", "P48507", "Q86YB8", "Q9UJX0", "Q14511", "P32929", "P32929", "Q9UJX0", "P35398", "P35398", "Q9H422", "P54274", "P98155", "Q15011", "Q15011", "P18847", "O60356", "P13995", "Q9Y5J3", "Q96DZ7");
        assertEquals(86, result.getTotalStudyCount());
        assertEquals(18271, result.getTotalPopCount());
        assertEquals(2336, result.getSelectedGoIds().size());
        assertEquals(2336, result.getRawP().size());
        assertEquals(2336, result.getAdjustedP().size());
        assertEquals(0.7864234327439016D, result.getAdjustedP(GoId.valueOf("GO:0003924")), 0.0000000000000001D);
        assertEquals(0.5629827688651199D, result.getAdjustedP(GoId.valueOf("GO:0060591")), 0.0000000000000001D);
        assertEquals(0.2555573013074879D, result.getAdjustedP(GoId.valueOf("GO:0009719")), 0.0000000000000001D);
        assertEquals(0.005243066425086403D, result.getAdjustedP(GoId.valueOf("GO:0009607")), 0.0000000000000001D);
        assertArrayEquals(new int[]{15, 0, 546, 15}, result.getCoverage(GoId.valueOf("GO:0009607")));
        result = GoBeanService.getDefault().getEnrichedResult(
                EnrichmentMethod.Intersection,
                MultipleTestCorrectionMethod.BenjaminiHochberg,
                0.01D,
                "Q92963", "Q16690", "O95271", "P17066", "Q9H4X1", "O75807", "Q16875", "P18847", "Q9UBD9", "Q9NX09", "P35398", "P10145", "Q86YR5", "P01100", "P29459", "Q9Y2D0", "P13995", "P98155", "P35638", "P08195", "Q8NHU3", "Q9C004", "P78504", "P15692", "P54274", "P49589", "Q9H4Q3", "P35318", "Q86YR5", "Q86UZ6", "Q96BD6", "Q15327", "O75293", "P26651", "Q17RY0", "O00257", "Q14511", "P08195", "P81172", "P48506", "P15018", "Q9Y272", "Q9C004", "Q9HD43", "P05121", "Q9H422", "P58004", "P53539", "Q86YR5", "Q14511", "Q8N9B5", "P18146", "Q9UJT0", "P78545", "Q7Z591", "P15407", "Q9UBS3", "P80297", "Q86YB8", "Q8IUR6", "P32929", "P28562", "P20809", "Q9Y5J3", "P48507", "P17735", "Q53EU6", "O14782", "Q9C004", "Q8N6G5", "P09601", "Q9BYN0", "P05412", "P17066", "P49910", "Q9HBL0", "Q6ZRY4", "Q96DZ7", "O60356", "P58166", "Q9UDY4", "P11309", "P17275", "Q9C004", "Q13794", "Q99075", "Q9UJT0", "P04732", "Q93083", "P10145", "Q16875", "O00622", "Q8IUR6", "Q96B67", "Q96B67", "Q8WV93", "Q05195", "P48506", "Q9UDY4", "P48507", "Q86YB8", "Q9UJX0", "Q14511", "P32929", "P32929", "Q9UJX0", "P35398", "P35398", "Q9H422", "P54274", "P98155", "Q15011", "Q15011", "P18847", "O60356", "P13995", "Q9Y5J3", "Q96DZ7");
        assertEquals(14, result.getSelectedGoIds().size());
        result = GoBeanService.getDefault().getEnrichedResult(
                EnrichmentMethod.Intersection,
                MultipleTestCorrectionMethod.BenjaminiHochberg,
                0.0D,
                "Q92963", "Q16690", "O95271", "P17066", "Q9H4X1", "O75807", "Q16875", "P18847", "Q9UBD9", "Q9NX09", "P35398", "P10145", "Q86YR5", "P01100", "P29459", "Q9Y2D0", "P13995", "P98155", "P35638", "P08195", "Q8NHU3", "Q9C004", "P78504", "P15692", "P54274", "P49589", "Q9H4Q3", "P35318", "Q86YR5", "Q86UZ6", "Q96BD6", "Q15327", "O75293", "P26651", "Q17RY0", "O00257", "Q14511", "P08195", "P81172", "P48506", "P15018", "Q9Y272", "Q9C004", "Q9HD43", "P05121", "Q9H422", "P58004", "P53539", "Q86YR5", "Q14511", "Q8N9B5", "P18146", "Q9UJT0", "P78545", "Q7Z591", "P15407", "Q9UBS3", "P80297", "Q86YB8", "Q8IUR6", "P32929", "P28562", "P20809", "Q9Y5J3", "P48507", "P17735", "Q53EU6", "O14782", "Q9C004", "Q8N6G5", "P09601", "Q9BYN0", "P05412", "P17066", "P49910", "Q9HBL0", "Q6ZRY4", "Q96DZ7", "O60356", "P58166", "Q9UDY4", "P11309", "P17275", "Q9C004", "Q13794", "Q99075", "Q9UJT0", "P04732", "Q93083", "P10145", "Q16875", "O00622", "Q8IUR6", "Q96B67", "Q96B67", "Q8WV93", "Q05195", "P48506", "Q9UDY4", "P48507", "Q86YB8", "Q9UJX0", "Q14511", "P32929", "P32929", "Q9UJX0", "P35398", "P35398", "Q9H422", "P54274", "P98155", "Q15011", "Q15011", "P18847", "O60356", "P13995", "Q9Y5J3", "Q96DZ7");
        assertEquals(0, result.getSelectedGoIds().size());
        result = GoBeanService.getDefault().getEnrichedResult(
                EnrichmentMethod.Intersection,
                MultipleTestCorrectionMethod.BenjaminiHochberg,
                1.0D,
                "Q92963", "Q16690", "O95271", "P17066", "Q9H4X1", "O75807", "Q16875", "P18847", "Q9UBD9", "Q9NX09", "P35398", "P10145", "Q86YR5", "P01100", "P29459", "Q9Y2D0", "P13995", "P98155", "P35638", "P08195", "Q8NHU3", "Q9C004", "P78504", "P15692", "P54274", "P49589", "Q9H4Q3", "P35318", "Q86YR5", "Q86UZ6", "Q96BD6", "Q15327", "O75293", "P26651", "Q17RY0", "O00257", "Q14511", "P08195", "P81172", "P48506", "P15018", "Q9Y272", "Q9C004", "Q9HD43", "P05121", "Q9H422", "P58004", "P53539", "Q86YR5", "Q14511", "Q8N9B5", "P18146", "Q9UJT0", "P78545", "Q7Z591", "P15407", "Q9UBS3", "P80297", "Q86YB8", "Q8IUR6", "P32929", "P28562", "P20809", "Q9Y5J3", "P48507", "P17735", "Q53EU6", "O14782", "Q9C004", "Q8N6G5", "P09601", "Q9BYN0", "P05412", "P17066", "P49910", "Q9HBL0", "Q6ZRY4", "Q96DZ7", "O60356", "P58166", "Q9UDY4", "P11309", "P17275", "Q9C004", "Q13794", "Q99075", "Q9UJT0", "P04732", "Q93083", "P10145", "Q16875", "O00622", "Q8IUR6", "Q96B67", "Q96B67", "Q8WV93", "Q05195", "P48506", "Q9UDY4", "P48507", "Q86YB8", "Q9UJX0", "Q14511", "P32929", "P32929", "Q9UJX0", "P35398", "P35398", "Q9H422", "P54274", "P98155", "Q15011", "Q15011", "P18847", "O60356", "P13995", "Q9Y5J3", "Q96DZ7");
        assertEquals(2336, result.getSelectedGoIds().size());
    }
}