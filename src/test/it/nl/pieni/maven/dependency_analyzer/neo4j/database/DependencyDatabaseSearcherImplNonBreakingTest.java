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
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ScopedRelation;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Non consistency based database tests
 * TODO write unit test for a artifact that is has under is parent groupId both a groupId and a artifactId
 * See line: if (node.hasProperty(NodeProperties.ARTIFACT_ID) && node.getProperty(NodeProperties.ARTIFACT_ID).equals(dependency.getArtifactId())) {
 * minus the hasProperty check
 */
public class DependencyDatabaseSearcherImplNonBreakingTest extends AbstractDatabaseImplTest {

    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private static DependencyDatabaseSearcher<Node> searcher;


    /**
     * Setup for the tests
     * @throws IOException in case of error
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        beforeBase();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
        searcher = new DependencyDatabaseSearcherImpl(log, database);
    }

    /**
     * Cleanup of database after test
     */
    @AfterClass
    public static void afterClass() {
        try {
            database.shutdownDatabase();
            searcher.shutdownSearcher();
        } catch (Exception e) {
            //Ignored
        }
    }

    @Test
    public void findGroupNodeEmptyDBTest() {
        Dependency dependency = getDependency();
        GroupNodeDecorator node = (GroupNodeDecorator) searcher.findGroupNode(dependency);
        assertNull(node);
    }

    @Test
    public void findGroupNodeTest() {
        Dependency dependency = getDependency();

        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependency);
        GroupNodeDecorator node = (GroupNodeDecorator) searcher.findGroupNode(dependency);
        assertNotNull(node);
        assertTrue(node.getGroupId().equals(dependency.getGroupId()));
    }

    @Test
    public void findArtifactNodeTest() {
        Dependency dependency = getDependency();

        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependency);
        ArtifactNodeDecorator node = (ArtifactNodeDecorator) searcher.findArtifactNode(dependency);
        assertNotNull(node);
        assertTrue(node.getArtifactId().equals(dependency.getArtifactId()));
    }

    @Test
    public void findVersionNodeTest() {
        Dependency dependency = getDependency();
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependency);
        VersionNodeDecorator node = (VersionNodeDecorator) searcher.findVersionNode(dependency);
        assertNotNull(node);
        assertTrue(node.getVersion().equals(dependency.getVersion()));

    }

    @Test
    public void getVersionNodesSingleResultTest() {
        Dependency dependency = getDependency();
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependency);
        List<VersionNode> versionNodes = searcher.getVersionNodes(dependency);
        assertNotNull(versionNodes);
        assertEquals(1, versionNodes.size());
        assertTrue(versionNodes.get(0).getVersion().equals(dependency.getVersion()));
    }

    @Test
    public void getVersionNodesMultiResultTest() {
        Dependency dependencyA = getDependency();
        Dependency dependencyA2 = dependencyA.clone();
        dependencyA2.setVersion("2.0");
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
        Dependency dependency = getDependency();
        List<VersionNode> versionNodes = searcher.getVersionNodes(dependency);
        assertNotNull(versionNodes);
        assertEquals(0, versionNodes.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDependingArtifactsBadScope() {
        Dependency dependencyA = getDependency();
        Dependency dependencyB = getDependency();
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        processor.addArtifact(dependencyB);
        processor.addRelation(dependencyB, dependencyA);
    }

    @Test
    public void getDependingArtifacts() {
        Dependency dependencyA = getDependency();
        dependencyA.setScope("compile");
        Dependency dependencyB = getDependency();

        Map<ScopedRelation, List<ArtifactNode>> result;
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);
        processor.addArtifact(dependencyA);
        processor.addArtifact(dependencyB);
        processor.addRelation(dependencyB, dependencyA);
        result = searcher.getDependingArtifacts(dependencyA);
        assertTrue(result.containsKey(ScopedRelation.compile));
        assertTrue(result.get(ScopedRelation.compile).size() == 1);
        assertTrue(result.get(ScopedRelation.compile).get(0).getArtifactId().equals(dependencyB.getArtifactId()));
    }

    @Test
    public void getDependingArtifactsNotFoundDependency() {
        Dependency dependency = getDependency();
        Map<ScopedRelation, List<ArtifactNode>> result;
        result = searcher.getDependingArtifacts(dependency);
        for (ScopedRelation scopedRelation : result.keySet()) {
            assertTrue(result.get(scopedRelation).size() == 0);
        }
    }

    @Test
    public void getVersionDependenciesTest() {
        Dependency dependencyA = getDependency();
        dependencyA.setScope("compile");
        Dependency dependencyB = getDependency();

        Map<VersionNode, List<VersionNode>> result;
        DependencyNodeProcessor processor = new DependencyNodeProcessorImpl(database, searcher, log);

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
        Dependency dependency = getDependency();
        Map<VersionNode, List<VersionNode>> result;
        result = searcher.getVersionDependencies(dependency);
        assertEquals(0, result.keySet().size());
    }
}
