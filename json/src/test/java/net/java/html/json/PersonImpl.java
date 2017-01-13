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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jaroslav Tulach
 */
@Model(className = "Person", properties = {
    @Property(name = "firstName", type = String.class),
    @Property(name = "lastName", type = String.class),
    @Property(name = "sex", type = Sex.class)
})
final class PersonImpl {
    @ComputedProperty 
    public static String fullName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
    
    @ComputedProperty
    public static List<String> bothNames(String firstName, String lastName) {
        return Arrays.asList(firstName, lastName);
    }
    
    @ComputedProperty
    public static String sexType(Sex sex) {
        return sex == null ? "unknown" : sex.toString();
    }
    
    @ComputedProperty static Sex attractedBy(Sex sex) {
        if (sex == null) {
            return null;
        }
        return sex == Sex.MALE ? Sex.FEMALE : Sex.MALE;
    }
    
    @Function
    static void changeSex(Person p, String data) {
        if (data != null) {
            p.setSex(Sex.valueOf(data));
            return;
        }
        if (p.getSex() == Sex.MALE) {
            p.setSex(Sex.FEMALE);
        } else {
            p.setSex(Sex.MALE);
        }
    }
    
    @Model(className = "People", instance = true, targetId="myPeople", properties = {
        @Property(array = true, name = "info", type = Person.class),
        @Property(array = true, name = "nicknames", type = String.class),
        @Property(array = true, name = "age", type = int.class),
        @Property(array = true, name = "sex", type = Sex.class)
    })
    public static class PeopleImpl {
        private int addAgeCount;
        private Runnable onInfoChange;
        
        @ModelOperation void onInfoChange(People self, Runnable r) {
            onInfoChange = r;
        }
        
        @ModelOperation void addAge42(People p) {
            p.getAge().add(42);
            addAgeCount++;
        }

        @OnReceive(url = "url", method = "WebSocket", data = String.class)
        void innerClass(People p, String d) {
        }
        
        @Function void inInnerClass(People p, Person data, int x, double y, String nick) throws IOException {
            p.getInfo().add(data);
            p.getAge().add(x);
            p.getAge().add((int)y);
            p.getNicknames().add(nick);
        }
        
        @ModelOperation void readAddAgeCount(People p, int[] holder, Runnable whenDone) {
            holder[0] = addAgeCount;
            whenDone.run();
        }
        
        @OnPropertyChange("age") void infoChange(People p) {
            if (onInfoChange != null) {
                onInfoChange.run();
            }
        }
    }
}
