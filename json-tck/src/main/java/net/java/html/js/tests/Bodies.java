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
package net.java.html.js.tests;

import java.util.concurrent.Callable;
import net.java.html.js.JavaScriptBody;

/**
 *
 * @author Jaroslav Tulach
 */
final class Bodies {
    @JavaScriptBody(args = { "a", "b" }, body = "return a + b;")
    public static native int sum(int a, int b);

    @JavaScriptBody(args = { "a", "b" }, javacall = true, body = 
        "return @net.java.html.js.tests.Bodies::sum(II)(a, b);"
    )
    public static native int sumJS(int a, int b);
    
    @JavaScriptBody(args = {"r"}, javacall = true, body = "r.@java.lang.Runnable::run()();")
    static native void callback(Runnable r);

    @JavaScriptBody(args = {"r"}, wait4js = false, keepAlive = false, javacall = true, body = "r.@java.lang.Runnable::run()();")
    static native void asyncCallback(Runnable r);
    
    @JavaScriptBody(args = {"c", "v"}, javacall = true, body = "var arr = c.@java.util.concurrent.Callable::call()(); arr.push(v); return arr;")
    static native Object callbackAndPush(Callable<String[]> c, String v);
    
    @JavaScriptBody(args = { "v" }, body = "return v;")
    public static native Object id(Object v);
    
    @JavaScriptBody(args = { "v" }, body = "return { 'x' : v };")
    public static native Object instance(int v);
    
    @JavaScriptBody(args = "o", body = "o.x++;")
    public static native void incrementX(Object o);

    @JavaScriptBody(args = "o", wait4js = true, body = "o.x++;")
    static native void incrementXAsync(Object o);

    @JavaScriptBody(args = "o", body = "return o.x;")
    public static native int readIntX(Object o);
    
    @JavaScriptBody(args = "o", body = "return o.x;")
    public static native Object readX(Object o);
    
    @JavaScriptBody(args = { "o", "x" }, keepAlive = false, body = "o.x = x;")
    public static native Object setX(Object o, Object x);

    @JavaScriptBody(args = { "c", "a", "b" }, keepAlive = false, javacall = true, body = 
        "return c.@net.java.html.js.tests.Sum::sum(II)(a, b);"
    )
    public static native int sumIndirect(Sum c, int a, int b);
    
    @JavaScriptBody(args = { "arr", "index" }, body = "return arr[index];")
    public static native Object select(Object[] arr, int index);

    @JavaScriptBody(args = { "arr" }, body = "return arr.length;")
    public static native int length(Object[] arr);
    
    @JavaScriptBody(args = { "o", "vo" }, body = "if (vo) o = o.valueOf(); return typeof o;")
    public static native String typeof(Object o, boolean useValueOf);

    @JavaScriptBody(args = { "b" }, body = "return typeof b;")
    public static native String typeof(boolean b);

    @JavaScriptBody(args = { "o" }, body = "return Array.isArray(o);")
    public static native boolean isArray(Object o);

    @JavaScriptBody(args = { "arr", "i", "value" }, body = "arr[i] = value; return arr[i];")
    public static native String modify(String[] arr, int i, String value);
    
    @JavaScriptBody(args = {}, body = "return true;")
    public static native boolean truth();
    
    @JavaScriptBody(args = { "s" }, javacall = true, body = 
        "return s.@net.java.html.js.tests.Sum::sum([Ljava/lang/Object;)([1, 2, 3]);"
    )
    public static native int sumArr(Sum s);
    
    @JavaScriptBody(args = {}, javacall = true, body = 
        "return @net.java.html.js.tests.Bodies::fourtyTwo()();"
    )
    public static native int staticCallback();

    @JavaScriptBody(args = {}, javacall = true, body = 
        "return function() { return @net.java.html.js.tests.Bodies::fourtyTwo()(); }"
    )
    public static native Object delayCallback();
    
    @JavaScriptBody(args = { "fn" }, body = "return fn();")
    public static native Object invokeFn(Object fn);
    
    static int fourtyTwo() {
        return 42;
    }
    
