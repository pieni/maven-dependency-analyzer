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
package nl.pieni.maven.dependency_analyzer.report;

import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testing of the {@link Log} wrapping
 */
public class LogWriterTest {
    private static final String LINESEPERATOR = System.getProperty("line.separator");

    /**
     * Sinngle line logging test
     * @throws IOException not tested
     */
    @Test
    public void singleLineTest() throws IOException {
        Log log = mock(Log.class);
        LogWriter writer = new LogWriter(log);
        writer.write("SomeString".toCharArray());
        verify(log).info(matches("SomeString"));
    }

    /**
     * Multi line logging test
     * @throws IOException not tested
     */
    @Test
    public void multiLineTest() throws IOException {
        Log log = mock(Log.class);
        LogWriter writer = new LogWriter(log);
        String str1 = "String1";
        String str2 = "String2";
        String logString = str1 + LINESEPERATOR + str2;
        writer.write(logString.toCharArray());
        verify(log).info(matches(str1));
        verify(log).info(matches(str2));
    }


    /**
     * Test to see if the substring parsing of the {@link LogWriter} works
     * @throws IOException not tested
     */
    @Test
    public void writeCleanBufferTest() throws IOException {
        char[] cbuf = new char[100];
        String tmp = "String";
        char[] tmpBuf = tmp.toCharArray();
        for (int i = 0; i < tmpBuf.length; i++) {
            cbuf[i] = tmpBuf[i];
        }
        Log log = mock(Log.class);
        LogWriter writer = new LogWriter(log);
        writer.write(cbuf, 0, tmp.length());
        verify(log).info(matches(tmp));
    }

    /**
     * Test to see if the substring parsing of the {@link LogWriter} works
     * @throws IOException not tested
     */
    @Test
    public void dirtyBufferTest() throws IOException {
        char[] cbuf = new char[100];
        String tmp = "String";
        char[] tmpBuf = tmp.toCharArray();
        for (int y = 0; y < 3; y++) {
            for (int i = 0; i < tmpBuf.length; i++) {
                cbuf[i] = tmpBuf[i];
            }
        }
        Log log = mock(Log.class);
        LogWriter writer = new LogWriter(log);
        writer.write(cbuf, 0, tmp.length());
        verify(log).info(matches(tmp));
    }

    /**
     * There are methods left blank in the implementation of {@link LogWriter} this test invokes these
     * Just to get the covarage up to par.
     * @throws IOException never thrown
     */
    @Test
    public void blankMethodsTest() throws IOException {
        Log log = mock(Log.class);
        LogWriter writer = new LogWriter(log);
        writer.flush();
        writer.close();


    }

}
