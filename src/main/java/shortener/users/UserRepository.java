package shortener.users;

import java.util.HashMap;
import java.util.NoSuchElementException;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.User;

/**
 * A database repository for Users module.
 *
 * <p>TODO: Replace mocked data with actual db interaction.
 */
@Singleton
public class UserRepository implements Repository<User, Integer> {

  // TODO: solve the issue with hashmaps
  private final HashMap<Integer, User> idUserHashMap;
  private final HashMap<String, User> emailUserHashMap;
  private int nextId;

  public UserRepository() {
    idUserHashMap = new HashMap<>();
    emailUserHashMap = new HashMap<>();
    nextId = 3; // TODO: nextId init
    User testUser1 = new User(1, "drew@ex.com", "qwerty123");
    User testUser2 = new User(2, "max@mail.ru", "lolpasswd");

    idUserHashMap.put(testUser1.id(), testUser1);
    idUserHashMap.put(testUser2.id(), testUser2);
    emailUserHashMap.put(testUser1.email(), testUser1);
    emailUserHashMap.put(testUser2.email(), testUser2);
  }

  /**
   * Constructor provided for testing purposes. TODO: remove it in the future
   *
   * @param initialUserList user data
   */
  public UserRepository(User[] initialUserList) {
    idUserHashMap = new HashMap<>();
    emailUserHashMap = new HashMap<>();

    for (User user : initialUserList) {
      idUserHashMap.put(user.id(), user);
      emailUserHashMap.put(user.email(), user);
    }
  }

  @Override
  public User[] search() {
    return idUserHashMap.values().toArray(new User[0]);
  }

  @Override
  public User get(Integer pk) throws NoSuchElementException {
    User user = idUserHashMap.get(pk);

    if (user != null) {
      return user;
    }

    throw new NoSuchElementException(
        String.format("User with the specified pk (%d) not found", pk));
  }

  public User get(String email) throws NoSuchElementException {
    User user = emailUserHashMap.get(email);
    if (user == null) {
      throw new NoSuchElementException(
          String.format("User with the specified email (%s) not found", email));
    }

    return user;
  }

  @Override
  public User create(User record) throws IllegalArgumentException {
    if (idUserHashMap.containsKey(record.id()) || emailUserHashMap.containsKey(record.email())) {
      throw new IllegalArgumentException("The specified value already exists");
    }
    idUserHashMap.put(record.id(), record);
    emailUserHashMap.put(record.email(), record);

    return record;
  }

  public User create(String email, String password) {
    if (emailUserHashMap.containsKey(email)) {
      throw new IllegalArgumentException("The specified value already exists");
    }
    User newUser = new User(nextId++, email, password);

    idUserHashMap.put(newUser.id(), newUser);
    emailUserHashMap.put(newUser.email(), newUser);

    return newUser;
  }

  @Override
  public User delete(Integer pk) throws NoSuchElementException {
    User userToDelete = idUserHashMap.remove(pk);
    if (userToDelete == null) {
      throw new NoSuchElementException();
    }

    return emailUserHashMap.remove(userToDelete.email());
  }

  public User delete(String email) throws NoSuchElementException {
    User userToDelete = emailUserHashMap.remove(email);
    if (userToDelete == null) {
      throw new NoSuchElementException();
    }

    return idUserHashMap.remove(userToDelete.id());
  }

  public String getUserPassword(String email) {
    return get(email).password();
  }
}
