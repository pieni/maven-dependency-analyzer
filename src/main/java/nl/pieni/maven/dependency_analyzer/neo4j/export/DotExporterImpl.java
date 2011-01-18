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

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.dot.DotExporter;
import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Export of the graph (partial) to a file in the dot language. see http://graphviz.org
 */
public class DotExporterImpl implements DotExporter {

    private final DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase;
    private final boolean includeVersions;
    private final Log LOG;
    private NodeWriter nodeWriter;
    private Node lastGroupNode = null;
    private boolean showFullpath = false;

    public DotExporterImpl(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, boolean includeVersions, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
        this.includeVersions = includeVersions;
    }

    public DotExporterImpl(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, boolean includeVersions, boolean showPath, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
        this.includeVersions = includeVersions;
        this.showFullpath = showPath;
    }

    @Override
    public void export(List<String> includeFilterPatterns, NodeWriter nodeWriter) throws IOException {
        this.nodeWriter = nodeWriter;
        Node refNode = dependencyDatabase.getDatabase().getReferenceNode();
        Iterable<Relationship> iter = refNode.getRelationships(Direction.OUTGOING);
        nodeWriter.writeRootNode(refNode);
        for (Relationship relationship : iter) {
            lastGroupNode = relationship.getStartNode();
            processNode(relationship.getOtherNode(refNode));
        }

        nodeWriter.close();
    }

    private void processNode(Node startNode) throws IOException {
        boolean startProcessed = false;
        LOG.info("Parse: " + nodeToString(startNode));
        if (!startNode.hasRelationship(Direction.OUTGOING)) {
            exportNode(startNode, null);
            return;
        }
        for (Relationship relationship : startNode.getRelationships(Direction.OUTGOING)) {
            LOG.debug("Processing Relationship " + relationship);
            if (!startProcessed) {
                exportNode(startNode, relationship);
                startProcessed = true;
            }
            Node otherNode = relationship.getOtherNode(startNode);
            processNode(otherNode);
        }
    }

    private void exportNode(Node node, Relationship type) throws IOException {
        LOG.info("Processing node: " + nodeToString(node));
        NodeType nodeType = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
        switch (nodeType) {
            case ArtifactNode:
                doArtifactNode(node, type);
                break;
            case GroupNode:
                doGroupNode(node, type);
                break;
            case VersionNode:
                if (includeVersions) {
                    doVersionNode(node, type);
                }
                break;
        }
    }

    private void doGroupNode(Node node, Relationship type) throws IOException {

        if (hasOnlyGroupNodeRelations(node)) {
            return;
        }

        assert type == null;

        GroupNodeDecorator groupNodeDecorator = new GroupNodeDecorator(node);
        nodeWriter.writeRelation(lastGroupNode, groupNodeDecorator, type);

        if (!showFullpath && lastGroupNode.hasProperty(NodeProperties.NODE_TYPE)) {
            nodeWriter.writeNode(groupNodeDecorator, new GroupNodeDecorator(lastGroupNode));
        } else {
            nodeWriter.writeNode(groupNodeDecorator);
        }
        lastGroupNode = node;
    }

    private void doVersionNode(Node node, Relationship type) throws IOException {
        if (!includeVersions) {
            return;
        }

        VersionNodeDecorator versionNode = new VersionNodeDecorator(node);
        nodeWriter.writeNode(versionNode);
        //A version nodes parent is always has a "version" relation to its parent.
        Node parentNode = (Node) versionNode.getParent();
        nodeWriter.writeRelation(parentNode, versionNode, getRelationBetweenNodes(parentNode, versionNode));
        //Optional it can have a versionDependency to some other node
        if (type != null) {
            nodeWriter.writeRelation(type.getOtherNode(node), versionNode, type);
        }
    }

    private void doArtifactNode(Node node, Relationship type) throws IOException {
        ArtifactNodeDecorator artifactNode = new ArtifactNodeDecorator(node);
        nodeWriter.writeNode(artifactNode);
        //The parent of a artifact node is always a group node, therefor a "has" relation
        Node parentNode = (Node) artifactNode.getParent();
        nodeWriter.writeRelation(parentNode, artifactNode, getRelationBetweenNodes(parentNode, artifactNode));
    }

    private Relationship getRelationBetweenNodes(Node startNode, Node endNode) {
        for (Relationship relationship : startNode.getRelationships(Direction.OUTGOING)) {
            if (relationship.getOtherNode(startNode).equals(endNode)) {
                return relationship;
            }
        }
        throw new AssertionError("Node: " + startNode + " and Node: " + endNode + " do not share a relation with each other");

    }


    private boolean hasOnlyGroupNodeRelations(Node node) {
        Iterable<Relationship> iterable = node.getRelationships(ArtifactRelations.has, Direction.OUTGOING);
        for (Relationship relationship : iterable) {
            Node otherNode = relationship.getOtherNode(node);
            NodeType nodeType = NodeType.fromString(otherNode.getProperty(NodeProperties.NODE_TYPE).toString());
            if (nodeType == NodeType.ArtifactNode) {
                return false;
            }
        }
        LOG.debug(nodeToString(node) + " has only relations to GroupNodes");
        return true;
    }

    /**
     * Create a string represention of the node
     *
     * @return a String
     */
    public String nodeToString(Node node) {
        StringBuffer buff = new StringBuffer();
        buff.append("Node{ Id = ");
        buff.append(node.getId());
        for (String key : node.getPropertyKeys()) {
            buff.append(" key = ");
            buff.append(key);
            buff.append(" value = ");
            buff.append(node.getProperty(key));
        }
        buff.append("}");

        return buff.toString();
    }
}
