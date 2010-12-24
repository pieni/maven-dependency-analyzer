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
import org.apache.maven.model.Dependency;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;

/**
 * Database tests (each test breaks the consistency of the DB)
 */
public class DependencyProcesserImplTest extends AbstractDatabaseImplTest {
    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private static DependencyNodeProcessor processor;

    @BeforeClass
    public static void beforeClass() throws IOException {
        try {
            beforeBase();
            database = new DependencyDatabaseImpl(log, getDBDirectory());
            DependencyDatabaseSearcher<Node> searcher = new DependencyDatabaseSearcherImpl(log, database);
            processor = new DependencyNodeProcessorImpl(database, searcher, log);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        database.shutdownDatabase();
        afterBase();
    }

    @Test
    public void addRelationTest() {
        Dependency dependencyA = getDependency();
        Dependency dependencyB = getDependency();
        dependencyB.setScope("compile");
        int count = processor.addArtifact(dependencyA);
        assertEquals(3, count);
        count = processor.addArtifact(dependencyB);
        assertEquals(3, count);
        count = processor.addRelation(dependencyA, dependencyB);
        assertEquals(2, count);
    }

    @Test
    public void addRelationDuplicationTest() {
        Dependency dependencyA = getDependency();
        Dependency dependencyB = getDependency();
        dependencyB.setScope("compile");
        int count = processor.addArtifact(dependencyA);
        assertEquals(3, count);
        count = processor.addArtifact(dependencyB);
        assertEquals(3, count);
        count = processor.addRelation(dependencyA, dependencyB);
        assertEquals(2, count);
        count = processor.addRelation(dependencyA, dependencyB);
        assertEquals(0, count);

    }

    @Test(expected = IllegalArgumentException.class)
    public void addRelationNoScopeTest() {
        Dependency dependencyA = getDependency();
        Dependency dependencyB = getDependency();
        int count = processor.addArtifact(dependencyA);
        assertEquals(3, count);
        count = processor.addArtifact(dependencyB);
        assertEquals(3, count);
        processor.addRelation(dependencyA, dependencyB);
    }

    @Test
    public void addArtifactNewTest() {
        Dependency dependency = getDependency();
        int count = processor.addArtifact(dependency);
        assertEquals(3, count);
    }

    @Test
    public void addArtifactTwiceSameTest() {
        Dependency dependency = getDependency();
        processor.addArtifact(dependency);
        int count = processor.addArtifact(dependency);
        assertEquals(0, count);
    }

}
