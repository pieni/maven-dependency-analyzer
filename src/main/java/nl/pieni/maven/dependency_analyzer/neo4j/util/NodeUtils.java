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

package nl.pieni.maven.dependency_analyzer.neo4j.util;

import nl.pieni.maven.dependency_analyzer.neo4j.enums.ScopedRelation;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * Various utils used
 */
public class NodeUtils {

    /**
     * Create a String representation of a {@link Node}
     * @param node the node
     * @return  String
     */
    public static String nodeToString(Node node) {
        StringBuffer buff = new StringBuffer();
        buff.append("Node{ Id = ");
        buff.append(node.getId());
        for (String key : node.getPropertyKeys()) {
            buff.append(" key = ");
            buff.append(key);
            buff.append(" value = ");
            buff.append(node.getProperty(key));
        }
        buff.append("}");

        return buff.toString();
    }

    /**
     * String representation of a relation
     * @param relation the Relation
     * @return String
     */
    public static String relation2String(Relationship relation) {
        return "Relationship { Id = " + relation.getId() + " type = " + relation.getType() + "}";
    }

    /**
     * Determine if the relation is a scope one.
     *
     * @param relationship the relation
     * @return true when scope relation
     */
    public static boolean isScoperelation(RelationshipType relationship) {
        return relationship instanceof ScopedRelation;
    }

}
