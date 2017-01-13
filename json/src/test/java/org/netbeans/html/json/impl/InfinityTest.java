/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013-2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Oracle. Portions Copyright 2013-2016 Oracle. All Rights Reserved.
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
package org.netbeans.html.json.impl;

import net.java.html.json.Model;
import net.java.html.json.Property;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import org.testng.annotations.Test;

@Model(className = "Infinity", properties = {
    @Property(name = "next", type = Infinity.class),
    @Property(name = "address", type = Address.class)
})
public class InfinityTest {
    @Test
    public void atLeastThousandStepsDeep() {
        Infinity infinity = new Infinity();
        int cnt = 0;
        while (++cnt < 1000) {
            infinity = infinity.getNext();
        }
        assertNotNull(infinity);
        assertEquals(cnt, 1000);
    }

    @Test
    public void afterInitializationRemainsTheSame() {
        Infinity infinity = new Infinity();
        Infinity first = infinity.getNext();
        Infinity second = infinity.getNext();
        assertSame(first, second);
    }

    @Test
    public void nullRemains() {
        Infinity infinity = new Infinity();
        infinity.setNext(null);
        assertNull(infinity.getNext(), "Remains null");
        assertNull(infinity.getNext(), "Again");
    }

    @Test
    public void ownValueRemains() {
        Infinity infinity = new Infinity();
        Infinity n = new Infinity();
        infinity.setNext(n);
        assertEquals(infinity.getNext(), n, "Remains n");
        assertEquals(infinity.getNext(), n, "Again n");
    }

    @Test
    public void nullRemainsAfterClone() {
        Infinity infinity = new Infinity();
        infinity.setNext(null);
        Infinity clone = infinity.clone();
        assertNull(clone.getNext(), "Remains null");
        assertNull(clone.getNext(), "Again");
        assertEquals(clone.hashCode(), infinity.hashCode(), "Same hashcode");
    }

    @Test
    public void ownValueRemainsAfterClone() {
        Infinity infinity = new Infinity();
        Infinity n = new Infinity();
        infinity.setNext(n);
        Infinity clone = infinity.clone();
        assertEquals(clone.getNext(), n, "Remains n");
        assertEquals(clone.getNext(), n, "Again n");
    }

    @Test
    public void hashCodeRemainsAfterClone() {
        Infinity infinity = new Infinity();
        Infinity n = new Infinity();
        infinity.setNext(n);
        Infinity clone = infinity.clone();
        assertEquals(clone.getNext(), n, "Remains n");
        assertEquals(clone.getNext(), n, "Again n");
        assertEquals(clone.hashCode(), infinity.hashCode(), "Same hashcode");
    }

    @Test
    public void simpleToStringWithNull() {
        Infinity infinity = new Infinity();
        assertNotNull(infinity.getAddress(), "Initialized will be stored as object");
        assertEquals("{\"next\":null,\"address\":{\"place\":null}}", infinity.toString());
        infinity.hashCode();

        Infinity second = new Infinity();
        assertEquals("{\"next\":null,\"address\":null}", second.toString(), "Uninitialized is turned into null");

        second.hashCode();
    }

    @Test
    public void toStringWithNullAndClone() {
        Infinity infinity = new Infinity();
        infinity.setNext(null);
        Infinity clone = infinity.clone();
        assertNull(infinity.getNext(), "Remains null");
        assertNotNull(infinity.getAddress(), "Address is initialized");
        assertNull(clone.getNext(), "Clone Remains null");
        assertNotNull(clone.getAddress(), "Clone Address is initialized");
        assertEquals(infinity.toString(), clone.toString());
        assertEquals(clone.hashCode(), infinity.hashCode(), "Same hashcode");
    }

}
