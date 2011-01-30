/*
 * Copyright (c) 2011 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.neo4j.export.dot.shapes;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeType;
import org.neo4j.graphdb.Node;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 29-1-11
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
public enum ShapeIdPrefix {


    Group("G"), //Group
    Artifact("A"), //Artifact
    Version("V"), //Version
    Root("R"); //Reference/root

    private String shortPrefix;

    ShapeIdPrefix(String shortPrefix) {
        this.shortPrefix = shortPrefix;
    }

    public static String fromNode(Node node) {
        if (node.hasProperty(NodeProperties.NODE_TYPE)) {
            switch (NodeType.fromString(node.getProperty(NodeProperties.NODE_TYPE))) {
                case ArtifactNode:
                    return Artifact.toString();
                case GroupNode:
                    return Group.toString();
                case VersionNode:
                    return Version.toString();
            }
        }

        return Root.toString();
    }

    public String toString() {
        return shortPrefix;
    }

}
