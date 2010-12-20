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

package nl.pieni.maven.dependency_analyzer.neo4j.database;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.NotInTransactionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * TODO Split is descructive and non descructive tests (speeeed).
 */
public class DependencyDatabaseSearcherImplTest extends AbstractDatabaseImplTest {


    private DependencyDatabase<GraphDatabaseService, Node> database;
    private DependencyDatabaseSearcher<Node> searcher;
    private Dependency dependencyA;
    private Dependency dependencyA2;
    private Dependency dependencyB;


    @Before
    public void beforeClass() throws IOException {
        beforeBase();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
        searcher = new DependencyDatabaseSearcherImpl(log, database);

        //Create the dependencies used
        dependencyA = new Dependency();
        dependencyA.setArtifactId("artifactId_A");
        dependencyA.setGroupId("groupId_A");
        dependencyA.setVersion("1.0");
        dependencyA.setType("jar");


        dependencyA2 = new Dependency();
        dependencyA2.setArtifactId("artifactId_A");
        dependencyA2.setGroupId("groupId_A");
        dependencyA2.setVersion("2.0");
        dependencyA2.setType("jar");


        dependencyB = new Dependency();
        dependencyB.setArtifactId("artifactId_B");
        dependencyB.setGroupId("groupId_B");
        dependencyB.setVersion("1.0");
        dependencyB.setType("jar");
    }

    @After
    public void afterClass() {
        try {
            database.shutdownDatabase();
            searcher.shutdownSearcher();
            afterBase();
        } finally {
            System.out.println("Done.");
        }
    }


    /**
     * Sort of BS Test cause i can't verify
     */
    @Test
    public void indexOnPropertyTest() {
        database.startTransaction();
        Node node = database.createNode();
        node.setProperty("Key", "Value");
        searcher.indexOnProperty(node, "Key");
        database.stopTransaction();
    }

    /**
     * Sort of BS Test cause i can't verify
     */
    @Test(expected = NotFoundException.class)
    public void indexOnPropertyInvalidKeyTest() {
        try {

            database.startTransaction();
            Node node = database.createNode();
            node.setProperty("Key", "Value");
            searcher.indexOnProperty(node, "key");
        } finally {
            database.stopTransaction();
        }
    }

    /**
     * Sort of BS Test cause i can't test the index here
     */
    @Test(expected = NotInTransactionException.class)
    public void indexOnPropertyNotInTransactionTest
    () {
        database.startTransaction();
        Node node = database.createNode();
        node.setProperty("Key", "Value");
        database.stopTransaction();
        searcher.indexOnProperty(node, "Key");
    }


    @Test(expected = NotFoundException.class)
    public void shutdownSearcher() {
        searcher.shutdownSearcher();
        database.startTransaction();
        Node node = database.createNode();
        searcher.indexOnProperty(node, "Key");
    }

    @Test
    public void findGroupNodeEmptyDBTest() {
        GroupNodeDecorator node = (GroupNodeDecorator) searcher.findGroupNode(dependencyA);
        assertNull(node);
    }

    @Test
    public void findGroupNodeTest() {
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        GroupNodeDecorator node = (GroupNodeDecorator) searcher.findGroupNode(dependencyA);
        assertNotNull(node);
        assertTrue(node.getGroupId().equals(dependencyA.getGroupId()));
    }

    @Test
    public void findArtifactNodeTest() {
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        ArtifactNodeDecorator node = (ArtifactNodeDecorator) searcher.findArtifactNode(dependencyA);
        assertNotNull(node);
        assertTrue(node.getArtifactId().equals(dependencyA.getArtifactId()));
    }

    @Test
    public void findVersionNodeTest() {

        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        VersionNodeDecorator node = (VersionNodeDecorator) searcher.findVersionNode(dependencyA);
        assertNotNull(node);
        assertTrue(node.getVersion().equals(dependencyA.getVersion()));

    }

    @Test
    public void getVersionNodesSingleResultTest() {
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        List<VersionNode> versionNodes = searcher.getVersionNodes(dependencyA);
        assertNotNull(versionNodes);
        assertEquals(1, versionNodes.size());
        assertTrue(versionNodes.get(0).getVersion().equals(dependencyA.getVersion()));
    }

    @Test
    public void getVersionNodesMultiResultTest() {
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        processor.addArtifact(dependencyA2);
        List<VersionNode> versionNodes = searcher.getVersionNodes(dependencyA);
        assertNotNull(versionNodes);
        assertEquals(2, versionNodes.size());
        assertTrue(versionNodes.get(0).getVersion().equals(dependencyA.getVersion()));
        assertTrue(versionNodes.get(1).getVersion().equals(dependencyA2.getVersion()));
    }

    @Test
    public void getVersionNodesNonProcessedDependency() {
        List<VersionNode> versionNodes = searcher.getVersionNodes(dependencyA);
        assertNotNull(versionNodes);
        assertEquals(0, versionNodes.size());
    }


    @Test(expected = IllegalArgumentException.class)
    public void getDependingArtifactsBadScope() {
        Map<DependencyScopeRelations, List<ArtifactNode>> result;
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        processor.addArtifact(dependencyB);
        processor.addRelation(dependencyB, dependencyA);
    }

    @Test
    public void getDependingArtifacts() {
        Map<DependencyScopeRelations, List<ArtifactNode>> result;
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        dependencyA.setScope("compile");
        processor.addArtifact(dependencyA);
        processor.addArtifact(dependencyB);
        processor.addRelation(dependencyB, dependencyA);
        result = searcher.getDependingArtifacts(dependencyA);
        assertTrue(result.containsKey(DependencyScopeRelations.compile));
        assertTrue(result.get(DependencyScopeRelations.compile).size() == 1);
        assertTrue(result.get(DependencyScopeRelations.compile).get(0).getArtifactId().equals(dependencyB.getArtifactId()));
    }

    @Test
    public void getDependingArtifactsNotFoundDependency() {
        Map<DependencyScopeRelations, List<ArtifactNode>> result;
        result = searcher.getDependingArtifacts(dependencyA);
        assertTrue(result.containsKey(DependencyScopeRelations.compile));
        for (DependencyScopeRelations dependencyScopeRelations : result.keySet()) {
            assertTrue(result.get(dependencyScopeRelations).size() == 0);
        }
    }


    @Test
    public void getVersionDependenciesTest() {
        Map<VersionNode, List<VersionNode>> result;
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        dependencyA.setScope("compile");
        processor.addArtifact(dependencyA);
        processor.addArtifact(dependencyB);
        processor.addRelation(dependencyB, dependencyA);
        result = searcher.getVersionDependencies(dependencyA);
        assertEquals(1, result.keySet().size());
        for (VersionNode versionNode : result.keySet()) {
            assertTrue(versionNode.getVersion().equals("1.0"));
            VersionNode otherVersionNode = result.get(versionNode).get(0);
            assertTrue(otherVersionNode.getVersion().equals("1.0"));
        }
    }

    @Test
    public void getVersionDependenciesEmptyTest() {
        Map<VersionNode, List<VersionNode>> result;
        result = searcher.getVersionDependencies(dependencyA);
        assertEquals(0, result.keySet().size());
    }


}
