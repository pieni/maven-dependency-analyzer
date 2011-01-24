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
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Export of the graph (partial) to a file in the dot language. see http://graphviz.org
 */
public class DotExporterImpl implements DotExporter {

    private boolean includeVersions = false;
    private IncludeFilterPatternMatcher includeFilterPatternMatcher = new IncludeFilterPatternMatcher(new ArrayList<String>() {
    });


    private final DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase;

    private final Log LOG;
    private NodeWriter nodeWriter;
    private Set<Node> exportNodes = new HashSet<Node>();
    private Map<Node, Set<Relationship>> exportNodesRelationships = new HashMap<Node, Set<Relationship>>();
    private Set<Relationship> referenceNodeRelationships = new HashSet<Relationship>();


    private final Set<Node> refNodeRelations = new HashSet<Node>();
    private final Set<Node> writtenGroupNodes = new HashSet<Node>();

    public DotExporterImpl(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
    }


    @Override
    public void setIncludePatters(List<String> includeFilterPatterns) {
        this.includeFilterPatternMatcher = new IncludeFilterPatternMatcher(includeFilterPatterns);
    }

    @Override
    public void setIncludeVersions(boolean includeVersions) {
        this.includeVersions = includeVersions;
    }

    @Override
    public void export(NodeWriter nodeWriter) throws IOException {
        this.nodeWriter = nodeWriter;

        //Select the Nodes
        selectNodes();

        //Select the relations
        selectRelationShips();

        //Select all references to the root node
        selectRefNodeRelationsShips();

        //Write the selected nodes and relations ships to file
        writeDotFile();

        nodeWriter.close();
    }

    private void selectRefNodeRelationsShips() {
        for (Node exportNode : exportNodes) {
            Iterable<Relationship> refRelationships = exportNode.getRelationships(Direction.INCOMING);
            for (Relationship refRelationship : refRelationships) {
                if (refRelationship.getStartNode().getId() == 0) {
                    referenceNodeRelationships.add(refRelationship);
                }
            }
        }
    }

    private void writeDotFile() throws IOException {

        nodeWriter.writeRootNode(dependencyDatabase.getDatabase().getReferenceNode());

        for (Node exportNode : exportNodes) {
            NodeType type = NodeType.fromString(exportNode.getProperty(NodeProperties.NODE_TYPE).toString());
            switch (type) {
                case ArtifactNode:
                    nodeWriter.writeNode(new ArtifactNodeDecorator(exportNode));
                    break;
                case GroupNode:
                    nodeWriter.writeNode(new GroupNodeDecorator(exportNode));
                    break;
                case VersionNode:
                    nodeWriter.writeNode(new VersionNodeDecorator(exportNode));
            }
        }

        for (Relationship referenceNodeRelationship : referenceNodeRelationships) {
            nodeWriter.writeNode2NodeRelation(referenceNodeRelationship.getStartNode(), referenceNodeRelationship.getEndNode(), ArtifactRelations.has);
        }

        Set<Node> nodes = exportNodesRelationships.keySet();
        for (Node startNode: nodes) {
            Set<Relationship> relationshipSet = exportNodesRelationships.get(startNode);
            for (Relationship relationship : relationshipSet) {
                nodeWriter.writeNode2NodeRelation(startNode, relationship.getEndNode(), relationship.getType());
            }
        }
    }

    /**
     * Parse through all nodes of the graph and select the nodes that must be written
     */
    private void selectNodes() {
        //
        Node refNode = dependencyDatabase.getDatabase().getReferenceNode();
        Iterable<Relationship> iter = refNode.getRelationships(Direction.OUTGOING);
        for (Relationship relationship : iter) {
            parseNode(relationship.getOtherNode(refNode));
        }
    }

    /**
     * Select all valid relations from the nodes in the exportNodes set
     */
    private void selectRelationShips() {
        for (Node exportedNode : exportNodes) {
            Iterable<Relationship> relations = exportedNode.getRelationships(Direction.OUTGOING);
            for (Relationship relation : relations) {
                if (exportNodes.contains(relation.getEndNode())) {
                    Set<Relationship> relationshipSet = exportNodesRelationships.get(exportedNode);
                    if (relationshipSet == null) {
                        relationshipSet = new HashSet<Relationship>();
                        exportNodesRelationships.put(exportedNode, relationshipSet);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selected relation: " + relation2String(relation) +  " for export");
                    }
                    relationshipSet.add(relation);
                }
            }
        }
    }

