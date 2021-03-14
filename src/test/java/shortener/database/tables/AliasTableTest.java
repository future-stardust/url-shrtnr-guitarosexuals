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
import shortener.database.entities.Alias;
import shortener.exceptions.database.UniqueViolation;


public class AliasTableTest {

  private static final String TEST_ROOT_DIRECTORY = "table-test-db";

  private final AliasTable table = new AliasTable(Path.of(TEST_ROOT_DIRECTORY));

  @AfterAll
  static void purgeRootDirectory() {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
  }

  @BeforeEach
  void setupRootDirectory() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    AliasTable.init(Path.of(TEST_ROOT_DIRECTORY));
  }

  @Test
  void initCorrectlyCreatesNeededFiles() throws IOException {
    TestUtils.purgeDirectory(new File(TEST_ROOT_DIRECTORY));
    Files.createDirectory(Path.of(TEST_ROOT_DIRECTORY));

    AliasTable.init(Path.of(TEST_ROOT_DIRECTORY));

    // Check the filesystem
    Assertions.assertThat(Files.exists(Path.of(TEST_ROOT_DIRECTORY, table.getTableName())))
        .isTrue();
  }

  @Test
  void prepareRecordForCreationThrowsIfSimilarAliasExists() throws IOException {
    Files.write(table.getWritableFilePath(), "test|https://example.com|1\n".getBytes(),
        StandardOpenOption.APPEND);

    Assertions.assertThatThrownBy(() -> {
      table.prepareRecordForCreation(new Alias("test", "https://example.com", 1L));
    }).isInstanceOf(UniqueViolation.class);
  }

  @Test
  void serializeWorksCorrectly() {
    Assertions.assertThat(table.serialize(new Alias("al", "https://example.com", 1L)))
        .isEqualTo("al|https://example.com|1");
  }

  @Test
  void deserializeWorksCorrectly() {
    Assertions.assertThat(table.deserialize("al|https://example.com|1"))
        .isEqualTo(new Alias("al", "https://example.com", 1L));
  }

}
