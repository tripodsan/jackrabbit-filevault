/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jackrabbit.vault.packagemgr.impl.rest.meta;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;

public class ActionInfo {

    private final Action sirenAction;

    private final Method method;

    private final Map<String, ParameterInfo> fields;

    public ActionInfo(Action sirenAction, Method method, Map<String, ParameterInfo> fields) {
        this.sirenAction = sirenAction;
        this.method = method;
        this.fields = fields;
    }

    public Action getSirenAction() {
        return sirenAction;
    }

    public Method getMethod() {
        return method;
    }

    public Map<String, ParameterInfo> getFields() {
        return fields;
    }

    public void execute(Object resource, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object[] args = new Object[fields.size()];

        try {
            method.invoke(resource, args);
        } catch (IllegalAccessException e) {
            throw new IOException(e);
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            throw new IOException(t);
        }

    }
}
