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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Export of the graph (partial) to a file in the dot language. see http://graphviz.org
 */
public class DotExporterImpl implements DotExporter {

    private final DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase;
    private final boolean includeVersions;
    private final Log LOG;
    private NodeWriter nodeWriter;
    private Node lastGroupNode = null;
    private Set<Node> refNodeRelations = new HashSet<Node>();
    private IncludeFilterPatternMatcher includeFilterPatternMatcher;

    public DotExporterImpl(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, boolean includeVersions, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
        this.includeVersions = includeVersions;
    }

    @Override
    public void export(List<String> includeFilterPatterns, NodeWriter nodeWriter) throws IOException {
        this.includeFilterPatternMatcher = new IncludeFilterPatternMatcher(includeFilterPatterns);
        this.nodeWriter = nodeWriter;
        Node refNode = dependencyDatabase.getDatabase().getReferenceNode();
        Iterable<Relationship> iter = refNode.getRelationships(Direction.OUTGOING);
        nodeWriter.writeRootNode(refNode);
        for (Relationship relationship : iter) {
            lastGroupNode = refNode;
            processNode(relationship.getOtherNode(refNode));
        }

        processReferenceNodeRelations(refNode);

        nodeWriter.close();
    }

    private void processReferenceNodeRelations(Node refNode) throws IOException {
        for (Node refNodeRelation : refNodeRelations) {
            if (includeFilterPatternMatcher.include(refNodeRelation)) {
                nodeWriter.writeReferenceRelation(refNode, refNodeRelation);
            }
        }
    }

    private void processNode(Node startNode) throws IOException {
        LOG.debug("Parse: " + nodeToString(startNode));

        if (!startNode.hasRelationship(Direction.OUTGOING)) {
            exportNode(startNode);
            return;
        }
        for (Relationship relationship : startNode.getRelationships(Direction.OUTGOING)) {
            LOG.debug("Processing Relationship " + relationship);
            boolean exportedStart = exportNode(relationship.getStartNode());
            Node endNode = relationship.getEndNode();
            boolean exportedEnd = exportNode(endNode);
            if (exportedStart && exportedEnd) {
                nodeWriter.writeRelation(relationship);
            }
            if (endNode.hasRelationship(Direction.OUTGOING)) {
                processNode(endNode);
            }
        }
    }


    private boolean exportNode(Node node) throws IOException {
        LOG.debug("Processing node: " + nodeToString(node));
        if (!includeFilterPatternMatcher.include(node)) {
            return false;
        }
        NodeType nodeType = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
        switch (nodeType) {
            case ArtifactNode:
                return doArtifactNode(node);
            case GroupNode:
                return doGroupNode(node);
            case VersionNode:
                if (includeVersions) {
                    return doVersionNode(node);
                }
                return false;
            default:
                throw new IllegalArgumentException("NodeType: " + nodeType + " is not valid in this context");
        }
    }

    private boolean doGroupNode(Node node) throws IOException {

        if (hasOnlyGroupNodeRelations(node)) {
            return false;
        }

        GroupNodeDecorator groupNodeDecorator = new GroupNodeDecorator(node);

        if (lastGroupNode.hasProperty(NodeProperties.NODE_TYPE)) {
            nodeWriter.writeNode(groupNodeDecorator, new GroupNodeDecorator(lastGroupNode));
        } else {
            nodeWriter.writeNode(groupNodeDecorator);
        }

        if (!lastGroupNode.hasProperty(NodeProperties.NODE_TYPE)) {
            refNodeRelations.add(node);
            lastGroupNode = node;
        }

        return true;
    }

    private boolean doVersionNode(Node node) throws IOException {
        if (!includeVersions) {
            return false;
        }

        VersionNodeDecorator versionNode = new VersionNodeDecorator(node);
        nodeWriter.writeNode(versionNode);
        return true;
    }

    private boolean doArtifactNode(Node node) throws IOException {
        ArtifactNodeDecorator artifactNode = new ArtifactNodeDecorator(node);
        nodeWriter.writeNode(artifactNode);
        return true;
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
    String nodeToString(Node node) {
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
