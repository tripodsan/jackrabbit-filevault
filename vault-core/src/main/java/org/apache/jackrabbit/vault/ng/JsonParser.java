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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.jcr.ValueFactory;

import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.xml.sax.InputSource;


/**
 * <code>JsonSaxAdapter</code>...
 */
public class JsonParser {

    public void parse(InputSource inputSource, Handler handler) throws IOException {
        try {
            String jsonString = IOUtils.toString(inputSource.getByteStream(), "utf-8");
            JSONObject obj = new JSONObject(jsonString);
            transform(handler, obj);
        } catch (JSONException e) {
            throw new IOException(e.toString());
        }
    }

    private void transform(Handler handler, JSONObject obj) throws JSONException {
        handler.startDocument();
        transform(handler, "", obj);
        handler.endDocument();
    }

    private void transform(Handler handler, String nodeName, JSONObject obj) throws JSONException {
        List<JsonProperty> props = new LinkedList<JsonProperty>();
        Iterator iter = obj.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            JsonProperty prop = JsonProperty.get(handler.getValueFactory(), obj, key);
            if (prop != null) {
                props.add(prop);
            }
        }
        handler.startNode(nodeName, props);
        iter = obj.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                transform(handler, key, (JSONObject) value);
            }
        }
        handler.endNode(nodeName);
    }

    public interface Handler {
        void startDocument() throws JSONException;
        void endDocument() throws JSONException;
        void startNode(String name, List<JsonProperty> props) throws JSONException;
        void endNode(String name) throws JSONException;
        ValueFactory getValueFactory() throws JSONException;
    }
}