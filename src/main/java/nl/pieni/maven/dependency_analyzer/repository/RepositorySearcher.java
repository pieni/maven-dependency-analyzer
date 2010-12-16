package nl.pieni.maven.dependency_analyzer.repository;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface to the repository searcher
 */
public interface RepositorySearcher {
    /**
     * Search the repo for  the patters provided
     * @param groupPatterns the pattern
     * @param repository the repo
     * @param packaging the packaging
     * @return Found artifacts grouped per GA keys
     * @throws IOException error communicating
     * @throws UnsupportedExistingLuceneIndexException should no happen
     */
    Map<String, ArtifactInfoGroup> searchIndexGrouped(List<String> groupPatterns, ArtifactRepository repository, List<String> packaging) throws IOException, UnsupportedExistingLuceneIndexException;
}
