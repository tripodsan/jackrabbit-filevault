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

package org.apache.jackrabbit.vault.packagemgr.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class RestUtils {

    public static void sendFile(HttpServletRequest request, HttpServletResponse response, Node file)
            throws IOException, RepositoryException {
        Binary bin = file.getProperty("jcr:content/jcr:data").getBinary();
        response.setContentType(file.getProperty("jcr:content/jcr:mimeType").getString());
        response.setContentLength((int) bin.getSize());
        IOUtils.copy(bin.getStream(), response.getOutputStream());
        bin.dispose();
    }

    public static String resolveHref(URI base, String href) {
        if (href.isEmpty()) {
            return base.toString();
        } else if (href.startsWith("/") || href.startsWith("?") ) {
            return base.toString() + href;
        }
        return href;
    }
}
