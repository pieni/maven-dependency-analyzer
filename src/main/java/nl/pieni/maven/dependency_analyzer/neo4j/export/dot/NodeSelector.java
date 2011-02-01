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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.util.NodeUtils;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Select nodes from the graph stored om the database.
 * Utilizes the {@link IncludeFilterPatternMatcher} to select the appropriate nodes
 */
class NodeSelector {
    private final Log LOG;
    private final DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase;
    private boolean includeVersions;
    private IncludeFilterPatternMatcher includeFilterPatternMatcher;
    private final Map<Node, Set<Relationship>> selectedNodeMap = new HashMap<Node, Set<Relationship>>();

    /**
     * Default constructor
     *
     * @param dependencyDatabase the Database
     * @param log
     */
    public NodeSelector(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
        setIncludeVersions(false);
        setIncludeFilterPatterns(new ArrayList<String>() {
        });
    }

    /**
     * Set the include filter patters
     * Default value is empty (nothing matches)
     *
     * @param includeFilterPatterns the pattern
     */
    public void setIncludeFilterPatterns(List<String> includeFilterPatterns) {
        this.includeFilterPatternMatcher = new IncludeFilterPatternMatcher(includeFilterPatterns);
    }

    /**
     * Boolean the select versions
     * Default value: false
     *
     * @param includeVersions false does not include versions.
     */
    public void setIncludeVersions(boolean includeVersions) {
        this.includeVersions = includeVersions;
    }

    /**
     * Select all Nodes and relations with regards to the patterns specified.
     * The Set contains all relavant relations see: {@link #setIncludeFilterPatterns(java.util.List)} and {@link #setIncludeVersions(boolean)}
     *
     * @return the map of nodes and relations
     */
    public Map<Node, Set<Relationship>> selectNodesAndRelations() {

        Set<Node> nodeSet = selectNodes();

        selectRelationShips(nodeSet);

        selectRefNodeRelationsShips(nodeSet);

        return selectedNodeMap;
    }

    /**
     * Parse through all nodes of the graph and select the nodes that must be written
     *
     * @return all selected nodes
     */
    private Set<Node> selectNodes() {
        Set<Node> nodeSet = new HashSet<Node>();

        Node refNode = dependencyDatabase.getDatabase().getReferenceNode();

        nodeSet.add(refNode);

        Iterable<Relationship> iter = refNode.getRelationships(Direction.OUTGOING);
        for (Relationship relationship : iter) {
            Set<Node> result = parseNode(relationship.getOtherNode(refNode));
            nodeSet.addAll(result);
        }

        return nodeSet;
    }

    /**
     * Select all valid relations from the nodes in the exportNodes_Refactor set
     *
     * @param selectedNodeSet the selected nodes.
     */
    private void selectRelationShips(Set<Node> selectedNodeSet) {


        for (Node exportedNode : selectedNodeSet) {
            Set<Relationship> relationshipSet = new HashSet<Relationship>();
            if (exportedNode.hasRelationship(Direction.OUTGOING)) {
                Iterable<Relationship> relations = exportedNode.getRelationships(Direction.OUTGOING);
                for (Relationship relation : relations) {
                    if (selectedNodeSet.contains(relation.getEndNode())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Selected relation: " + NodeUtils.relation2String(relation) + " for export");
                        }
                        relationshipSet.add(relation);
                    }
                }
            }

            selectedNodeMap.put(exportedNode, relationshipSet);
        }
    }

    /**
     * Select all sub nodes (including startNode)
     * This method is called recursively for the sub nodes (i.e. the end relations)
     *
     * @param startNode start point
     * @return set of nodes selected
     */
    private Set<Node> parseNode(Node startNode) {
        Set<Node> selectNodeSet = new HashSet<Node>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parse: " + NodeUtils.nodeToString(startNode));
        }

        for (Relationship relationship : startNode.getRelationships(Direction.OUTGOING)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Processing Relationship " + relationship);
            }
            for (Node node : relationship.getNodes()) {
                if (exportNode(node)) {
                    selectNodeSet.add(node);
                }
            }
            if (relationship.getEndNode().hasRelationship(Direction.OUTGOING)) {
                selectNodeSet.addAll(parseNode(relationship.getEndNode()));
            }
        }

        return selectNodeSet;
    }

    /**
     * Select all references that are coming from the root node.
     *
     * @param nodeSet the set of Node to process
     */
    private void selectRefNodeRelationsShips(Set<Node> nodeSet) {
        for (Node exportNode : nodeSet) {
            Iterable<Relationship> refRelationships = exportNode.getRelationships(Direction.INCOMING);
            for (Relationship refRelationship : refRelationships) {
                if (refRelationship.getStartNode().getId() == 0) {
                    processNodeAndRelation(refRelationship.getStartNode(), refRelationship);
                }
            }
        }
    }


    /**
     * Select and add the relations to the entry in the map that is identified by exportNode.
     *
     * @param exportedNode the relations from this node
     * @param relation     the relation to verify
     */
    private Set<Relationship> processNodeAndRelation(Node exportedNode, Relationship relation) {
        Set<Relationship> relationshipSet = new HashSet<Relationship>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Selected relation: " + NodeUtils.relation2String(relation) + " for export");
        }
        relationshipSet.add(relation);
        return relationshipSet;
    }

    /**
     * Determine if this node must be included
     *
     * @param node the node
     * @return true when included
     */
    private boolean exportNode(Node node) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing node: " + NodeUtils.nodeToString(node));
        }
        if (!includeFilterPatternMatcher.include(node)) {
            return false;
        }
        NodeType nodeType = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
        if (nodeType == NodeType.VersionNode && !includeVersions) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node: " + NodeUtils.nodeToString(node) + " selected for output");
        }
        return true;
    }
}
