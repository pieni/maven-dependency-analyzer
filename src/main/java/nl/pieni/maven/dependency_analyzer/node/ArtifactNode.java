/*
 * Copyright (c) 2010 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.node;

/**
 * Interface for the an ArtifactNode
 */
public interface ArtifactNode extends DependencyNode {
    /**
     * Retrieve the artifact ID
     *
     * @return artifactId
     */
    String getArtifactId();

    /**
     * Return the type of the artifact
     *
     * @return the Type
     */
    String getType();

    /**
     * Retrieve the parent of the Artifact (i.e. the groupId)
     *
     * @return {@link GroupNode}
     */
    GroupNode getParent();
}
