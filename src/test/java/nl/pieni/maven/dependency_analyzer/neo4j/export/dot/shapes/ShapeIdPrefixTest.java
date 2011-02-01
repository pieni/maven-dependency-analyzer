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

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ShapeIdPrefix Test  (code coverage)
 */
public class ShapeIdPrefixTest {

    /**
     * Test from string conversions
     */
    @Test
    public void fromStringTest() {
        ShapeIdPrefix[] values = ShapeIdPrefix.values();
        for (ShapeIdPrefix type : values) {
            String value = ShapeIdPrefix.fromNode(getMockForType(type));
            assertEquals(value, type.toString());
        }
    }

    private Node getMockForType(ShapeIdPrefix prefix) {
        Node node = mock(Node.class);

        switch (prefix) {
            case Artifact:
                when(node.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
                when(node.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.ArtifactNode);
                break;
            case Group:
                when(node.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
                when(node.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode);
                break;
            case Version:
                when(node.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
                when(node.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.VersionNode);
                break;
            default:
                when(node.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(false);
        }
        return node;
    }




}
