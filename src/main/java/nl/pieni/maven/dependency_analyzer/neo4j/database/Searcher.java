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

import org.apache.maven.model.Dependency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.Node;

/**
 * Interface to the database searcher
 */
public interface Searcher {
    /**
     * Find the version node for specified {@link org.apache.maven.model.Dependency}
     * @param dependency The dependency
     * @return the Found node, null if not found
     */
    @NotNull
    Node findVersionNode(Dependency dependency);

    /**
     * Find the artifact node for specified {@link org.apache.maven.model.Dependency}
     * @param dependency The dependency
     * @return the Found node, null if not found
     */
    @Nullable
    Node findArtifactNode(Dependency dependency);

    /**
     * Find the group node for specified {@link org.apache.maven.model.Dependency}
     * @param dependency The dependency
     * @return the Found node, null if not found
     */
    Node findGroupNode(Dependency dependency);
}
