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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class BaseBuilder<B extends BaseBuilder<B>> {

    protected String href = "";

    protected Set<String> rels = new TreeSet<>();

    protected TreeSet<String> classes = new TreeSet<String>();

    protected String name;

    protected String type;

    protected String title;

    public B withHref(String href) {
        this.href = href;
        //noinspection unchecked
        return (B) this;
    }

    public B addRel(String rel) {
        this.rels.add(rel);
        //noinspection unchecked
        return (B) this;
    }

    public B withRels(Set<String> rels) {
        this.rels.addAll(rels);
        //noinspection unchecked
        return (B) this;
    }

    public B withRels(String[] rels) {
        this.rels.addAll(Arrays.asList(rels));
        //noinspection unchecked
        return (B) this;
    }

    public B addClass(String clazz) {
        this.classes.add(clazz);
        //noinspection unchecked
        return (B) this;
    }

    public B withClasses(Set<String> classes) {
        this.classes.addAll(classes);
        //noinspection unchecked
        return (B) this;
    }

    public B withClasses(String[] classes) {
        this.classes.addAll(Arrays.asList(classes));
        //noinspection unchecked
        return (B) this;
    }

    public B withName(String name) {
        this.name = name;
        //noinspection unchecked
        return (B) this;
    }

    public B withType(String type) {
        this.type = type;
        //noinspection unchecked
        return (B) this;
    }

    public B withTitle(String title) {
        this.title = title;
        //noinspection unchecked
        return (B) this;
    }
}
