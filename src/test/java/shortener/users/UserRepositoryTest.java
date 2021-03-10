package shortener.users;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortener.database.entities.User;

@MicronautTest
public class UserRepositoryTest {

  UserRepository userRepository;

  @BeforeEach
  void testDataSetup() {
    userRepository = new UserRepository(new User[]{
        new User(1, "test1@ex.com", "password1"),
        new User(2, "test2@mail.com", "password2"),
        new User(3, "test3@em.ua", "password3")
    });
  }

  @Test
  void searchTest() {
    assertThat(userRepository.search()).isNotNull();
    assertThat(userRepository.search()).hasSize(3);
  }

  @Test
  void getExistingUserTest() {
    assertThat(userRepository.get(1)).isNotNull();
    assertThat(userRepository.get("test1@ex.com")).isNotNull();
  }

  @Test
  void getNonExistingUserTest() {
    assertThatThrownBy(() -> userRepository.get(10),
        String.valueOf(NoSuchElementException.class)
    );
    assertThatThrownBy(() -> userRepository.get("notuser@mail.ru"),
        String.valueOf(NoSuchElementException.class)
    );
  }

  @Test
  void createNonExistingUserTest() {
    User userRecord = new User(4, "newuser@mail.com", "coolpassword");
    User createdUser = userRepository.create(userRecord);

    assertThat(createdUser).isNotNull();
    assertThat(createdUser).isEqualTo(userRecord);
  }

  @Test
  void createAlreadyExistingUserTest() {
    User busyMailUser = new User(4, "test1@ex.com", "coolpassword");
    User busyIdUser = new User(3, "newuser@mail.com", "coolpassword");


    assertThatThrownBy(() -> userRepository.create(busyMailUser),
        String.valueOf(IllegalArgumentException.class)
    );
    assertThatThrownBy(() -> userRepository.create(busyIdUser),
        String.valueOf(IllegalArgumentException.class)
    );
  }

  @Test
  void deleteExistingUserTest() {
    assertThat(userRepository.delete(1)).isNotNull();
    assertThat(userRepository.delete("test2@mail.com")).isNotNull();
    assertThat(userRepository.search()).hasSize(1);
  }

  @Test
  void deleteNonExistingUserTest() {
    assertThatThrownBy(() -> userRepository.delete(10),
        String.valueOf(NoSuchElementException.class)
    );

    assertThatThrownBy(() -> userRepository.delete("notuser@mail.ru"),
        String.valueOf(NoSuchElementException.class)
    );

    assertThat(userRepository.search()).hasSize(3);
  }

  @Test
  void getUserPasswordTest() {
    assertThat(userRepository.getUserPassword("test1@ex.com")).isEqualTo("password1");
  }
}