    private void parseNode(Node startNode) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Parse: " + nodeToString(startNode));
        }

        for (Relationship relationship : startNode.getRelationships(Direction.OUTGOING)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing Relationship " + relationship);
            }
            exportNode(relationship.getStartNode());
            Node endNode = relationship.getEndNode();
            exportNode(endNode);
            if (endNode.hasRelationship(Direction.OUTGOING)) {
                parseNode(endNode);
            }
        }
    }


    private void exportNode(Node node) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing node: " + nodeToString(node));
        }
        if (!includeFilterPatternMatcher.include(node)) {
            return;
        }
        NodeType nodeType = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
        if (nodeType == NodeType.VersionNode && !includeVersions) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node: " + nodeToString(node) + " selected for output");
        }
        exportNodes.add(node);
    }


//    private boolean writeGroupNode(Node groupNode) throws IOException {
//
//        Node writtenParent = findWrittenParent(groupNode);
//
//        boolean result = true;
//        if (null != writtenParent && writtenParent.hasProperty(NodeProperties.NODE_TYPE)) {
//            nodeWriter.writeNode(new GroupNodeDecorator(groupNode), new GroupNodeDecorator(writtenParent));
//            result = false;
//        } else {
//            nodeWriter.writeNode(new GroupNodeDecorator(groupNode));
//            refNodeRelations.add(groupNode);
//        }
//
//        //lastGroupNode = writtenParent;
//        writtenGroupNodes.add(groupNode);
//        return result;
//    }

//    private Node findWrittenParent(Node node) {
//        if (node == null || !node.hasProperty(NodeProperties.NODE_TYPE)) {
//            return null;
//        }
//
//        if (writtenGroupNodes.contains(node)) {
//            return node;
//        }
//
//        Iterable<Relationship> parentRelationships = node.getRelationships(ArtifactRelations.has, Direction.INCOMING);
//        for (Relationship parentRelationship : parentRelationships) {
//            Node parentNode = parentRelationship.getOtherNode(node);
//            return findWrittenParent(findWrittenParent(parentNode));
//        }
//
//        throw new IllegalArgumentException("GroupNode " + nodeToString(node) + " has no parent");
//    }

//    private boolean hasArtifactRelations(Node node) {
//        Iterable<Relationship> iterable = node.getRelationships(ArtifactRelations.has, Direction.OUTGOING);
//        for (Relationship relationship : iterable) {
//            Node endNode = relationship.getEndNode();
//            NodeType type = NodeType.fromString(endNode.getProperty(NodeProperties.NODE_TYPE).toString());
//            if (type == NodeType.ArtifactNode) {
//                return true;
//            }
//        }
//        return false;
//    }


//    /**
//     * Check to see if writes are required in the sub tree
//     *
//     * @param node
//     * @return
//     */
//    private boolean hasWritesInMultiplePaths(Node node) {
//        LOG.debug("hasWritesInMultiplePaths: " + nodeToString(node));
//
//        int count = 0;
//        //Get all the group relations
//        Iterable<Relationship> iterable = node.getRelationships(ArtifactRelations.has, Direction.OUTGOING);
//        for (Relationship relationship : iterable) {
//            Node otherNode = relationship.getOtherNode(node);
//            if (hasWriteInPath(otherNode)) {
//                count++;
//            }
//        }
//
//        LOG.debug("hasWritesInMultiplePaths count = " + count);
//        return count >= 2;
//    }

//    /**
//     * Create a string representation of the node
//     *
//     * @param node the Node
//     * @return a String
//     */
//
//    private boolean hasWriteInPath(Node node) {
//        Iterable<Relationship> iterable = node.getRelationships(Direction.OUTGOING);
//        for (Relationship relationship : iterable) {
//            Node endNode = relationship.getEndNode();
//            NodeType type = NodeType.fromString(endNode.getProperty(NodeProperties.NODE_TYPE).toString());
//            if (type == NodeType.ArtifactNode && includeFilterPatternMatcher.include(endNode)) {
//                LOG.debug("hasWritesInPath result = " + true);
//                return true;
//            }
//
//            if (type == NodeType.GroupNode && includeFilterPatternMatcher.include(endNode)) {
//                return hasWriteInPath(endNode);
//            }
//        }
//        return false;
//    }

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

    private String relation2String(Relationship relation) {
        return "Relationship { Id = " + relation.getId() + " type = " + relation.getType() + "}";
    }
}
