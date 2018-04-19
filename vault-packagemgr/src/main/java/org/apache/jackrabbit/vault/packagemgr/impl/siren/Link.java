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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Links represent navigational transitions. In JSON Siren, links are represented as an array inside the entity
 */
public interface Link extends Comparable<Link> {

    String SELF = "self";

    /**
     * Defines the relationship of the link to its entity, per<a href="https://tools.ietf.org/html/rfc5988">Web Linking (RFC5988)</a>
     * and <a href="http://www.iana.org/assignments/link-relations/link-relations.xhtml">Link Relations</a>.
     * Required.
     * @return the relations
     */
    @Nonnull
    Set<String> getRels();

    /**
     * The URI of the linked resource. Required.
     * @return the URI
     */
    @Nonnull
    String getHref();

    /**
     * Describes aspects of the link based on the current representation. Possible values are implementation-dependent
     * and should be documented. Optional.
     * @return the set of classes
     */
    @Nullable
    Set<String> getClasses();

    /**
     * Defines media type of the linked resource, per <a href="https://tools.ietf.org/html/rfc5988">Web Linking (RFC5988)</a>. Optional.
     * @return the media type
     */
    @Nullable
    String getType();

    /**
     * Text describing the nature of a link. Optional.
     * @return the title
     */
    @Nullable
    String getTitle();

}