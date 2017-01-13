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
package net.java.html.json;

import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className="BooleanArray", builder = "put", properties = {
    @Property(name = "array", type = boolean.class, array = true)
})
public class BooleanArrayTest {
    @ComputedProperty static int length(List<Boolean> array) {
        return array.size();
    }

    @ComputedProperty static List<Integer> lengthAsList(List<Boolean> array) {
        return Collections.nCopies(1, array.size());
    }

    @ComputedProperty static List<String> lengthTextList(List<Boolean> array) {
        return Collections.nCopies(1, "" + array.size());
    }
    
    @Test public void generatedConstructorWithPrimitiveType() {
        boolean[] arr = new boolean[10];
        arr[3] = true;
        BooleanArray a = new BooleanArray().putArray(arr);
        Assert.assertEquals(a.getArray().size(), 10, "Ten elements");
        Assert.assertEquals(a.getArray().get(3).booleanValue(), true, "Value ten");
        Assert.assertEquals(a.getLength(), 10, "Derived property is OK too");
        Assert.assertEquals(a.getLengthTextList().get(0), "10", "Derived string list property is OK");
        Assert.assertEquals((int)a.getLengthAsList().get(0), 10, "Derived Integer list property is OK");
    }
}
