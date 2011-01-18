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

package nl.pieni.maven.dependency_analyzer.test_helpers;


import org.apache.maven.plugin.logging.Log;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 15-1-11
 * Time: 21:37
 * To change this template use File | Settings | File Templates.
 */
public class SimpleLogger implements Log {
    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(CharSequence content) {
        System.out.println("debug = " + content);
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
        System.out.println("debug = " + content);
        System.out.println("debug:error = " + error);
    }

    @Override
    public void debug(Throwable error) {
        System.out.println("debug:error = " + error);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(CharSequence content) {
        System.out.println("info = " + content);
    }

    @Override
    public void info(CharSequence content, Throwable error) {
        System.out.println("info = " + content);
        System.out.println("info:error = " + error);
    }

    @Override
    public void info(Throwable error) {
        System.out.println("info:error = " + error);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(CharSequence content) {
        System.out.println("warn = " + content);
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
        System.out.println("warn = " + content);
        System.out.println("warn:error = " + error);
    }

    @Override
    public void warn(Throwable error) {
        System.out.println("warn:error = " + error);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(CharSequence content) {
        System.out.println("error = " + content);
    }

    @Override
    public void error(CharSequence content, Throwable error) {
        System.out.println("error = " + content);
        System.out.println("error:error = " + error);
    }

    @Override
    public void error(Throwable error) {
        System.out.println("error:error = " + error);
    }
}
