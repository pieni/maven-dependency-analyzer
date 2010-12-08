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

package nl.pieni.maven.dependency_graph.neo4j.node;

import nl.pieni.maven.dependency_graph.neo4j.database.DependencyDatabase;
import nl.pieni.maven.dependency_graph.neo4j.enums.NodeType;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import static nl.pieni.maven.dependency_graph.neo4j.enums.NodeProperties.NODE_TYPE;


/**
 * Abstract Base class for all Nodes
 */
public abstract class AbstractNodeFactory {
    protected final Log LOGGER;

    private final DependencyDatabase database;

    /**
     * Default constructor
     *
     * @param database The database
     * @param logger   the logger
     */
    public AbstractNodeFactory(DependencyDatabase database, final Log logger) {
        this.database = database;
        this.LOGGER = logger;
    }

    /**
     * Add index entry for the specified property
     *
     * @param node the node
     * @param key  the key
     */
    protected void indexOnProperty(@NotNull final Node node, final String key) {
        database.getIndexService().index(node, key, node.getProperty(key));
    }

    /**
     * Helper method for logging
     *
     * @param node the Node to convert
     * @return String of node and properties
     */
    protected String node2String(@NotNull final Node node) {
        StringBuffer buff = new StringBuffer();
        buff.append("Node{ Id = ");
        buff.append(node.getId());
        for (String key : node.getPropertyKeys()) {
            buff.append(" key = ");
            buff.append(key);
            buff.append(" value = ");
            buff.append(node.getProperty(key));
        }

        return buff.toString();
    }

    protected Node createNode(NodeType type) {
        Node node = database.createNode();
        node.setProperty(NODE_TYPE, type.toString());
        return node;
    }

    /**
     * Get the Database
     *
     * @return the Database
     */
    public DependencyDatabase getDatabase() {
        return database;
    }


    /**
     * Create a specific node for insertion into the DB
     * @param dependency The {@link Dependency}
     * @return {@link Node} created
     */
    abstract protected Node create(final Dependency dependency);

    /**
     * Insert a {@link Dependency} into the DB
     * @param dependency
     * @return Number of {@link Node}'s inserted into the DB
     */
    abstract public int insert(final Dependency dependency);
}
