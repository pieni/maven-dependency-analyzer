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

package nl.pieni.maven.dependency_analyzer.neo4j.enums;

/**
 * Properties stored in the various nodes
 * (Actually want this to be an enum but that leads to multiple casts in Neo4j specific code
 */
public interface NodeProperties {
    /** Version property **/
    static final String VERSION = "version";
    /** Artifact ID property **/
    static final String ARTIFACT_ID = "artifact";
    /** Group ID property **/
    static final String GROUP_ID = "group";
    /** Artifact type property **/
    static final String ARTIFACT_TYPE = "type";
    /** Node type property see {@link NodeType} **/
    static final String NODE_TYPE = "NodeType";
}
