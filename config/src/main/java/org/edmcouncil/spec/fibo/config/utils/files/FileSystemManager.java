package org.edmcouncil.spec.fibo.config.utils.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class FileSystemManager {

  private static final String WEASEL_HOME_DIR_NAME = ".weasel";
  private static final String WEASEL_ONTOLOGY_FILE_NAME = "AboutFIBOProd.rdf";
  private static final String WEASEL_CONFIG_FILE_NAME = "weasel_config.xml";

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemManager.class);

  public Path getWeaselHomeDir() {
    Path userHomeDir;
    String userHomeProperty = System.getProperty("user.home");
    userHomeDir = Paths.get(userHomeProperty);
    LOG.trace("User home dir is '{}'.", userHomeDir);
    return userHomeDir.resolve(WEASEL_HOME_DIR_NAME);
  }

  private Path createDirIfNotExists(Path dirToCreate) throws IOException {
    if (Files.notExists(dirToCreate)) {
      createDir(dirToCreate);
    }
    return dirToCreate;
  }

  public void createDir(Path dirPath) throws IOException {
    try {
      Files.createDirectory(dirPath);
    } catch (IOException ex) {
      String msg = String.format("Unable to create a dir '%s'.", dirPath);
      throw new IOException(msg, ex);
    }
  }

  public Path getDefaultPathToOntologyFile() throws IOException {
    Path homeDir = getWeaselHomeDir();
    return createDirIfNotExists(homeDir).resolve(WEASEL_ONTOLOGY_FILE_NAME);
  }

  public Path getPathToWeaselConfigFile() throws IOException {
    Path homeDir = getWeaselHomeDir();
    return createDirIfNotExists(homeDir).resolve(WEASEL_CONFIG_FILE_NAME);
  }

  public Path getPathToOntologyFile(String pathString) throws IOException {
    Path path = Paths.get(pathString);
    if (path.isAbsolute()) {
      return path;

    } else {
      Path homeDir = getWeaselHomeDir();
      return createDirIfNotExists(homeDir).resolve(path);
    }
  }

}
