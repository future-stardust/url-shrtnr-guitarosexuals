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
import shortener.database.entities.UserAlias;


public class UserAliasTableTest {

  private static final String TEST_ROOT_DIRECTORY = "table-test-db";

  private final UserAliasTable table = new UserAliasTable(Path.of(TEST_ROOT_DIRECTORY));

  @AfterAll
  static void purgeRootDirectory() {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
  }

  @BeforeEach
  void setupRootDirectory() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    UserAliasTable.init(Path.of(TEST_ROOT_DIRECTORY));
  }

  @Test
  void initCorrectlyCreatesNeededFiles() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    UserAliasTable.init(Path.of(TEST_ROOT_DIRECTORY));

    // Check the filesystem
    Assertions.assertThat(Files.exists(Path.of(TEST_ROOT_DIRECTORY, table.getTableName())))
        .isTrue();
  }

  @Test
  void prepareRecordForCreationThrowsIfSimilarAliasExists() throws IOException {
    Files.write(table.getWritableFilePath(), "test|1\n".getBytes(),
        StandardOpenOption.APPEND);

    Assertions.assertThatThrownBy(() -> {
      table.prepareRecordForCreation(new UserAlias(1L, "test"));
    }).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void serializeWorksCorrectly() {
    Assertions.assertThat(table.serialize(new UserAlias(1L, "al")))
        .isEqualTo("al|1");
  }

  @Test
  void deserializeWorksCorrectly() {
    Assertions.assertThat(table.deserialize("al|1"))
        .isEqualTo(new UserAlias(1L, "al"));
  }

}