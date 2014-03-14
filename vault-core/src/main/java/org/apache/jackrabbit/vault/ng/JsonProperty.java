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

package org.apache.jackrabbit.vault.ng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.util.XMLChar;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.jackrabbit.vault.util.ValueComparator;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.io.JSONWriter;

/**
 * Helper class that represents a (jcr) property in the json format.
 * It contains formatting and parsing methods for writing/reading enhanced
 * json properties.
 */
public class JsonProperty {

    /**
     * name of the property
     */
    public final String name;

    /**
     * final json name, suffixed with type
     */
    public final String jsonName;

    /**
     * value(s) of the property. always contains at least one value if this is
     * not a mv property.
     */
    public final Value[] values;

    /**
     * indicates a MV property
     */
    public final boolean isMulti;

    /**
     * type of this property (can be undefined)
     */
    public final int type;

    public static final SimpleDateFormat ECMA_FORMAT = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.US);

    /**
     * set of unambigous property names
     */
    private static final Set<String> UNAMBIGOUS = new HashSet<String>();
    static {
        UNAMBIGOUS.add("jcr:primaryType");
        UNAMBIGOUS.add("jcr:mixinTypes");
    }

    /**
     * Creates a new property.
     * @param name name of the property
     * @param values values.
     * @param multi multiple flag
     * @param type type of the property
     * @throws IllegalArgumentException if single value property and not
     *         exactly 1 value is given.
     */
    public JsonProperty(String name, Value[] values, boolean multi, int type) {
        this.name = name;
        this.values = values;
        isMulti = multi;
        // validate type
        if (type == PropertyType.UNDEFINED) {
            if (name.equals("jcr:primaryType") || name.equals("jcr:mixinTypes")) {
                type = PropertyType.NAME;
            }
        }
        this.type = type;

        // calculate JSON name
        if (multi && type == PropertyType.STRING
                || (type == PropertyType.STRING
                || type == PropertyType.BOOLEAN
                || type == PropertyType.LONG
                || type == PropertyType.DOUBLE
                || UNAMBIGOUS.contains(name))) {
            jsonName = name;
        } else {
            jsonName = name + "{" + PropertyType.nameFromValue(type) + "}";
        }

        if (!isMulti && values.length != 1) {
            throw new IllegalArgumentException("Single value property needs exactly 1 value.");
        }
    }

    public JsonProperty(Property prop) throws RepositoryException {
        this(
                prop.getName(),
                prop.isMultiple() ? prop.getValues() : new Value[]{prop.getValue()},
                prop.isMultiple(),
                prop.getType());
    }

    /**
     * Formats the given jcr property to the enhanced docview syntax.
     * @param sort if <code>true</code> multivalue properties are sorted
     * @throws javax.jcr.RepositoryException if a repository error occurs
     */
    public void write(JSONWriter w, boolean sort)
            throws RepositoryException, JSONException {
        // only write values for non binaries
        if (type != PropertyType.BINARY) {
            w.key(jsonName);
            if (isMulti) {
                w.array();
                if (sort) {
                    Arrays.sort(values, ValueComparator.getInstance());
                }
                for (Value v: values) {
                    write(w, v, type);
                }
                w.endArray();
            } else {
                write(w, values[0], type);
            }
        }
    }

    public static void write(JSONWriter writer, Value v, int type)
            throws RepositoryException, JSONException {
        switch (type) {
            case PropertyType.BOOLEAN:
                writer.value(v.getBoolean());
                break;
            case PropertyType.LONG:
                writer.value(v.getLong());
                break;
            case PropertyType.DOUBLE:
                writer.value((v.getDouble()));
                break;
            case PropertyType.DATE:
                synchronized (ECMA_FORMAT) {
                    writer.value(ECMA_FORMAT.format(v.getDate().getTime()));
                }
                break;
            default:
                writer.value(v.getString());

        }
    }

    /**
     * Escapes the value
     * @param buf buffer to append to
     * @param value value to escape
     * @param isMulti indicates multi value property
     */
    protected static void escape(StringBuffer buf, String value, boolean isMulti) {
        for (int i=0; i<value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\') {
                buf.append("\\\\");
            } else if (c == ',' && isMulti) {
                buf.append("\\,");
            } else if (i == 0 && !isMulti && (c == '[' || c == '{')) {
                buf.append('\\').append(c);
            } else if ( XMLChar.isInvalid(c)) {
                buf.append("\\u");
                buf.append(Text.hexTable[(c >> 12) & 15]);
                buf.append(Text.hexTable[(c >> 8) & 15]);
                buf.append(Text.hexTable[(c >> 4) & 15]);
                buf.append(Text.hexTable[c & 15]);
            } else {
                buf.append(c);
            }
        }
    }

    /**
     * Sets this property on the given node
     *
     * @param node the node
     * @return <code>true</code> if the value was modified.
     * @throws javax.jcr.RepositoryException if a repository error occurs
     */
    public boolean apply(Node node) throws RepositoryException {
        Property prop = node.hasProperty(name) ? node.getProperty(name) : null;
        // check if multiple flags are equal
        if (prop != null && isMulti != prop.getDefinition().isMultiple()) {
            prop.remove();
            prop = null;
        }
        if (prop != null) {
            int propType = prop.getType();
            if (propType != type && (propType != PropertyType.STRING || type != PropertyType.UNDEFINED)) {
                // never compare if types differ
                prop = null;
            }
        }
        if (isMulti) {
            Value[] vs = prop == null ? null : prop.getValues();
            if (vs != null && vs.length == values.length) {
                // quick check all values
                boolean modified = false;
                for (int i=0; i<vs.length; i++) {
                    if (!vs[i].getString().equals(values[i].getString())) {
                        modified = true;
                    }
                }
                if (!modified) {
                    return false;
                }
            }
            if (type == PropertyType.UNDEFINED) {
                node.setProperty(name, values);
            } else {
                node.setProperty(name, values, type);
            }
            // assume modified
            return true;
        } else {
            Value v = prop == null ? null : prop.getValue();
            if (v == null || !v.getString().equals(values[0].getString())) {
                try {
                    if (type == PropertyType.UNDEFINED) {
                        node.setProperty(name, values[0]);
                    } else {
                        node.setProperty(name, values[0], type);
                    }
                } catch (ValueFormatException e) {
                    // forcing string
                    node.setProperty(name, values[0], PropertyType.STRING);
                }
                return true;
            }
        }
        return false;
    }

    public static JsonProperty get(ValueFactory factory, JSONObject obj, String name)
            throws JSONException {
        Object value = obj.opt(name);
        if (value == null || value instanceof JSONObject) {
            return null;
        }
        // decode type
        int type = PropertyType.UNDEFINED;
        int idx = name.lastIndexOf('{');
        if (idx > 0) {
            int lIdx = name.lastIndexOf('}');
            if (lIdx < 0) {
                lIdx = name.length();
            }
            type = PropertyType.valueFromName(name.substring(idx+1, lIdx));
            name = name.substring(0, idx);
        }
        Value[] values;
        boolean isMulti = false;
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            values = new Value[array.length()];
            for (int i=0; i<array.length(); i++) {
                try {
                    values[i] = convert(factory, array.get(i), type);
                } catch (ParseException e) {
                    throw new JSONException(e);
                } catch (ValueFormatException e) {
                    throw new JSONException(e);
                }
            }
            isMulti = true;
        } else {
            try {
                values = new Value[]{convert(factory, value, type)};
            } catch (ParseException e) {
                throw new JSONException(e);
            } catch (ValueFormatException e) {
                throw new JSONException(e);
            }
            // override type
            type = values[0].getType();
        }
        return new JsonProperty(name, values, isMulti, type);
    }

    private static Value convert(ValueFactory factory, Object value, int type)
            throws ParseException, ValueFormatException {
        if (type == PropertyType.DATE) {
            synchronized (ECMA_FORMAT) {
                Date date = ECMA_FORMAT.parse(value.toString());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                return factory.createValue(cal);
            }
        } else if (type == PropertyType.UNDEFINED) {
            if (value instanceof Boolean) {
                return factory.createValue(((Boolean) value));
            } else if (value instanceof Integer) {
                return factory.createValue(((Integer) value));
            } else if (value instanceof Long) {
                return factory.createValue(((Long) value));
            } else if (value instanceof Double) {
                return factory.createValue(((Double) value));
            } else {
                return factory.createValue(value.toString());
            }
        } else {
            // simple conversion. todo: improve
            return factory.createValue(value.toString(), type);
        }
    }


}