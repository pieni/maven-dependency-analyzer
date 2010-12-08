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

package nl.pieni.maven.dependency_graph.neo4j.database;

import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.index.IndexService;



/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 2-12-10
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public interface DependencyDatabase {
    GraphDatabaseService getGraphDb();

    IndexService getIndexService();

    Searcher getSearcher();

    Log getLog();

    Node createNode();

    Transaction startTransaction();

    void stopTransaction();

    void shutdown();
}
