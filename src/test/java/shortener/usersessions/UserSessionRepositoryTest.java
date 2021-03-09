package shortener.usersessions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortener.database.UserSession;
import shortener.users.UserSessionRepository;

@MicronautTest
public class UserSessionRepositoryTest {

  UserSessionRepository userSessionRepository;

  @BeforeEach
  void testDataSetup() {
    userSessionRepository = new UserSessionRepository(new UserSession[]{
        new UserSession(1, "token1"),
        new UserSession(2, "token2")
    });
  }

  @Test
  void getExistingSessionTest() {
    assertThat(userSessionRepository.get("token1")).isNotNull();
  }

  @Test
  void getNonExistingSessionTest() {
    assertThatThrownBy(() -> userSessionRepository.get("non-existing-token"),
        String.valueOf(NoSuchElementException.class)
    );
  }

  @Test
  void createNewSessionTest() {
    UserSession userSession = new UserSession(3, "new-token");

    int expectedSessionCount = userSessionRepository.search().length + 1;

    assertThat(userSessionRepository.create(userSession)).isNotNull();
    assertThat(userSessionRepository.get("new-token")).isEqualTo(userSession);
    assertThat(userSessionRepository.search().length).isEqualTo(expectedSessionCount);
  }

  @Test
  void replaceExistingSessionTest() {
    UserSession userSession = new UserSession(2, "new-token");

    int expectedSessionCount = userSessionRepository.search().length;

    assertThat(userSessionRepository.create(userSession)).isNotNull();
    assertThat(userSessionRepository.get("new-token")).isEqualTo(userSession);
    assertThat(userSessionRepository.search().length).isEqualTo(expectedSessionCount);
  }

  @Test
  void deleteExistingSession() {
    int expectedSessionCount = userSessionRepository.search().length - 1;

    assertThat(userSessionRepository.delete("token1")).isNotNull();
    assertThat(userSessionRepository.search().length).isEqualTo(expectedSessionCount);
  }

  @Test
  void deleteNonExistingSession() {
    int expectedSessionCount = userSessionRepository.search().length;

    assertThatThrownBy(() -> userSessionRepository.delete("non-existing-token"),
        String.valueOf(NoSuchElementException.class)
    );
    assertThat(userSessionRepository.search().length).isEqualTo(expectedSessionCount);
  }
}
