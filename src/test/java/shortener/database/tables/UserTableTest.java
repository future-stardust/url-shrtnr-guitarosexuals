package shortener.database.tables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortener.TestUtils;
import shortener.database.entities.User;


public class UserTableTest {

  private static final String TEST_ROOT_DIRECTORY = "table-test-db";

  private final UserTable table = new UserTable(Path.of(TEST_ROOT_DIRECTORY));

  @AfterAll
  static void purgeRootDirectory() {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
  }

  @BeforeEach
  void setupRootDirectory() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    UserTable.init(Path.of(TEST_ROOT_DIRECTORY));
  }

  @Test
  void initCorrectlyCreatesNeededFiles() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    UserTable.init(Path.of(TEST_ROOT_DIRECTORY));

    // Check the filesystem
    Assertions.assertThat(Files.exists(Path.of(TEST_ROOT_DIRECTORY, table.getTableName())))
        .isTrue();
  }

  @Test
  void prepareRecordForCreationThrowsIfSimilarRecordExists() throws IOException {
    Files.write(table.getWritableFilePath(), "1|test@email.com|another-password\n".getBytes(),
        StandardOpenOption.APPEND);

    Assertions.assertThatThrownBy(() -> {
      table.prepareRecordForCreation(new User(null, "test@email.com", "pa$$word"));
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void serializeWorksCorrectly() {
    Assertions.assertThat(table.serialize(new User(1L, "test@email.com", "pa$$word")))
        .isEqualTo("1|test@email.com|pa$$word");
  }

  @Test
  void deserializeWorksCorrectly() {
    Assertions.assertThat(table.deserialize("1|test@email.com|pa$$word"))
        .isEqualTo(new User(1L, "test@email.com", "pa$$word"));
  }

}
