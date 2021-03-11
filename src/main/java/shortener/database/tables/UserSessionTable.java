package shortener.database.tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.inject.Singleton;
import shortener.database.entities.UserSession;

/**
 * UserSession database table implementation.
 */
@Singleton
public class UserSessionTable implements DatabaseTable<UserSession, Long> {

  private static final String TABLE_NAME = "usersessions";

  private final Path filePath;


  public UserSessionTable(Path rootPath) {
    filePath = rootPath.resolve(TABLE_NAME);
  }


  /**
   * Checks if files needed for table to function properly exists and creates them if needed.
   *
   * @param rootPath Path to the root directory.
   * @throws IOException Filesystem error.
   */
  public static void init(Path rootPath) throws IOException {
    Path filePath = rootPath.resolve(TABLE_NAME);

    if (!Files.exists(filePath)) {
      Files.createFile(filePath);
    }
  }


  @Override
  public String getTableName() {
    return TABLE_NAME;
  }


  @Override
  public UserSession prepareRecordForCreation(UserSession recordToCreate)
      throws IllegalArgumentException, IOException {
    boolean sessionForSimilarUserExists =
        readTable().parallel()
            .anyMatch(line -> deserialize(line).userId().equals(recordToCreate.userId()));

    if (sessionForSimilarUserExists) {
      throw new IllegalArgumentException("A session for the provided user already exists");
    }

    return recordToCreate;
  }


  @Override
  public Path getWritableFilePath() {
    // TODO: Add an overloaded method which would accept a pk as a hint for file selection.
    return filePath;
  }


  @Override
  public Stream<String> readTable() throws IOException {
    // TODO: Add an overloaded method which would accept a pk as a hint for file selection.
    return Files.lines(filePath);
  }


  @Override
  public String serialize(UserSession record) {
    return record.userId() + "|" + record.token();
  }


  @Override
  public UserSession deserialize(String serialized) {
    String[] fields = serialized.split("\\|");

    return new UserSession(Long.parseLong(fields[0], 10), fields[1]);
  }
}
