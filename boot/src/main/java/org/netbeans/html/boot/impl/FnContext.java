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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.html.boot.spi.Fn;

/**
 *
 * @author Jaroslav Tulach
 */
public final class FnContext implements Closeable {
    private static final Logger LOG = Logger.getLogger(FnContext.class.getName());
    private static final FnContext DUMMY;
    static {
        DUMMY = new FnContext(null, null);
        DUMMY.prev = DUMMY;
    }

    public static URL isJavaScriptCapable(ClassLoader l) {
        if (l instanceof JsClassLoader) {
            return null;
        }
        return l.getResource("META-INF/net.java.html.js.classes");
    }

    public static ClassLoader newLoader(URL res, FindResources impl, Fn.Presenter p, ClassLoader parent) {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        Throwable t;
        try {
            Method newLoader = Class.forName("org.netbeans.html.boot.impl.FnUtils") // NOI18N
                .getMethod("newLoader", FindResources.class, Fn.Presenter.class, ClassLoader.class);
            return (ClassLoader) newLoader.invoke(null, impl, p, parent);
        } catch (LinkageError ex) {
            t = ex;
        } catch (Exception ex) {
            t = ex;
        }
        pw.println("When using @JavaScriptBody methods, one needs to either:");
        pw.println(" - include asm-5.0.jar on runtime classpath");
        pw.println(" - post process classes, see http://bits.netbeans.org/html+java/dev/net/java/html/js/package-summary.html#post-process");
        pw.append("However following classes has not been processed from ").println(res);
        
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(res.openStream()));
            for (;;) {
                String line = r.readLine();
                if (line == null) {
                    break;
                }
                pw.append("  ").println(line);
            }
            r.close();
        } catch (IOException io) {
            pw.append("Cannot read ").println(res);
            io.printStackTrace(pw);
        }
        pw.println("Cannot initialize asm-5.0.jar!");
        pw.flush();
        LOG.log(Level.SEVERE, w.toString(), t);
        return null;
    }

    private Object prev;
    private final Fn.Presenter current;
    private FnContext(Fn.Presenter prevP, Fn.Presenter newP) {
        this.current = newP;
        this.prev = prevP;
    }

    @Override
    public void close() throws IOException {
        if (prev != this) {
            currentPresenter((Fn.Presenter)prev);
            prev = this;
            if (current instanceof Flushable) {
                ((Flushable)current).flush();
            }
        }
    }
/*
    @Override
    protected void finalize() throws Throwable {
        if (prev != null) {
            LOG.warning("Unclosed context!");
        }
    }
*/
    public static Closeable activate(Fn.Presenter newP) {
        final Fn.Presenter oldP = currentPresenter(newP);
        if (oldP == newP) {
            return DUMMY;
        }
        return new FnContext(oldP, newP);
    }
    
    
    private static final ThreadLocal<Fn.Presenter> CURRENT = new ThreadLocal<Fn.Presenter>();

    public static Fn.Presenter currentPresenter(Fn.Presenter p) {
        Fn.Presenter prev = CURRENT.get();
        CURRENT.set(p);
        return prev;
    }

    public static Fn.Presenter currentPresenter(boolean fail) {
        Fn.Presenter p = CURRENT.get();
        if (p == null && fail) {
            throw new IllegalStateException("No current WebView context around!");
        }
        return p;
    }
    
}
