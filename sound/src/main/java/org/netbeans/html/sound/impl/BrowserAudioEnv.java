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
package org.netbeans.html.sound.impl;

import net.java.html.js.JavaScriptBody;
import org.netbeans.html.sound.spi.AudioEnvironment;

/** The default audio provider that delegates to HTML5 Audio tag
 * it is used if no other {@link AudioEnvironment} is found.
 *
 * @author Jaroslav Tulach
 */
public final class BrowserAudioEnv implements AudioEnvironment<Object> {
    public static final AudioEnvironment<?> DEFAULT = new BrowserAudioEnv();
    
    private BrowserAudioEnv() {
    }
    
    @Override
    @JavaScriptBody(args = { "src" }, body = ""
        + "if (!Audio) return null;"
        + "return new Audio(src);")
    public Object create(String src) {
        // null if not running in browser
        return null;
    }

    @Override @JavaScriptBody(args = { "a" }, body = "a.play();")
    public void play(Object a) {
    }

    @Override @JavaScriptBody(args = { "a" }, body = "a.pause();")
    public void pause(Object a) {
    }

    @Override @JavaScriptBody(args = { "a", "volume" }, body = "a.setVolume(volume);")
    public void setVolume(Object a, double volume) {
    }

    @Override
    @JavaScriptBody(args = "a", body = "return true;")
    public boolean isSupported(Object a) {
        return false;
    }
}
