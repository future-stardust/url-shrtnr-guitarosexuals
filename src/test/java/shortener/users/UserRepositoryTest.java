package shortener.users;


import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.Database;
import shortener.database.entities.User;
import shortener.database.tables.UserTable;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;

@MicronautTest
public class UserRepositoryTest {

  @Inject
  UserRepository testable;

  @Inject
  Database db;

  @MockBean(Database.class)
  public Database mockDb() {
    return Mockito.mock(Database.class);
  }


  @Test
  void searchSuccessfullyReturnsRecords() {

    var records =
        Arrays.asList(new User(1L, "test@email.com", "pa$$word"),
            new User(2L, "test2@email.com", "pa$$word"));

    Mockito.when(db.search(Mockito.any(UserTable.class)))
        .thenReturn(records);

    Assertions.assertThat(testable.search()).isEqualTo(records);
  }

  @Test
  void getSuccessfullyReturnsRecordIfFound() {
    var record = new User(1L, "test@email.com", "pa$$word");

    Mockito.when(db.get(Mockito.any(UserTable.class), Mockito.eq(1L)))
        .thenReturn(record);

    Assertions.assertThat(testable.get(1L)).isEqualTo(record);
  }

  @Test
  void getThrowsIfRecordNotFound() {
    Mockito.when(db.get(Mockito.any(UserTable.class), Mockito.any()))
        .thenThrow(new NotFound("users", 1L));

    Assertions.assertThatThrownBy(() -> testable.get(1L)).isInstanceOf(NotFound.class);
  }

  @Test
  void getByEmailSuccessfullyReturnsRecordIfFound() {
    var testUser = new User(1L, "test@email.com", "pa$$word");

    var records =
        Arrays.asList(testUser,
            new User(2L, "test2@email.com", "pa$$word"));

    Mockito.when(db.search(Mockito.any(UserTable.class), Mockito.any(), Mockito.anyLong()))
        .thenReturn(records);

    Assertions.assertThat(testable.getByEmail("test@email.com")).isEqualTo(testUser);
  }

  @Test
  void getByEmailThrowsIfRecordNotFound() {
    Mockito.when(db.search(Mockito.any(UserTable.class), Mockito.any(), Mockito.anyLong()))
        .thenReturn(Collections.emptyList());

    Assertions.assertThatThrownBy(() -> testable.getByEmail("test@email.com"))
        .isInstanceOf(NotFound.class);
  }

  @Test
  void createSuccessfullyCreatesRecord() {
    var record = new User(1L, "test@email.com", "pa$$word");

    Mockito.when(db.create(Mockito.any(UserTable.class), Mockito.any()))
        .thenReturn(record);

    Assertions.assertThat(testable.create(record)).isEqualTo(record);
  }

  @Test
  void createThrowsIfSuchRecordExists() {
    var record = new User(1L, "test@email.com", "pa$$word");

    Mockito.when(db.create(Mockito.any(UserTable.class), Mockito.any()))
        .thenThrow(new UniqueViolation("users"));

    Assertions.assertThatThrownBy(() -> testable.create(record))
        .isInstanceOf(UniqueViolation.class);
  }

  @Test
  void deleteSuccessfullyRemovesRecord() {
    var record = new User(1L, "test@email.com", "pa$$word");

    Mockito.when(db.delete(Mockito.any(UserTable.class), Mockito.any()))
        .thenReturn(record);

    Assertions.assertThat(testable.delete(1L)).isEqualTo(record);
  }

  @Test
  void deleteThrowsIfRecordNotFound() {
    Mockito.when(db.delete(Mockito.any(UserTable.class), Mockito.any()))
        .thenThrow(new NotFound("users", 1L));

    Assertions.assertThatThrownBy(() -> testable.delete(1L)).isInstanceOf(NotFound.class);
  }
}
