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

package org.apache.jackrabbit.vault.packagemgr.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class ReflectionUtils {

    private static final String[] GETTER_PREFIX = {"get", "is", "has"};

    public static void addStrings(Set<String> set, Object obj) {
        if (obj == null) {
            return;
        }
        if (obj.getClass().isArray()) {
            final int len = Array.getLength(obj);
            for (int i=0; i<len; i++) {
                set.add(Array.get(obj, i).toString());
            }
        } else {
            set.add(obj.toString());
        }
    }

    public static Object getValue(Object obj, Member member) {
        try {
            if (member instanceof Field) {
                return ((Field) member).get(obj);
            } else if (member instanceof Method) {
                return ((Method) member).invoke(obj);
            }
            return null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String getStringValue(Object obj, Member member) {
        final Object ret = getValue(obj, member);
        return ret == null ? null : ret.toString();
    }

    public static String methodToPropertyName(String name) {
        for (String pfx: GETTER_PREFIX) {
            final int len = pfx.length();
            if (name.length() > len && name.startsWith(pfx)) {
                Character firstLetter = Character.toLowerCase(name.charAt(len));
                return firstLetter + name.substring(len + 1);
            }
        }
        return name;
    }

    public static String methodToActionName(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        StringBuilder ret = new StringBuilder();
        boolean isLowerCase = false;
        for (int i=0; i<name.length();i++) {
            char c = name.charAt(i);
            if (Character.isLowerCase(c)) {
                isLowerCase = true;
                ret.append(c);
            } else {
                if (isLowerCase) {
                    ret.append('-');
                }
                isLowerCase = false;
                ret.append(Character.toLowerCase(c));
            }
        }
        return ret.toString();
    }

    public static void addProperty(Map<String, Object> properties, String name, boolean flattenMap, Object value) {
        if (value instanceof Map && flattenMap) {
            Map<?,?> map = (Map) value;
            for (Map.Entry entry: map.entrySet()) {
                addProperty(properties, entry.getKey().toString(), false, entry.getValue());
            }
            return;
        }
        properties.put(name, value);
    }

    public static Member[] getFieldsAndMethods(Class clazz) {
        Field[] fields = clazz.getFields();
        Method[] methods = clazz.getMethods();
        Member[] ret = new Member[fields.length + methods.length];
        System.arraycopy(fields, 0, ret, 0, fields.length);
        System.arraycopy(methods, 0, ret, fields.length, methods.length);
        return ret;
    }
}
