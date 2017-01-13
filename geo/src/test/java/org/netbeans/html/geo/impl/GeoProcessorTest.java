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
package org.netbeans.html.geo.impl;

import java.io.IOException;
import org.testng.annotations.Test;

/** Test whether the annotation processor detects errors correctly.
 *
 * @author Jaroslav Tulach
 */
public class GeoProcessorTest {
    
    public GeoProcessorTest() {
    }

    @Test public void onLocationMethodHasToTakePositionParameter() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation\n"
            + "  public static void cantCallMe() {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("first argument must be net.java.html.geo.Position");
    }
    
    @Test public void onLocationMethodCannotBePrivate() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation\n"
            + "  private static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("cannot be private");
    }
    
    @Test public void onErrorHasToExist() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation(onError=\"doesNotExist\")\n"
            + "  static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("not find doesNotExist");
    }

    @Test public void onErrorWouldHaveToBeStatic() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation(onError=\"notStatic\")\n"
            + "  static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "  void notStatic(Exception e) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("have to be static");
    }

    @Test public void onErrorMustAcceptExceptionArgument() throws IOException {
        Compile res = Compile.create("", "package x;\n"
            + "class UseOnLocation {\n"
            + "  @net.java.html.geo.OnLocation(onError=\"notStatic\")\n"
            + "  static void cantCallMe(net.java.html.geo.Position p) {}\n"
            + "  static void notStatic(java.io.IOException e) {}\n"
            + "}\n"
        );
        res.assertErrors();
        res.assertError("Error method first argument needs to be Exception");
    }
    
}
