package shortener.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import shortener.database.tables.AliasTable;
import shortener.database.tables.DatabaseTable;
import shortener.database.tables.UserAliasTable;
import shortener.database.tables.UserSessionTable;
import shortener.database.tables.UserTable;

/**
 * Database implementation class.
 */
@Singleton
public class Database {

  private static final String DEFAULT_ROOT_DIRECTORY = "data";

  public UserTable userTable;
  public AliasTable aliasTable;
  public UserAliasTable userAliasTable;
  public UserSessionTable userSessionTable;


  /**
   * Constructs an instance of Database with default root path.
   */
  public Database() {
    this(DEFAULT_ROOT_DIRECTORY);
  }

  /**
   * Constructs an instance of Database using provided `rootDirectory`.
   *
   * @param rootDirectory A string path to the root db directory.
   */
  public Database(String rootDirectory) {
    Path rootPath = Path.of(rootDirectory);

    userTable = new UserTable(rootPath);
    aliasTable = new AliasTable(rootPath);
    userAliasTable = new UserAliasTable(rootPath);
    userSessionTable = new UserSessionTable(rootPath);
  }


  /**
   * Sets up root folder of the database file system using the default root directory.
   *
   * @throws IOException Occurs during the database file system setup.
   */
  public static void init() throws IOException {
    init(DEFAULT_ROOT_DIRECTORY);
  }


  /**
   * Sets up root folder of the database file system.
   *
   * @param rootDirectory String path to the root directory.
   * @throws IOException Occurs during the database file system setup.
   */
  public static void init(String rootDirectory) throws IOException {
    Path rootPath = Path.of(rootDirectory);

    if (Files.exists(rootPath)) {
      if (!Files.isDirectory(rootPath)) {
        throw new IOException("The root path is not a directory!");
      }
    } else {
      Files.createDirectory(rootPath);
    }

    // Init tables
    UserTable.init(rootPath);
    AliasTable.init(rootPath);
    UserAliasTable.init(rootPath);
    UserSessionTable.init(rootPath);
  }


  /**
   * Searches through the database table.
   *
   * @param databaseTable Database table to operate over. (usage: db.search(db.userTable)).
   * @param <EntityT>     Entity type, inherited from the `databaseTable`
   * @return List of all records.
   * @throws IOException File system exception.
   */
  public <EntityT> List<EntityT> search(DatabaseTable<EntityT, ?> databaseTable)
      throws IOException {
    return databaseTable.readTable()
        .parallel()
        .map(databaseTable::deserialize)
        .collect(Collectors.toList());
  }

  /**
   * Searches through the database table and filters records by the provided `predicate`.
   *
   * @param databaseTable Database table to operate over. (usage: db.search(db.userTable)).
   * @param predicate     Predicate lambda to filter records by.
   * @param <EntityT>     Entity type, inherited from the `databaseTable`
   * @return List of all records.
   * @throws IOException File system exception.
   */
  public <EntityT> List<EntityT> search(DatabaseTable<EntityT, ?> databaseTable,
                                        Predicate<EntityT> predicate) throws IOException {
    return databaseTable.readTable()
        .parallel()
        .map(databaseTable::deserialize)
        .filter(predicate)
        .collect(Collectors.toList());
  }

  /**
   * Searches through the database table and filters records by the provided `predicate` and
   * selects `limit` records.
   *
   * @param databaseTable Database table to operate over. (usage: db.search(db.userTable)).
   * @param predicate     Predicate lambda to filter records by.
   * @param limit         Amount of records to select.
   * @param <EntityT>     Entity type, inherited from the `databaseTable`
   * @return List of all records.
   * @throws IOException File system exception.
   */
  public <EntityT> List<EntityT> search(DatabaseTable<EntityT, ?> databaseTable,
                                        Predicate<EntityT> predicate, long limit)
      throws IOException {
    return databaseTable.readTable()
        .parallel()
        .map(databaseTable::deserialize)
        .filter(predicate)
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * Returns a record selected by the provided `pk`.
   *
   * @param databaseTable Database table to operate over. (usage: db.search(db.userTable)).
   * @param pk            Primary key to get a record by.
   * @param <EntityT>     Entity type, inherited from the `databaseTable`
   * @param <PrimaryKeyT> Primary key type, inherited from the `databaseTable`
   * @return Found record.
   * @throws NoSuchElementException Thrown if no element found by the provided `pk`.
   * @throws IOException            File system exception.
   */
  public <EntityT, PrimaryKeyT> EntityT get(DatabaseTable<EntityT, PrimaryKeyT> databaseTable,
                                            PrimaryKeyT pk)
      throws NoSuchElementException, IOException {
    Pattern lineRegex = Pattern.compile("^" + pk + "\\|" + ".*", Pattern.CASE_INSENSITIVE);

    return databaseTable.readTable()
        .filter(line -> lineRegex.matcher(line).matches())
        .findFirst()
        .map(databaseTable::deserialize)
        .orElseThrow(() -> new NoSuchElementException(
            "No \"" + databaseTable.getTableName() + "\" record found by provided pk=\"" + pk
                + "\""));

  }

  /**
   * Inserts a provided `recordToCreate` into the provided `databaseTable` and generates a primary
   * key for it.
   *
   * @param databaseTable  Database table to operate over. (usage: db.search(db.userTable)).
   * @param recordToCreate A record that's inserted into the db table.
   * @param <EntityT>      Entity type, inherited from the `databaseTable`
   * @param <PrimaryKeyT>  Primary key type, inherited from the `databaseTable`
   * @return Created recordToCreate.
   * @throws IllegalArgumentException Thrown if any table field uniqueness check did not pass.
   * @throws IOException              File system exception.
   */
  public <EntityT, PrimaryKeyT> EntityT create(DatabaseTable<EntityT, PrimaryKeyT> databaseTable,
                                               EntityT recordToCreate)
      throws IllegalArgumentException, IOException {

    EntityT recordToSave = databaseTable.prepareRecordForCreation(recordToCreate);

    Files.write(databaseTable.getWritableFilePath(),
        (System.lineSeparator() + databaseTable.serialize(recordToSave)).getBytes(),
        StandardOpenOption.APPEND);

    return recordToSave;
  }

  /**
   * Deletes a record from the `databaseTable` by the provided `pk`.
   *
   * @param databaseTable Database table to operate over. (usage: db.search(db.userTable)).
   * @param pk            Primary key to delete a record by.
   * @param <EntityT>     Entity type, inherited from the `databaseTable`
   * @param <PrimaryKeyT> Primary key type, inherited from the `databaseTable`
   * @return Deleted record.
   * @throws NoSuchElementException Thrown if no element found by the provided `pk`.
   * @throws IOException            File system exception.
   */
  public <EntityT, PrimaryKeyT> EntityT delete(DatabaseTable<EntityT, PrimaryKeyT> databaseTable,
                                               PrimaryKeyT pk)
      throws NoSuchElementException, IOException {

    // Check if the record exists
    final EntityT record = get(databaseTable, pk);

    Pattern lineRegex = Pattern.compile("^" + pk + "\\|" + ".*", Pattern.CASE_INSENSITIVE);

    String modifiedLines =
        databaseTable.readTable().filter(line -> !lineRegex.matcher(line).matches())
            .reduce((acc, line) -> acc + System.lineSeparator() + line).orElse("");

    Files.write(databaseTable.getWritableFilePath(), modifiedLines.getBytes(),
        StandardOpenOption.TRUNCATE_EXISTING);

    return record;
  }

}
