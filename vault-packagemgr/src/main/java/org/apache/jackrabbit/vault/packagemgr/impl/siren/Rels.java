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
package org.apache.jackrabbit.vault.packagemgr.impl.siren;

/**
 * {@code Rels}...
 */
public interface Rels {

    static final String SELF = "self";

    static final String BASE_URI = "http://jackrabbit.apache.org/filevault/rels";

    static final String VLT_PACKAGES = BASE_URI + "/packages";
    static final String VLT_PACKAGE = BASE_URI + "/package";
    static final String VLT_PACKAGE_BRIEF = BASE_URI + "/package-brief";
    static final String VLT_PACKAGE_DOWNLOAD = BASE_URI + "/package-download";
}