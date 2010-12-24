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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.TransactionFailureException;

import java.io.IOException;

import static junit.framework.Assert.assertNull;

/**
 * Database tests
 */

public class DependencyDatabaseImplTest extends AbstractDatabaseImplTest {

    private static DependencyDatabase<GraphDatabaseService, Node> database;
    private boolean dbClosed = false;


    @Before
    public void before() throws IOException {
        beforeBase();
        database = new DependencyDatabaseImpl(log, getDBDirectory());
    }

    @After
    public void after() {
        if (!dbClosed) {
            database.shutdownDatabase();
        }
        afterBase();
    }


    @Test(expected = NotInTransactionException.class)
    public void createNodeNotInTransactionTest() {
        Node node = database.createNode();
        assertNull(node);
    }


    @Test(expected = IllegalStateException.class)
    public void shutdownDatabaseAndCreateNode() {
        database.shutdownDatabase();
        dbClosed = true;
        database.startTransaction();
        database.createNode();
        database.stopTransaction();
    }

    @Ignore
    @Test(expected = TransactionFailureException.class)
    public void shutdownDatabaseAndPendingTransaction() {
        try {
            database.startTransaction();
            database.createNode();
            database.shutdownDatabase();
            dbClosed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
