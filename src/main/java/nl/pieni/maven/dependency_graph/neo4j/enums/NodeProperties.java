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

package nl.pieni.maven.dependency_graph.neo4j.enums;

import org.jetbrains.annotations.NotNull;

/**
 * Properties stored in the various nodes
 */
public interface NodeProperties {
    @NotNull
    static final String VERSION = "version";
    @NotNull
    static final String ARTIFACT_ID = "artifact";
    @NotNull
    static final String GROUP_ID = "group";
    @NotNull
    static final String TYPE = "type";
    @NotNull
    static final String NODE_TYPE = "NodeType";
}
