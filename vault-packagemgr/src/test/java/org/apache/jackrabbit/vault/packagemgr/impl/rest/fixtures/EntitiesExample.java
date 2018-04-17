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
import java.util.Collection;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiEntities;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiRelation;

@ApiModel
public class EntitiesExample {

    @ApiEntities
    public Collection<SimpleEntity> entities() {
        return Arrays.asList(
                new SimpleEntity("1", "Hello, world."),
                new SimpleEntity("2", "Jackrabbit is cool.")
        );
    }

    @ApiModel
    public static class SimpleEntity {

        private final String id;

        private final String title;

        public SimpleEntity(String id, String title) {
            this.id = id;
            this.title = title;
        }

        @ApiClass
        public static String CLASS = "simple-entity";

        @ApiRelation
        public static String REL = "http://filevault.apache.org/base/api/simple";

        @ApiLink(ApiLink.SELF)
        public String selfLink() {
            return "/" + id;
        }

        @ApiProperty
        public String getTitle() {
            return title;
        }

        @ApiProperty(context = ApiProperty.Context.INLINE)
        public String getInlineTitle() {
            return "inline: " + title;
        }

        @ApiProperty(context = ApiProperty.Context.ENTITY)
        public String getEntityTitle() {
            return "entity: " + title;
        }


    }
}
