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
 * Implementation of a DOT (@see http://graphviz.org) format Node file.
 */
public class NodeWriterImpl implements NodeWriter {

    private final Writer writer;
    private final String lineSeparator = System.getProperty("line.separator");
    private final Log LOG;
    private final Set<Node> visitedNodes = new HashSet<Node>();
    private final Set<Relationship> visitedRelations = new HashSet<Relationship>();

    /**
     * Default constructor
     * @param writer the putput stream writer
     * @param LOG the Logger
     * @throws IOException in case of error
     */
    public NodeWriterImpl(Writer writer, Log LOG) throws IOException {
        this.LOG = LOG;
        this.writer = writer;
        startGraph();
    }

    /**
     * Insert the standard start of a DOT graph
     * @throws IOException in case of error
     */
    private void startGraph() throws IOException {
        writer.write(" digraph G {" + lineSeparator);
    }

    /**
     * End the graph correctly
     * @throws IOException in case of error;
     */
    private void endGraph() throws IOException {
        writer.write("}");
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        LOG.debug("Closing file");
        endGraph();
        LOG.info("Exported " + visitedNodes.size() + " nodes");
        LOG.info("Exported " + visitedRelations.size() + " edges");
        writer.flush();
        writer.close();
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(VersionNodeDecorator node) throws IOException {
        if (visitedNodes.add(node)) {
            LOG.debug("Writing VersionNode " + node);
            String version = node.getVersion();
            long nodeId = node.getId();
            writeNode(nodeId, version, NodeShape.component);
        }
    }


        /**
     * {@inheritDoc}
     */
    @Override
    public void writeRootNode(Node node) throws IOException {
        if (visitedNodes.add(node)) {
            LOG.debug("Writing RootNode");
            writeNode(node.getId(), "root", NodeShape.box);
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(ArtifactNodeDecorator node) throws IOException {
        if (visitedNodes.add(node)) {
            LOG.debug("Writing ArtifactNode " + node);
            String artifactId = node.getArtifactId();
            long nodeId = node.getId();
            writeNode(nodeId, artifactId, NodeShape.rect);
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(GroupNodeDecorator node) throws IOException {
        if (visitedNodes.add(node)) {
            LOG.debug("Writing GroupNode " + node);
            String groupId = node.getGroupId();
            long nodeId = node.getId();
            writeNode(nodeId, groupId, NodeShape.folder);
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void writeRelation(Relationship relationship) throws IOException {
        if (visitedRelations.add(relationship)) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            LOG.debug("Writing Relation " + startNode.getId() + "-> " + endNode.getId() + " (" + relationship + ")");
            writer.write("\tN" + startNode.getId() + " -> " + "N" + endNode.getId() + " [label=\"" + relationship.getType() + "\"]" + lineSeparator);
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(GroupNodeDecorator currentNode, GroupNodeDecorator previous) throws IOException {
        if (visitedNodes.add(currentNode)) {
            LOG.debug("Writing GroupNode " + previous);
            writeNode(currentNode.getId(), getAddedGroupIdPart(currentNode.getGroupId(), previous.getGroupId()), NodeShape.folder);
        }
    }

        /**
     * {@inheritDoc}
     */
    @Override
    public void writeReferenceRelation(Node refNode, Node childNode) throws IOException {
        writer.write("\tN" + refNode.getId() + " -> " + "N" + childNode.getId() + " [label=\"" + ArtifactRelations.has + "\"]" + lineSeparator);
    }

    /**
     * Determine the part of the groupId that is added.
     * current.length() > previous.length()
     * @param current the current value (full path)
     * @param previous the previous part
     * @return the difference.
     */
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

    /**
     * Actual write of the Node to the writer
     * @param id the Id
     * @param labelText the label text
     * @param shape the shape of the node
     * @throws IOException in case of error
     */
    private void writeNode(long id, String labelText, NodeShape shape) throws IOException {
        writer.write("\tN" + id + " [label=\"" + labelText + "\"" + " shape=" + shape + "]" + lineSeparator);
    }
}
