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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes;

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.EdgeStyle;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 26-1-11
 * Time: 21:00
 * To change this template use File | Settings | File Templates.
 */
public class DotEdge {
    private final String endId;
    private final String startId;
    private final EdgeStyle edgeStyle;
    private final String label;


    public DotEdge(Relationship relationship) {
        Node startNode = relationship.getStartNode();
        this.startId = ShapeIdPrefix.fromNode(startNode) + startNode.getId();
        Node endNode = relationship.getEndNode();
        this.endId = ShapeIdPrefix.fromNode(endNode) + endNode.getId();

        if (isScoperelation(relationship.getType())) {
            this.edgeStyle = EdgeStyle.dotted;
        } else {
            this.edgeStyle = EdgeStyle.solid;
        }

        this.label = relationship.getType().toString();
    }

    public String getStartId() {
        return this.startId;
    }

    public String getEndId() {
        return this.endId;
    }

    public EdgeStyle getEdgeStyle() {
        return this.edgeStyle;
    }

    public String getLabel() {
        return this.label;
    }

    public String toString() {
        return this.getStartId() + " -> " + this.getEndId() + " [label=" + this.getLabel() + " style=" + this.getEdgeStyle() + "]";
    }

    private boolean isScoperelation(RelationshipType relationship) {
        if (relationship instanceof DependencyScopeRelations) {
            return true;
        }
        return false;
    }
}
