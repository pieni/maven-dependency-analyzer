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

import nl.pieni.maven.dependency_analyzer.dot.NodeShapes.NodeShape;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import org.neo4j.graphdb.Node;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 26-1-11
 * Time: 22:42
 * To change this template use File | Settings | File Templates.
 */
public class GroupDotShape extends DotShape {

    public GroupDotShape(Node node) {
        super(node.getId(), NodeShape.folder, node.getProperty(NodeProperties.GROUP_ID).toString());
    }

    public GroupDotShape(Node node, int tokenCount) {
        super(node.getId(), NodeShape.folder, makeLabel(node, tokenCount));
    }

    private static String makeLabel(Node node, int tokenCount) {
        String groupId = node.getProperty(NodeProperties.GROUP_ID).toString();
        String[] tokens = groupId.split(".");
        String result = "";

        for(int i=tokens.length - tokenCount;i<tokens.length;i++) {
            if (result.length() > 0) {
                result += ".";
            }
            result += tokens[i];
        }

        return result;
    }
}
