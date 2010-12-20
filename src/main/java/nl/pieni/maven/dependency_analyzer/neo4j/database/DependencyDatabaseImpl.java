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
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.node.ArtifactNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.neo4j.node.VersionNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for accessing the GRaph Database
 */
public class DependencyDatabaseImpl implements DependencyDatabase<GraphDatabaseService, Node> {
    private final Log LOGGER;
    private GraphDatabaseService graphDb;
    private Transaction transaction;
    private int transactionCount = 0;


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
        getLOGGER().debug("Starting Transaction");
        transactionCount++;
        this.transaction = getDatabase().beginTx();
        return;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopTransaction() {
        this.transaction.success();
        this.transaction.finish();
        transactionCount--;
        getLOGGER().debug("Finish Transaction");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdownDatabase() {
        if (transactionCount != 0) {
            getLOGGER().error("Transaction count = " + transactionCount);
        }

        getDatabase().shutdown();
    }
}
