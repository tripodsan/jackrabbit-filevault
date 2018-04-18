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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Action;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelInfo {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(ModelInfo.class);

    private final Object resource;

    private final Collection<ActionInfo> actions;

    private final Entity sirenEntity;

    public ModelInfo(Object resource, Collection<ActionInfo> actions, Entity sirenEntity) {
        this.resource = resource;
        this.actions = actions;
        this.sirenEntity = sirenEntity;
    }

    public Object getResource() {
        return resource;
    }

    public Collection<ActionInfo> getActions() {
        return actions;
    }

    public Entity getSirenEntity() {
        return sirenEntity;
    }

    public ActionInfo getAction(Action.Method method, String contentType) {
        for (ActionInfo ai: actions) {
            Action a = ai.getSirenAction();
            if (method == a.getMethod() && contentType.equals(a.getType())) {
                return ai;
            }
        }
        log.warn("no action found for {} with type {}", method, contentType);
        return null;
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final Action.Method method = Action.Method.valueOf(req.getMethod());
        ActionInfo action = getAction(method, req.getContentType());
        if (action == null) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        action.execute(resource, req, resp);
    }
}
