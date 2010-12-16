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

import org.apache.maven.model.Dependency;

/**
 * Interface for the dependency database
 */
public interface DependencyNodeProcessor {

    /**
     * Add a {@link Dependency} to the database
     *
     * @param dependency the {@link Dependency}
     * @return number of elements create in the DB
     */
    int addArtifact(Dependency dependency);

    /**
     * Add a relation between the source and target dependency. Both the artifact to artifact relation and the version to version relation is added.
     *
     * @param sourceDependency the source
     * @param targetDependency the target
     * @return the amount of relations created
     */
    int addRelation(Dependency sourceDependency, Dependency targetDependency);
}
