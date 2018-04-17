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

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;

@ApiModel
public class Filevault extends Base<Filevault> {

    public static final String VLT_BASE_URI = "http://jackrabbit.apache.org/filevault/rels";

    public static final String REL_VLT_PACKAGES = VLT_BASE_URI + "/packages";

    @ApiClass
    public static final String CLASS = "filevault";

    @ApiLink(REL_VLT_PACKAGES)
    public static final String PACKAGES = "/packages";

    @ApiProperty(name = "api-version")
    public static final String apiVersion = "1.0";

    @ApiProperty
    public static final String version = "3.2.0";
}
