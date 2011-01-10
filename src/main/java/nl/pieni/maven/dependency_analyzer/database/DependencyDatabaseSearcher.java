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

import nl.pieni.maven.dependency_analyzer.enums.DependencyScopeRelations;
import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import nl.pieni.maven.dependency_analyzer.node.VersionNode;
import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Map;

/**
 * Interface to the database searcher
 */
public interface DependencyDatabaseSearcher<N> {
    /**
     * Find the version statement for specified {@link org.apache.maven.model.Dependency}
     *
     * @param dependency The dependency
     * @return the Found statement, null if not found
     */
    VersionNode findVersionNode(Dependency dependency);

    /**
     * Find the artifact statement for specified {@link org.apache.maven.model.Dependency}
     *
     * @param dependency The dependency
     * @return the Found statement, null if not found
     */
    ArtifactNode findArtifactNode(Dependency dependency);

    /**
     * Find the group statement for specified {@link org.apache.maven.model.Dependency}
     *
     * @param dependency The dependency
     * @return the Found statement, null if not found
     */
    GroupNode findGroupNode(Dependency dependency);


    /**
     * //TODO JAVADOC
     * @param partialGroupId
     * @return
     */
    GroupNode findGroupNode(String partialGroupId);

    /**
     * Retrieve all version nodes
     * @param dependency the artifact
     * @return the list of version statement for the artifact
     */
    List<VersionNode> getVersionNodes(Dependency dependency);

    /**
     * Retrieve the list of artifacts that have a dependency on this {@link ArtifactNode}
     *
     * @param dependency the Artifact
     * @return A separate list for each ({@link Dependency}) scope
     */
    Map<DependencyScopeRelations, List<ArtifactNode>> getDependingArtifacts(Dependency dependency);

    /**
     * Retrieve the list of {@link VersionNode} elements that are dependent on the {@link ArtifactNode}.
     * @param dependency the source
     * @return List for each version available of the {@link ArtifactNode}
     */
    Map<VersionNode, List<VersionNode>> getVersionDependencies(Dependency dependency);

    /**
     * Shutdown the searcher, ie. disconnect
     */
    void shutdownSearcher();

    /**
     * Add index entry for the specified property
     * @param node the statement
     * @param key  the key
     */
    void indexOnProperty(final N node, final String key);
}
