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

package nl.pieni.maven.dependency_analyzer.database;

import org.apache.maven.plugin.logging.Log;


/**
 * Interface to the Dependency database.
 * @param <DB> the Database implementation
 * @param <N> The node type of the implementation
 */
public interface DependencyDatabase<DB, N> extends DependencyDatabaseSearcher {

    /**
     * Retrieve the Database instance
     * @return the database
     */
    DB getDatabase();

    /**
     * get the Logger instance
     * @return the Logger
     */
    Log getLOGGER();

    /**
     * Create a node in the database, see {@link #getDatabase()}.
     * @return the new node
     */
    N createNode();

    /**
     * Add index entry for the specified property
     * @param node the node
     * @param key  the key
     */
    void indexOnProperty(final N node, final String key);

    /**
     * Shutdown the database, i.e. close the connection
     */
    void shutdownDatabase();

    /**
     * Support for transactions, start a transaction
     *
     */
    void startTransaction();

    /**
     * Support for transactions, stop the transaction
     */
    void stopTransaction();
}
