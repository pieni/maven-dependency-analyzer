package nl.pieni.maven.dependency_analyzer.node;

import org.apache.maven.model.Dependency;

/**
 * Created by IntelliJ IDEA.
 * User: pieter
 * Date: 11-12-10
 * Time: 19:48
 * To change this template use File | Settings | File Templates.
 */
public interface GroupNode {
    void setDependency(Dependency dependency);
    String getGroupId();
}
