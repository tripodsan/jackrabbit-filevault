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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiLink;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.LinkExample;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.Link;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.AnnotationTransformer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkTests {

    private static final Set<String> REL_OTHER = Collections.singleton("other");

    private static final Set<String> REL_FOO_BAR = new HashSet<>(Arrays.asList("foo", "bar"));

    private static final String HREF_TEST1 = "http://filevault.apache.org/test1";

    private static final String HREF_TEST2 = "http://filevault.apache.org/test2";

    private static final String HREF_TEST3 = "http://filevault.apache.org/test3";

    private static final String HREF_TEST4 = "http://filevault.apache.org/test4";

    private void testMember(String name, String expectedHref, Set<String> expectedRels) throws Exception {
        AnnotationTransformer transformer = new AnnotationTransformer();
        LinkExample obj = new LinkExample();
        Member member;
        ApiLink annotation;
        try {
            Field field = LinkExample.class.getField(name);
            annotation = field.getAnnotation(ApiLink.class);
            member = field;
        } catch (NoSuchFieldException e) {
            Method method = LinkExample.class.getMethod(name);
            annotation = method.getAnnotation(ApiLink.class);
            member = method;
        }
        Link link = transformer.transformLink(obj, annotation, member);
        assertEquals("href", expectedHref, link.getHref());
        assertEquals("rel", expectedRels, link.getRels());
    }

    @Test
    public void testStaticLink() throws Exception {
        testMember("TEST1", HREF_TEST1, REL_OTHER);
    }

    @Test
    public void testMultiRels() throws Exception {
        testMember("TEST2", HREF_TEST2, REL_FOO_BAR);
    }

    @Test
    public void testInstanceLink() throws Exception {
        testMember("test3", HREF_TEST3, REL_OTHER);
    }

    @Test
    public void testMethodLink() throws Exception {
        testMember("test4", HREF_TEST4, REL_OTHER);
    }


}
