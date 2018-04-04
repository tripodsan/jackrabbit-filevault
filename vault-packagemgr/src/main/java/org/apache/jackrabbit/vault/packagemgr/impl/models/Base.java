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

package org.apache.jackrabbit.vault.packagemgr.impl.models;

import java.io.IOException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.PackageRoute;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.json.SirenJsonWriter;

public abstract class Base {

    String baseHref;

    public Base withBaseHref(String baseHref) {
        this.baseHref = baseHref;
        return this;
    }

    public abstract Entity buildEntity() throws IOException;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Entity entity = buildEntity();
        try (SirenJsonWriter out = new SirenJsonWriter(response.getWriter())) {
            out.write(entity);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public void sendFile(HttpServletRequest request, HttpServletResponse response, Node file)
            throws IOException, RepositoryException {
        Binary bin = file.getProperty("jcr:content/jcr:data").getBinary();
        response.setContentType(file.getProperty("jcr:content/jcr:mimeType").getString());
        response.setContentLength((int) bin.getSize());
        IOUtils.copy(bin.getStream(), response.getOutputStream());
        bin.dispose();
    }
}
