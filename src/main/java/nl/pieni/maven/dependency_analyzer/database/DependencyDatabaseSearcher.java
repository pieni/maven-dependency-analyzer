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
public interface DependencyDatabaseSearcher {
    /**
     * Find the version node for specified {@link org.apache.maven.model.Dependency}
     *
     * @param dependency The dependency
     * @return the Found node, null if not found
     */
    VersionNode findVersionNode(Dependency dependency);

    /**
     * Find the artifact node for specified {@link org.apache.maven.model.Dependency}
     *
     * @param dependency The dependency
     * @return the Found node, null if not found
     */
    ArtifactNode findArtifactNode(Dependency dependency);

    /**
     * Find the group node for specified {@link org.apache.maven.model.Dependency}
     *
     * @param dependency The dependency
     * @return the Found node, null if not found
     */
    GroupNode findGroupNode(Dependency dependency);

    /**
     * Retrieve all version nodes
     * @param node the artifact
     * @return the list of version node for the artifact
     */
    List<VersionNode> getVersionNodes(ArtifactNode node);

    /**
     * Retrieve the list of artifacts that have a dependency on this {@link ArtifactNode}
     *
     * @param node the Artifact node
     * @return A separate list for each ({@link Dependency}) scope
     */
    Map<DependencyScopeRelations, List<ArtifactNode>> getDependingArtifacts(ArtifactNode node);

    /**
     * Retrieve the list of {@link VersionNode} elements that are dependent on the {@link ArtifactNode}.
     * @param node the source node
     * @return List for each version available of the {@link ArtifactNode}
     */
    Map<VersionNode, List<VersionNode>> getVersionDependencies(ArtifactNode node);

    /**
     * Shutdown the searcher, ie. disconnect
     */
    void shutdownSearcher();
}
