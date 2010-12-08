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

package nl.pieni.maven.dependency_analyzer.neo4j.database.impl;

import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.neo4j.database.Searcher;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * Abstract base class for accessing the GRaph Database
 */
public class DependencyDatabaseImpl implements DependencyDatabase {
    private final Log log;
    private GraphDatabaseService graphDb;
    private IndexService indexService;
    private Searcher searcher;
    private boolean inTransaction = false;
    private Transaction transaction;
    private int transactionCount = 0;

    public DependencyDatabaseImpl(final Log logger, String dbDirectory) {
        this.log = logger;
        this.graphDb = new EmbeddedGraphDatabase(dbDirectory);
        this.indexService = new LuceneIndexService(graphDb);
        searcher = new SearcherImpl(getIndexService(), logger);
    }

    @Override
    public GraphDatabaseService getGraphDb() {
        return graphDb;
    }

    @Override
    public IndexService getIndexService() {
        return indexService;
    }

    @Override
    public Searcher getSearcher() {
        return searcher;
    }

    @Override
    public Log getLog() {
        return log;
    }

    @Override
    public Node createNode() {
        return graphDb.createNode();
    }

    @Override
    public Transaction startTransaction() {
        getLog().debug("Starting Transaction");
        transactionCount++;
        transaction = getGraphDb().beginTx();
        return transaction;
    }

    @Override
    public void stopTransaction() {
        this.transaction.success();
        this.transaction.finish();
        transactionCount--;
        getLog().debug("Finish Transaction");
    }

    @Override
    public void shutdown() {
        if (transactionCount != 0) {
            getLog().error("Transaction count = " + transactionCount);
        }

        getGraphDb().shutdown();
        getIndexService().shutdown();
    }
}