    @JavaScriptBody(args = { "arr" }, body = 
        "var sum = 0;\n" +
        "for (var i = 0; i < arr.length; i++) {\n" +
        "  sum += arr[i];\n" +
        "}\n" +
        "return sum;\n"
    )
    public static native double sumVector(double[] arr);
    
    @JavaScriptBody(args = { "arr" }, body = 
        "var sum = 0;\n" +
        "for (var i = 0; i < arr.length; i++) {\n" +
        "  for (var j = 0; j < arr[i].length; j++) {\n" +
        "    sum += arr[i][j];\n" +
        "  }\n" +
        "}\n" +
        "return sum;\n"
    )
    public static native double sumMatrix(double[][] arr);

    static void incCounter(int howMuch, final Object js) {
        for (int i = 0; i < howMuch; i++) {
            asyncCallback(new Runnable() {
                @Override
                public void run() {
                    incrementXAsync(js);
                }
            });
        }
    }
    
    @JavaScriptBody(args = {}, javacall = true, body = 
        "var v = { x : 0 };\n" +
        "@net.java.html.js.tests.Bodies::incCounter(ILjava/lang/Object;)(42, v);\n" +
        "return v.x;\n"
    )
    static native int incAsync();
    
    @JavaScriptBody(args = { "arr" }, body = 
        "var ret = [];\n" +
        "for (var i in arr) {\n" +
        "  ret.push(arr[i]);\n" +
        "}\n" +
        "return ret;\n"
    )
    static native Object[] forIn(Object[] in);

    @JavaScriptBody(args = { "max" }, body = 
        "var arr = [];\n"
      + "for (var i = 0; i < max; i++) {\n"
      + "  arr.push(i);\n"
      + "}\n"
      + "return arr.length;"
    )
    static native int gc(double max);

    @JavaScriptBody(args = {}, body = ""
        + "var o = {};\n"
        + "return o.x;\n"
    )
    static native Object unknown();

    @JavaScriptBody(args = {}, body = ""
        + "return new Array(2);\n"
    )
    static native Object[] unknownArray();

    @JavaScriptBody(args = { "sum" }, javacall = true, body = ""
        + "var arr = [];\n"
        + "arr[1] = null;\n"
        + "arr[2] = 1;\n"
        + "return sum.@net.java.html.js.tests.Sum::sumNonNull([Ljava/lang/Object;)(arr);\n"
    )
    static native int sumNonNull(Sum sum);

    @JavaScriptBody(args = { "sum", "p" }, javacall = true, body = ""
        + "var obj = {};\n"
        + "obj.x = 1;\n"
        + "return sum.@net.java.html.js.tests.Sum::checkNonNull(Ljava/lang/Object;)(obj[p]);\n"
    )
    static native boolean nonNull(Sum sum, String p);

    @JavaScriptBody(args = {}, javacall = true, body = 
        "return @net.java.html.js.tests.Bodies::problematicString()();"
    )
    public static native String problematicCallback();

    @JavaScriptBody(args = { "sum" }, javacall = true, body = 
        "return sum.@net.java.html.js.tests.Sum::all(ZBSIJFDCLjava/lang/String;)(false, 1, 2, 3, 5, 6, 7, 32, 'TheEND');\n"
    )
    static native String primitiveTypes(Sum sum);

    @JavaScriptBody(args = { "call" }, javacall = true, body = ""
        + "var b = call.@java.util.concurrent.Callable::call()();\n"
        + "return b ? 'yes' : 'no';\n"
    )
    static native String yesNo(Callable<Boolean> call);

    @JavaScriptBody(args = {"arr", "val"}, body = "return arr[0] === val;")
    public static native boolean isInArray(Object[] arr, Object val);
    
    @JavaScriptBody(args = {}, body = "return globalString;")
    static native String readGlobalString();

    @JavaScriptBody(args = {}, body = "return global2String;")
    static native String readGlobal2String();
    
    static String problematicString() {
        return "{\n" +
"    MyViewModel: {\n" +
"//      ViewModel: JavaViewModel,\n" +
"\n" +
"    }          \n" +
"}";
    }
}
