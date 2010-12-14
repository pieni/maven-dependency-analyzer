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

package nl.pieni.maven.dependency_analyzer.neo4j.node.factory;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;


/**
 * Abstract Base class for all Nodes
 */
public abstract class AbstractNodeFactory<T> {
    protected final Log LOGGER;

    private final DependencyDatabase<GraphDatabaseService, Node> database;

    /**
     * Default constructor
     *
     * @param database The database
     * @param logger   the logger
     */
    public AbstractNodeFactory(DependencyDatabase<GraphDatabaseService, Node> database, final Log logger) {
        this.database = database;
        this.LOGGER = logger;
    }

    /**
     * Get the Database
     *
     * @return the Database
     */
    public DependencyDatabase<GraphDatabaseService, Node> getDatabase() {
        return this.database;
    }


    /**
     * Create a specific node for insertion into the DB
     * @param dependency The {@link Dependency}
     * @return {@link Node} created
     */
    abstract protected T create(final Dependency dependency);

    /**
     * Insert a {@link Dependency} into the DB
     * @param dependency
     * @return Number of {@link Node}'s inserted into the DB
     */
    abstract public int insert(final Dependency dependency);
}
