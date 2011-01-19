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

package nl.pieni.maven.dependency_analyzer.neo4j.node.factory;

import nl.pieni.maven.dependency_analyzer.database.DependencyDatabase;
import nl.pieni.maven.dependency_analyzer.database.DependencyDatabaseSearcher;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.ArtifactRelations;
import nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties;
import nl.pieni.maven.dependency_analyzer.neo4j.node.GroupNodeDecorator;
import nl.pieni.maven.dependency_analyzer.node.GroupNode;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.StringTokenizer;

import static nl.pieni.maven.dependency_analyzer.neo4j.enums.NodeProperties.GROUP_ID;

/**
 * GroupNodeFactory
 */
public class GroupNodeFactory extends AbstractNodeFactory<GroupNode> {


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public GroupNodeFactory(DependencyDatabase database, DependencyDatabaseSearcher<Node> searcher, final Log logger) {
        super(database, searcher, logger);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GroupNode create(final Dependency dependency) {
        create(dependency, "");
        return getSearcher().findGroupNode(dependency);
    }

    private int create(final Dependency dependency, final String existingPath) {
        int createCount = 0;
        GroupNodeDecorator currentNode = (GroupNodeDecorator) getSearcher().findGroupNode(existingPath);
        String tmp = dependency.getGroupId().substring(existingPath.length(), dependency.getGroupId().length());
        StringTokenizer stringTokenizer = new StringTokenizer(tmp, ".");
        String createPath = "";
        getDatabase().startTransaction();
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            Node node = getDatabase().createNode();
            createCount++;
            Dependency nextDependency = new Dependency();
            createPath = existingPath + (existingPath.length() != 0 ? "." : "") + createPath + (createPath.length() != 0 ? "." : "") + token;
            nextDependency.setGroupId(createPath);
            GroupNodeDecorator nextNode = new GroupNodeDecorator(node, nextDependency);
            getSearcher().indexOnProperty(node, GROUP_ID);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Created GroupNode: " + nextNode);
            }
            if (currentNode == null) {
                Node refNode = getDatabase().getDatabase().getReferenceNode();
                Relationship relationship = refNode.createRelationshipTo(nextNode, ArtifactRelations.has);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Added " + relationship + " between " + refNode + " and " + nextNode);
                }
            } else {
                Relationship relationship = currentNode.createRelationshipTo(nextNode, ArtifactRelations.has);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Added " + relationship + " between " + currentNode + " and " + nextNode);
                }
            }
            currentNode = nextNode;
        }
        getDatabase().stopTransaction();
        return createCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int insert(final Dependency dependency) {
        int nodeCount = 0;


        GroupNodeDecorator node = (GroupNodeDecorator) getSearcher().findGroupNode(dependency);
        if (node != null) {
            return nodeCount;
        }

        String existingPath = findExistingPath(dependency);

        if (existingPath.length() == 0) {
            //Insert full path
            return create(dependency, "");
        }
        //Partial path known
        return create(dependency, existingPath);
    }

    private String findExistingPath(Dependency dependency) {
        String groupId = dependency.getGroupId();
        StringTokenizer strTok = new StringTokenizer(groupId, ".");
        String searchGroup = "";
        String foundPath = "";
        int count = strTok.countTokens();
        for (int i = 0; i < count; i++) {
            searchGroup += strTok.nextToken();
            if (getSearcher().findGroupNode(searchGroup) == null) {
                return foundPath;
            }
            foundPath = searchGroup;
            searchGroup = foundPath + ".";
        }
        return foundPath;
    }
}
