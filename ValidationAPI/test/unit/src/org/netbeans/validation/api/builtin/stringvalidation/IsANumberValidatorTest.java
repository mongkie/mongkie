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
package org.netbeans.validation.api.builtin.stringvalidation;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import org.junit.Test;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class IsANumberValidatorTest {

    public IsANumberValidatorTest() {
    }

    @Test
    public void testValidate() throws ParseException {
        //XXX need to find a locale that does *not* support 100's position separators for
        //this test to be meaningful - both 1,234 and 1.234 are valid numbers
        //in both Locale.US & Locale.GERMAN
        Validator <String> v = new IsANumberValidator ();
        Locale.setDefault(Locale.US);
        Problems p = new Problems();
        v.validate(p, "foo", "1.234");
        assertNull(p.getLeadProblem());

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        Number n = nf.parse("1.234");
        assertTrue(1.234D == n.doubleValue());

        v.validate(p, "foo", "2.0e7");
        assertNull(p.getLeadProblem());
        
        v.validate(p, "foo", "-0e-0");
        assertNull(p.getLeadProblem());

        p = new Problems();
        v.validate(p, "foo", " 2.0e7 "); // whitespace, should fail
        assertTrue(p.hasFatal());
        p = new Problems();
        v.validate(p, "foo", "10.0blahbla");
        assertTrue(p.hasFatal());
        p = new Problems();
        v.validate(p, "foo", "blahbla10.0");
        assertTrue(p.hasFatal());

        p = new Problems();
        Locale.setDefault(Locale.GERMAN);
        v.validate(p, "foo", "1,234");
        assertNull(p.getLeadProblem());
    }

}