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

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.dot.DotExporter;
import nl.pieni.maven.dependency_analyzer.neo4j.database.AbstractDatabaseImplTest;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseSearcherImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyNodeProcessorImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.apache.maven.model.Dependency;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * Test the node selection
 * TODO Implement test cases
 */
public class NodeSelectorTest extends AbstractDatabaseImplTest {

    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private static DependencyNodeProcessor processor;
    private static final String BASE_GROUP_ID = "nl.pieni.maven";
    private int dependencyCnt;
    private final List<String> starIncludeList;
    private NodeSelector selector;

    public NodeSelectorTest() {
        this.starIncludeList = new ArrayList<String>();
        this.starIncludeList.add("*");
    }

    @Before
    public void before() throws IOException {
        beforeBase();
        log = new SimpleLogger();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
        DependencyDatabaseSearcher<Node> searcher = new DependencyDatabaseSearcherImpl(log, database);
        processor = new DependencyNodeProcessorImpl(database, searcher, log);
        this.selector = new NodeSelector(database, log);
    }

    @Test
    public void testSingeArtifactWithStarInclude() throws Exception {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);
        selector.setIncludeVersions(true);

        selector.setIncludeFilterPatterns(starIncludeList);
        selector.setIncludeVersions(true);
        Map<Node, Set<Relationship>> selection = selector.selectNodesAndRelations();
        assertEquals(6, selection.size());
        for (Node node : selection.keySet()) {
            Set<Relationship> nodeRelations = selection.get(node);
            if (node.hasProperty(NodeProperties.NODE_TYPE) && (NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE)) == NodeType.VersionNode)) {
                assertEquals(0, nodeRelations.size());
            } else {
                assertEquals(1, nodeRelations.size());
            }
        }
    }

    @Test
    public void testSingeArtifactWithStarIncludeExcludeVersions() throws Exception {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);
        selector.setIncludeVersions(true);

        selector.setIncludeFilterPatterns(starIncludeList);
        selector.setIncludeVersions(false);
        Map<Node, Set<Relationship>> selection = selector.selectNodesAndRelations();
        assertEquals(5, selection.size());
        for (Node node : selection.keySet()) {
            if (node.hasProperty(NodeProperties.NODE_TYPE) && NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE)) == NodeType.ArtifactNode) {
                assertEquals(0, selection.get(node).size());
            } else {
                assertEquals(1, selection.get(node).size());
            }
        }
    }


    @Test
    public void testTwoArtifactWithSingleInclude() throws Exception {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);

        dependency = getDependency("some.other.group.id");
        processor.addArtifact(dependency);

        selector.setIncludeVersions(true);
        List<String> singleIncludeList = new ArrayList<String>();
        singleIncludeList.add(BASE_GROUP_ID);

        selector.setIncludeFilterPatterns(singleIncludeList);
        selector.setIncludeVersions(true);

        Map<Node, Set<Relationship>> selection = selector.selectNodesAndRelations();
        assertEquals(6, selection.size());
        for (Node node : selection.keySet()) {
            Set<Relationship> nodeRelations = selection.get(node);
            if (node.hasProperty(NodeProperties.NODE_TYPE) && (NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE)) == NodeType.VersionNode)) {
                assertEquals(0, nodeRelations.size());
            } else {
                assertEquals(1, nodeRelations.size());
            }
        }
    }

    @Test
    public void testTwoArtifactWithStarEndingInclude() throws Exception {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);

        dependency = getDependency(BASE_GROUP_ID + ".something.else");
        processor.addArtifact(dependency);

        selector.setIncludeVersions(true);
        List<String> singleIncludeList = new ArrayList<String>();
        singleIncludeList.add(BASE_GROUP_ID + "*");

        selector.setIncludeFilterPatterns(singleIncludeList);
        selector.setIncludeVersions(true);

        Map<Node, Set<Relationship>> selection = selector.selectNodesAndRelations();
        assertEquals(10, selection.size());
        for (Node node : selection.keySet()) {
            Set<Relationship> nodeRelations = selection.get(node);
            if (node.hasProperty(NodeProperties.NODE_TYPE)) {
                NodeType type = NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE));
                if (type == NodeType.VersionNode) {
                    assertEquals(0, nodeRelations.size());
                    continue;
                }
                if (type == NodeType.GroupNode && node.getProperty(NodeProperties.GROUP_ID).equals(BASE_GROUP_ID)) {
                    assertEquals(2, nodeRelations.size());
                    continue;
                }
            }
            assertEquals(1, nodeRelations.size());
        }
    }

    @Test
    public void testSingeArtifactWithEmptyInclude() throws Exception {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);
        selector.setIncludeVersions(true);
        List<String> emptyIncludeList = new ArrayList<String>();
        selector.setIncludeFilterPatterns(emptyIncludeList);
        selector.setIncludeVersions(true);
        Map<Node, Set<Relationship>> selection = selector.selectNodesAndRelations();
        assertEquals(1, selection.size());
    }


    private Dependency getDependency(String groupId) {
        Dependency dependency = new Dependency();
        dependency.setArtifactId("artifactId_" + dependencyCnt);
        dependency.setGroupId(groupId);
        dependency.setVersion("1.0");
        dependency.setType("jar");
        this.dependencyCnt++;
        return dependency;
    }
}
