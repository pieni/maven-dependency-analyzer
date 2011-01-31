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
import nl.pieni.maven.dependency_analyzer.neo4j.node.AbstractNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * test classes for the pattern matcher
 */
public class IncludeFilterPatternMatcherTest {

    @Test
    public void includeGroupMatchFull() {
        Node node = mock(Node.class);
        when(node.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
        when(node.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode.toString());
        when(node.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni.maven");
        List<String> pattern = new ArrayList<String>();
        pattern.add("nl.pieni.maven");
        IncludeFilterPatternMatcher matcher = new IncludeFilterPatternMatcher(pattern);
        assertTrue(matcher.include(node));
    }

    @Test
    public void includeGroupStartMatch() {
        Node startNode = mock(Node.class);
        when(startNode.hasProperty(NodeProperties.NODE_TYPE)).thenReturn(true);
        when(startNode.getProperty(NodeProperties.NODE_TYPE)).thenReturn(NodeType.GroupNode.toString());
        when(startNode.getProperty(NodeProperties.GROUP_ID)).thenReturn("nl.pieni.maven.some");
        Iterable<Relationship> relationshipIterable = mock(Iterable.class);
        Iterator<Relationship> relationshipIterator = mock(Iterator.class);
        when(relationshipIterable.iterator()).thenReturn(relationshipIterator);
        when(relationshipIterator.hasNext()).thenReturn(true).thenReturn(false);
        List<String> pattern = new ArrayList<String>();
        pattern.add("nl.pieni.maven.*");
        IncludeFilterPatternMatcher matcher = new IncludeFilterPatternMatcher(pattern);
        assertTrue(matcher.include(startNode));
    }
}
