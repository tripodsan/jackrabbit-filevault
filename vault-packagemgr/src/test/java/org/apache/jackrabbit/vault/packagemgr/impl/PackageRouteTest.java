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

import org.apache.jackrabbit.vault.packaging.PackageId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * {@code PackageRouteTest}...
 */
public class PackageRouteTest {

    @Test
    public void testRoutesSingleName() {
        testRouteEquals("/-/foo/-", ":foo");
    }

    @Test
    public void testRoutesSimpleGroup() {
        testRouteEquals("/group/foo/-", "group:foo");
    }

    @Test
    public void testRoutesDeepGroup() {
        testRouteEquals("/group/deep/foo/-", "group/deep:foo");
    }

    @Test
    public void testRoutesVersion() {
        testRouteEquals("/group/deep/foo/1.0", "group/deep:foo:1.0");
    }

    @Test
    public void testRoutesNoGroup() {
        testRouteEquals("/-/foo/1.0", ":foo:1.0");
    }

    private void testRouteEquals(String pfx, String packageId) {
        assertRouteEquals(pfx, packageId, null, null);
        assertRouteEquals(pfx + "/thumbnail.png", packageId, "thumbnail", "thumbnail.png");
        assertRouteEquals(pfx + "/screenshot/1.png", packageId, "screenshot", "1.png");
        String downloadName = PackageId.fromString(packageId).getDownloadName();
        assertRouteEquals(pfx + "/" + downloadName, packageId, "download", downloadName);
    }

    private void assertRouteEquals(String path, String packageId, String command, String file) {
        PackageRoute r = new PackageRoute(path);
        assertEquals(packageId, r.getPackageId() == null ? null : r.getPackageId().toString());
        assertEquals(command, r.getCommand());
        assertEquals(file, r.getFile());
    }
}