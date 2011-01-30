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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.optimize;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.EdgeStyle;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.ArtifactDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.VersionDotShape;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 26-1-11
 * Time: 22:59
 * To change this template use File | Settings | File Templates.
 */
public class DotExportOptimizer {

    private Map<Node, Set<Relationship>> exportNodeMap = new HashMap<Node, Set<Relationship>>();
    private Set<OptimizedGroupDotShape> optimizedGroupDotShapes = new HashSet<OptimizedGroupDotShape>();
    private Set<ArtifactDotShape> artifactDotShapes = new HashSet<ArtifactDotShape>();
    private Set<DotEdge> edges = new HashSet<DotEdge>();
    private Set<VersionDotShape> versionDotShapes = new HashSet<VersionDotShape>();


    public DotExportOptimizer() {
    }

    public Map<DotShape, Set<DotEdge>> optimize(final Map<Node, Set<Relationship>> nodeSetMap) {
        //Copy the original
        exportNodeMap.putAll(nodeSetMap);

        //Select all nodes that have a ArtifactNode or more then 1 group node as child
        Set<Node> endNodes = findGroupEnds();

        //Convert the end nodes to a Shape and remove the nodes from the input map
        convertGroupNodes(endNodes);

        //Find all parent groups
        mergeGroups();

        createGroupEdges();

        convertArtifactNodes();

        convertVersionNodes();

//        createArtifactEdges();
//
//        createVersionEdges();

        //TODO merge all edges
        //TODO merge all Shapes
        return null;
    }

    private void convertVersionNodes() {
        for (Node node : exportNodeMap.keySet()) {
            NodeType type = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE));
            if (type == NodeType.VersionNode) {
                //VersionDotShape shape = new VersionDotShape(node);
                //versionDotShapes.add(shape);
                exportNodeMap.remove(node);
            }
        }
    }

    private void convertArtifactNodes() {
        for (Node node : exportNodeMap.keySet()) {
            NodeType type = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE));
            if (type == NodeType.ArtifactNode) {
                //ArtifactDotShape shape = new ArtifactDotShape(node);
                //artifactDotShapes.add(shape);
                exportNodeMap.remove(node);
            }
        }
    }


    private void createGroupEdges() {
        for (OptimizedGroupDotShape optimizedGroupDotShape : optimizedGroupDotShapes) {
            if (optimizedGroupDotShape.getStartNode() == null) {
                edges.addAll(createRootEdges(optimizedGroupDotShape));
            }
            edges.addAll(getGroupShapeEdges(optimizedGroupDotShape));
        }
    }

    private Set<DotEdge> getGroupShapeEdges(OptimizedGroupDotShape shapeOptimized) {
        Set<DotEdge> edges = new HashSet<DotEdge>();
//        for (Relationship endRelationship : shapeOptimized.getEndRelationships()) {
//            Node relationEnd = endRelationship.getEndNode();
//            NodeType type = NodeType.fromString(relationEnd.getProperty(NodeProperties.NODE_TYPE).toString());
//            switch (type) {
//                case ArtifactNode:
//                    edges.add(new DotEdge(shapeOptimized.getId(), "N" + relationEnd.getId(), EdgeStyle.solid, endRelationship.getType()));
//                    break;
//                case GroupNode:
//                    OptimizedGroupDotShape endOptimizedGroup = findGroupShapeStartingWith(relationEnd);
//                    edges.add(new DotEdge(shapeOptimized.getId(), endOptimizedGroup.getId(), EdgeStyle.solid, endRelationship.getType()));
//                    break;
//                default:
//                    throw new IllegalStateException("A GroupShape cannot have a relation with a VersionNode" + relationEnd);
//            }
//        }

        return edges;
    }

    private Collection<? extends DotEdge> createRootEdges(OptimizedGroupDotShape rootShapeOptimized) {
        Set<DotEdge> result = new HashSet<DotEdge>();
//        Set<Relationship> relations = rootShapeOptimized.getEndRelationships();
//        for (Relationship relation : relations) {
//            Node endNode = relation.getEndNode();
//            OptimizedGroupDotShape endOptimizedGroupShape = findGroupShapeStartingWith(endNode);
//            DotEdge edge = new DotEdge(rootShapeOptimized.getId(), endOptimizedGroupShape.getId(), EdgeStyle.solid, ArtifactRelations.has);
//            result.add(edge);
//        }

        return result;
    }

    private OptimizedGroupDotShape findGroupShapeStartingWith(Node shape) {
        for (OptimizedGroupDotShape optimizedGroupDotShape : optimizedGroupDotShapes) {
            if (optimizedGroupDotShape.startsWith(shape)) {
                return optimizedGroupDotShape;
            }
        }
        throw new IllegalArgumentException("No starting node find for end of " + shape);
    }

    private void mergeGroups() {

//        for (OptimizedGroupDotShape optimizedGroupShape : optimizedGroupDotShapes) {
//            Node node = optimizedGroupShape.getEndNode();
//            Iterable<Relationship> inboundRelationships = node.getRelationships(ArtifactRelations.has, Direction.INCOMING);
//            int relationCount = 0;
//            for (Relationship inboundRelationship : inboundRelationships) {
//                if ((relationCount++) > 1) {
//                    throw new IllegalStateException("Database inconsistency. node:" + node + " has multiple inbound relations");
//                }
//                Node startNode = inboundRelationship.getStartNode();
//                if (startNode.getId() == 0L) {
//                    //this is the root Node, go to the next GroupShape
//                    continue;
//                }
//
//                optimizedGroupShape.setStartNode(startNode);
//                exportNodeMap.remove(startNode);
//            }
//        }
    }

    private void convertGroupNodes(final Set<Node> endNodes) {

//        for (Node groupNode : endNodes) {
//            OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(groupNode);
//            shapeOptimized.setEndRelationships(exportNodeMap.get(groupNode));
//            exportNodeMap.remove(groupNode);
//            optimizedGroupDotShapes.add(shapeOptimized);
//        }
    }

    private Set<Node> findGroupEnds() {
        Set<Node> endNodes = new HashSet<Node>();
        for (Node node : exportNodeMap.keySet()) {
            if (hasNoArtifactRelations(node) && hasSingleGroupRelation(node)) {
                endNodes.add(node);
            }
        }

        return endNodes;
    }

    private boolean hasSingleGroupRelation(final Node node) {
        Set<Relationship> relationshipSet = exportNodeMap.get(node);
        if (relationshipSet.size() > 1) {
            return false;
        }
        return true;
    }

    private boolean hasNoArtifactRelations(final Node node) {
        Iterable<Relationship> iterable = node.getRelationships(ArtifactRelations.has, Direction.OUTGOING);
        for (Relationship relationship : iterable) {
            Node endNode = relationship.getEndNode();
            NodeType type = NodeType.fromString(endNode.getProperty(NodeProperties.NODE_TYPE).toString());
            if (type == NodeType.ArtifactNode) {
                return false;
            }
        }
        return true;
    }
}
