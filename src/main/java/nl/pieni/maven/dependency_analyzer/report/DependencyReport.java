/*
 * Copyright (c) 2010 Pieter van der Meer (pieter@pieni.nl)
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

package nl.pieni.maven.dependency_analyzer.report;

import nl.pieni.maven.dependency_analyzer.node.ArtifactNode;
import org.apache.maven.model.Dependency;

import java.io.IOException;
import java.io.Writer;

/**
 * Place holder for the data collected for the report.
 */
public interface DependencyReport {

    /**
     * Print the report using the provided writer
     *
     * @param dependency  dependency
     * @param writer     the Writer
     * @throws IOException In case of error
     */
    void createReport(Dependency dependency, Writer writer) throws IOException;

}
