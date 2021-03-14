package shortener.database.tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.inject.Singleton;
import shortener.database.entities.Alias;
import shortener.exceptions.database.UniqueViolation;

/**
 * Alias database table implementation.
 */
@Singleton
public class AliasTable implements DatabaseTable<Alias, String> {

  private static final String TABLE_NAME = "aliases";

  private final Path filePath;


  public AliasTable(Path rootPath) {
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
  public Alias prepareRecordForCreation(Alias recordToCreate)
      throws UniqueViolation, IOException {
    boolean aliasWithSimilarAliasExists =
        readTable().parallel()
            .anyMatch(line -> deserialize(line).alias().equals(recordToCreate.alias()));

    if (aliasWithSimilarAliasExists) {
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
  public String serialize(Alias record) {
    return record.alias() + "|" + record.url() + "|" + record.userId() + "|" + record.usages();
  }


  @Override
  public Alias deserialize(String serialized) {
    String[] fields = serialized.split("\\|");

    return new Alias(fields[0], fields[1], Long.parseLong(fields[2], 10),
        Integer.parseInt(fields[3], 10));
  }
}
