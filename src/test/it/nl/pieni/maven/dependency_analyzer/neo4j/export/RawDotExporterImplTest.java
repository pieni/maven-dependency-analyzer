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
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ScopedRelation;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.RawDotExporterImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.raw.NodeWriter;
import nl.pieni.maven.dependency_analyzer.matchers.ArtifactNodeDecoratorMatcher;
import nl.pieni.maven.dependency_analyzer.matchers.GroupNodeDecoratorMatcher;
import nl.pieni.maven.dependency_analyzer.matchers.RelationshipTypeMatcher;
import nl.pieni.maven.dependency_analyzer.matchers.VersionNodeDecoratorMatcher;
import nl.pieni.maven.dependency_analyzer.neo4j.database.AbstractDatabaseImplTest;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseSearcherImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyNodeProcessorImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.raw.NodeWriterImpl;
import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.apache.maven.model.Dependency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test cases for the exportRaw to a Dot file.
 */
public class RawDotExporterImplTest extends AbstractDatabaseImplTest {

    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private static DependencyNodeProcessor processor;
    private static DotExporter exporter;
    private static final String BASE_GROUP_ID = "nl.pieni.maven";
    private int dependencyCnt;
    private final List<String> starIncludeList;

    public RawDotExporterImplTest() {
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
    }

    @After
    public void after() {
        database.shutdownDatabase();
    }

    @Test
    public void testSingleArtifact() throws IOException {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);

        exporter = new RawDotExporterImpl(database, log);
        exporter.setIncludeVersions(true);
        //NodeWriter writer = mock(NodeWriter.class);
    FileOutputStream fos = new FileOutputStream("target/compile_dependency.dot");
    Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
    NodeWriter writer = new NodeWriterImpl(osWriter, log);
        exporter.setIncludePatters(starIncludeList);
        exporter.export(writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency.getGroupId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency.getArtifactId())));
        verify(writer).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency.getVersion())));
        verify(writer, times(4)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.has)));
        verify(writer).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.version)));
    }

    @Test
    public void testTwoArtifactSameGroupId() throws IOException {
        String groupId = BASE_GROUP_ID;

        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);

        exporter = new RawDotExporterImpl(database, log);
        exporter.setIncludeVersions(true);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.setIncludePatters(starIncludeList);
        exporter.export(writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId)));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, times(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(5)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.has)));
        verify(writer, times(2)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.version)));
    }

    @Test
    public void testTwoArtifactDifferentGroupId() throws IOException {

        Dependency dependency_1 = getDependency("nl.pieni.maven");
        processor.addArtifact(dependency_1);

        Dependency dependency_2 = getDependency("org.pieni.maven");
        processor.addArtifact(dependency_2);

        exporter = new RawDotExporterImpl(database, log);
        exporter.setIncludeVersions(true);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.setIncludePatters(starIncludeList);
        exporter.export(writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, times(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(2)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.version)));
        verify(writer, times(8)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.has)));
    }


    @Test
    public void testChildGroupIds() throws IOException {
        String groupId = BASE_GROUP_ID;
        Dependency dependency_1 = getDependency(groupId);
        processor.addArtifact(dependency_1);

        groupId = groupId + ".cgi_1";
        Dependency dependency_2 = getDependency(groupId);
        processor.addArtifact(dependency_2);

        exporter = new RawDotExporterImpl(database, log);
        exporter.setIncludeVersions(true);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.setIncludePatters(starIncludeList);
        exporter.export(writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, times(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(6)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.has)));
        verify(writer, times(2)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.version)));
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

        exporter = new RawDotExporterImpl(database, log);
        exporter.setIncludeVersions(true);
        NodeWriter writer = mock(NodeWriter.class);
        exporter.setIncludePatters(starIncludeList);
        exporter.export(writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(2)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.version)));
        verify(writer).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ScopedRelation.compile)));
        verify(writer, times(6)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.has)));
        verify(writer).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.depends)));
    }

    @Test
    public void testIncludeListTwoTreesNoDependencies() throws IOException {
        String groupId_1 = "nl.pieni.maven";
        Dependency dependency_1 = getDependency(groupId_1);
        processor.addArtifact(dependency_1);

        String groupId_2 = "org.pieni.maven";
        Dependency dependency_2 = getDependency(groupId_2);
        processor.addArtifact(dependency_2);

        exporter = new RawDotExporterImpl(database, log);
        exporter.setIncludeVersions(true);
        NodeWriter writer = mock(NodeWriter.class);
        List<String> includeList = new ArrayList<String>();
        includeList.add("nl.pieni.maven*");
        exporter.setIncludePatters(includeList);
        exporter.export(writer);

        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
        verify(writer).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
        verify(writer).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
        verify(writer, times(4)).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.has)));
        verify(writer).writeNode2NodeRelation(Matchers.<Node>any(), Matchers.<Node>any(), argThat(new RelationshipTypeMatcher(ArtifactRelations.version)));

    }

    //    @Test
