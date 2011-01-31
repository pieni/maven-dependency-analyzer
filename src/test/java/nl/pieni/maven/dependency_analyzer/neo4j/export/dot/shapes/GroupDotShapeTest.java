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
import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test classes for group shapes
 */
public class GroupDotShapeTest {

    private GroupDotShape shape;
    public static final String GROUP_NAME = "nl.pieni.maven.dependency_analyzer";

    @Before
    public void before() {
        Node startNode= mock(Node.class);
        when(startNode.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
        when(startNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode);
        when(startNode.getId()).thenReturn(1L);
        when(startNode.getProperty(NodeProperties.GROUP_ID)).thenReturn(GROUP_NAME);

        Node endNode= mock(Node.class);
        when(endNode.getId()).thenReturn(2L);
        when(endNode.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
        when(endNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.ArtifactNode);

        Relationship relation= mock(Relationship.class);
        when(relation.getStartNode()).thenReturn(startNode);


        when(relation.getType()).thenReturn(ArtifactRelations.has);
        when(relation.getEndNode()).thenReturn(endNode);

        Set<Relationship> relationSet = new HashSet<Relationship>();
        relationSet.add(relation);
        shape = new GroupDotShape(startNode, relationSet);
    }

    @Test
    public void testGetId() throws Exception {
        assertEquals("G1", shape.getId());
    }

    @Test
    public void testGetShape() throws Exception {
        assertEquals(NodeShape.folder, shape.getShape());
    }

    @Test
    public void testGetLabel() throws Exception {
        assertEquals(GROUP_NAME, shape.getLabel());
    }

    @Test
    public void testToString() throws Exception {
        assertThat(shape.toString(), containsString("G1"));
        assertThat(shape.toString(), containsString(GROUP_NAME));
        assertThat(shape.toString(), containsString(NodeShape.folder.toString()));
        assertEquals("G1 [ label=\"" + GROUP_NAME + "\" shape=\"" + NodeShape.folder+ "\" ]", shape.toString());
    }

    @Test
    public void testGetEdges() {
        Set<DotEdge> dotEdges = shape.getEdges();
        for (DotEdge dotEdge : dotEdges) {
            assertEquals(EdgeStyle.solid, dotEdge.getEdgeStyle());
            assertEquals("A2", dotEdge.getEndId());
            assertEquals("G1", dotEdge.getStartId());
            assertEquals(ArtifactRelations.has.toString(), dotEdge.getLabel());
            assertThat(dotEdge.toString(), containsString("G1 -> A2"));
            assertThat(dotEdge.toString(), containsString("label=\"" + ArtifactRelations.has + "\""));
            assertThat(dotEdge.toString(), containsString("style=\"" + EdgeStyle.solid + "\""));
        }
    }

}
