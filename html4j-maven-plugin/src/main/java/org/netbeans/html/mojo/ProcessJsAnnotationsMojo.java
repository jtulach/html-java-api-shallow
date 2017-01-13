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
package org.netbeans.html.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.objectweb.asm.ClassReader;

@Mojo(
    name="process-js-annotations",
    requiresDependencyResolution = ResolutionScope.COMPILE,
    defaultPhase= LifecyclePhase.PROCESS_CLASSES
)
public final class ProcessJsAnnotationsMojo extends AbstractMojo {
    @Component
    private MavenProject prj;
    
    @Parameter(defaultValue = "${project.build.directory}/classes")
    private File classes;
    
    public ProcessJsAnnotationsMojo() {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        LinkedList<URL> arr = new LinkedList<URL>();
        boolean foundAsm = false;
        for (Artifact a : prj.getArtifacts()) {
            final File f = a.getFile();
            if (f != null) {
                if (a.getArtifactId().equals("asm")) {
                    foundAsm = true;
                }
                try {
                    arr.add(f.toURI().toURL());
                } catch (MalformedURLException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        if (!foundAsm) {
            URL loc = ClassReader.class.getProtectionDomain().getCodeSource().getLocation();
            arr.addFirst(loc);
        }
        try {
            arr.add(classes.toURI().toURL());
            URLClassLoader l = new URLClassLoader(arr.toArray(new URL[arr.size()]));
            File master = new File(new File(classes, "META-INF"), "net.java.html.js.classes");
            processClasses(l, master, classes);
        } catch (IOException ex) {
            throw new MojoExecutionException("Problem converting JavaScriptXXX annotations", ex);
        }
    }
    
    private void processClasses(ClassLoader l, File master, File f) throws IOException, MojoExecutionException {
        if (!f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            boolean classes = new File(f, "net.java.html.js.classes").exists();
            File[] arr = f.listFiles();
            if (arr != null) {
                for (File file : arr) {
                    if (classes || file.isDirectory()) {
                        processClasses(l, master, file);
                    }
                }
            }
            return;
        }
        
        if (!f.getName().endsWith(".class")) {
            return;
        }
        
        byte[] arr = new byte[(int)f.length()];
        FileInputStream is = new FileInputStream(f);
        try {
            readArr(arr, is);
        } finally {
            is.close();
        }

        byte[] newArr = null;
        try {
            Class<?> fnUtils = l.loadClass("org.netbeans.html.boot.impl.FnUtils");
            Method transform = fnUtils.getMethod("transform", byte[].class, ClassLoader.class);
            
            newArr = (byte[]) transform.invoke(null, arr, l);
            if (newArr == null || newArr == arr) {
                return;
            }
            filterClass(new File(f.getParentFile(), "net.java.html.js.classes"), f.getName());
            filterClass(master, f.getName());
        } catch (Exception ex) {
            throw new MojoExecutionException("Can't process " + f, ex);
        }
        getLog().info("Processing " + f);
        writeArr(f, newArr);        
    }

    private void writeArr(File f, byte[] newArr) throws IOException, FileNotFoundException {
        FileOutputStream os = new FileOutputStream(f);
        try {
            os.write(newArr);
        } finally {
            os.close();
        }
    }

    private static void readArr(byte[] arr, InputStream is) throws IOException {
        int off = 0;
        while (off< arr.length) {
            int read = is.read(arr, off, arr.length - off);
            if (read == -1) {
                break;
            }
            off += read;
        }
    }
    
    private static void filterClass(File f, String className) throws IOException {
        if (!f.exists()) {
            return;
        }
        if (className.endsWith(".class")) {
            className = className.substring(0, className.length() - 6);
        }
        
        BufferedReader r = new BufferedReader(new FileReader(f));
        List<String> arr = new ArrayList<String>();
        boolean modified = false;
        for (;;) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (line.endsWith(className)) {
                modified = true;
                continue;
            }
            arr.add(line);
        }
        r.close();
        
        if (modified) {
            if (arr.isEmpty()) {
                f.delete();
            } else {
                FileWriter w = new FileWriter(f);
                for (String l : arr) {
                    w.write(l);
                    w.write("\n");
                }
                w.close();
            }
        }
    }
}
