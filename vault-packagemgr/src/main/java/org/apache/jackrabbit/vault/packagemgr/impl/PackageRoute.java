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

import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.vault.packaging.PackageId;

public class PackageRoute {

    private PackageId pkgId;

    private String command;

    private String file;

    public static String getPackageAPIPath(String baseHref, PackageId id) {
        StringBuilder b = new StringBuilder(baseHref).append("/packages/");
        if (id.getGroup().length() > 0) {
            b.append(id.getGroup());
            b.append("/");
        }
        b.append(id.getName());
        if (id.getVersionString().length() > 0) {
            b.append("/").append(id.getVersionString());
        }
        return b.toString();
    }

    public PackageRoute(String path) {
        String[] segs = Text.explode(path, '/');
        // empty
        if (segs.length == 0) {
            return;
        }

        // /name
        if (segs.length == 1) {
            pkgId = new PackageId("", segs[0], "");
            return;
        }

        String last = segs[segs.length-1];
        if ("thumbnail.png".equals(last)) {
            pkgId = createPackageId(segs, 0, segs.length - 1);
            command = "thumbnail";
            file = last;
        } else if (segs.length > 2 && "screenshot".equals(segs[segs.length - 2])) {
            pkgId = createPackageId(segs, 0, segs.length - 2);
            command = "screenshot";
            file = last;
        } else {
            pkgId = createPackageId(segs, 0, segs.length - 1);
            if (last.equals(pkgId.getDownloadName())) {
                file = last;
                command = "download";
            } else {
                pkgId = createPackageId(segs, 0, segs.length);
            }
        }
    }

    private static PackageId createPackageId(String[] segs, int from, int to) {
        StringBuilder b = new StringBuilder();
        while (from < to - 2) {
            String seg = segs[from++];
            if (!seg.equals("-")) {
                b.append("/").append(seg);
            }
        }
        String group = b.toString();
        String name = segs[to-2];
        String version = segs[to-1].equals("-") ? "" : segs[to-1];
        return new PackageId(group, name, version);
    }

    public PackageId getPackageId() {
        return pkgId;
    }

    public String getCommand() {
        return command;
    }

    public String getFile() {
        return file;
    }
}
