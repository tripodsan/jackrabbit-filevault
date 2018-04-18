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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.jackrabbit.vault.packagemgr.impl.ReflectionUtils;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.fixtures.PropertyExample;
import org.apache.jackrabbit.vault.packagemgr.impl.siren.builder.AnnotationTransformer;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PropertyTests {

    private static final Map<String, Object> TEST_PROPERTIES = new HashMap<>();
    private static final Map<String, Object> TEST_SUB_MAP = new HashMap<>();
    static {
        TEST_SUB_MAP.put("foo", "Hello");
        TEST_SUB_MAP.put("bar", "world");

        TEST_PROPERTIES.put("string-property", "Hello, world.");
        TEST_PROPERTIES.put("string-property-1", "Hello, world.");
        TEST_PROPERTIES.put("stringProperty", "Hello, world.");
        TEST_PROPERTIES.put("intProperty", 42);
        TEST_PROPERTIES.put("floatProperty", 3.14f);
        TEST_PROPERTIES.put("doubleProperty", Math.PI);
        TEST_PROPERTIES.put("booleanProperty", true);
        TEST_PROPERTIES.put("stringArray", new String[]{"hello", "world"});
        TEST_PROPERTIES.put("intArray", new int[]{2,3,5,7,11,13});
        TEST_PROPERTIES.put("methodStringProperty", "Hello, world.");
        TEST_PROPERTIES.put("methodBooleanProperty", true);
        TEST_PROPERTIES.put("getPropertyLooksLikeMethod", "foo");
        TEST_PROPERTIES.put("mapProperty", TEST_SUB_MAP);
        TEST_PROPERTIES.put("foo", "Hello");
        TEST_PROPERTIES.put("bar", "world");
        TEST_PROPERTIES.put("onlyInFoo", "foo");
        Calendar date = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        date.setTimeInMillis(0);
        TEST_PROPERTIES.put("dateProperty", date);
    }

    @Test
    public void testProperty() throws Exception {
        AnnotationTransformer transformer = new AnnotationTransformer()
                .withModel(new PropertyExample());
        transformer.collectClasses(); // need to init the classes
        Map<String, Object> properties = transformer.collectProperties();
        for (Map.Entry<String, Object> e: TEST_PROPERTIES.entrySet()) {
            Object v = properties.remove(e.getKey());
            assertNotNull("Property:" + e.getKey(), v);
            if (v instanceof int[]) {
                assertArrayEquals("Property:" + e.getKey(), (int[]) e.getValue(), (int[]) v);
            } else if (v instanceof String[]) {
                assertArrayEquals("Property:" + e.getKey(), (String[]) e.getValue(), (String[]) v);
            } else {
                assertEquals("Property:" + e.getKey(), e.getValue(), v);
            }
        }
        assertEquals("no additional properties", new HashMap(), properties);
    }

    @Test
    public void testMethodToPropertyName() {
        assertEquals("foo", ReflectionUtils.methodToPropertyName("foo"));
        assertEquals("foo", ReflectionUtils.methodToPropertyName("getfoo"));
        assertEquals("foo", ReflectionUtils.methodToPropertyName("getFoo"));
        assertEquals("foo", ReflectionUtils.methodToPropertyName("isFoo"));
        assertEquals("foo", ReflectionUtils.methodToPropertyName("hasFoo"));
        assertEquals("setFoo", ReflectionUtils.methodToPropertyName("setFoo"));
        assertEquals("get", ReflectionUtils.methodToPropertyName("get"));

    }
}
