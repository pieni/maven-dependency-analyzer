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
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.ShapeDotExporterImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.export.dot.writer.shape.ShapeDotWriter;
import nl.pieni.maven.dependency_analyzer.test_helpers.SimpleLogger;
import org.apache.maven.model.Dependency;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test cases for the exportRaw to a Dot file.
 */
public class ShapeDotExporterImplTest extends AbstractDatabaseImplTest {

    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private static DependencyNodeProcessor processor;
    private static DotExporter exporter;
    private static final String BASE_GROUP_ID = "nl.pieni.maven";
    private int dependencyCnt;
    private final List<String> starIncludeList;
    Writer fileWriter = mock(Writer.class);
    private ShapeDotWriter writer = new ShapeDotWriter(fileWriter, new SimpleLogger());

    public ShapeDotExporterImplTest() {
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
        exporter = new ShapeDotExporterImpl(database, log);
        writer = new ShapeDotWriter(fileWriter, log);
    }

    @After
    public void after() {
        database.shutdownDatabase();
    }

    @Test
    public void testSingleArtifact() throws IOException {
        Dependency dependency = getDependency(BASE_GROUP_ID);
        processor.addArtifact(dependency);


        exporter.setIncludeVersions(true);
        exporter.setIncludePatters(starIncludeList);
        exporter.export(writer);


        verify(fileWriter).write(contains("digraph G {"));
        verify(fileWriter).write(contains("R0 [ label=\"root\" shape=\"box\" ]"));
        verify(fileWriter).write(contains("G1 [ label=\"nl\" shape=\"folder\" ]"));
        verify(fileWriter).write(contains("G2 [ label=\"nl.pieni\" shape=\"folder\" ]"));
        verify(fileWriter).write(contains("G3 [ label=\"nl.pieni.maven\" shape=\"folder\" ]"));
        verify(fileWriter).write(contains("A4 [ label=\"artifactId_0\" shape=\"rect\" ]"));
        verify(fileWriter).write(contains("V5 [ label=\"1.0\" shape=\"component\" ]"));
        verify(fileWriter).write(contains("G1 -> G2 [label=\"has\" style=\"solid\" ]"));
        verify(fileWriter).write(contains("G2 -> G3 [label=\"has\" style=\"solid\" ]"));
        verify(fileWriter).write(contains("A4 -> V5 [label=\"version\" style=\"solid\" ]"));
        verify(fileWriter).write(contains("R0 -> G1 [label=\"has\" style=\"solid\" ]"));
        verify(fileWriter).write(contains("G3 -> A4 [label=\"has\" style=\"solid\" ]"));
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
