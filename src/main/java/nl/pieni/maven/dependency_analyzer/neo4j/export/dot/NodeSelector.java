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
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 25-1-11
 * Time: 21:27
 * To change this template use File | Settings | File Templates.
 */
public class NodeSelector {
    private final Log LOG;
    private final DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase;
    private boolean includeVersions;
    private IncludeFilterPatternMatcher includeFilterPatternMatcher;
    private Map<Node, Set<Relationship>> selectedNodeMap = new HashMap<Node, Set<Relationship>>();

    public NodeSelector(DependencyDatabase<GraphDatabaseService, Node> dependencyDatabase, Log log) {
        this.dependencyDatabase = dependencyDatabase;
        this.LOG = log;
        setIncludeVersions(false);
        setIncludeFilterPatterns(new ArrayList<String>() {});
    }

    public void setIncludeFilterPatterns(List<String> includeFilterPatterns) {
        this.includeFilterPatternMatcher = new IncludeFilterPatternMatcher(includeFilterPatterns);
    }

    public void setIncludeVersions(boolean includeVersions) {
        this.includeVersions = includeVersions;
    }

    public Map<Node, Set<Relationship>> selectNodesAndRelations() {

        Set<Node> nodeSet = selectNodes();

        selectRelationShips(nodeSet);

        selectRefNodeRelationsShips(nodeSet);

        return selectedNodeMap;
    }

    /**
     * Parse through all nodes of the graph and select the nodes that must be written
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

    private Set<Node> parseNode(Node startNode) {
        Set<Node> selectNodeSet = new HashSet<Node>();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parse: " + nodeToString(startNode));
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
     * Select all valid relations from the nodes in the exportNodes_Refactor set
     */
    private void selectRelationShips(Set<Node> selectedNodeSet) {
        for (Node exportedNode : selectedNodeSet) {
            if (exportedNode.hasRelationship(Direction.OUTGOING)) {
                Iterable<Relationship> relations = exportedNode.getRelationships(Direction.OUTGOING);
                for (Relationship relation : relations) {
                    if (selectedNodeSet.contains(relation.getEndNode())) {
                        processNodeAndRelation(exportedNode, relation);
                    }
                }
            } else {
                //Its a VersionNode
                Set<Relationship> relationshipSet = new HashSet<Relationship>();
                selectedNodeMap.put(exportedNode, relationshipSet);
            }
        }
    }

    private void processNodeAndRelation(Node exportedNode, Relationship relation) {
        Set<Relationship> relationshipSet = selectedNodeMap.get(exportedNode);
        if (relationshipSet == null) {
            relationshipSet = new HashSet<Relationship>();
            selectedNodeMap.put(exportedNode, relationshipSet);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Selected relation: " + relation2String(relation) + " for exportRaw");
        }
        relationshipSet.add(relation);
    }

    private boolean exportNode(Node node) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Processing node: " + nodeToString(node));
        }
        if (!includeFilterPatternMatcher.include(node)) {
            return false;
        }
        NodeType nodeType = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE).toString());
        if (nodeType == NodeType.VersionNode && !includeVersions) {
            return false;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Node: " + nodeToString(node) + " selected for output");
        }
        return true;
    }



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
