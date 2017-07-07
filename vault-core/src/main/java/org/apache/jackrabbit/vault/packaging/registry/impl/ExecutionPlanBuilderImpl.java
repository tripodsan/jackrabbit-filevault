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
package org.apache.jackrabbit.vault.packaging.registry.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;
import org.apache.jackrabbit.vault.packaging.registry.ExecutionPlan;
import org.apache.jackrabbit.vault.packaging.registry.ExecutionPlanBuilder;
import org.apache.jackrabbit.vault.packaging.registry.PackageTask;
import org.apache.jackrabbit.vault.packaging.registry.PackageTaskBuilder;
import org.apache.jackrabbit.vault.util.RejectingEntityResolver;
import org.apache.jackrabbit.vault.util.xml.serialize.OutputFormat;
import org.apache.jackrabbit.vault.util.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * {@code ExecutionPlanBuilderImpl}...
 */
public class ExecutionPlanBuilderImpl implements ExecutionPlanBuilder {

    private final static String ATTR_VERSION = "1.0";
    private static final String TAG_EXECUTION_PLAN = "executionPlan";
    private static final String TAG_TASK = "task";
    private static final String ATTR_CMD = "cmd";
    private static final String ATTR_PACKAGE_ID = "packageId";

    public static final double SUPPORTED_VERSION = 1.0;

    protected double version = SUPPORTED_VERSION;

    private final List<TaskBuilder> tasks = new LinkedList<TaskBuilder>();

    private final JcrPackageRegistry registry;

    private Session session;

    private ProgressTrackerListener listener;

    private ExecutionPlanImpl plan;

    ExecutionPlanBuilderImpl(JcrPackageRegistry registry) {
        this.registry = registry;
    }

    @Nonnull
    @Override
    public ExecutionPlanBuilder save(@Nonnull OutputStream out) throws IOException, PackageException {
        validate();
        try {
            XMLSerializer ser = new XMLSerializer(out, new OutputFormat("xml", "UTF-8", true));
            ser.startDocument();
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(null, null, ATTR_VERSION, "CDATA", String.valueOf(version));
            ser.startElement(null, null, TAG_EXECUTION_PLAN, attrs);

            for (PackageTask task: plan.getTasks()) {
                attrs = new AttributesImpl();
                attrs.addAttribute(null, null, ATTR_CMD, "CDATA", task.getType().name().toLowerCase());
                attrs.addAttribute(null, null, ATTR_PACKAGE_ID, "CDATA", task.getPackageId().toString());
                ser.startElement(null, null, TAG_TASK, attrs);
                ser.endElement(TAG_TASK);
            }
            ser.endElement(TAG_EXECUTION_PLAN);
            ser.endDocument();
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    @Nonnull
    @Override
    public ExecutionPlanBuilder load(@Nonnull InputStream in) throws IOException {
        tasks.clear();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new RejectingEntityResolver());
            Document document = builder.parse(in);
            Element doc = document.getDocumentElement();
            if (!TAG_EXECUTION_PLAN.equals(doc.getNodeName())) {
                throw new IOException("<" + TAG_EXECUTION_PLAN + "> expected.");
            }
            String v = doc.getAttribute(ATTR_VERSION);
            if (v == null || "".equals(v)) {
                v = "1.0";
            }
            version = Double.parseDouble(v);
            if (version > SUPPORTED_VERSION) {
                throw new IOException("version " + version + " not supported.");
            }
            read(doc);
        } catch (ParserConfigurationException e) {
            throw new IOException("Unable to create configuration XML parser", e);
        } catch (SAXException e) {
            throw new IOException("Configuration file syntax error.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return this;
    }

    private void read(Element elem) throws IOException {
        NodeList nl = elem.getChildNodes();
        for (int i=0; i<nl.getLength(); i++) {
            Node child = nl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (!TAG_TASK.equals(child.getNodeName())) {
                    throw new IOException("<" + TAG_TASK + "> expected.");
                }
                readTask((Element) child);
            }
        }
    }

    private void readTask(Element elem) throws IOException {
        PackageTask.Type type = PackageTask.Type.valueOf(elem.getAttribute(ATTR_CMD).toUpperCase());
        PackageId id = PackageId.fromString(elem.getAttribute(ATTR_PACKAGE_ID));
        addTask().with(id).with(type);
    }

    @Nonnull
    @Override
    public PackageTaskBuilder addTask() {
        TaskBuilder task = new TaskBuilder();
        tasks.add(task);
        plan = null; // invalidate potential plan
        return task;
    }

    @Nonnull
    @Override
    public ExecutionPlanBuilder with(@Nonnull Session session) {
        this.session = session;
        return this;
    }

    @Nonnull
    @Override
    public ExecutionPlanBuilder with(@Nonnull ProgressTrackerListener listener) {
        this.listener = listener;
        return this;
    }

    @Nonnull
    @Override
    public ExecutionPlanBuilder validate() throws IOException, PackageException {
        List<PackageTask> packageTasks = new ArrayList<PackageTask>(tasks.size());
        for (TaskBuilder task: tasks) {
            if (task.id == null || task.type == null) {
                throw new PackageException("task builder must have package id and type defined.");
            }
            packageTasks.add(new PackageTaskImpl(task.id, task.type));
        }
        plan = new ExecutionPlanImpl(packageTasks);
        return this;
    }

    @Nonnull
    @Override
    public ExecutionPlan build() throws IOException, PackageException {
        if (plan == null) {
            validate();
        }
        return plan.with(registry).with(listener);
    }

    private class TaskBuilder implements PackageTaskBuilder {
        private PackageId id;
        private PackageTask.Type type;

        public PackageTaskBuilder with(@Nonnull PackageId id) {
            this.id = id;
            return this;
        }

        @Nonnull
        @Override
        public ExecutionPlanBuilder with(@Nonnull PackageTask.Type type) {
            this.type = type;
            return ExecutionPlanBuilderImpl.this;
        }
    }
}