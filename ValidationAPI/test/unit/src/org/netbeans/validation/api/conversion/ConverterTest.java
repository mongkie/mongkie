/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.validation.api.conversion;

import javax.swing.text.Document;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
public class ConverterTest {
    @Test
    public void testFind() {
        assertNotNull (Converter.find(String.class, Document.class));
        try {
            Converter.find (String.class, Foo.class);
            fail ("Exception not thrown for unavailable converter");
        } catch (IllegalArgumentException e) {}
        Converter.register(new ConverterImpl());
        assertNotNull (Converter.find (String.class, Foo.class));
        assertNotNull (Converter.find (String.class, Bar.class));
    }

    public static class Foo {
        private final String s;
        public Foo(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Foo other = (Foo) obj;
            if ((this.s == null) ? (other.s != null) : !this.s.equals(other.s)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.s != null ? this.s.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    public static class Bar {
        private final String s;
        public Bar(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Foo other = (Foo) obj;
            if ((this.s == null) ? (other.s != null) : !this.s.equals(other.s)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.s != null ? this.s.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return s;
        }
    }

    public static class ConverterImpl extends Converter<String, Foo> {

        public ConverterImpl() {
            super(String.class, Foo.class);
        }

        public Validator<Foo> convert(Validator<String> from) {
            return new V();
        }

        private static final class V extends AbstractValidator<Foo> {
            V() {
                super (Foo.class);
            }

            public void validate(Problems problems, String compName, Foo model) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }

    public static class BarConverter extends Converter<String, Bar> {

        public BarConverter() {
            super(String.class, Bar.class);
        }

        public Validator<Bar> convert(Validator<String> from) {
            return new V();
        }

        private static final class V extends AbstractValidator<Bar> {
            V() {
                super (Bar.class);
            }

            public void validate(Problems problems, String compName, Bar model) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }

}