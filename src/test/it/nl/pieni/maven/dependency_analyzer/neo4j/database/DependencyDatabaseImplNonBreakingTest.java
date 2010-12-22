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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.io.IOException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Non database consistency breaking unit tests
 */
public class DependencyDatabaseImplNonBreakingTest extends AbstractDatabaseImplTest {
    private static DependencyDatabase<GraphDatabaseService, Node> database;


    @BeforeClass
    public static void beforeClass() throws IOException {
        beforeBase();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
    }

    @AfterClass
    public static void afterClass() {
        try {
            database.shutdownDatabase();
            afterBase();
        } finally {
            System.out.println("Done.");
        }
    }

    @Test
    public void getDatabaseTest() {
        assertNotNull(database.getDatabase());
    }

    @Test
    public void getLOGGERTest() {
        assertNotNull(database.getLOGGER());
    }

    @Test
    public void createNodeTest() {
        database.startTransaction();
        Node node = database.createNode();
        database.stopTransaction();
        assertNotNull(node);
        assertTrue(node.getId() != 0);
    }
}
