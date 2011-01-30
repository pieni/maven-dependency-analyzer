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
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.optimize.OptimizedGroupDotShape;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 27-1-11
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */
public class OptimizedGroupDotShapeTest {
//    @Test
//    public void testGetStartNode() throws Exception {
//        Node endNode = mock(Node.class);
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        Node startNode = mock(Node.class);
//        shapeOptimized.setStartNode(startNode);
//        assertEquals(startNode, shapeOptimized.getStartNode());
//    }
//
//    @Test
//    public void testGetId() throws Exception {
//        Node endNode = mock(Node.class);
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        when(endNode.getId()).thenReturn(1L);
//        assertThat(shapeOptimized.getId(), containsString("GN1"));
//    }
//
//
//    @Test
//    public void testEndRelationships() throws Exception {
//        Node startNode = mock(Node.class);
//
//        Node endNode = mock(Node.class);
//        when(endNode.getId()).thenReturn(1L);
//
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        shapeOptimized.setStartNode(startNode);
//
//        Relationship relation1 = mock(Relationship.class);
//        Node relationEndNode = mock(Node.class);
//        when(relation1.getEndNode()).thenReturn(relationEndNode);
//        when(relationEndNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.ArtifactNode);
//        when(relationEndNode.getId()).thenReturn(2L);
//        when(relation1.getEndNode()).thenReturn(relationEndNode);
//        when(relation1.getType()).thenReturn(ArtifactRelations.has);
//        shapeOptimized.setEndRelationship(relation1);
//
//        Set<DotEdge> result = shapeOptimized.getEdges();
//        assertTrue(result.size() == 1);
//        DotEdge edge = result.iterator().next();
//        assertEquals(EdgeStyle.solid, edge.getEdgeStyle());
//        assertThat(edge.getEndId(), containsString("N2"));
//        assertThat(edge.getStartId(), containsString("GN1"));
//        assertEquals(ArtifactRelations.has.toString(), edge.getLabel());
//        assertThat(edge.toString(), containsString("GN1 -> N2 [label=has style=solid]"));
//    }
//
//
//    @Test(expected = IllegalStateException.class)
//    public void testEndRelationshipsIllegalArgument() throws Exception {
//        Node startNode = mock(Node.class);
//
//        Node endNode = mock(Node.class);
//        when(endNode.getId()).thenReturn(1L);
//
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        shapeOptimized.setStartNode(startNode);
//
//        Relationship relation1 = mock(Relationship.class);
//        Node relationEndNode = mock(Node.class);
//        when(relation1.getEndNode()).thenReturn(relationEndNode);
//        when(relationEndNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.VersionNode);
//        when(relationEndNode.getId()).thenReturn(2L);
//        when(relation1.getEndNode()).thenReturn(relationEndNode);
//        when(relation1.getType()).thenReturn(ArtifactRelations.has);
//        shapeOptimized.setEndRelationship(relation1);
//
//        shapeOptimized.getEdges();
//    }
//
//
//    @Test
//    public void testGetEdges() throws Exception {
//
//    }
//
//    @Test
//    public void testMakeLabelSinglePart() throws Exception {
//        Node startNode = mock(Node.class);
//
//        Node endNode = mock(Node.class);
//        when(endNode.getId()).thenReturn(1L);
//        when(endNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni.maven");
//
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        when(startNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni");
//        shapeOptimized.setStartNode(startNode);
//
//        String label = shapeOptimized.getLabel();
//        assertThat(label, containsString("maven"));
//    }
//
//    @Test
//    public void testToString() throws Exception {
//        Node startNode = mock(Node.class);
//
//        Node endNode = mock(Node.class);
//        when(endNode.getId()).thenReturn(1L);
//        when(endNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni.maven");
//
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        when(startNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni");
//        shapeOptimized.setStartNode(startNode);
//
//        String label = shapeOptimized.toString();
//        assertThat(label, containsString("GN1"));
//        assertThat(label, containsString("style=\"folder\""));
//        assertThat(label, containsString("label=\"maven\""));
//
//    }
//
//    @Test
//    public void testMakeLabelMultiPartPart() throws Exception {
//        Node startNode = mock(Node.class);
//
//        Node endNode = mock(Node.class);
//        when(endNode.getId()).thenReturn(1L);
//        when(endNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni.maven.dependency_analyzer");
//
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        when(startNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni");
//        shapeOptimized.setStartNode(startNode);
//
//        String label = shapeOptimized.getLabel();
//        assertThat(label, containsString("maven.dependency_analyzer"));
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    public void testMakeLabelException() throws Exception {
//        Node startNode = mock(Node.class);
//
//        Node endNode = mock(Node.class);
//        when(endNode.getId()).thenReturn(1L);
//        when(endNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni");
//
//        OptimizedGroupDotShape shapeOptimized = new OptimizedGroupDotShape(endNode);
//        when(startNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni.maven.dependency_analyzer");
//        shapeOptimized.setStartNode(startNode);
//
//        String label = shapeOptimized.getLabel();
//        assertThat(label, containsString("maven.dependency_analyzer"));
//    }
//
//
}
