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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiClass;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiModel;
import org.apache.jackrabbit.vault.packagemgr.impl.rest.annotations.ApiProperty;

@ApiModel(classes = "foo")
public class PropertyExample {

    @ApiProperty(name = "string-property")
    public static final String STRING_PROPERTY = "Hello, world.";

    @ApiProperty("string-property-1")
    public static final String STRING_PROPERTY_1 = "Hello, world.";

    @ApiProperty
    public static final String stringProperty = "Hello, world.";

    @ApiProperty
    public static final String nullStringProperty = null;

    @ApiProperty
    public final int intProperty = 42;

    @ApiProperty
    public final float floatProperty = 3.14f;

    @ApiProperty
    public final double doubleProperty = Math.PI;

    @ApiProperty
    public final boolean booleanProperty = true;

    @ApiProperty
    public Calendar dateProperty() {
        Calendar c = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis(0);
        return c;
    }

    @ApiProperty
    public final String[] stringArray = {"hello", "world"};

    @ApiProperty
    public final int[] intArray = {2,3,5,7,11,13};

    @ApiProperty
    public String getMethodStringProperty() {
        return "Hello, world.";
    }

    @ApiProperty
    public Boolean isMethodBooleanProperty() {
        return true;
    }

    @ApiProperty
    public String getPropertyLooksLikeMethod = "foo";

    @ApiProperty
    public Map<String, String> mapProperty() {
        Map<String, String> ret = new HashMap<>();
        ret.put("foo", "Hello");
        ret.put("bar", "world");
        return ret;
    }

    @ApiProperty(flatten = true)
    public Map<String, String> flatMapProperty() {
        Map<String, String> ret = new HashMap<>();
        ret.put("foo", "Hello");
        ret.put("bar", "world");
        return ret;
    }

    @ApiProperty(context = "foo")
    public String onlyInFoo = "foo";

    @ApiProperty(context = "bar")
    public String onlyInBar = "bar";
}
