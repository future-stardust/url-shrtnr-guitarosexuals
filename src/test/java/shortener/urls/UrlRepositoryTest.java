package shortener.urls;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.Arrays;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.Database;
import shortener.database.entities.Alias;
import shortener.database.tables.AliasTable;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;

@MicronautTest
public class UrlRepositoryTest {

  @Inject
  Database db;

  @Inject
  UrlRepository testable;

  @MockBean(Database.class)
  public Database mockDb() {
    return Mockito.mock(Database.class);
  }

  @Test
  void searchSuccessfullyReturnsRecords() {

    var records =
        Arrays.asList(new Alias("test", "https://example.com", 1L),
            new Alias("second-alias", "https://example.com", 1L));

    Mockito.when(db.search(Mockito.any(AliasTable.class)))
        .thenReturn(records);

    Assertions.assertThat(testable.search()).isEqualTo(records);
  }

  @Test
  void searchByUserIdSuccessfullyReturnsRecords() {

    var records =
        Arrays.asList(new Alias("test", "https://example.com", 1L),
            new Alias("second-alias", "https://example.com", 1L));

    Mockito.when(db.search(Mockito.any(AliasTable.class), Mockito.any()))
        .thenReturn(records);

    Assertions.assertThat(testable.searchByUserId(1L)).hasSize(2);
  }

  @Test
  void getSuccessfullyReturnsRecordIfFound() {
    var record = new Alias("test", "https://example.com", 1L);

    Mockito.when(db.get(Mockito.any(AliasTable.class), Mockito.eq("test")))
        .thenReturn(record);

    Assertions.assertThat(testable.get("test")).isEqualTo(record);
  }

  @Test
  void getThrowsIfRecordNotFound() {
    Mockito.when(db.get(Mockito.any(AliasTable.class), Mockito.any()))
        .thenThrow(new NotFound("aliases", "test"));

    Assertions.assertThatThrownBy(() -> testable.get("test")).isInstanceOf(NotFound.class);
  }

  @Test
  void createSuccessfullyCreatesRecord() {
    var record = new Alias("test", "https://example.com", 1L);

    Mockito.when(db.create(Mockito.any(AliasTable.class), Mockito.any()))
        .thenReturn(record);

    Assertions.assertThat(testable.create(record)).isEqualTo(record);
  }

  @Test
  void createThrowsIfSuchRecordExists() {
    var record = new Alias("test", "https://example.com", 1L);

    Mockito.when(db.create(Mockito.any(AliasTable.class), Mockito.any()))
        .thenThrow(new UniqueViolation("aliases"));

    Assertions.assertThatThrownBy(() -> testable.create(record))
        .isInstanceOf(UniqueViolation.class);
  }

  @Test
  void createSuccessfullyCreatesRecordWithGeneratedAlias() {
    var record = new Alias("generated", "https://example.com", 1L);

    Mockito.when(db.get(Mockito.any(AliasTable.class), Mockito.any()))
        .thenThrow(new NotFound("aliases", "generated"));

    Mockito.when(db.create(Mockito.any(AliasTable.class), Mockito.any()))
        .thenReturn(record);

    Assertions.assertThat(testable.createRandomAlias("https://example.com", 1L)).isEqualTo(record);
  }

  @Test
  void deleteSuccessfullyRemovesRecord() {
    var record = new Alias("test", "https://example.com", 1L);

    Mockito.when(db.delete(Mockito.any(AliasTable.class), Mockito.any()))
        .thenReturn(record);

    Assertions.assertThat(testable.delete("test")).isEqualTo(record);
  }

  @Test
  void deleteThrowsIfRecordNotFound() {
    Mockito.when(db.delete(Mockito.any(AliasTable.class), Mockito.any()))
        .thenThrow(new NotFound("aliases", "test"));

    Assertions.assertThatThrownBy(() -> testable.delete("test")).isInstanceOf(NotFound.class);
  }
}
