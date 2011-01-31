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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.convert;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.AbstractDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.ArtifactDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.GroupDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.RootShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.VersionDotShape;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Conversion of a Neo4J format to a Dot format
 */
public class Neo4J2Dot {

    private Map<Node, Set<Relationship>> exportNodeMap;
    Set<AbstractDotShape> shapeSetAbstract = new HashSet<AbstractDotShape>();
    Set<DotEdge> edgeSet = new HashSet<DotEdge>();

    /**
     * Default constructor
     * Converst the Nodes to DotShapes and the Set of relations to Edges
     * @param exportNodeMap
     */
    public Neo4J2Dot(Map<Node, Set<Relationship>> exportNodeMap) {
        this.exportNodeMap = exportNodeMap;
        convertNodes();
        convertRelations();
    }

    /**
     * Return a Set of shapes
     * @return the shapes
     */
    public Set<AbstractDotShape> getShapes() {
        return shapeSetAbstract;
    }

    /**
     * Return a  set of edges
     * @return the edges
     */
    public Set<DotEdge> getEdges() {
        return edgeSet;
    }

    /**
     * The actual conversion of the nodes
     */
    private void convertNodes() {
        for (Node node : exportNodeMap.keySet()) {
            Set<Relationship> relations = exportNodeMap.get(node);
            if (node.hasProperty(NodeProperties.NODE_TYPE)) {
                NodeType type = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE));
                switch (type) {
                    case ArtifactNode:
                        shapeSetAbstract.add(new ArtifactDotShape(node, relations));
                        break;
                    case VersionNode:
                        shapeSetAbstract.add(new VersionDotShape(node, relations));
                        break;
                    case GroupNode:
                        shapeSetAbstract.add(new GroupDotShape(node, relations));
                        break;
                }
            } else {
                shapeSetAbstract.add(new RootShape(node, relations));
            }
        }
    }

    /**
     * Conversion of the relations
     */
    private void convertRelations() {
        for (AbstractDotShape shapeAbstract : shapeSetAbstract) {
            edgeSet.addAll(shapeAbstract.getEdges());
        }
    }
}
