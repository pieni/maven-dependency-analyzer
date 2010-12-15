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

package nl.pieni.maven.dependency_analyzer.mojo;

import nl.pieni.maven.dependency_analyzer.database.DependencyNodeProcessor;
import nl.pieni.maven.dependency_analyzer.filter.DependencyIncludeFilter;
import nl.pieni.maven.dependency_analyzer.neo4j.database.DependencyNodeProcessorImpl;
import nl.pieni.maven.dependency_analyzer.repository.remote.RemoteRepositorySearcher;
import nl.pieni.maven.dependency_analyzer.util.VersionComparator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.updater.IndexUpdater;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Creation of the dependency DB
 *
 * @goal create-db
 * @phase process-sources
 * @requiredProject false
 */
public class DependencyGraphDBMojo
        extends AbstractDependencyMojo {

    /**
     * The nexus indexer
     *
     * @component
     */
    private NexusIndexer indexer;

    /**
     * Updater for Nexus index (download)
     *
     * @component
     */
    private IndexUpdater indexUpdater;

    /**
     * used to for creation of the MavenProject
     *
     * @component
     */
    private SettingsBuilder settingsBuilder;

    /**
     * Location of the local repository
     *
     * @parameter default-value="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * Maven builder for Projects
     *
     * @component
     */
    private ProjectBuilder projectBuilder;

    /**
     * Node database
     */
    protected DependencyNodeProcessor nodeProcessor;

    /**
     * List of all defined remote repositories from settings.xml
     *
     * @parameter default-value="${project.remoteArtifactRepositories}"
     */
    protected List<ArtifactRepository> remoteRepositories;


    /**
     * List of groupId's to retrieve from the repository.
     * A star (*) is appended to the pattern specified
     *
     * @parameter property="groupFilterPatterns"
     */
    private List<String> groupFilterPatterns;

    /**
     * List of groupId:artifactId elements for inclusion in the graph generated.
     * follows syntax from maven-common-artifact-filters (AbstractStrictPatternArtifactFilter)
     *
     * @parameter property="includeFilterPatterns"
     */
    private List<String> includeFilterPatterns;

    /**
     * The type of artifacts to search. remember, the search is performed against the packaging that is defined in the
     * pom file used to create the artifact.
     *
     * @parameter property="packaging"
     */
    private List<String> packaging;

    /**
     * Allow the snapshot repositories defined in settings.xml to be used in the search.
     *
     * @parameter property="allowSnapshots" default-value="true"
     */
    private Boolean allowSnapshots;

    /**
     * Select the latest released version of the artifacts found.
     *
     * @parameter property="useLatestOnly" default-value="true"
     */
    private Boolean useLatestOnly;

    /**
     * Target folder
     *
     * @parameter property="indexDirectory" default-value="${project.build.directory}"
     */
    File indexDirectory;

    /*
     * Filter for the inclusions
     */
    private DependencyIncludeFilter includeFilter;

    private int nodeCreateCount = 0;
    private int relationCreateCount = 0;


    /**
     * Create a Neo4j database and fill it with the dependencies and the relations found.
     *
     * @throws MojoExecutionException
     */
    public void execute()
            throws MojoExecutionException {

        getLog().info("Starting " + this.getClass().getName());

        setup();

        try {
            for (ArtifactRepository remoteRepository : remoteRepositories) {
                Map<String, ArtifactInfoGroup> stringArtifactInfoGroupMap;

                stringArtifactInfoGroupMap = repositorySearcher.searchIndexGrouped(groupFilterPatterns, remoteRepository, packaging);

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
    protected void setup() {
        super.setup();
        includeFilter = new DependencyIncludeFilter(includeFilterPatterns);
        nodeProcessor = new DependencyNodeProcessorImpl(database, getLog());
        repositorySearcher = new RemoteRepositorySearcher(indexer, indexUpdater, getLog(), indexDirectory, allowSnapshots);
    }

    /**
     * process/parse a artifact returned from the nexus query.
     * the project and its found dependencies are added to the database
     *
     * @param source the found artifact
     * @return Number of elements added to the Database
     * @throws SettingsBuildingException Unable to parse the settings file
     */
    private void processArtifactInfo(@NotNull final ArtifactInfo source) throws SettingsBuildingException {

        try {
            MavenProject mavenProject = artifactInfo2MavenProject(source);
            Dependency project = artifactInfo2Dependency(source);

            getLog().info("Processing: " + project);
            List<Dependency> dependencyList = mavenProject.getDependencies();
            List<Dependency> filtered = includeFilter.filter(dependencyList);
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
    @NotNull
    private Dependency artifactInfo2Dependency(@NotNull final ArtifactInfo project) {
        Dependency dependency = new Dependency();
        dependency.setArtifactId(project.artifactId);
        dependency.setGroupId(project.groupId);
        dependency.setClassifier(project.classifier);
        dependency.setScope(null);
        dependency.setType(project.fextension);
        dependency.setVersion(project.version);

        return dependency;
    }

    /**
     * Setup the context for the creation of a {@link org.apache.maven.project.MavenProject}.
     *
     * @return Building context
     * @throws SettingsBuildingException Unable to parse the settings file.
     */
    @NotNull
    private ProjectBuildingRequest makeBuildingRequest() throws SettingsBuildingException {
        ProjectBuildingRequest buildRequest = new DefaultProjectBuildingRequest();

        SettingsBuildingResult settingsBuildingResult = settingsBuilder.build(new DefaultSettingsBuildingRequest());
        Settings settings = settingsBuildingResult.getEffectiveSettings();
        buildRequest.setActiveProfileIds(settings.getActiveProfiles());
        buildRequest.setLocalRepository(localRepository);
        MavenRepositorySystemSession mrs = new MavenRepositorySystemSession();
        mrs.setLocalRepositoryManager(new SimpleLocalRepositoryManager(localRepository.getBasedir()));
        buildRequest.setRepositorySession(mrs);
        buildRequest.setRemoteRepositories(remoteRepositories);
        return buildRequest;
    }

    /**
     * Convert a {|@link ArtifactInfo} object to a Maven project.
     *
     * @param artifactInfo The artifact
     * @return A {@link org.apache.maven.project.MavenProject}
     * @throws org.apache.maven.project.ProjectBuildingException
     *          Error parsing the POM file.
     * @throws org.apache.maven.settings.building.SettingsBuildingException
     *          Unable to parse the settings file
     */
    MavenProject artifactInfo2MavenProject(@NotNull ArtifactInfo artifactInfo) throws ProjectBuildingException, SettingsBuildingException {
        VersionRange versionRange = VersionRange.createFromVersion(artifactInfo.version);
        Artifact parentArtifact = new DefaultArtifact(artifactInfo.groupId, artifactInfo.artifactId, versionRange, "compile", "pom", null, new DefaultArtifactHandler());
        ProjectBuildingResult buildingResult = projectBuilder.build(parentArtifact, makeBuildingRequest());
        return buildingResult.getProject();
    }


}
