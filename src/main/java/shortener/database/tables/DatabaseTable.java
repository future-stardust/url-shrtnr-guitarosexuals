package shortener.database.tables;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Database table interface.
 *
 * @param <EntityT>     Table entity type.
 * @param <PrimaryKeyT> Table primary key type.
 */
public interface DatabaseTable<EntityT, PrimaryKeyT> {

  /**
   * Returns table name.
   *
   * @return Database table name.
   */
  String getTableName();

  /**
   * Returns a unique primary key depending on db state or provided `recordToCreate`.
   *
   * <p>Run other unique fields checks here (e.g. check here if User.email is unique).
   *
   * @param recordToCreate A record to save.
   * @return Unique primary key to identify the passed `recordToCreate` by.
   * @throws IllegalArgumentException Should be thrown if any uniqueness checks fail.
   * @throws IOException              Should be thrown if failed to calculate a key depending
   *                                  on the db state.
   */
  EntityT prepareRecordForCreation(EntityT recordToCreate)
      throws IllegalArgumentException, IOException;

  /**
   * Returns a path to a file.
   *
   * @return Path to a file to write to.
   */
  Path getWritableFilePath();

  /**
   * Returns a stream of serialized records.
   *
   * @return Stream of serialized table records.
   */
  Stream<String> readTable() throws IOException;

  /**
   * Serializes a record to a csv-string.
   *
   * @return csv-string.
   */
  String serialize(EntityT record);

  /**
   * Creates a new record of a serialized string.
   *
   * @param serialized A csv-serialized record.
   * @return Deserialized record.
   */
  EntityT deserialize(String serialized);

}
