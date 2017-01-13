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
package org.netbeans.html.boot.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author Jaroslav Tulach
 */
public class JavaScriptProcesorTest {
    
    @Test public void detectCallbackToNonExistingClass() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Runable::run()();\"\n" // typo
            + "  )\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("java.lang.Runable"); // typo
    }

    @Test public void detectCallbackToNonExistingMethod() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Runnable::cancel()();\"\n"
            + "  )\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("method cancel");
    }

    @Test public void detectCallbackToNonExistingParams() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Runnable::run(I)(10);\"\n"
            + "  )\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("wrong parameters: (I)");
    }

    @Test public void objectTypeParamsAreOK() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\"}, javacall=true, body =\n"
            + "    \"r.@java.lang.Object::equals(Ljava/lang/Object;)(null);\"\n"
            + "  )\n"
            + "  private static native void testEqual(Object r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertNoErrors();
    }
    
    @Test public void misorderNotified() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptBody;\n"
            + "class X {\n"
            + "  @JavaScriptBody(args={\"r\", \"a\", \"b\"}, body =\"\"\n"
            + "  )\n"
            + "  private static native void testEqual(Object p, String q, int r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        List<Diagnostic<? extends JavaFileObject>> warnings = c.getDiagnostics(Diagnostic.Kind.WARNING);
        assertTrue(warnings.size() >= 1, "There are warnings: " + warnings);
        for (Diagnostic<? extends JavaFileObject> w : warnings) {
            if (w.getMessage(Locale.US).contains("Actual method parameter names and args")) {
                return;
            }
        }
        fail("Expecting order warning: " + warnings);
    }

    @Test public void needJavaScriptBodyToUseResource() throws IOException {
        String code = "package x.y.z;\n"
            + "import net.java.html.js.JavaScriptResource;\n"
            + "@JavaScriptResource(\"x.html\")\n"
            + "class X {\n"
            + "  private static native void callback(Runnable r);\n"
            + "}\n";
        
        Compile c = Compile.create("", code);
        c.assertErrors();
        c.assertError("needs @JavaScriptBody");
    }
    
    @Test public void generatesCallbacksThatReturnObject() throws Exception {
        Class<?> callbacksForTestPkg = Class.forName("org.netbeans.html.boot.impl.$JsCallbacks$");
        Method m = callbacksForTestPkg.getDeclaredMethod("java_lang_Runnable$run$", Runnable.class);
        assertEquals(m.getReturnType(), java.lang.Object.class, "All methods always return object");
    }
    
    @Test public void hasInstanceField() throws Exception {
        Class<?> callbacksForTestPkg = Class.forName("org.netbeans.html.boot.impl.$JsCallbacks$");
        Field f = callbacksForTestPkg.getDeclaredField("VM");
        f.setAccessible(true);
        assertTrue(callbacksForTestPkg.isInstance(f.get(null)), "Singleton field VM");
    }
}
