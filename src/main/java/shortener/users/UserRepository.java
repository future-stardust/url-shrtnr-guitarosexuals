package shortener.users;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.entities.User;
import shortener.exceptions.database.NotFound;

/**
 * A database repository for Users module.
 *
 * <p>TODO: Replace mocked data with actual db interaction.
 */
@Singleton
public class UserRepository implements Repository<User, Long> {

  /**
   * Method which is used to hash password.
   *
   * @param rawPassword not hashed password
   * @param emailAddress email address used as seed
   * @return hashed password
   */
  public String hashOut(String rawPassword, String emailAddress) {
    try {
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");

      final String salt = emailAddress;
      final String localSeed = "AmCDmG";
      final String passWithSalt = rawPassword + salt + localSeed;

      byte[] passBytes = passWithSalt.getBytes();
      byte[] passHash = sha256.digest(passBytes);

      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < passHash.length; i++) {
        sb.append(Integer.toString((passHash[i] & 0xff) + 0x100, 16).substring(1));
      }
      final String hashedPassword = sb.toString();

      return hashedPassword;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }


  // TODO: solve the issue with hashmaps
  private final HashMap<Long, User> idUserHashMap;
  private final HashMap<String, User> emailUserHashMap;
  private long nextId;

  /**
   * UserRepository constructor.
   */
  public UserRepository() {
    idUserHashMap = new HashMap<>();
    emailUserHashMap = new HashMap<>();
    nextId = 3; // TODO: nextId init

    User testUser1 = new User(1L, "drew@ex.com",
                              "e50f759eddc80daf88e8f2acf87b3a2ccb31ea125f5ffd4db0bbe93d3208f365");

    User testUser2 = new User(2L, "max@mail.ru",
                              "94b478002799d79f4bf297a8c3a1e945f19e11fca59380c9c2c510ac98910452");

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
  public List<User> search() {
    return new ArrayList<>(idUserHashMap.values());
  }

  @Override
  public User get(Long pk) throws NotFound {
    User user = idUserHashMap.get(pk);

    if (user != null) {
      return user;
    }

    // TODO: tablename from database class
    throw new NotFound("users", pk);
  }

  /**
   * Method for getting User record by email field.
   *
   * @param email email string
   * @return User object with specified email
   * @throws NotFound if such user not found
   */
  public User get(String email) throws NotFound {
    User user = emailUserHashMap.get(email);
    if (user == null) {
      // TODO: tablename from database class
      throw new NotFound("users", email);
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

  /**
   * Method for creating new User record with specified user data. Unlike create(User record), this
   * method includes ID generation.
   *
   * @param email    email of new user
   * @param password password of new user
   * @return created user record
   */
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
  public User delete(Long pk) throws NotFound {
    User userToDelete = idUserHashMap.remove(pk);
    if (userToDelete == null) {
      // TODO: tablename from database class
      throw new NotFound("users", pk);
    }

    return emailUserHashMap.remove(userToDelete.email());
  }

  /**
   * Method for deleting user record by email field.
   *
   * @param email email of user to be deleted
   * @return deleted user record
   * @throws NotFound if such user not found
   */
  public User delete(String email) throws NotFound {
    User userToDelete = emailUserHashMap.remove(email);
    if (userToDelete == null) {
      // TODO: tablename from database class
      throw new NotFound("users", email);
    }

    return idUserHashMap.remove(userToDelete.id());
  }

  public String getUserPassword(String email) {
    return get(email).password();
  }
}
