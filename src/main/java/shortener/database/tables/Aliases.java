package shortener.database.tables;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortener.database.entities.Alias;

/**
 * Aliases.
 */
public class Aliases {

  private static final Logger logger = LoggerFactory.getLogger(Aliases.class);

  private static final int[] asciiCodes = {
    // 0 - 9
    48, 49, 50, 51, 52, 53, 54, 55, 56, 57,
    // A - Z
    65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88,
    89, 90,
    // _
    95,
    // a - z
    97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116,
    117, 118, 119, 120, 121, 122
    };

  /**
   * Creates 63 files for storing records in a index way.
   */
  public static void init(File localStoragePath) {
    for (int code : asciiCodes) {
      File file = new File(localStoragePath.toString(), code + ".bin");
      try {
        file.createNewFile();
      } catch (IOException exc) {
        logger.error(String.format("Error on creation of %s\n%s", file.toString(), exc.toString()));
      }
    }
  }

  public static void insert(Alias record) {
    // TODO: insert a single record
  }

  // TODO: fill other methods
  // TODO: probably add interface Table for all these tables
}
