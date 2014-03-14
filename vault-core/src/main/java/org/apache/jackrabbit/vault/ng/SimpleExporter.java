/*
 * Copyright 1997-2011 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
package org.apache.jackrabbit.vault.ng;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.api.ProgressTrackerListener;
import org.apache.jackrabbit.vault.fs.api.WorkspaceFilter;
import org.apache.jackrabbit.vault.fs.filter.NodeTypeItemFilter;
import org.apache.jackrabbit.vault.fs.spi.ProgressTracker;
import org.apache.jackrabbit.vault.util.JcrConstants;
import org.apache.jackrabbit.vault.util.MimeTypes;
import org.apache.jackrabbit.vault.util.PlatformNameFormat;
import org.apache.jackrabbit.vault.util.Text;
import org.apache.jackrabbit.vault.util.Tree;

/**
 * <code>SimpleExporter</code>...
 */
public class SimpleExporter {

    /**
     * default logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleExporter.class);

    private final File localRoot;

    private ProgressTracker tracker;

    private static Set<String> FULL_NT_NAMES = new HashSet<String>();
    static {
        FULL_NT_NAMES.add("rep:AccessControl");
        FULL_NT_NAMES.add("rep:Policy");
        FULL_NT_NAMES.add("cq:Widget");
        FULL_NT_NAMES.add("cq:EditConfig");
        FULL_NT_NAMES.add("cq:WorkflowModel");
        FULL_NT_NAMES.add("vlt:FullCoverage");
        FULL_NT_NAMES.add("mix:language");
        FULL_NT_NAMES.add("sling:OsgiConfig");
    }

    public SimpleExporter(File localRoot) {
        this.localRoot = localRoot;
    }

    public void setVerbose(ProgressTrackerListener out) {
        if (out == null) {
            tracker = null;
        } else {
            if (tracker == null) {
                tracker = new ProgressTracker();
            }
            tracker.setListener(out);
        }
    }

    public void export(Session session, String root, WorkspaceFilter filter) throws RepositoryException, IOException {
        // build filter tree
        Tree<PathFilterSet> filterTree = new Tree<PathFilterSet>();
        for (PathFilterSet set: filter.getFilterSets()) {
            filterTree.put(set.getRoot(), set);
        }
        //String commonRoot = filterTree.getRootPath();
        Node rootNode = session.getNode(root);
        export(rootNode, "", filter);
    }

    protected void export(Node node, String localPath, WorkspaceFilter filter) throws RepositoryException, IOException {
        String path = node.getPath();
        String name = Text.getName(path);
        String platformName = PlatformNameFormat.getPlatformName(name);
        if (!filter.covers(path)) {
            if (filter.isAncestor(path)) {
                NodeIterator iter = node.getNodes();
                String newLocalPath = name.length() == 0
                        ? ""
                        : localPath + "/" + platformName;
                while (iter.hasNext()) {
                    Node child = iter.nextNode();
                    export(child, newLocalPath, filter);
                }
            }
            return;
        }
        TreeWalker wlk = new TreeWalker();
        String jsonFileName = null;
        String subDirName = platformName;

        if (name.length() == 0) {
            // special case for root node
            jsonFileName = "_content.json";
            wlk.setMaxDepth(0);

        } else if (node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE)
                && node.hasProperty(JcrConstants.JCR_CONTENT + "/" + JcrConstants.JCR_DATA)) {
            // file
            Node content = node.getNode(JcrConstants.JCR_CONTENT);
            Property p = content.getProperty(JcrConstants.JCR_DATA);
            write(localPath + "/" + platformName, p);
            if (needsDir(node, content)) {
                subDirName = platformName + ".dir";
                jsonFileName = localPath + "/" + subDirName + "/_content.json";
                wlk.getContentFilter().addExclude(
                        new NodeTypeItemFilter(JcrConstants.NT_HIERARCHYNODE, true, 1, Integer.MAX_VALUE)
                ).seal();
                wlk.getIgnoredPaths().add(p.getPath());
            }
        } else if (isNodeType(node, FULL_NT_NAMES)) {
            // full coverage
            jsonFileName = localPath + "/" + platformName + ".json";

        } else if (node.isNodeType(JcrConstants.NT_HIERARCHYNODE) &&
                node.hasNode(JcrConstants.JCR_CONTENT)) {
            jsonFileName = localPath + "/" + subDirName + "/_content.json";
            wlk.getContentFilter().addExclude(
                    new NodeTypeItemFilter(JcrConstants.NT_HIERARCHYNODE, true, 1, Integer.MAX_VALUE)
            ).seal();
        } else {
            // todo: handle empty dirs for git
            if (!node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FOLDER)
                    || node.getMixinNodeTypes().length != 0) {
                jsonFileName = localPath + "/" + subDirName + "/_content.json";
                wlk.setMaxDepth(0);
            } else {
                // need to add the sub nodes here
                NodeIterator iter = node.getNodes();
                while (iter.hasNext()) {
                    wlk.getIgnored().add(iter.nextNode());
                }
            }
        }

        if (jsonFileName != null) {
            // export properties
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                JsonFormatter fmt = new JsonFormatter(null, out);
                wlk.walk(fmt, node, filter);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(out);
            }
            write(jsonFileName, out.toByteArray());
        }
        for (Property binary: wlk.getBinaries()) {
            // get relative path of binary in respect to current node
            StringBuilder relPath = new StringBuilder(localPath);
            if (subDirName.length() > 0) {
                relPath.append("/").append(subDirName);
            }
            for (String segment : Text.explode(binary.getPath().substring(path.length() + 1), '/')) {
                relPath.append("/").append(PlatformNameFormat.getPlatformName(segment));
            }
            relPath.append(".binary");
            write(relPath.toString(), binary);
        }
        for (Node ignored: wlk.getIgnored()) {
            // get relative path of sub node in respect to current node
            StringBuilder relPath = new StringBuilder(localPath);
            if (subDirName.length() > 0) {
                relPath.append("/").append(subDirName);
            }
            String[] segments = Text.explode(ignored.getPath().substring(path.length() + 1), '/');
            for (int i=0; i<segments.length - 1; i++) {
                relPath.append("/").append(PlatformNameFormat.getPlatformName(segments[i]));
            }
            export(ignored, relPath.toString(), filter);
        }
    }

    protected void track(String action, String path) {
        if ("E".equals(action)) {
            log.error("{} {}", action, path);
        } else {
            log.debug("{} {}", action, path);
        }
        if (tracker != null) {
            tracker.track(action, path);
        }
    }

    protected void track(Exception e, String path) {
        log.error("E {} ({})", path, e.toString());
        if (tracker != null) {
            tracker.track(e, path);
        }
    }


    protected void write(String relPath, byte[] data) throws IOException {
        File local = new File(localRoot, relPath);
        if (!local.getParentFile().exists()) {
            local.getParentFile().mkdirs();
        }
        try {
            OutputStream out = FileUtils.openOutputStream(local);
            out.write(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        track("A", relPath);
    }

    protected void write(String relPath, Property p) throws RepositoryException {
        File local = new File(localRoot, relPath);
        if (!local.getParentFile().exists()) {
            local.getParentFile().mkdirs();
        }

        Binary bin = p.getBinary();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = bin.getStream();
            out = FileUtils.openOutputStream(local);
            IOUtils.copy(in, out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            if (bin != null) {
                bin.dispose();
            }
        }
    }

    private boolean needsDir(Node node, Node content) throws RepositoryException {
        // retrieve basic properties
        long lastModified = 0;
        String encoding = null;
        try {
            lastModified = content.getProperty(JcrConstants.JCR_LASTMODIFIED).getDate().getTimeInMillis();
        } catch (RepositoryException e) {
            // ignore
        }
        String mimeType = null;
        if (content.hasProperty(JcrConstants.JCR_MIMETYPE)) {
            try {
                mimeType = content.getProperty(JcrConstants.JCR_MIMETYPE).getString();
            } catch (RepositoryException e) {
                // ignore
            }
        }
        if (mimeType == null) {
            // guess mime type from name
            mimeType = MimeTypes.getMimeType(node.getName(), MimeTypes.APPLICATION_OCTET_STREAM);
        }

        if (content.hasProperty(JcrConstants.JCR_ENCODING)) {
            try {
                encoding = content.getProperty(JcrConstants.JCR_ENCODING).getString();
            } catch (RepositoryException e) {
                // ignore
            }
        }

        // check needs .dir artifact nt:file. we trust that the repository does
        // not add other properties than the one defined in JCR.
        boolean needsDir = !node.getPrimaryNodeType().getName().equals(JcrConstants.NT_FILE);
        if (!needsDir) {
            // suppress mix:lockable (todo: make configurable)
            if (node.hasProperty(JcrConstants.JCR_MIXINTYPES)) {
                for (Value v: node.getProperty(JcrConstants.JCR_MIXINTYPES).getValues()) {
                    if (!v.getString().equals(JcrConstants.MIX_LOCKABLE)) {
                        needsDir = true;
                        break;
                    }
                }
            }
        }
        if (!needsDir) {
            needsDir = !content.getPrimaryNodeType().getName().equals(JcrConstants.NT_RESOURCE);
        }
        if (!needsDir) {
            if (content.hasProperty(JcrConstants.JCR_MIXINTYPES)) {
                for (Value v: content.getProperty(JcrConstants.JCR_MIXINTYPES).getValues()) {
                    if (!v.getString().equals(JcrConstants.MIX_LOCKABLE)) {
                        needsDir = true;
                        break;
                    }
                }
            }
        }
        if (!needsDir) {
            needsDir = !MimeTypes.matches(node.getName(), mimeType, MimeTypes.APPLICATION_OCTET_STREAM);
        }
        if (!needsDir && encoding != null) {
            needsDir = !"utf-8".equals(encoding) || MimeTypes.isBinary(mimeType);
        }
        return needsDir;
    }

    private boolean isNodeType(Node node, Set<String> names) {
        for (String name:names) {
            try {
                if (node.isNodeType(name)) {
                    return true;
                }
            } catch (RepositoryException e) {
                // ignore
            }
        }
        return false;
    }

    private static class ExportInfo {


    }
}