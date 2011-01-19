/*
 * Copyright 2010 Pieter van der Meer (pieter(at)pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.mojo.create;

import nl.pieni.maven.dependency_analyzer.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyNodeProcessorImpl;
import nl.pieni.maven.dependency_analyzer.util.VersionComparator;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Creation of the dependency DB
 *
 * @goal create
 * @phase process-sources
 * @requiredProject false
 */
public class CreateDatabaseMojo
        extends AbstractParserMojo {


    /**
     * Node Processor
     */
    private DependencyNodeProcessor nodeProcessor;


    /**
     * The type of artifacts to search. remember, the search is performed against the packaging that is defined in the
     * pom file used to create the artifact. Usable values: jar, war and ear
     *
     * @parameter property="packaging"
     */
    private List<String> packaging;

    /*
     * Counter for the statement count
     */
    private int nodeCreateCount = 0;

    /*
     * Counter for the relation count
     */
    private int relationCreateCount = 0;

    /**
     * Create a Neo4j database and fill it with the dependencies and the relations found.
     *
     * @throws MojoExecutionException
     */
    public void execute()
            throws MojoExecutionException {

        if (getLog().isDebugEnabled()) {
            getLog().debug("Starting " + this.getClass().getName());
        }


        setup();

        try {
            for (ArtifactRepository remoteRepository : remoteRepositories) {
                Map<String, ArtifactInfoGroup> stringArtifactInfoGroupMap;

                stringArtifactInfoGroupMap = repositorySearcher.searchIndexGrouped(getGroupFilterPatterns(), remoteRepository, packaging);

                Set<String> groupKeySet = stringArtifactInfoGroupMap.keySet();
                for (String key : groupKeySet) {
                    ArtifactInfoGroup artifactInfoGroup = stringArtifactInfoGroupMap.get(key);
                    TreeSet<ArtifactInfo> sortedArtifactInfoTreeSet = new TreeSet<ArtifactInfo>(new VersionComparator());
                    sortedArtifactInfoTreeSet.addAll(artifactInfoGroup.getArtifactInfos());
                    if (useLatestOnly) {
                        ArtifactInfo source = sortedArtifactInfoTreeSet.last();
                        processArtifactInfo(source);
                    } else {
                        for (ArtifactInfo artifactInfo : sortedArtifactInfoTreeSet) {
                            processArtifactInfo(artifactInfo);
                        }
                    }
                }
            }

            getLog().info("Inserted " + nodeCreateCount + " elements in the Database");
            getLog().info("Created " + relationCreateCount + " relations in the Database");

        } catch (IOException e) {
            throw new MojoExecutionException("Error communicating", e);
        } catch (SettingsBuildingException e) {
            throw new MojoExecutionException("Error parsing settings", e);
        } catch (UnsupportedExistingLuceneIndexException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } finally {
            tearDown();
        }
    }

    /**
     * Initialize the environment required for processing.
     */
    protected void setup() throws MojoExecutionException {
        super.setup();

        this.nodeProcessor = new DependencyNodeProcessorImpl(getDatabase(), getSearcher(), getLog());
    }

    /**
     * process/parse a artifact returned from the nexus query.
     * the project and its found dependencies are added to the database
     *
     * @param source the found artifact
     * @throws SettingsBuildingException Unable to parse the settings file
     */
    private void processArtifactInfo(final ArtifactInfo source) throws SettingsBuildingException {

        try {
            MavenProject mavenProject = artifactInfo2MavenProject(source);
            Dependency project = artifactInfo2Dependency(source);

            getLog().info("Processing: " + project);
            List<Dependency> dependencyList = mavenProject.getDependencies();
            List<Dependency> filtered = getIncludeFilter().filter(dependencyList);
            if (filtered.size() > 0) {
                nodeCreateCount += nodeProcessor.addArtifact(project);
            } else {
                getLog().info("No dependencies for inclusion selected");
            }

            for (Dependency dependency : filtered) {
                getLog().info("Adding dependency: " + dependency);
                nodeCreateCount += nodeProcessor.addArtifact(dependency);
                relationCreateCount += nodeProcessor.addRelation(project, dependency);

            }
        } catch (ProjectBuildingException e) {
            getLog().info("Error building project " + source);
        }
    }

    /**
     * The Database only works with {@link org.apache.maven.model.Dependency} objects. Transform the project to
     * a dependency object
     *
     * @param project The project being parsed
     * @return {@link org.apache.maven.model.Dependency} object
     */

    private Dependency artifactInfo2Dependency(final ArtifactInfo project) {
        Dependency dependency = new Dependency();
        dependency.setArtifactId(project.artifactId);
        dependency.setGroupId(project.groupId);
        dependency.setClassifier(project.classifier);
        dependency.setScope(null);
        dependency.setType(project.fextension);
        dependency.setVersion(project.version);

        return dependency;
    }

}
