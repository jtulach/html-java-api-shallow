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
package org.netbeans.html.json.spi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jaroslav Tulach
 */
final class Observers {
    private static final LinkedList<Watcher> GLOBAL = new LinkedList<Watcher>();
    private final List<Watcher> watchers = new ArrayList<Watcher>();
    private final List<Ref> observers = new ArrayList<Ref>();

    Observers() {
        assert Thread.holdsLock(GLOBAL);
    }
    
    static void beginComputing(Proto p, String name) {
        synchronized (GLOBAL) {
            verifyUnlocked(p);
            final Watcher nw = new Watcher(p, name);
            GLOBAL.push(nw);
        }
    }
    
    static void verifyUnlocked(Proto p) {
        synchronized (GLOBAL) {
            for (Watcher w : GLOBAL) {
                if (w.proto == p) {
                    if (w.owner == Thread.currentThread()) {
                        throw new IllegalStateException("Re-entrant attempt to access " + p);
                    }
                }
            }
        }        
    }

    static void accessingValue(Proto p, String propName) {
        synchronized (GLOBAL) {
            verifyUnlocked(p);
            for (Watcher w : GLOBAL) {
                Observers mine = p.observers(true);
                mine.add(w, new Ref(w, propName));
            }
        }
    }
    
    static void finishComputing(Proto p) {
        synchronized (GLOBAL) {
            boolean found = false;
            Iterator<Watcher> it = GLOBAL.iterator();
            while (it.hasNext()) {
                Watcher w = it.next();
                if (w.proto == p && w.owner == Thread.currentThread()) {
                    if (w.prop != null) {
                        Observers mine = p.observers(true);
                        mine.add(w);
                    }
                    found = true;
                    it.remove();
                }
            }
            if (!found) {
                throw new IllegalStateException("Cannot find " + p + " in " + GLOBAL);
            }
        }
    }
    
    private static final class Ref extends WeakReference<Watcher> {
        private final String prop;
        
        public Ref(Watcher ref, String prop) {
            super(ref);
            this.prop = prop;
        }
        
        final Watcher watcher() {
            Watcher w = get();
            if (w == null) {
                return null;
            }
            final Observers o = w.proto.observers(false);
            if (o == null) {
                return null;
            }
            if (o.find(w.prop) == w) {
                return w;
            }
            return null;
        }
    }
    
    private Watcher find(String prop) {
        if (prop == null) {
            return null;
        }
        for (Watcher w : watchers) {
            if (prop.equals(w.prop)) {
                return w;
            }
        }
        return null;
    }

        final void add(Watcher w) {
        for (int i = 0; i < watchers.size(); i++) {
            Watcher ith = watchers.get(i);
            if (w.prop == null) {
                if (ith.prop == null) {
                    watchers.set(i, w);
                    return;
                }
            } else if (w.prop.equals(ith.prop)) {
                watchers.set(i, w);
                return;
            }
        }
        watchers.add(w);
    }

    static final void valueHasMutated(Proto p, String propName) {
        List<Watcher> mutated = new LinkedList<Watcher>();
        synchronized (GLOBAL) {
            Observers mine = p.observers(false);
            if (mine == null) {
                return;
            }
            Iterator<Ref> it = mine.observers.iterator();
            while (it.hasNext()) {
                Ref ref = it.next();
                if (ref.get() == null) {
                    it.remove();
                    continue;
                }
                if (ref.prop.equals(propName)) {
                    Watcher w = ref.watcher();
                    if (w != null) {
                        mutated.add(w);
                    }
                }
            }
        }
        for (Watcher w : mutated) {
            w.proto.valueHasMutated(w.prop);
        }
    }

    void add(Watcher w, Ref r) {
        Thread.holdsLock(GLOBAL);
        if (w == null) {
            return;
        }
        Iterator<Ref> it = observers.iterator();
        while (it.hasNext()) {
            Ref ref = it.next();
            if (r == ref) {
                return;
            }
            final Watcher rw = ref.get();
            if (rw == null) {
                it.remove();
                continue;
            }
            if (rw == w && r.prop.equals(r.prop)) {
                return;
            }
        }
        observers.add(r);
    }
    
    private static final class Watcher {
        final Thread owner;
        final Proto proto;
        final String prop;

        Watcher(Proto proto, String prop) {
            this.owner = Thread.currentThread();
            this.proto = proto;
            this.prop = prop;
        }
        
        @Override
        public String toString() {
            return "Watcher: " + proto + ", " + prop;
        }
    }
}
