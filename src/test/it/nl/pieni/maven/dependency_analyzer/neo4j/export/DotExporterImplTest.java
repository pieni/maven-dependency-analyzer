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

package nl.pieni.maven.dependency_analyzer.neo4j.export;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.dot.DotExporter;
import nl.pieni.maven.dependency_analyzer.dot.NodeWriter;
import nl.pieni.maven.dependency_analyzer.matchers.ArtifactNodeDecoratorMatcher;
import nl.pieni.maven.dependency_analyzer.matchers.GroupNodeDecoratorMatcher;
import nl.pieni.maven.dependency_analyzer.matchers.RelationshipMatcher;
import nl.pieni.maven.dependency_analyzer.matchers.VersionNodeDecoratorMatcher;
import nl.pieni.maven.dependency_analyzer.neo4j.database.AbstractDatabaseImplTest;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseSearcherImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyNodeProcessorImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.apache.maven.model.Dependency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test cases for the export to a Dot file.
 */
public class DotExporterImplTest extends AbstractDatabaseImplTest {

    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private static DependencyNodeProcessor processor;
    private static DotExporter exporter;
    private static final String BASE_GROUP_ID = "nl.pieni.maven";
    private int dependencyCnt;
    private final List<String> emptyIncludeList;

    public DotExporterImplTest() {
        this.emptyIncludeList = new ArrayList<String>();
        this.emptyIncludeList.add("*");
    }


    @Before
    public void before() throws IOException {
        beforeBase();
        log = new SimpleLogger();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
        DependencyDatabaseSearcher<Node> searcher = new DependencyDatabaseSearcherImpl(log, database);
        processor = new DependencyNodeProcessorImpl(database, searcher, log);
    }

    @After
    public void after() {
        database.shutdownDatabase();
        afterBase();
    }

    @Test
    public void testSingleArtifact() throws IOException {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency.getArtifactId())));
        verify(writer).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency.getVersion())));
    }

    @Test
    public void testTwoArtifactSameGroupId() throws IOException {
        String groupId = BASE_GROUP_ID;

        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId)));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, times(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
    }

    @Test
    public void testTwoArtifactDifferentGroupId() throws IOException {

        Dependency dependency_1 = getDependency(BASE_GROUP_ID + ".i_1");
        processor.addArtifact(dependency_1);

        Dependency dependency_2 = getDependency(BASE_GROUP_ID + ".i_2");
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())), argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, times(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(2)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
        verify(writer, times(2)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
    }


    @Test
    public void testChildGroupIds() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())), argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
    }

    @Test
    public void testSingleCompileDependency() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);
        dependency_2.setScope("compile");
        processor.addRelation(dependency_1, dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())), argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, atLeast(2)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
        verify(writer, atLeast(3)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(DependencyScopeRelations.compile)));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.depends)));
    }

    @Test
    public void testTwoArtifactDifferentTopLevelGroup() throws IOException {
        String groupId_1 = "nl.pieni.maven";

        Dependency dependency_1 = getDependency(groupId_1);
        processor.addArtifact(dependency_1);

        String groupId_2 = "org.pieni.maven";
        Dependency dependency_2 = getDependency(groupId_2);
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId_1)));
        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId_2)));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, times(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(2)).writeReferenceRelation(any(Node.class), any(Node.class));
    }

    @Test
    public void testIncludeStarVersionsTwoArtifact() throws IOException {
        String groupId = BASE_GROUP_ID;

        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, false, log);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId)));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
    }

    @Test
    public void testIncludeListNoDependency() throws IOException {
        String groupId_1 = "nl.pieni.maven";
        Dependency dependency_1 = getDependency(groupId_1);
        processor.addArtifact(dependency_1);

        String groupId_2 = "org.pieni.maven";
        Dependency dependency_2 = getDependency(groupId_2);
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.maven");
        exporter.export(excludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId_1)));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(1)).writeReferenceRelation(any(Node.class), any(Node.class));
    }

    @Test
    public void testIncludeListWithStarNoDependency() throws IOException {
        String groupId_1 = "nl.pieni.maven";
        Dependency dependency_1 = getDependency(groupId_1);
        processor.addArtifact(dependency_1);

        String groupId_2 = "nl.pieni.maven.xx";
        Dependency dependency_2 = getDependency(groupId_2);
        processor.addArtifact(dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.maven*");
        exporter.export(excludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId_1)));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(1)).writeReferenceRelation(any(Node.class), any(Node.class));
    }

    @Test
    public void testIncludeListSingleCompileDependency() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);
        dependency_2.setScope("compile");
        processor.addRelation(dependency_1, dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.maven");
        exporter.export(excludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
    }

    @Test
    public void testIncludeListSingleCompileDependencyNoVersions() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);
        dependency_2.setScope("compile");
        processor.addRelation(dependency_1, dependency_2);

        exporter = new DotExporterImpl(database, false, log);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.maven");
        exporter.export(excludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
    }

    @Test
    public void testIncludeListSingleCompileDependencyExcludePart() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);
        dependency_2.setScope("compile");
        processor.addRelation(dependency_1, dependency_2);

        groupId = "a.b.c";
        Dependency dependency_3 = getDependency(groupId);
        processor.addArtifact(dependency_3);
        dependency_3.setScope("compile");
        processor.addRelation(dependency_1, dependency_3);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.maven");
        exporter.export(excludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
    }

    @Test
    public void testIncludeListSingleCompileDependencyWithStar() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);
        dependency_2.setScope("compile");
        processor.addRelation(dependency_1, dependency_2);

        exporter = new DotExporterImpl(database, true, log);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.maven*");
        exporter.export(excludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())), argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, atLeast(2)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
        verify(writer, atLeast(3)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(DependencyScopeRelations.compile)));
        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.depends)));
    }


    @Test
    public void testFilterWithStartSingleArtifact() throws IOException {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);

        exporter = new DotExporterImpl(database, true, log);

        NodeWriter writer = mock(NodeWriter.class);
        List<String> excludeList = new ArrayList<String>();
        excludeList.add("nl.pieni.*");
        exporter.export(emptyIncludeList, writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency.getGroupId())));
        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency.getArtifactId())));
        verify(writer).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency.getVersion())));
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
