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

import org.junit.Test;
import org.netbeans.validation.api.Problems;
import static org.junit.Assert.*;

/**
 *
 * @author Tim Boudreau
 */
public class IpAddressValidatorTest {
    private IpAddressValidator v = new IpAddressValidator();

    @Test
    public void testValidate() {
        assertValid ("192.168.2.1");
        assertValid ("192.168.2.1:2080");

        assertInvalid ("192.168.2.1:2080303");
        assertInvalid ("1.2");
        assertInvalid ("192.168.2.1:");
        assertInvalid ("192.168.2.1.");
        assertInvalid ("192.168.2 .1");
        assertInvalid ("192.168. .1");
        assertInvalid ("192.168. 2.1");
        assertInvalid (".192.168.2.1");
        assertInvalid ("192.168.2.bad");
        assertInvalid ("foo.bar.com");
        assertInvalid ("foo.bar.com:2080");
        assertInvalid ("foo");
        assertInvalid ("192.168.2.1::");
        assertInvalid (":192.168.2.1");
        assertInvalid ("2020:192.168.2.1");
        assertInvalid ("192.168.2.1:192.168.2.1");
        assertInvalid ("192.168.2.1:202p");
        assertInvalid ("192.168.2.1:2080303:3030");
        assertInvalid ("192.168.2..1:2080303:3030");
        assertInvalid ("192.168.2..1:2080");
        assertInvalid ("192.168.2..1::2080");
        assertInvalid ("192.168:23.2.1:2080303:3030");
        assertInvalid ("192.168:23.2.1:2080 ");
        assertInvalid (" 192.168:23.2.1:2080 ");
        assertInvalid (" 192.168:23.2.1:2080");
        assertInvalid (" 192.168:23.2.1");
        assertInvalid (" 192.168:23.2.1 ");
        assertInvalid ("192.168:23.2.1 ");
        assertInvalid ("192.168.2.1.abc");
        assertInvalid ("192.168.2.abc");
        assertInvalid ("192.168.2:2020:");
        assertInvalid ("");
        assertInvalid (".");
    }

   private void assertValid(String string) {
        Problems p = new Problems();
        v.validate(p, "X", string);
        assertNull(p.getLeadProblem());
    }

    private void assertInvalid(String string) {
        Problems p = new Problems();
        v.validate(p, "X", string);
        assertTrue(p.hasFatal());
    }
}