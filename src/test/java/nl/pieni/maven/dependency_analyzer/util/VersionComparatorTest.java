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

import org.junit.Test;
import org.sonatype.nexus.index.ArtifactInfo;

import static org.junit.Assert.assertTrue;

/**
 * Version comparison test
 */
public class VersionComparatorTest {

    private final VersionComparator comparator = new VersionComparator();

    @Test
    public void equal() {
        ArtifactInfo artifactInfo1 = new ArtifactInfo("", "", "", "1.0", "");
        assertTrue(0 == comparator.compare(artifactInfo1, artifactInfo1));
    }

    @Test
    public void less() {
        ArtifactInfo artifactInfo1 = new ArtifactInfo("", "", "", "1.0", "");
        ArtifactInfo artifactInfo2 = new ArtifactInfo("", "", "", "2.0", "");
        assertTrue(0 > comparator.compare(artifactInfo1, artifactInfo2));
    }

    @Test
    public void more() {
        ArtifactInfo artifactInfo1 = new ArtifactInfo("", "", "", "1.0", "");
        ArtifactInfo artifactInfo2 = new ArtifactInfo("", "", "", "2.0", "");
        assertTrue(0 < comparator.compare(artifactInfo2, artifactInfo1));
    }
}
