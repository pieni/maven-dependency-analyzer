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

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.neo4j.graphdb.Node;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 26-1-11
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class DotShape {
    private long id;
    private NodeShape nodeShape;
    private String label;

    protected DotShape(long id, NodeShape nodeShape, String label) {
        this.id = id;
        this.nodeShape = nodeShape;
        this.label = label;
    }

    public long getId() {
        return id;
    }

    public NodeShape getNodeShape() {
        return nodeShape;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "N" + id + " [style=" + nodeShape + "label=" + label + "]";
    }
}
