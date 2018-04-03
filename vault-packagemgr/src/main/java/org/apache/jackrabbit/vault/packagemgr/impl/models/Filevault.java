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

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Entity;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Rels;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.EntityBuilder;

public class Filevault extends Base {

    public static String CLASS = "filevault";

    @Override
    public Entity buildEntity() {
        return new EntityBuilder()
                .addClass(CLASS)
                .addProperty("version", "3.2.0")
                .addProperty("api-version", "1.0")
                .addLink(Rels.SELF, baseHref)
                .addLink(Rels.REL_VLT_PACKAGES, baseHref + "/packages");
    }
}
