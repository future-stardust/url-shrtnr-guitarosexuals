package shortener.database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortener.TestUtils;
import shortener.database.entities.User;

public class DatabaseTest {

  private static final String TEST_DATABASE_DIRECTORY = "test-db";

  Database db = new Database(TEST_DATABASE_DIRECTORY);

  @AfterAll
  static void purgeDb() {
    TestUtils.purgeDirectory(new File(TEST_DATABASE_DIRECTORY));
  }

  @BeforeEach
  void setupDb() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_DATABASE_DIRECTORY));

    Database.init(TEST_DATABASE_DIRECTORY);
  }

  @Test
  void initCorrectlyCreatesDatabaseDirectory() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_DATABASE_DIRECTORY));

    Database.init(TEST_DATABASE_DIRECTORY);

    // Check the filesystem
    Assertions.assertThat(new File(TEST_DATABASE_DIRECTORY)).exists().isDirectory();
  }

  @Test
  void createSuccessfullyInsertsRecord() throws IOException {
    User record = db.create(db.userTable, new User(null, "test@email.com", "pa$$word"));

    Assertions.assertThat(record).isNotNull();
    Assertions.assertThat(record.id()).isEqualTo(1L);

    // Check the filesystem
    Path tablePath = Path.of(TEST_DATABASE_DIRECTORY, db.userTable.getTableName());

    Assertions.assertThat(Files.lines(tablePath)
        .anyMatch(line -> line.contains(record.id().toString()) && line.contains(record.email())))
        .isTrue();
  }

  @Test
  void createThrowsIfRecordIsNotUnique() throws IOException {
    db.create(db.userTable, new User(null, "same@email.com", "pa$$word"));

    // Try to create a user with same `email` -- unique for a user
    Assertions.assertThatThrownBy(() -> {
      db.create(db.userTable, new User(null, "same@email.com", "pa$$word"));
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void searchSuccessfullyReturnsRecords() throws IOException {
    db.create(db.userTable, new User(null, "1@email.com", "pa$$word"));
    db.create(db.userTable, new User(null, "2@email.com", "pa$$word"));
    db.create(db.userTable, new User(null, "3@email.com", "other-pass"));

    List<User> recordList = db.search(db.userTable);
    Assertions.assertThat(recordList).hasSize(3);

    List<User> recordListFoundWithPredicate =
        db.search(db.userTable, user -> user.password().equals("pa$$word"));
    Assertions.assertThat(recordListFoundWithPredicate).hasSize(2);

    List<User> recordListFoundWithPredicateAndLimit =
        db.search(db.userTable, user -> user.password().equals("pa$$word"), 1);
    Assertions.assertThat(recordListFoundWithPredicateAndLimit).hasSize(1);
  }

  @Test
  void getSuccessfullyRetrievesRecord() throws IOException {
    User createdRecord = db.create(db.userTable, new User(null, "test@email.com", "pa$$word"));

    User record = db.get(db.userTable, createdRecord.id());

    Assertions.assertThat(record).isNotNull().isEqualTo(createdRecord);
  }

  @Test
  void getThrowsIfRecordNotFound() {
    Assertions.assertThatThrownBy(() -> db.get(db.userTable, 1337L))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void deleteSuccessfullyRemovesRecord() throws IOException {
    User createdRecord = db.create(db.userTable, new User(null, "test@email.com", "pa$$word"));

    User deletedRecord = db.delete(db.userTable, createdRecord.id());

    Assertions.assertThat(deletedRecord).isEqualTo(createdRecord);

    // Check filesystem
    Path tablePath = Path.of(TEST_DATABASE_DIRECTORY, db.userTable.getTableName());

    System.out.println(Files.readAllLines(tablePath));

    Assertions.assertThat(Files.lines(tablePath))
        .noneMatch(
            line -> line.contains(deletedRecord.id().toString())
                && line.contains(deletedRecord.email()));
  }

  @Test
  void deleteThrowsIfNoRecordFound() {
    Assertions.assertThatThrownBy(() -> db.delete(db.userTable, 1337L))
        .isInstanceOf(NoSuchElementException.class);
  }

}
