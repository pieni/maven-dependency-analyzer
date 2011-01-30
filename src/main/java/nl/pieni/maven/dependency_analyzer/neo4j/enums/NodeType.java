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

import java.util.HashMap;
import java.util.Map;

/**
 * The Node types used
 * Implements the J Bloch implementation for String to enum value casting
 */
public enum NodeType {
    GroupNode,
    ArtifactNode,
    VersionNode;


    private static final Map<String, NodeType> STRING2ENUM = new HashMap<String, NodeType>();

    static {
        for (NodeType val : values()) {
            STRING2ENUM.put(val.toString(), val);
        }
    }

    /**
     * Gets the enum for a code.
     *
     * @param pcode the code.
     * @return the corresponding enum.
     */
    public static NodeType fromString(final String pcode) {
        return STRING2ENUM.get(pcode);
    }

    /**
     * Gets the enum for a code.
     *
     * @param pcode the code.
     * @return the corresponding enum.
     */
    public static NodeType fromString(final Object pcode) {
        return STRING2ENUM.get(pcode.toString());
    }
}
