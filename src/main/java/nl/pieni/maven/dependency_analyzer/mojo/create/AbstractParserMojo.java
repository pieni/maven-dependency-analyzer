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

package nl.pieni.maven.dependency_analyzer.mojo.create;

import nl.pieni.maven.dependency_analyzer.filter.DependencyIncludeFilter;
import nl.pieni.maven.dependency_analyzer.mojo.AbstractAnalyzeMojo;
import nl.pieni.maven.dependency_analyzer.repository.remote.RemoteRepositorySearcher;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.VersionRange;
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
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManager;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.updater.IndexUpdater;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 5-1-11
 * Time: 23:04
 * To change this template use File | Settings | File Templates.
 */
abstract public class AbstractParserMojo extends AbstractAnalyzeMojo {

    /**
     * The nexus indexer
     *
     * @component
     */
    protected NexusIndexer indexer;

    /**
     * Updater for Nexus index (download)
     *
     * @component
     */
    protected IndexUpdater indexUpdater;

    /**
     * used to for creation of the MavenProject
     *
     * @component
     */
    protected SettingsBuilder settingsBuilder;

    /**
     * Location of the local repository
     *
     * @parameter default-value="${localRepository}"
     */
    protected ArtifactRepository localRepository;

    /**
     * Maven builder for Projects
     *
     * @component
     */
    protected ProjectBuilder projectBuilder;

    /**
     * List of all defined remote repositories from settings.xml
     *
     * @parameter default-value="${project.remoteArtifactRepositories}"
     */
    protected List<ArtifactRepository> remoteRepositories;

    /**
     * Allow the snapshot repositories defined in settings.xml to be used in the search.
     *
     * @parameter property="allowSnapshots" default-value="true"
     */
    protected Boolean allowSnapshots;

    /**
     * Select the latest released version of the artifacts found.
     *
     * @parameter property="useLatestOnly" default-value="true"
     */
    protected Boolean useLatestOnly;

    /**
     * Folder to store the DB in
     *
     * @parameter property="indexDirectory" default-value="${project.build.directory}"
     */
    protected File indexDirectory;

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


    /*
    * Filter for the inclusions
    */
    private DependencyIncludeFilter includeFilter;



    /*
     * Required to process the pom file
     */
    protected ProjectBuildingRequest buildRequest;

    protected void setup() throws MojoExecutionException {
        super.setup();
        this.repositorySearcher = new RemoteRepositorySearcher(indexer, indexUpdater, getLog(), indexDirectory, allowSnapshots);
        this.buildRequest = makeBuildingRequest();
        this.includeFilter = new DependencyIncludeFilter(includeFilterPatterns);
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
    MavenProject artifactInfo2MavenProject(ArtifactInfo artifactInfo) throws ProjectBuildingException, SettingsBuildingException {
        VersionRange versionRange = VersionRange.createFromVersion(artifactInfo.version);
        Artifact parentArtifact = new DefaultArtifact(artifactInfo.groupId, artifactInfo.artifactId, versionRange, "compile", "pom", null, new DefaultArtifactHandler());
        ProjectBuildingResult buildingResult = projectBuilder.build(parentArtifact, this.buildRequest);
        return buildingResult.getProject();
    }

    /**
     * Setup the context for the creation of a {@link org.apache.maven.project.MavenProject}.
     *
     * @return Building context
     * @throws MojoExecutionException Unable to parse the settings file.
     * @throws org.apache.maven.plugin.MojoExecutionException
     *                                in case of error
     */
    private ProjectBuildingRequest makeBuildingRequest() throws MojoExecutionException {

        try {
            if (buildRequest == null) {
                buildRequest = new DefaultProjectBuildingRequest();

                SettingsBuildingResult settingsBuildingResult = settingsBuilder.build(new DefaultSettingsBuildingRequest());
                Settings settings = settingsBuildingResult.getEffectiveSettings();
                buildRequest.setActiveProfileIds(settings.getActiveProfiles());
                buildRequest.setLocalRepository(localRepository);
                MavenRepositorySystemSession mrs = new MavenRepositorySystemSession();
                mrs.setLocalRepositoryManager(new SimpleLocalRepositoryManager(localRepository.getBasedir()));
                buildRequest.setRepositorySession(mrs);
                buildRequest.setRemoteRepositories(remoteRepositories);
            }
            return buildRequest;
        } catch (SettingsBuildingException e) {
            throw new MojoExecutionException("Unable to create building request", e);
        }
    }

    public List<String> getGroupFilterPatterns() {
        return groupFilterPatterns;
    }

    public DependencyIncludeFilter getIncludeFilter() {
        return includeFilter;
    }
}
