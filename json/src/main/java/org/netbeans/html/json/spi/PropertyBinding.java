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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import net.java.html.BrwsrCtx;
import net.java.html.json.ComputedProperty;
import org.netbeans.html.json.impl.Bindings;
import org.netbeans.html.json.impl.JSON;
import org.netbeans.html.json.impl.PropertyBindingAccessor;
import org.netbeans.html.json.impl.RcvrJSON;

/** Describes a property when one is asked to
 * bind it
 *
 * @author Jaroslav Tulach
 */
public abstract class PropertyBinding {
    PropertyBinding() {
    }

    static {
        new PropertyBindingAccessor() {
            @Override
            protected JSONCall newCall(BrwsrCtx ctx, RcvrJSON callback, String headers, String urlBefore, String urlAfter, String method, Object data) {
                return new JSONCall(ctx, callback, headers, urlBefore, urlAfter, method, data);
            }

            @Override
            protected Bindings bindings(Proto proto, boolean initialize, Object copyFrom) {
                return initialize ? proto.initBindings(copyFrom) : proto.getBindings();
            }

            @Override
            protected void notifyChange(Proto proto, int propIndex) {
                proto.onChange(propIndex);
            }

            @Override
            protected Proto findProto(Proto.Type<?> type, Object object) {
                return type.protoFor(object);
            }

            @Override
            protected <Model> Model cloneTo(Proto.Type<Model> type, Model model, BrwsrCtx c) {
                return type.cloneTo(model, c);
            }

            @Override
            protected Object read(Proto.Type<?> from, BrwsrCtx c, Object data) {
                return from.read(c, data);
            }

            @Override
            protected <M> PropertyBinding newBinding(
                Proto.Type<M> access, Bindings<?> bindings, String name, int index, M model, byte propertyType) {
                return new Impl(model, bindings, name, index, access, propertyType);
            }
        };
    }

    /** Name of the property this binding represents.
     * @return name of the property
     */
    public abstract String getPropertyName();

    /** Changes value of the property. Can be called only on dedicated
     * thread. See {@link Technology#runSafe(java.lang.Runnable)}.
     *
     * @param v new value of the property
     */
    public abstract void setValue(Object v);

    /** Obtains current value of the property this binding represents.
     * Can be called only on dedicated
     * thread. See {@link Technology#runSafe(java.lang.Runnable)}.
     *
     * @return the value or <code>null</code>
     */
    public abstract Object getValue();

    /** Is this property read only?. Or can one call {@link #setValue(java.lang.Object)}?
     * The property can still change, but only as a result of other
     * properties being changed, just like {@link ComputedProperty} can.
     *
     * @return true, if this property is read only
     */
    public abstract boolean isReadOnly();

    /** Is this property constant?. If a property is constant, than its
     * value cannot changed after it is read.
     *
     * @return true, if this property is constant
     * @since 1.3
     */
    public abstract boolean isConstant();

    /** Returns identical version of the binding, but one that holds on the
     * original model object via weak reference.
     *
     * @return binding that uses weak reference
     * @since 1.1
     */
    public abstract PropertyBinding weak();

    private static abstract class AImpl<M> extends PropertyBinding {
        public final String name;
        public final byte propertyType;
        final Proto.Type<M> access;
        final Bindings<?> bindings;
        final int index;

        public AImpl(Bindings<?> bindings, String name, int index, Proto.Type<M> access, byte propertyType) {
            this.bindings = bindings;
            this.name = name;
            this.index = index;
            this.access = access;
            this.propertyType = propertyType;
        }

        protected abstract M model();

        @Override
        public void setValue(Object v) {
            M model = model();
            if (model == null) {
                return;
            }
            access.setValue(model, index, v);
        }

        @Override
        public Object getValue() {
            M model = model();
            if (model == null) {
                return null;
            }
            Object v = access.getValue(model, index);
            Object r = JSON.find(v, bindings);
            return r == null ? v : r;
        }

        @Override
        public boolean isReadOnly() {
            return (propertyType & 1) != 0;
        }

        @Override
        public boolean isConstant() {
            return (propertyType & 2) != 0;
        }

        @Override
        public String getPropertyName() {
            return name;
        }
    } // end of PBData

    private static final class Impl<M> extends AImpl<M> {
        private final M model;

        public Impl(M model, Bindings<?> bindings, String name, int index, Proto.Type<M> access, byte propertyType) {
            super(bindings, name, index, access, propertyType);
            this.model = model;
        }

        @Override
        protected M model() {
            return model;
        }

        @Override
        public PropertyBinding weak() {
            return new Weak(model, bindings, name, index, access, propertyType);
        }
    }

    private static final class Weak<M> extends AImpl<M> {
        private final Reference<M> ref;
        public Weak(M model, Bindings<?> bindings, String name, int index, Proto.Type<M> access, byte propertyType) {
            super(bindings, name, index, access, propertyType);
            this.ref = new WeakReference<M>(model);
        }

        @Override
        protected M model() {
            return ref.get();
        }

        @Override
        public PropertyBinding weak() {
            return this;
        }
    }
}
