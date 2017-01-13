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
package net.java.html.geo;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/** Testing correctness of the generated code.
 */
public class OnLocationTest {
    static int cnt;
    static @OnLocation void onLocation(Position p) {
        assertNotNull(p, "Position object provided");
        cnt++;
    }

    @Test public void createOneTimeQueryStatic() {
        net.java.html.geo.Position.Handle h = OnLocationHandle.createQuery();
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }
    
    @Test public void onLocationHandleCallback() throws Throwable {
        net.java.html.geo.Position.Handle h = OnLocationHandle.createQuery();
        cnt = 0;
        h.onLocation(new Position(0L, null));
        assertEquals(cnt, 1, "The callback has been made");
    }

    @Test public void createRepeatableWatchStatic() {
        net.java.html.geo.Position.Handle h = OnLocationHandle.createQuery();
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }

    int instCnt;
    Throwable instT;
    @OnLocation(onError = "someError") void instance(Position p) throws Error {
        assertNotNull(p, "Some position passed in");
        instCnt++;
    }
    void someError(Throwable t) throws Exception {
        instT = t;
        instCnt++;
    }
    
    @Test public void createOneTimeQueryInstance() {
        OnLocationTest t = new OnLocationTest();
        
        net.java.html.geo.Position.Handle h = InstanceHandle.createQuery(t);
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }
    
    @Test public void onInstanceCallback() throws Throwable {
        OnLocationTest t = new OnLocationTest();
        net.java.html.geo.Position.Handle h = InstanceHandle.createWatch(t);
        h.onLocation(new Position(0L, null));
        assertEquals(t.instCnt, 1, "One callback made");
    }

    @Test public void onInstanceError() throws Throwable {
        net.java.html.geo.Position.Handle h = InstanceHandle.createWatch(this);
        InterruptedException e = new InterruptedException();
        h.onError(e);
        assertEquals(instCnt, 1, "One callback made");
        assertEquals(instT, e, "The same exception passed in");
    }

    @Test public void createRepeatableWatch() {
        OnLocationTest t = new OnLocationTest();
        
        net.java.html.geo.Position.Handle h = InstanceHandle.createWatch(t);
        h.setHighAccuracy(false);
        h.setTimeout(1000L);
        h.setMaximumAge(1000L);
        if (h.isSupported()) h.start();
        h.stop();
    }
    
    @OnLocation(onError = "errParam") void withParam(Position pos, int param) {
        instCnt = param;
    }
    
    void errParam(Exception ex, int param) {
        instCnt = param;
    }
}