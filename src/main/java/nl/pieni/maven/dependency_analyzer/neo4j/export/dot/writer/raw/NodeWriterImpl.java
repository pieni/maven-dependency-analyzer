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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.raw;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.AbstractDotWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.util.NodeUtils;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Implementation of a DOT (@see http://graphviz.org) format Node file.
 */
public class NodeWriterImpl extends AbstractDotWriter implements NodeWriter {

    private final Set<Node> visitedNodes = new HashSet<Node>();
    private final Set<Relationship> visitedRelations = new HashSet<Relationship>();
    private final Map<Node, Set<Node>> nodeNodeRelations = new HashMap<Node, Set<Node>>();


    public NodeWriterImpl(Writer writer, Log LOG) {
        super(writer, LOG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        getLog().debug("Closing file");
        endGraph();
        getLog().info("Exported " + visitedNodes.size() + " nodes");
        getLog().info("Exported " + visitedRelations.size() + " edges");
        getLog().info("Exported " + nodeNodeRelations.size() + " edges (node2node");
        super.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(VersionNodeDecorator node) throws IOException {
        if (visitedNodes.add(node)) {
            getLog().debug("Writing VersionNode " + node);
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
            getLog().debug("Writing RootNode");
            writeNode(node.getId(), "root", NodeShape.box);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(ArtifactNodeDecorator node) throws IOException {
        if (visitedNodes.add(node)) {
            getLog().debug("Writing ArtifactNode " + node);
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
            getLog().debug("Writing GroupNode " + node);
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
            if (NodeUtils.isScoperelation(relationship.getType())) {
                getLog().debug("Writing Relation " + startNode.getId() + "-> " + endNode.getId() + " (" + relationship + ")");
                getWriter().write("\tN" + startNode.getId() + " -> " + "N" + endNode.getId() + " [label=\"" + relationship.getType() + "\" style=dotted]" + LINE_SEPARATOR);
            } else {
                getLog().debug("Writing Relation " + startNode.getId() + "-> " + endNode.getId() + " (" + relationship + ")");
                getWriter().write("\tN" + startNode.getId() + " -> " + "N" + endNode.getId() + " [label=\"" + relationship.getType() + "\"]" + LINE_SEPARATOR);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode(GroupNodeDecorator currentNode, GroupNodeDecorator previous) throws IOException {
        if (visitedNodes.add(currentNode)) {
            getLog().debug("Writing GroupNode " + previous);
            writeNode(currentNode.getId(), getAddedGroupIdPart(currentNode.getGroupId(), previous.getGroupId()), NodeShape.folder);
            writeNode2NodeRelation(previous, currentNode, ArtifactRelations.has);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeNode2NodeRelation(Node startNode, Node endNode, RelationshipType type) throws IOException {

        Set<Node> related = nodeNodeRelations.get(startNode);
        if (related == null) {
            related = new HashSet<Node>();
            nodeNodeRelations.put(startNode, related);
        }

        if (related.add(endNode)) {
            getLog().debug("Writing Node 2 Node Relation " + startNode + " -> " + endNode + " type = " + type);
            if (NodeUtils.isScoperelation(type)) {
                getWriter().write("\tN" + startNode.getId() + " -> " + "N" + endNode.getId() + " [label=\"" + type + "\" style=dotted]" + LINE_SEPARATOR);
            } else {
                getWriter().write("\tN" + startNode.getId() + " -> " + "N" + endNode.getId() + " [label=\"" + type + "\"]" + LINE_SEPARATOR);
            }
        }
    }

    /**
     * Determine the part of the groupId that is added.
     * current.length() > previous.length()
     *
     * @param current  the current value (full path)
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
     *
     * @param id        the Id
     * @param labelText the label text
     * @param shape     the shape of the node
     * @throws IOException in case of error
     */
    private void writeNode(long id, String labelText, NodeShape shape) throws IOException {
        getWriter().write("\tN" + id + " [label=\"" + labelText + "\"" + " shape=" + shape + "]" + LINE_SEPARATOR);
    }
}
