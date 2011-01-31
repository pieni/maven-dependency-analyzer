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
import nl.pieni.maven.dependency_analyzer.neo4j.util.NodeUtils;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * Edges of the Dot Graph
 */
public class DotEdge {
    private final String endId;
    private final String startId;
    private final EdgeStyle edgeStyle;
    private final String label;


    /**
     * Default constructor
     * @param relationship the relation
     */
    public DotEdge(Relationship relationship) {
        Node startNode = relationship.getStartNode();
        this.startId = ShapeIdPrefix.fromNode(startNode) + startNode.getId();
        Node endNode = relationship.getEndNode();
        this.endId = ShapeIdPrefix.fromNode(endNode) + endNode.getId();

        if (NodeUtils.isScoperelation(relationship.getType())) {
            this.edgeStyle = EdgeStyle.dotted;
        } else {
            this.edgeStyle = EdgeStyle.solid;
        }

        this.label = relationship.getType().toString();
    }

    /**
     * Starting ID of the edge
     * @return the Id
     */
    public String getStartId() {
        return this.startId;
    }

    /**
     * End ID of the edge
     * @return the Id
     */
    public String getEndId() {
        return this.endId;
    }

    /**
     * The style (line) of the edge see {@link EdgeStyle}
     * @return the Style
     */
    public EdgeStyle getEdgeStyle() {
        return this.edgeStyle;
    }

    /**
     * The label for the edge
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Dot representation of the edge
     * @return
     */
    public String toString() {
        return this.getStartId() + " -> " + this.getEndId() + " [label=\"" + this.getLabel() + "\" style=\"" + this.getEdgeStyle() + "\" ]";
    }

}
