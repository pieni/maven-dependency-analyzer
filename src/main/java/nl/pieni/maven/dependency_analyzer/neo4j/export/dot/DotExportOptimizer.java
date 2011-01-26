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

import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotEdge;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes.DotShape;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
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

    public DotExportOptimizer(Map<Node, Set<Relationship>> exportNodeMap) {
        this.exportNodeMap = exportNodeMap;
    }

    private Map<DotShape, Set<DotEdge>> optimize(Map<Node, Set<Relationship>> nodeSetMap) {
        throw new NotImplementedException();
    }

    private boolean hasSingleGroupRelation(Node node) {
        Set<Relationship> relationshipSet = exportNodeMap.get(node);
        if (relationshipSet.size() > 1) {
            return false;
        }
        return true;
    }

    private boolean hasNoArtifactRelations(Node node) {
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
