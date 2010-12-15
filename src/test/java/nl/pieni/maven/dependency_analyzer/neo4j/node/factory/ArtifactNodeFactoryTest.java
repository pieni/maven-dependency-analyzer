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
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

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
public class ArtifactNodeFactoryTest {

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
        @SuppressWarnings("unchecked")
        DependencyDatabase<GraphDatabaseService, Node> database = mock(DependencyDatabase.class);
        Log log = mock(Log.class);
        Node node = createMockNode();
        when(database.createNode()).thenReturn(node);

        ArtifactNodeFactory factory = new ArtifactNodeFactory(database, log);
        factory.create(dependency);

        verify(node).setProperty(NodeProperties.NODE_TYPE, NodeType.ArtifactNode);
        verify(database).startTransaction();
        verify(database).stopTransaction();
        verify(log).info(startsWith("Create ArtifactNode: Node{ Id = 1"));
    }

    @Test
    public void insertExistingTest() {
        Dependency dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");
        @SuppressWarnings("unchecked")
        DependencyDatabase<GraphDatabaseService, Node> database = mock(DependencyDatabase.class);
        GroupNodeDecorator groupNode = mock(GroupNodeDecorator.class);
        when(database.findGroupNode(dependency)).thenReturn(groupNode);

        ArtifactNodeDecorator artifactNode = mock(ArtifactNodeDecorator.class);
        when(database.findArtifactNode(dependency)).thenReturn(artifactNode);

        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
        ArtifactNodeFactory factory = new ArtifactNodeFactory(database, log);
        assertEquals(0, factory.insert(dependency));
    }

    @Test
    public void insertNewTest() {
        Dependency dependency = mock(Dependency.class);
        when(dependency.getVersion()).thenReturn("1.0");
        @SuppressWarnings("unchecked")
        DependencyDatabase<GraphDatabaseService, Node> database = mock(DependencyDatabase.class);
        GroupNodeDecorator groupNode = mock(GroupNodeDecorator.class);
        when(database.findGroupNode(dependency)).thenReturn(groupNode);

        when(database.findArtifactNode(dependency)).thenReturn(null);

        Log log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
        Node artifactNode = createMockNode();
        when(database.createNode()).thenReturn(artifactNode);

        ArtifactNodeFactory factory = new ArtifactNodeFactory(database, log);
        assertEquals(1, factory.insert(dependency));

        verify(artifactNode).setProperty(NodeProperties.NODE_TYPE, NodeType.ArtifactNode);
        verify(database).indexOnProperty(artifactNode, NodeProperties.ARTIFACT_ID);
        verify(database, times(2)).startTransaction();
        verify(database, times(2)).stopTransaction();
        verify(log).info(startsWith("Create ArtifactNode: "));
    }
}
