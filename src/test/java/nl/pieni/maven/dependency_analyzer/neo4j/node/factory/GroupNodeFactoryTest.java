/*
 * Copyright (c) 2010 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.neo4j.node.factory;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.mockito.Matchers;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VersionNode decorator testing
 */
public class GroupNodeFactoryTest {

    Node createMockNode() {
        Node node = mock(Node.class);
        final Map<String, String> keyValueMap = new HashMap<String, String>() {
            {
            }
        };
        when(node.getPropertyKeys()).thenReturn(keyValueMap.keySet());
        for (String key : keyValueMap.keySet()) {
            when(node.getProperty(key)).thenReturn(keyValueMap.get(key));
        }
        when(node.getId()).thenReturn(1L);
        return node;
    }

    @Test
    public void createTest() {
        Dependency dependency = mock(Dependency.class);
        when(dependency.getGroupId()).thenReturn("nl.pieni");
        @SuppressWarnings("unchecked")
        DependencyDatabase<GraphDatabaseService, Node> database = mock(DependencyDatabase.class);
        @SuppressWarnings("unchecked")
        DependencyDatabaseSearcher<Node> searcher = mock(DependencyDatabaseSearcher.class);
        @SuppressWarnings("unchecked")
        GraphDatabaseService dbService = mock(GraphDatabaseService.class);
        when(database.getDatabase()).thenReturn(dbService);
        Node refNode = mock(Node.class);
        when(dbService.getReferenceNode()).thenReturn(refNode);


        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
        Node node = createMockNode();
        when(database.createNode()).thenReturn(node);

        GroupNodeFactory factory = new GroupNodeFactory(database, searcher, log);
        factory.create(dependency);
        verify(refNode).createRelationshipTo(Matchers.<Node>anyObject(), Matchers.<RelationshipType>anyObject());
        verify(node, times(2)).setProperty(NodeProperties.NODE_TYPE, NodeType.GroupNode.name());
        verify(database).startTransaction();
        verify(database).stopTransaction();
        verify(log, times(2)).debug(startsWith("Created GroupNode:"));
    }

    @Test
    public void insertNewTest() {
        Dependency dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");
        when(dependency.getGroupId()).thenReturn("nl.pieni.maven");
        @SuppressWarnings("unchecked")
        DependencyDatabase<GraphDatabaseService, Node> database = mock(DependencyDatabase.class);
        @SuppressWarnings("unchecked")
        DependencyDatabaseSearcher<Node> searcher = mock(DependencyDatabaseSearcher.class);
        when(searcher.findGroupNode(dependency)).thenReturn(null);

        Node groupNode = createMockNode();
        when(database.createNode()).thenReturn(groupNode);

        Node referenceNode = mock(Node.class);
        GraphDatabaseService graphDatabaseService = mock(GraphDatabaseService.class);
        when(database.getDatabase()).thenReturn(graphDatabaseService);
        when(graphDatabaseService.getReferenceNode()).thenReturn(referenceNode);

        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
        GroupNodeFactory factory = new GroupNodeFactory(database, searcher, log);
        assertEquals(3, factory.insert(dependency));
        verify(groupNode, times(3)).setProperty(NodeProperties.NODE_TYPE, NodeType.GroupNode.name());
        verify(searcher, times(3)).indexOnProperty(groupNode, NodeProperties.GROUP_ID);
        verify(database, times(1)).startTransaction();
        verify(database, times(1)).stopTransaction();
        verify(log, times(3)).debug(startsWith("Created GroupNode: "));
    }

    @Test
    public void insertExistingTest() {
        Dependency dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");
        @SuppressWarnings("unchecked")
        DependencyDatabase<GraphDatabaseService, Node> database = mock(DependencyDatabase.class);
        @SuppressWarnings("unchecked")
        DependencyDatabaseSearcher<Node> searcher = mock(DependencyDatabaseSearcher.class);
        GroupNodeDecorator groupNode = mock(GroupNodeDecorator.class);
        when(searcher.findGroupNode(dependency)).thenReturn(groupNode);

        Log log = mock(Log.class);
        GroupNodeFactory factory = new GroupNodeFactory(database, searcher, log);
        when(log.isDebugEnabled()).thenReturn(true);
        assertEquals(0, factory.insert(dependency));
    }
}
