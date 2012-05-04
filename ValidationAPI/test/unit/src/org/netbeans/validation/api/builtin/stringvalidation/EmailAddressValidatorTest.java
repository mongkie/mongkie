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

import java.util.regex.Pattern;
import org.junit.Test;
import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.Problems;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class EmailAddressValidatorTest {

    @Test
    public void testPattern() throws Exception {
        Pattern p = EmailAddressValidator.ADDRESS_PATTERN;
//        assertTrue (p.matcher("foo@bar.com").matches());
        assertTrue (p.matcher("Foo Bar <foo@bar.com>").matches());
    }

    private Problem doValidate(String emailAddress){
        Problems p = new Problems();
        String compName = "";
        new EmailAddressValidator().validate(p, compName, emailAddress);
        return p.getLeadProblem();
    }

    @Test
    public void testValidate() {
        assertTrue (true);
        // boolean val = doValidate("foo@bar.com");
        assertTrue (doValidate("foo@bar.com") == null);
        assertTrue (doValidate("Foo Bar <foo@bar.com>") == null);
        assertTrue (doValidate(" Foo Bar <foo@bar.com>") == null);
        assertTrue (doValidate(" Foo Bar <foo@192.168.2.1>") == null);

        assertFalse (doValidate("Foo Bar <foo@bar.com> ") == null);
        assertFalse (doValidate(" Foo Bar <foo@bar.com> ") == null);
        assertFalse (doValidate(" foo@bar.com") == null);
        assertFalse (doValidate("foo@") == null);
        assertFalse (doValidate(" foo@.") == null);

        assertFalse (doValidate("Foo Bar < foo@bar.com>") == null);
        assertFalse (doValidate("Foo Bar <foo@>") == null);
        assertFalse (doValidate("Foo Bar < foo@.>") == null);
        assertFalse (doValidate("Foo Bar < foo@128.>") == null);
        assertFalse (doValidate("Foo Bar < foo@128.foo>") == null);

        assertTrue (doValidate("фу бар <foo@bar.com>") == null);

        assertFalse (doValidate("Foo Bar <фу@бар.com>") == null);
        assertFalse (doValidate("Foo Bar <фу@bar.com>") == null);
        assertFalse (doValidate("Foo Bar <foo@bar@baz.com>") == null);

        assertFalse (doValidate("foo@bar.") == null);
        assertFalse (doValidate("фу бар <фу@бар.com>") == null);
        assertFalse (doValidate("фу@бар.com") == null);
        assertFalse (doValidate("Foo Bar <foo@bar.>") == null);
        assertFalse (doValidate("Foo Bar <foo@bar.com >") == null);
        assertFalse (doValidate(" Foo Bar <foo@192.168.2.boo>") == null);
        assertFalse (doValidate("foo@bar.com ") == null);
        assertFalse (doValidate("Foo Bar <foo@bar.com:>") == null);
    }
}