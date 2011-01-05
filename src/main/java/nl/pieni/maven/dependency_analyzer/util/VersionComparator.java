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

package nl.pieni.maven.dependency_analyzer.util;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.sonatype.nexus.index.ArtifactInfo;

import java.util.Comparator;

/**
 * Compares versions of an ArtifactInfo object.
 */
public class VersionComparator implements Comparator<ArtifactInfo> {

    /**
     * @inheritDoc
     */
    @Override
    public int compare( final ArtifactInfo f1,  final ArtifactInfo f2) {

        ComparableVersion cv1 = new ComparableVersion(f1.version);
        ComparableVersion cv2 = new ComparableVersion(f2.version);

        return cv1.compareTo(cv2);
    }
}