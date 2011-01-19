/*
 * Copyright  2010 Pieter van der Meer (pieter@pieni.nl)
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
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * Abstract base class for accessing the GRaph Database
 */
public class DependencyDatabaseImpl implements DependencyDatabase<GraphDatabaseService, Node> {
    private final Log LOGGER;
    private GraphDatabaseService graphDb;
    private Transaction transaction;
    private int transactionCount = 0;
    private static int BATCH_SIZE = 100;


    /**
     * Default constructor
     *
     * @param logger      the logger
     * @param dbDirectory the directory in which the DB is created
     */
    public DependencyDatabaseImpl(final Log logger, String dbDirectory) {
        this.LOGGER = logger;
        this.graphDb = new EmbeddedGraphDatabase(dbDirectory);


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphDatabaseService getDatabase() {
        return graphDb;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Log getLOGGER() {
        return LOGGER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node createNode() {
        return graphDb.createNode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTransaction() {

        if (this.transactionCount == 0) {
            getLOGGER().debug("Starting Transaction");
            this.transaction = getDatabase().beginTx();
        } else {
            if (getLOGGER().isDebugEnabled()) {
                getLOGGER().debug("Reusing Transaction");
            }
        }
        this.transactionCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopTransaction() {
        if (this.transactionCount < BATCH_SIZE) {
            if (getLOGGER().isDebugEnabled()) {
                getLOGGER().debug("Batch size (" + BATCH_SIZE + ") not reached, not finishing (yet)");
            }
            return;
        }
        getLOGGER().info("Reached batch size (" + BATCH_SIZE + ") finishing transaction");
        finishTransactions();
    }

    private void finishTransactions() {

        this.transaction.success();
        this.transaction.finish();
        if (getLOGGER().isDebugEnabled()) {
            getLOGGER().debug("Closed Transaction");
        }
        this.transactionCount = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownDatabase() {
        if (transactionCount != 0) {
            finishTransactions();
            getLOGGER().info("Finished open transactions");
        }

        getDatabase().shutdown();
    }
}
