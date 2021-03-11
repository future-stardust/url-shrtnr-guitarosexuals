package shortener.database;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import shortener.database.tables.aliases;

/**
 * Local Storage for Database.
 */
public class LocalStorage {

  private static final Path rootPath = Paths.get("./../../../../..").toAbsolutePath();
  private static final String localPath = "data";
  private static final File storageDir = new File(rootPath.toString(), localPath);
  private static final String[] tables = {"aliases", "users", "usersSessions", "usersAliases"};

  private static boolean isInited() {
    return Files.exists(storageDir.toPath());
  }

  /**
   * Inits local data directory.
   */
  private static void init() {
    // create dir itself
    storageDir.mkdir();

    // create dirs for tables
    for (String table : tables) {
      File tableDir = new File(storageDir, table);
      tableDir.mkdir();
    }

    // init all tables' inner content
    aliases.init(storageDir);
    // TODO: call init on all tables
  }

  /**
   * Main routine.
   */
  public static void synchronize() {
    if (!isInited()) {
      init();
    }
  }
}
