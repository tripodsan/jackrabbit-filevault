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
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.auth.core.AuthenticationSupport;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.context.ServletContextHelper;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(service = ServletContextHelper.class,
           property = {
               HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_NAME + "=" + HttpContext.CONTEXT_NAME,
               HttpWhiteboardConstants.HTTP_WHITEBOARD_CONTEXT_PATH + "=" + HttpContext.CONTEXT_PATH
           }
        )
public class HttpContext extends ServletContextHelper {

    public static final String CONTEXT_PATH = "/system/jackrabbit/filevault";

    public static final String CONTEXT_NAME = "org.apache.jackrabbit.vault";

    /**
     * Handles security
     *
     * @see #handleSecurity(HttpServletRequest, HttpServletResponse)
     */
    @Reference
    private AuthenticationSupport authenticationSupport;

    /**
     * Returns the real context path that is used to mount this context.
     * @param req servlet request
     * @return the context path
     */
    public static String getRealContextPath(HttpServletRequest req) {
        final String path = req.getContextPath();
        if (path.equals(HttpContext.CONTEXT_PATH)) {
            return "";
        }
        return path.substring(0, path.length() - HttpContext.CONTEXT_PATH.length());
    }

    /**
     * Returns a request wrapper that transforms the context path back to the original one
     * @param req request
     * @return the request wrapper
     */
    public static HttpServletRequest createContextPathAdapterRequest(HttpServletRequest req) {
        return new HttpServletRequestWrapper(req) {

            @Override
            public String getContextPath() {
                return getRealContextPath((HttpServletRequest) getRequest());
            }

        };

    }

    /**
     * Always returns <code>null</code> because resources are all provided
     * through the {@link PackageMgrServlet}.
     */
    @Override
    public URL getResource(String name) {
        return null;
    }

    /**
     * Tries to authenticate the request using the
     * <code>SlingAuthenticator</code>. If the authenticator or the Repository
     * is missing this method returns <code>false</code> and sends a 503/SERVICE
     * UNAVAILABLE status back to the client.
     */
    @Override
    public boolean handleSecurity(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        final AuthenticationSupport authenticator = this.authenticationSupport;
        if (authenticator != null) {
            return authenticator.handleSecurity(createContextPathAdapterRequest(request), response);
        }

        // send 503/SERVICE UNAVAILABLE, flush to ensure delivery
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "AuthenticationSupport service missing. Cannot authenticate request.");
        response.flushBuffer();

        // terminate this request now
        return false;
    }
}
