package shortener;

import java.io.File;

/**
 * Custom utilities needed for testing.
 */
public class TestUtils {

  /**
   * Deletes given directory with content.
   *
   * @param directoryToBeDeleted Directory File to be deleted.
   */
  public static void purgeDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        purgeDirectory(file);
      }
    }
    directoryToBeDeleted.delete();
  }

}
