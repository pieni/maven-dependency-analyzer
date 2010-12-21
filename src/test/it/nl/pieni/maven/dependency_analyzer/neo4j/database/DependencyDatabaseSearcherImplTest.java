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


    @Before
    public void beforeClass() throws IOException {
        beforeBase();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
        searcher = new DependencyDatabaseSearcherImpl(log, database);

        //Create the dependencies used
        Dependency dependencyA=new Dependency();
        dependencyA.setArtifactId("artifactId_A");
        dependencyA.setGroupId("groupId_A");
        dependencyA.setVersion("1.0");
        dependencyA.setType("jar");


        Dependency dependencyA2=new Dependency();
        dependencyA2.setArtifactId("artifactId_A");
        dependencyA2.setGroupId("groupId_A");
        dependencyA2.setVersion("2.0");
        dependencyA2.setType("jar");


        Dependency dependencyB=new Dependency();
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



























}