//    public void testIncludeListWithStarNoDependency() throws IOException {
//        String groupId_1 = "nl.pieni.maven";
//        Dependency dependency_1 = getDependency(groupId_1);
//        processor.addArtifact(dependency_1);
//
//        String groupId_2 = "nl.pieni.maven.xx";
//        Dependency dependency_2 = getDependency(groupId_2);
//        processor.addArtifact(dependency_2);
//
//        exporter = new RawDotExporterImpl(database, true, log);
//        NodeWriter writer = mock(NodeWriter.class);
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add("nl.pieni.maven*");
//        exporter.setIncludePatters(excludeList);
//        exporter.exportRaw(writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(groupId_1)));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
//        verify(writer, times(1)).writeNode2NodeRelation(any(Node.class), any(Node.class));
//    }
//
//    @Test
//    public void testIncludeListSingleCompileDependency() throws IOException {
//        String groupId = BASE_GROUP_ID;
//        Dependency dependency_1 = getDependency(groupId);
//        processor.addArtifact(dependency_1);
//
//        groupId = groupId + ".cgi_1";
//        Dependency dependency_2 = getDependency(groupId);
//        processor.addArtifact(dependency_2);
//        dependency_2.setScope("compile");
//        processor.addRelation(dependency_1, dependency_2);
//
//        exporter = new RawDotExporterImpl(database, true, log);
//        NodeWriter writer = mock(NodeWriter.class);
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add("nl.pieni.maven");
//        exporter.exportRaw(excludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
//    }
//
//    @Test
//    public void testIncludeListSingleCompileDependencyNoVersions() throws IOException {
//        String groupId = BASE_GROUP_ID;
//        Dependency dependency_1 = getDependency(groupId);
//        processor.addArtifact(dependency_1);
//
//        groupId = groupId + ".cgi_1";
//        Dependency dependency_2 = getDependency(groupId);
//        processor.addArtifact(dependency_2);
//        dependency_2.setScope("compile");
//        processor.addRelation(dependency_1, dependency_2);
//
//        exporter = new RawDotExporterImpl(database, false, log);
//        NodeWriter writer = mock(NodeWriter.class);
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add("nl.pieni.maven");
//        exporter.exportRaw(excludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
//    }
//
//    @Test
//    public void testIncludeListSingleCompileDependencyExcludePart() throws IOException {
//        String groupId = BASE_GROUP_ID;
//        Dependency dependency_1 = getDependency(groupId);
//        processor.addArtifact(dependency_1);
//
//        groupId = groupId + ".cgi_1";
//        Dependency dependency_2 = getDependency(groupId);
//        processor.addArtifact(dependency_2);
//        dependency_2.setScope("compile");
//        processor.addRelation(dependency_1, dependency_2);
//
//        groupId = "a.b.c";
//        Dependency dependency_3 = getDependency(groupId);
//        processor.addArtifact(dependency_3);
//        dependency_3.setScope("compile");
//        processor.addRelation(dependency_1, dependency_3);
//
//        exporter = new RawDotExporterImpl(database, true, log);
//        NodeWriter writer = mock(NodeWriter.class);
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add("nl.pieni.maven");
//        exporter.exportRaw(excludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
//    }
//
//    @Test
//    public void testIncludeListSingleCompileDependencyWithStar() throws IOException {
//        String groupId = BASE_GROUP_ID;
//        Dependency dependency_1 = getDependency(groupId);
//        processor.addArtifact(dependency_1);
//
//        groupId = groupId + ".cgi_1";
//        Dependency dependency_2 = getDependency(groupId);
//        processor.addArtifact(dependency_2);
//        dependency_2.setScope("compile");
//        processor.addRelation(dependency_1, dependency_2);
//
//        exporter = new RawDotExporterImpl(database, true, log);
//        NodeWriter writer = mock(NodeWriter.class);
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add("nl.pieni.maven*");
//        exporter.exportRaw(excludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())), argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
//        verify(writer, atLeast(2)).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency_1.getVersion())));
//        verify(writer, atLeast(2)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.version)));
//        verify(writer, atLeast(3)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.has)));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ScopedRelation.compile)));
//        verify(writer, atLeast(1)).writeRelation(argThat(new RelationshipMatcher(ArtifactRelations.depends)));
//    }
//
//
//    @Test
//    public void testFilterWithStartSingleArtifact() throws IOException {
//        Dependency dependency = getDependency(BASE_GROUP_ID);
//        processor.addArtifact(dependency);
//
//        exporter = new RawDotExporterImpl(database, true, log);
//
//        NodeWriter writer = mock(NodeWriter.class);
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add("nl.pieni.*");
//        exporter.exportRaw(excludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency.getArtifactId())));
//        verify(writer).writeNode(argThat(new VersionNodeDecoratorMatcher(dependency.getVersion())));
//    }
//
//    @Test
//    public void testTwoGroupsAndArtifacts() throws IOException {
//        String groupId_1 = BASE_GROUP_ID + ".group_1";
//        String groupId_2 = BASE_GROUP_ID + ".group_2";
//
//        Dependency dependency_1 = getDependency(groupId_1);
//        processor.addArtifact(dependency_1);
//
//
//        Dependency dependency_2 = getDependency(groupId_2);
//        processor.addArtifact(dependency_2);
//
//        exporter = new RawDotExporterImpl(database, false, log);
//        NodeWriter writer = mock(NodeWriter.class);
////        FileOutputStream fos = new FileOutputStream("target/compile_dependency.dot");
////        Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
////        NodeWriter writer = new NodeWriterImpl(osWriter, log);
//
//        exporter.exportRaw(starIncludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)));
//        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)), argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)));
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//        );
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
//    }
//
//    @Test
//    public void testFourGroupsAndArtifacts() throws IOException {
//        String groupId_1 = BASE_GROUP_ID + ".group_1";
//        String groupId_2 = BASE_GROUP_ID + ".group_2";
//        String groupId_3 = BASE_GROUP_ID + ".group_2.a";
//        String groupId_4 = BASE_GROUP_ID + ".group_3.b";
//
//
//        Dependency dependency_1 = getDependency(groupId_1);
//        processor.addArtifact(dependency_1);
//
//        Dependency dependency_2 = getDependency(groupId_2);
//        processor.addArtifact(dependency_2);
//        Dependency dependency_3 = getDependency(groupId_3);
//        processor.addArtifact(dependency_3);
//        Dependency dependency_4 = getDependency(groupId_4);
//        processor.addArtifact(dependency_4);
//
//        exporter = new RawDotExporterImpl(database, false, log);
//        NodeWriter writer = mock(NodeWriter.class);
////        FileOutputStream fos = new FileOutputStream("c:/temp/compile_dependency.dot");
////        Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
////        NodeWriter writer = new NodeWriterImpl(osWriter, log);
//
//        exporter.exportRaw(starIncludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)));
//        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)), argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)));
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId()))
//        );
//
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId()))
//        );
//
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_3.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID + ".group_2"))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_3.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_3.getGroupId()))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_4.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_4.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_4.getGroupId()))
//        );
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_3.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_4.getArtifactId())));
//    }
//
//@Test
//    public void testFourGroupsAndArtifactsWithExclude() throws IOException {
//        String groupId_1 = BASE_GROUP_ID + ".group_1";
//        String groupId_2 = BASE_GROUP_ID + ".group_2";
//        String groupId_3 = BASE_GROUP_ID + ".group_2.a";
//        String groupId_4 = "com.pieni.xx" + ".group_3.b";
//
//
//        Dependency dependency_1 = getDependency(groupId_1);
//        processor.addArtifact(dependency_1);
//
//        Dependency dependency_2 = getDependency(groupId_2);
//        processor.addArtifact(dependency_2);
//        Dependency dependency_3 = getDependency(groupId_3);
//        processor.addArtifact(dependency_3);
//        Dependency dependency_4 = getDependency(groupId_4);
//        processor.addArtifact(dependency_4);
//
//        exporter = new RawDotExporterImpl(database, false, log);
////        NodeWriter writer = mock(NodeWriter.class);
//        FileOutputStream fos = new FileOutputStream("c:/temp/compile_dependency.dot");
//        Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
//        NodeWriter writer = new NodeWriterImpl(osWriter, log);
//        List<String> includeList = new ArrayList<String>();
//        includeList.add(BASE_GROUP_ID + "*");
//        exporter.exportRaw(includeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)));
//        verify(writer, atLeast(1)).writeNode(argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)), argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID)));
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId()))
//        );
//
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_2.getGroupId()))
//        );
//
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_3.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID + ".group_2"))
//        );
//        verify(writer, atLeast(1)).writeNode(
//                argThat(new GroupNodeDecoratorMatcher(dependency_3.getGroupId())),
//                argThat(new GroupNodeDecoratorMatcher(dependency_3.getGroupId()))
//        );
////        verify(writer, atLeast(1)).writeNode(
////                argThat(new GroupNodeDecoratorMatcher(dependency_4.getGroupId())),
////                argThat(new GroupNodeDecoratorMatcher(BASE_GROUP_ID))
////
////        );
////        verify(writer, atLeast(1)).writeNode(
////                argThat(new GroupNodeDecoratorMatcher(dependency_4.getGroupId())),
////                argThat(new GroupNodeDecoratorMatcher(dependency_4.getGroupId()))
////        );
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_2.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_3.getArtifactId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_4.getArtifactId())));
//    }
//
//    @Test
//    public void testTwoGroupsAndArtifactsOneExcluded() throws IOException {
//        String groupId_1 = BASE_GROUP_ID + ".group_1";
//        String groupId_2 = BASE_GROUP_ID + ".group_2";
//
//        Dependency dependency_1 = getDependency(groupId_1);
//        processor.addArtifact(dependency_1);
//
//
//        Dependency dependency_2 = getDependency(groupId_2);
//        processor.addArtifact(dependency_2);
//
//        exporter = new RawDotExporterImpl(database, false, log);
//        NodeWriter writer = mock(NodeWriter.class);
////        FileOutputStream fos = new FileOutputStream("target/compile_dependency.dot");
////        Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
////        NodeWriter writer = new NodeWriterImpl(osWriter, log);
//
//        List<String> excludeList = new ArrayList<String>();
//        excludeList.add(groupId_1);
//        exporter.exportRaw(excludeList, writer);
//
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer).writeNode(argThat(new GroupNodeDecoratorMatcher(dependency_1.getGroupId())));
//        verify(writer, atLeast(1)).writeNode(argThat(new ArtifactNodeDecoratorMatcher(dependency_1.getArtifactId())));
//    }
//

//    FileOutputStream fos = new FileOutputStream("target/compile_dependency.dot");
//    Writer osWriter = new OutputStreamWriter(fos, "UTF-8");
//    NodeWriter writer = new NodeWriterImpl(osWriter, log);


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
