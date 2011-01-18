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

package nl.pieni.maven.dependency_analyzer.neo4j.database;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for database unit testing
 */
public abstract class AbstractDatabaseImplTest {

    private final static String BASE_TEST_LOCATION = System.getProperty("java.io.tmpdir") + "/dependency_analyzer_test/";
    private static File testDirectory;
    public static Log log;
    private static int dependencyCnt = 0;

    private static boolean deleteDirectory(File path) {
        if (path.equals(testDirectory)) {
            System.out.println("Removing directory: " + path.getAbsolutePath());
        }
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (file.delete()) {
                        System.out.println("Unable to delete file = " + file);
                    }
                }
            }
        }
        return (path.delete());
    }

    public static File createTempDirectory()
            throws IOException {
        final File temp;

        File basedir = new File(BASE_TEST_LOCATION);
        if (!basedir.exists()) {
            basedir.mkdir();
        }

        temp = File.createTempFile("test-db", "xx", basedir);
        temp.deleteOnExit();

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        System.out.println("Creating directory: " + temp.getAbsolutePath());
        return (temp);
    }

    static public void beforeBase() throws IOException {
        testDirectory = createTempDirectory();
        log = mock(Log.class);
        when(log.isDebugEnabled()).thenReturn(true);
        when(log.isInfoEnabled()).thenReturn(true);
    }

    static public void afterBase() {
        //deleteDirectory(testDirectory);
    }

    protected static String getDBDirectory() {
        return testDirectory.getAbsolutePath();
    }

    Dependency getDependency() {
        Dependency dependency = new Dependency();
        dependency.setArtifactId("artifactId_" + dependencyCnt);
        dependency.setGroupId("nl.pieni.maven.groupId_" + dependencyCnt);
        dependency.setVersion("1.0");
        dependency.setType("jar");
        dependencyCnt++;
        return dependency;
    }
}
