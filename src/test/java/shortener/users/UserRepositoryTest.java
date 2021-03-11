package shortener.users;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortener.database.entities.User;
import shortener.exceptions.database.NotFound;

import javax.inject.Inject;

@MicronautTest
public class UserRepositoryTest {

  @Inject
  UserRepository userRepository;

  @BeforeEach
  void testDataSetup() {
    userRepository = new UserRepository(new User[]{
        new User(1L, "test1@ex.com", userRepository.hashOut("password1", "test1@ex.com")),
        new User(2L, "test2@mail.com",userRepository.hashOut("password2", "test2@mail.com")),
        new User(3L, "test3@em.ua", userRepository.hashOut("password3", "test3@em.ua"))
    });
  }

  @Test
  void searchTest() {
    assertThat(userRepository.search()).isNotNull();
    assertThat(userRepository.search()).hasSize(3);
  }

  @Test
  void getExistingUserTest() {
    assertThat(userRepository.get(1L)).isNotNull();
    assertThat(userRepository.get("test1@ex.com")).isNotNull();
  }

  @Test
  void getNonExistingUserTest() {
    assertThatThrownBy(() -> userRepository.get(10L),
        String.valueOf(NotFound.class)
    );
    assertThatThrownBy(() -> userRepository.get("notuser@mail.ru"),
        String.valueOf(NotFound.class)
    );
  }

  @Test
  void createNonExistingUserTest() {
    User userRecord = new User(4L, "newuser@mail.com", "coolpassword");
    User createdUser = userRepository.create(userRecord);

    assertThat(createdUser).isNotNull();
    assertThat(createdUser).isEqualTo(userRecord);
  }

  @Test
  void createAlreadyExistingUserTest() {
    User busyMailUser = new User(4L, "test1@ex.com", "coolpassword");
    User busyIdUser = new User(3L, "newuser@mail.com", "coolpassword");


    assertThatThrownBy(() -> userRepository.create(busyMailUser),
        String.valueOf(IllegalArgumentException.class)
    );
    assertThatThrownBy(() -> userRepository.create(busyIdUser),
        String.valueOf(IllegalArgumentException.class)
    );
  }

  @Test
  void deleteExistingUserTest() {
    assertThat(userRepository.delete(1L)).isNotNull();
    assertThat(userRepository.delete("test2@mail.com")).isNotNull();
    assertThat(userRepository.search()).hasSize(1);
  }

  @Test
  void deleteNonExistingUserTest() {
    assertThatThrownBy(() -> userRepository.delete(10L),
        String.valueOf(NotFound.class)
    );

    assertThatThrownBy(() -> userRepository.delete("notuser@mail.ru"),
        String.valueOf(NotFound.class)
    );

    assertThat(userRepository.search()).hasSize(3);
  }

  @Test
  void getUserPasswordTest() {
    assertThat(userRepository.getUserPassword("test1@ex.com")).isEqualTo("da0eb01bba47fe9fffd1ce6d539a2b4dcf49cbc99f4fa3a18de63e1715262e99");
    assertThat(userRepository.getUserPassword("test2@mail.com")).isEqualTo("2cace643e8431817df9a5a809ac121d13bcbaa3e49d628abe8de445a65142963");
    assertThat(userRepository.getUserPassword("test3@em.ua")).isEqualTo("491e68e41368e324801c11ddcae3ec38376ac6251b5bb768344346fab3f673b5");
  }
}
