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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures;

import java.util.Arrays;
import java.util.List;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;

@ApiModel
public class LinkExample {

    @ApiLink(ApiLink.SELF)
    public static final String SELF = "";

    @ApiLink("other")
    public static final String TEST1 = "http://filevault.apache.org/test1";

    @ApiLink({"foo", "bar"})
    public static final String TEST2 = "http://filevault.apache.org/test2";

    @ApiLink("other")
    public final String test3 = "http://filevault.apache.org/test3";

    @ApiLink("other")
    public String test4() {
        return "http://filevault.apache.org/test4";
    }

    @ApiLink("packages")
    public String test6() {
        return "/packages";
    }

    @ApiLink("other")
    public String nullLinkIsIgnored() {
        return null;
    }

    @ApiLink("screenshot")
    public String[] stringArrayTest = {
            "http://filevault.apache.org/test5",
            "http://filevault.apache.org/test6",
    };

    @ApiLink("icon")
    public List<String> iterableTest() {
        return Arrays.asList(
                "http://filevault.apache.org/test7",
                "http://filevault.apache.org/test8"
        );
    };

    @ApiLink(
            value = "other",
            classes = {"foo", "bar"},
            type = "image/png",
            title = "Hello, world."
    )
    public static final String TEST9 = "http://filevault.apache.org/test9";

    public static final String TEST5 = "no link annotation";

}
