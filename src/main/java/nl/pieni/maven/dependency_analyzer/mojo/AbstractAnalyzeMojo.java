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

package nl.pieni.maven.dependency_analyzer.mojo;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseImpl;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyDatabaseSearcherImpl;
import nl.pieni.maven.dependency_analyzer.repository.RepositorySearcher;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * base class for the Mojo's
 */
public abstract class AbstractAnalyzeMojo extends AbstractMojo {

    /*
     * The searching engine
     */
    protected RepositorySearcher repositorySearcher;

    /**
     * Select the latest released version of the artifacts found.
     *
     * @parameter property="databaseDirectory" default-value="target/neo4j"
     */
    String databaseDirectory;

    /**
     * The Dependency database
     */
    private DependencyDatabase database;

    /**
     * The searcher
     */
    private DependencyDatabaseSearcher searcher;


    @SuppressWarnings("unchecked")
    protected void setup() throws MojoExecutionException {
        database = new DependencyDatabaseImpl(getLog(), databaseDirectory);
        searcher = new DependencyDatabaseSearcherImpl(getLog(), database);
    }

    protected void tearDown() {
        database.shutdownDatabase();
    }

    public DependencyDatabase getDatabase() {
        return database;
    }

    public DependencyDatabaseSearcher getSearcher() {
        return searcher;
    }
}
