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
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 2-12-10
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public interface DependencyDatabase<DB, N> extends DependencyDatabaseSearcher {

    DB getDatabase();

    Log getLOGGER();

    N createNode();

    /**
     * Add index entry for the specified property
     *
     * @param node the node
     * @param key  the key
     */
    void indexOnProperty(final N node, final String key);

    void shutdownDatabase();

    void startTransaction();

    void stopTransaction();


}
