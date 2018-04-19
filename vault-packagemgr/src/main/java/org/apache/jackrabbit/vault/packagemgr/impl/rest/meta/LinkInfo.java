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

package org.apache.jackrabbit.vault.packagemgr.impl.rest.meta;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.LinkBuilder;

public class LinkInfo {

    private Member member;

    private Set<String> rels;

    private Set<String> classes;

    private String title;

    private String type;

    private LinkInfo() {
    }

    public LinkBuilder[] createSirenLinkBuilders(Object resource) {
        String[] values = ReflectionUtils.getStringValues(resource, member);
        if (values == null || values.length == 0) {
            return new LinkBuilder[0];
        }
        LinkBuilder[] ret = new LinkBuilder[values.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = new LinkBuilder()
                    .withHref(values[i])
                    .withRels(rels)
                    .withClasses(classes)
                    .withTitle(title)
                    .withType(type);
        }
        return ret;
    }

    public static class Builder {

        private LinkInfo info;

        public Builder() {
            this.info = new LinkInfo();
        }

        public Builder withMember(Member member) {
            info.member = member;
            return this;
        }

        public Builder withRels(Collection<String> rels) {
            info.rels = new TreeSet<>(rels);
            return this;
        }

        public Builder withClasses(Collection<String> classes) {
            info.classes = new TreeSet<>(classes);
            return this;
        }

        public Builder withTitle(String title) {
            info.title = title;
            return this;
        }

        public Builder withType(String type) {
            info.type = type;
            return this;
        }

        public LinkInfo build() {
            try {
                return info;
            } finally {
                info = null;
            }
        }
    }
}
