package shortener.database.tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.inject.Singleton;
import shortener.database.entities.UserSession;
import shortener.exceptions.database.UniqueViolation;

/**
 * UserSession database table implementation.
 */
@Singleton
public class UserSessionTable implements DatabaseTable<UserSession, String> {

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
      throws UniqueViolation, IOException {
    boolean sessionForSimilarUserExists =
        readTable().parallel()
            .anyMatch(line -> deserialize(line).token().equals(recordToCreate.token()));

    if (sessionForSimilarUserExists) {
      throw new UniqueViolation(TABLE_NAME);
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
    return Files.lines(filePath).filter(line -> line != null && !line.isBlank());
  }


  @Override
  public String serialize(UserSession record) {
    return record.token() + "|" + record.userId();
  }


  @Override
  public UserSession deserialize(String serialized) {
    String[] fields = serialized.split("\\|");

    return new UserSession(Long.parseLong(fields[1], 10), fields[0]);
  }
}
