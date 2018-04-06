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
package org.apache.jackrabbit.vault.packagemgr.impl.siren.builder;

import java.util.Set;
import java.util.TreeSet;

import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;

/**
 * {@code LinkBuilder}...
 */
public class LinkBuilder extends BaseBuilder implements Link {

    private String href = "";

    private TreeSet<String> rels = new TreeSet<String>();

    public LinkBuilder addRel(String rel) {
        rels.add(rel);
        return this;
    }

    public LinkBuilder withRels(Set<String> rels) {
        this.rels.addAll(rels);
        return this;
    }

    public LinkBuilder withHref(String href) {
        this.href = href == null ? "" : href;
        return this;
    }

    @Override
    public LinkBuilder withTitle(String title) {
        super.withTitle(title);
        return this;
    }

    public Set<String> getRels() {
        return rels;
    }

    public String getHref() {
        return href;
    }

    public int compareTo(Link o) {
        return href.compareTo(o.getHref());
    }
}