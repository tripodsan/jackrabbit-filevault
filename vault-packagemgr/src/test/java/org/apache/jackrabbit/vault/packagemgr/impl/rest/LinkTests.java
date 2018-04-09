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

package org.apache.jackrabbit.vault.packagemgr.impl.rest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.LinkExample;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.AnnotationTransformer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkTests {

    private static final Set<String> REL_OTHER = Collections.singleton("other");

    private static final Set<String> REL_PACKAGES = Collections.singleton("packages");

    private static final Set<String> REL_FOO_BAR = new HashSet<>(Arrays.asList("foo", "bar"));

    private static final Map<String, Set<String>> TEST_LINKS = new HashMap<>();
    static {
        TEST_LINKS.put("http://filevault.apache.org/test1", REL_OTHER);
        TEST_LINKS.put("http://filevault.apache.org/test2", REL_FOO_BAR);
        TEST_LINKS.put("http://filevault.apache.org/test3", REL_OTHER);
        TEST_LINKS.put("http://filevault.apache.org/test4", REL_OTHER);
        TEST_LINKS.put("http://filevault.apache.org/base/api/packages", REL_PACKAGES);
    }

    @Test
    public void testLinks() throws Exception {
        AnnotationTransformer transformer = new AnnotationTransformer()
                .withBaseHref("http://filevault.apache.org/base/api")
                .withModel(new LinkExample());
        Map<String, Set<String>> tests = new HashMap<>(TEST_LINKS);
        for (Link link: transformer.collectLinks()) {
            Set<String> rels = tests.remove(link.getHref());
            assertEquals("Link: " + link.getHref(), rels, link.getRels());
        }
        assertEquals("empty", new HashMap(), tests);
    }


}
