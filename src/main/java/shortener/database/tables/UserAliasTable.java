package shortener.database.tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.inject.Singleton;
import shortener.database.entities.UserAlias;
import shortener.exceptions.database.UniqueViolation;

/**
 * UserAlias database table implementation.
 */
@Singleton
public class UserAliasTable implements DatabaseTable<UserAlias, String> {

  private static final String TABLE_NAME = "useraliases";

  private final Path filePath;


  public UserAliasTable(Path rootPath) {
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
  public UserAlias prepareRecordForCreation(UserAlias recordToCreate)
      throws UniqueViolation, IOException {
    boolean userAliasWithSimilarAliasExists =
        readTable().parallel()
            .anyMatch(line -> deserialize(line).alias().equals(recordToCreate.alias()));

    if (userAliasWithSimilarAliasExists) {
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
    return Files.lines(filePath);
  }


  @Override
  public String serialize(UserAlias record) {
    return record.alias() + "|" + record.userId();
  }


  @Override
  public UserAlias deserialize(String serialized) {
    String[] fields = serialized.split("\\|");

    return new UserAlias(Long.parseLong(fields[1], 10), fields[0]);
  }
}
