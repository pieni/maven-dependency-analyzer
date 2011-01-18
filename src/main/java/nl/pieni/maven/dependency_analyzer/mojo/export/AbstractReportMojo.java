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

package nl.pieni.maven.dependency_analyzer.mojo.export;

import nl.pieni.maven.dependency_analyzer.mojo.AbstractAnalyzeMojo;
import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.StringTokenizer;

/**
 * Abstract base class for report mojo's
 */
public abstract class AbstractReportMojo extends AbstractAnalyzeMojo {

    /**
     * Convert the string (groupId:artifactId) to a {@link org.apache.maven.model.Dependency} object.
     * @param reportArtifact The G:A string
     * @return a dependency object
     */
    protected Dependency stringToGaDependency(String reportArtifact) {
        StringTokenizer strTok = new StringTokenizer(reportArtifact, ":");
        if (strTok.countTokens() != 2) {
            getLog().error("reportArtifacts string: " + reportArtifact + " is not valid, requires groupId:artifactId");
            return null;
        }
        String groupId = strTok.nextToken();
        String artifactId = strTok.nextToken();
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        return dependency;
    }
}
