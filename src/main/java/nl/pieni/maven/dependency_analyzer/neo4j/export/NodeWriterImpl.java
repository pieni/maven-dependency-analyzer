/*
 * Copyright (c) 2011 Pieter van der Meer (pieter@pieni.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.pieni.maven.dependency_analyzer.neo4j.export;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 15-1-11
 * Time: 23:10
 * To change this template use File | Settings | File Templates.
 */
public class NodeWriterImpl implements NodeWriter {

    private Writer writer;
    private final String lineSeparator = System.getProperty("line.separator");
    private final Log LOG;
    private Set<Node> visitedNodes = new HashSet<Node>();
    private Set<Relationship> visitedRelations = new HashSet<Relationship>();

    public NodeWriterImpl(String outputFile, Log LOG) throws IOException {
        this.LOG = LOG;
        FileOutputStream fos = new FileOutputStream(outputFile);
        LOG.info("Created output file: " + outputFile);
        this.writer = new OutputStreamWriter(fos, "UTF-8");
        startGraph();
    }

    private void startGraph() throws IOException {
        writer.write(" digraph G {" + lineSeparator);
    }

    private void endGraph() throws IOException {
        writer.write("}");
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing file");
        endGraph();
        writer.flush();
        writer.close();
    }

    @Override
    public void writeNode(VersionNodeDecorator node) throws IOException {
        if (visitedNodes.add((Node)node)) {
            LOG.debug("Writing VersionNode " + node);
            String version = node.getVersion();
            long nodeId = node.getId();
            writeNode(nodeId, version, NodeShape.component);
        }
    }


    @Override
    public void writeRootNode(Node node) throws IOException {
        if (visitedNodes.add(node)) {
            LOG.debug("Writing RootNode");
            writeNode(node.getId(), "root", NodeShape.box);
        }
    }

    @Override
    public void writeNode(ArtifactNodeDecorator node) throws IOException {
        if (visitedNodes.add((Node)node)) {
            LOG.debug("Writing ArtifactNode " + node);
            String artifactId = node.getArtifactId();
            long nodeId = node.getId();
            writeNode(nodeId, artifactId, NodeShape.rect);
        }
    }

    @Override
    public void writeNode(GroupNodeDecorator node) throws IOException {
        if (visitedNodes.add(node)) {
            LOG.debug("Writing GroupNode " + node);
            String groupId = node.getGroupId();
            long nodeId = node.getId();
            writeNode(nodeId, groupId, NodeShape.folder);
        }
    }

    @Override
    public void writeRelation(Node startNode, Node otherNode, Relationship type) throws IOException {
        if (visitedRelations.add(type)) {
            LOG.debug("Writing Relation " + startNode.getId() + "-> " + otherNode.getId() + " (" + type + ")");
            writer.write("\tN" + startNode.getId() + " -> " + "N" + otherNode.getId() + " [label=\"" + type.getType() + "\"]" + lineSeparator);
        }
    }

    @Override
    public void writeNode(GroupNodeDecorator current, GroupNodeDecorator last) throws IOException {
        if (visitedNodes.add(current)) {
            LOG.debug("Writing GroupNode " + last);
            writeNode(current.getId(), getAddedGroupIdPart(current.getGroupId(), last.getGroupId()), NodeShape.folder);
        }
    }

    @Override
    public void writeReferenceRelation(Node refNode, Node refNodeRelation) throws IOException {
        writer.write("\tN" + refNode.getId() + " -> " + "N" + refNodeRelation.getId() + " [label=\"" + ArtifactRelations.has + "\"]" + lineSeparator);
    }

    private String getAddedGroupIdPart(String current, String previous) {
        StringTokenizer stringTokenizer = new StringTokenizer(previous, ".");
        String tmp = "";
        String match = "";
        while (stringTokenizer.hasMoreElements()) {
            tmp += stringTokenizer.nextToken();
            if (current.startsWith(tmp)) {
                tmp += ".";
                match = tmp;
            }
        }

        return current.substring(match.length(), current.length());
    }

    private void writeNode(long id, String labelText, NodeShape shape) throws IOException {
        writer.write("\tN" + id + " [label=\"" + labelText + "\"" + " shape=" + shape + "]" + lineSeparator);
    }
}
