package shortener.usersessions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.Database;
import shortener.database.entities.UserSession;
import shortener.database.tables.UserSessionTable;
import shortener.exceptions.database.NotFound;
import shortener.users.UserSessionRepository;

@MicronautTest
public class UserSessionRepositoryTest {

  @Inject
  UserSessionRepository testable;

  @Inject
  Database db;

  @MockBean(Database.class)
  public Database mockDb() {
    return Mockito.mock(Database.class);
  }

  @Test
  void getExistingSessionTest() {
    Mockito.when(db.get(Mockito.any(UserSessionTable.class), Mockito.any()))
        .thenReturn(new UserSession(1L, "token1"));

    assertThat(testable.get("token1")).isNotNull();
  }

  @Test
  void getNonExistingSessionTest() {
    Mockito.when(db.get(Mockito.any(UserSessionTable.class), Mockito.any()))
        .thenThrow(new NotFound("usersessions", "non-existing-token"));

    assertThatThrownBy(() -> testable.get("non-existing-token")).isInstanceOf(NotFound.class);
  }

  @Test
  void createNewSessionTest() {
    UserSession userSession = new UserSession(1L, "new-token");

    Mockito.when(db.create(Mockito.any(UserSessionTable.class), Mockito.any()))
        .thenReturn(userSession);

    assertThat(testable.create(userSession)).isEqualTo(userSession);
  }

  @Test
  void deleteExistingSession() {
    Mockito.when(db.delete(Mockito.any(UserSessionTable.class), Mockito.any()))
        .thenReturn(new UserSession(1L, "token1"));


    assertThat(testable.delete("token1")).isNotNull();
  }

  @Test
  void deleteNonExistingSession() {
    Mockito.when(db.delete(Mockito.any(UserSessionTable.class), Mockito.any()))
        .thenThrow(new NotFound("usersessions", "non-existing-token"));

    assertThatThrownBy(() -> testable.delete("non-existing-token")).isInstanceOf(NotFound.class);
  }
}
