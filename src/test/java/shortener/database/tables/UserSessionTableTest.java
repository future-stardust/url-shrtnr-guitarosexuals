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
import shortener.database.entities.UserSession;
import shortener.exceptions.database.UniqueViolation;


public class UserSessionTableTest {

  private static final String TEST_ROOT_DIRECTORY = "table-test-db";

  private final UserSessionTable table = new UserSessionTable(Path.of(TEST_ROOT_DIRECTORY));

  @AfterAll
  static void purgeRootDirectory() {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
  }

  @BeforeEach
  void setupRootDirectory() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    UserSessionTable.init(Path.of(TEST_ROOT_DIRECTORY));
  }

  @Test
  void initCorrectlyCreatesNeededFiles() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    UserSessionTable.init(Path.of(TEST_ROOT_DIRECTORY));

    // Check the filesystem
    Assertions.assertThat(Files.exists(Path.of(TEST_ROOT_DIRECTORY, table.getTableName())))
        .isTrue();
  }

  @Test
  void prepareRecordForCreationThrowsIfSimilarRecordExists() throws IOException {
    Files.write(table.getWritableFilePath(), "1|token-token\n".getBytes(),
        StandardOpenOption.APPEND);

    Assertions.assertThatThrownBy(() -> {
      table.prepareRecordForCreation(new UserSession(1L, "another-token-token"));
    }).isInstanceOf(UniqueViolation.class);
  }

  @Test
  void serializeWorksCorrectly() {
    Assertions.assertThat(table.serialize(new UserSession(1L, "token-token")))
        .isEqualTo("1|token-token");
  }

  @Test
  void deserializeWorksCorrectly() {
    Assertions.assertThat(table.deserialize("1|token-token"))
        .isEqualTo(new UserSession(1L, "token-token"));
  }

}
