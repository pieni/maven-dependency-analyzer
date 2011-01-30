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
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.ArtifactDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.GroupDotShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.RootShape;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.VersionDotShape;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 29-1-11
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
public class Neo4J2Dot {

    private Map<Node, Set<Relationship>> exportNodeMap;
    Set<DotShape> shapeSet = new HashSet<DotShape>();
    Set<DotEdge> edgeSet = new HashSet<DotEdge>();

    public Neo4J2Dot(Map<Node, Set<Relationship>> exportNodeMap) {
        this.exportNodeMap = exportNodeMap;
        convertNodes();
        convertRelations();
    }

    public Set<DotShape> getShapes() {
        return shapeSet;
    }

    public Set<DotEdge> getEdges() {
        return edgeSet;
    }

    private void convertNodes() {
        for (Node node : exportNodeMap.keySet()) {
            DotShape shape;
            Set<Relationship> relations = exportNodeMap.get(node);
            if (node.hasProperty(NodeProperties.NODE_TYPE)) {
                NodeType type = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE));
                switch (type) {
                    case ArtifactNode:
                        shapeSet.add(new ArtifactDotShape(node, relations));
                        break;
                    case VersionNode:
                        shapeSet.add(new VersionDotShape(node, relations));
                        break;
                    case GroupNode:
                        shapeSet.add(new GroupDotShape(node, relations));
                        break;
                }
            } else {
                shapeSet.add(new RootShape(node, relations));
            }
        }
    }

    public void convertRelations() {
        for (DotShape shape : shapeSet) {
            edgeSet.addAll(shape.getEdges());
        }
    }
}
