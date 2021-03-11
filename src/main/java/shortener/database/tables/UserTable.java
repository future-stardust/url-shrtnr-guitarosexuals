package shortener.database.tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.inject.Singleton;
import shortener.database.entities.User;

/**
 * User database table implementation.
 */
@Singleton
public class UserTable implements DatabaseTable<User, Long> {

  private static final String TABLE_NAME = "users";

  private final Path filePath;


  public UserTable(Path rootPath) {
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
  public User prepareRecordForCreation(User recordToCreate)
      throws IllegalArgumentException, IOException {
    boolean userWithSimilarEmailExists =
        readTable().parallel()
            .anyMatch(line -> deserialize(line).email().equals(recordToCreate.email()));

    if (userWithSimilarEmailExists) {
      throw new IllegalArgumentException("An account with such email already exists");
    }

    Long lastLineId = readTable().reduce((first, second) -> second).map(line -> {
      String[] fields = line.split("\\|");
      return Long.parseLong(fields[0]);
    }).orElse(0L);

    Long primaryKey = lastLineId + 1;

    return new User(primaryKey, recordToCreate.email(), recordToCreate.password());
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
  public String serialize(User record) {
    return record.id() + "|" + record.email() + "|" + record.password();
  }


  @Override
  public User deserialize(String serialized) {
    String[] fields = serialized.split("\\|");

    return new User(Long.parseLong(fields[0], 10), fields[1], fields[2]);
  }
}
