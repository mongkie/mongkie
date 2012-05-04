/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.validation.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class ProblemTest {
    /**
     * Test of isWorseThan method, of class Problem.
     */
    @Test
    public void testIsWorseThan() {
        Problem p0 = null;
        Problem a = new Problem ("a", Severity.INFO);
        Problem b = new Problem ("b", Severity.WARNING);
        Problem c = new Problem ("c", Severity.FATAL);

        assertSame( a, Problem.worst(a, p0) );
        assertSame( b, Problem.worst(p0, b) );
        assertSame( c, Problem.worst(c, p0) );
        assertSame( b, Problem.worst(b, a) );
        assertSame( c, Problem.worst(a, c) );
        assertSame( c, Problem.worst(c, b) );

        List l = new ArrayList(Arrays.asList(a, b, c, p0));
        assertSame (a, l.get(0));
        assertSame (c, l.get(2));
        assertSame (p0, l.get(3));

        assertTrue (c.isFatal());
        assertFalse (b.isFatal());
        assertFalse (a.isFatal());

        assertEquals ("a", a.getMessage());
        assertEquals ("b", b.getMessage());
        assertEquals ("c", c.getMessage());

        Problems problems = new Problems();

        problems.add (a);
        assertFalse(problems.hasFatal());
        problems.add (b);
        assertFalse(problems.hasFatal());
        problems.add (c);
        assertTrue(problems.hasFatal());
        problems.add(p0);
        assertTrue(problems.hasFatal());
        assertSame (c, problems.getLeadProblem());
        assertTrue(problems.hasFatal());

        Problems clean = new Problems();
        assertFalse(clean.hasFatal());
        clean.putAll(problems);
        assertTrue(clean.hasFatal());
        Problems alsoClean = new Problems();
        assertFalse(alsoClean.hasFatal());
        alsoClean.add("info", Severity.INFO);
        assertFalse(alsoClean.hasFatal());
        alsoClean.add("warning", Severity.WARNING);
        assertFalse(alsoClean.hasFatal());
        alsoClean.add("fatal");
        assertTrue(alsoClean.hasFatal());
    }


}