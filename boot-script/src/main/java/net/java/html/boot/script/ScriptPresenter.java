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
package net.java.html.boot.script;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.netbeans.html.boot.spi.Fn;
import org.netbeans.html.boot.spi.Fn.Presenter;

/** Implementation of {@link Presenter} that delegates
 * to Java {@link ScriptEngine scripting} API. The presenter runs headless
 * without appropriate simulation of browser APIs. Its primary usefulness
 * is inside testing environments. 
 * <p>
 * One can load in browser simulation for example from 
 * <a href="http://www.envjs.com/">env.js</a>. The best way to achieve so,
 * is to wait until JDK-8046013 gets fixed....
 * 
 *
 * @author Jaroslav Tulach
 */
final class ScriptPresenter implements Fn.KeepAlive,
Presenter, Fn.FromJavaScript, Fn.ToJavaScript, Executor {
    private static final Logger LOG = Logger.getLogger(ScriptPresenter.class.getName());
    private static final boolean JDK7;
    static {
        boolean jdk7;
        try {
            Class.forName("java.lang.FunctionalInterface");
            jdk7 = false;
        } catch (ClassNotFoundException ex) {
            jdk7 = true;
        }
        JDK7 = jdk7;
    }
    private final ScriptEngine eng;
    private final Executor exc;
    private final Object undefined;

    public ScriptPresenter(Executor exc) {
        this.exc = exc;
        try {
            eng = new ScriptEngineManager().getEngineByName("javascript");
            eng.eval("function alert(msg) { Packages.java.lang.System.out.println(msg); };");
            eng.eval("function confirm(msg) { Packages.java.lang.System.out.println(msg); return true; };");
            eng.eval("function prompt(msg, txt) { Packages.java.lang.System.out.println(msg + ':' + txt); return txt; };");
            Object undef;
            if (JDK7) {
                undef = new JDK7Callback().undefined(eng);
            } else {
                undef = ((Object[])eng.eval("Java.to([undefined])"))[0];
            }
            this.undefined = undef;
        } catch (ScriptException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Fn defineFn(String code, String... names) {
        return defineImpl(code, names, null);
    }

    @Override
    public Fn defineFn(String code, String[] names, boolean[] keepAlive) {
        return defineImpl(code, names, keepAlive);
    }    
    private FnImpl defineImpl(String code, String[] names, boolean[] keepAlive) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function() {\n");
        sb.append("  return function(");
        String sep = "";
        if (names != null) for (String n : names) {
            sb.append(sep).append(n);
            sep = ",";
        }
        sb.append(") {\n");
        sb.append(code);
        sb.append("\n  };\n");
        sb.append("})()\n");

        final Object fn;
        try {
            fn = eng.eval(sb.toString());
        } catch (ScriptException ex) {
            throw new IllegalStateException(ex);
        }
        return new FnImpl(this, fn, keepAlive);
    }

    @Override
    public void displayPage(URL page, Runnable onPageLoad) {
        try {
            eng.eval("if (typeof window !== 'undefined') window.location = '" + page + "'");
        } catch (ScriptException ex) {
            LOG.log(Level.SEVERE, "Cannot load " + page, ex);
        }
        if (onPageLoad != null) {
            onPageLoad.run();
        }
    }

    @Override
    public void loadScript(Reader code) throws Exception {
        eng.eval(code);
    }
    
    //
    // array conversions
    //
    
    final Object convertArrays(Object[] arr) throws Exception {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] instanceof Object[]) {
                arr[i] = convertArrays((Object[]) arr[i]);
            }
        }
        final Object wrapArr = wrapArrFn().invokeImpl(null, false, arr); // NOI18N
        return wrapArr;
    }

    private FnImpl wrapArrImpl;
    private FnImpl wrapArrFn() {
        if (wrapArrImpl == null) {
            try {
                wrapArrImpl = defineImpl("return Array.prototype.slice.call(arguments);", null, null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return wrapArrImpl;
    }

    final Object checkArray(Object val) throws Exception {
        final FnImpl fn = arraySizeFn();
        final Object fnRes = fn.invokeImpl(null, false, val, null);
        int length = ((Number) fnRes).intValue();
        if (length == -1) {
            return val;
        }
        Object[] arr = new Object[length];
        fn.invokeImpl(null, false, val, arr);
        return arr;
    }

    private FnImpl arraySize;
    private FnImpl arraySizeFn() {
        if (arraySize == null) {
            try {
                arraySize = defineImpl("\n"
                    + "if (to === null) {\n"
                    + "  if (Object.prototype.toString.call(arr) === '[object Array]') return arr.length;\n"
                    + "  else return -1;\n"
                    + "} else {\n"
                    + "  var l = arr.length;\n"
                    + "  for (var i = 0; i < l; i++) {\n"
                    + "    to[i] = arr[i] === undefined ? null : arr[i];\n"
                    + "  }\n"
                    + "  return l;\n"
                    + "}", new String[] { "arr", "to" }, null
                );
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        return arraySize;
    }

    @Override
    public Object toJava(Object toJS) {
        if (toJS instanceof Weak) {
            toJS = ((Weak)toJS).get();
        }
        if (toJS == undefined) {
            return null;
        }
        try {
            return checkArray(toJS);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @Override
    public Object toJavaScript(Object toReturn) {
        if (toReturn instanceof Object[]) {
            try {
                return convertArrays((Object[])toReturn);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            if (JDK7) {
                if (toReturn instanceof Boolean) {
                    return ((Boolean)toReturn) ? true : null;
                }
            }
            return toReturn;
        }
    }

    @Override
    public void execute(final Runnable command) {
        if (Fn.activePresenter() == this) {
            command.run();
            return;
        }
        
        class Wrap implements Runnable {
            public void run() {
                try (Closeable c = Fn.activate(ScriptPresenter.this)) {
                    command.run();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        final Runnable wrap = new Wrap();
        if (exc == null) {
            wrap.run();
        } else {
            exc.execute(wrap);
        }
    }

    private class FnImpl extends Fn {

        private final Object fn;
        private final boolean[] keepAlive;

        public FnImpl(Presenter presenter, Object fn, boolean[] keepAlive) {
            super(presenter);
            this.fn = fn;
            this.keepAlive = keepAlive;
        }

        @Override
        public Object invoke(Object thiz, Object... args) throws Exception {
            return invokeImpl(thiz, true, args);
        }

            final Object invokeImpl(Object thiz, boolean arrayChecks, Object... args) throws Exception {
                List<Object> all = new ArrayList<>(args.length + 1);
                all.add(thiz == null ? fn : thiz);
                for (int i = 0; i < args.length; i++) {
                    Object conv = args[i];
                    if (arrayChecks) {
                        if (args[i] instanceof Object[]) {
                            Object[] arr = (Object[]) args[i];
                            conv = ((ScriptPresenter) presenter()).convertArrays(arr);
                        }
                        if (conv != null && keepAlive != null
                            && !keepAlive[i] && !isJSReady(conv)
                            && !conv.getClass().getSimpleName().equals("$JsCallbacks$") // NOI18N
                            ) {
                            conv = new Weak(conv);
                        }
                        if (conv instanceof Character) {
                            conv = (int)(Character)conv;
                        }
                    }
                    all.add(conv);
                }
                Object ret = ((Invocable)eng).invokeMethod(fn, "call", all.toArray()); // NOI18N
                if (ret instanceof Weak) {
                    ret = ((Weak)ret).get();
                }
                if (ret == fn) {
                    return null;
                }
                if (!arrayChecks) {
                    return ret;
                }
                return ((ScriptPresenter)presenter()).checkArray(ret);
            }
    }
    
    private static boolean isJSReady(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return true;
        }
        if (obj instanceof Number) {
            return true;
        }
        final String cn = obj.getClass().getName();
        if (cn.startsWith("jdk.nashorn") || ( // NOI18N
            cn.contains(".mozilla.") && cn.contains(".Native") // NOI18N
        )) {
            return true;
        }
        if (obj instanceof Character) {
            return true;
        }
        return false;
    }    
    
    private static final class Weak extends WeakReference<Object> {
        public Weak(Object referent) {
            super(referent);
        }
    }

    private static final class JDK7Callback implements ObjectOutput {
        private Object undefined;

        @Override
        public void writeObject(Object obj) throws IOException {
            undefined = obj;
        }

        @Override
        public void write(int b) throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
        }

        @Override
        public void writeByte(int v) throws IOException {
        }

        @Override
        public void writeShort(int v) throws IOException {
        }

        @Override
        public void writeChar(int v) throws IOException {
        }

        @Override
        public void writeInt(int v) throws IOException {
        }

        @Override
        public void writeLong(long v) throws IOException {
        }

        @Override
        public void writeFloat(float v) throws IOException {
        }

        @Override
        public void writeDouble(double v) throws IOException {
        }

        @Override
        public void writeBytes(String s) throws IOException {
        }

        @Override
        public void writeChars(String s) throws IOException {
        }

        @Override
        public void writeUTF(String s) throws IOException {
        }

        public Object undefined(ScriptEngine eng) {
            try {
                eng.eval("function isJDK7Undefined(js) { js.writeObject(undefined); }");
                Invocable inv = (Invocable) eng;
                inv.invokeFunction("isJDK7Undefined", this);
            } catch (NoSuchMethodException | ScriptException ex) {
                throw new IllegalStateException(ex);
            }
            return undefined;
        }
    }

}
