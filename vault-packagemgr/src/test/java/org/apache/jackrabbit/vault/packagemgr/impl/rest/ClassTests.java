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
import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.ClassExample;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.ClassExampleWithModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.meta.AnnotationTransformer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClassTests {

    private static final Set<String> TEST1_CLASSES = new HashSet<>(Arrays.asList("test1", "test2a", "test2b", "test3", "test4", "test5a", "test5b"));

    private static final Set<String> TEST2_CLASSES = new HashSet<>(Collections.singletonList("main"));

    @Test
    public void testMemberClasses() throws Exception {
        AnnotationTransformer transformer = new AnnotationTransformer()
                .withModel(new ClassExample());
        Set<String> classes = transformer.collectClasses();
        assertEquals("Classes", TEST1_CLASSES, classes);
    }

    @Test
    public void testModelClasses() throws Exception {
        AnnotationTransformer transformer = new AnnotationTransformer()
                .withModel(new ClassExampleWithModel());
        Set<String> classes = transformer.collectClasses();
        assertEquals("Classes", TEST2_CLASSES, classes);
    }


}
