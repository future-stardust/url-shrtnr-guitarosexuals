package shortener.users;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import shortener.database.Database;
import shortener.database.Repository;
import shortener.database.entities.User;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.users.protection.HashFunction;

/**
 * A database repository for Users module.
 */
@Singleton
public class UserRepository implements Repository<User, Long> {

  @Inject
  Database db;

  @Override
  public List<User> search() {
    return db.search(db.userTable);
  }

  @Override
  public User get(Long pk) throws NotFound {
    return db.get(db.userTable, pk);
  }

  public User getByEmail(String email) throws NotFound {
    return db.search(db.userTable, u -> u.email().equals(email), 1).stream().findFirst()
        .orElseThrow(() -> new NotFound(db.userTable.getTableName(), email));
  }

  @Override
  public User create(User record) throws UniqueViolation {
    return db.create(db.userTable, record);
  }

  /**
   * Method for creating new User record with specified user data. Unlike create(User record), this
   * method includes ID generation.
   *
   * @param email    email of new user
   * @param password raw password of new user
   * @return created user record
   */
  public User create(String email, String password) throws UniqueViolation {
    User newUser = new User(null, email, HashFunction.hashOut(password, email));

    return db.create(db.userTable, newUser);
  }

  @Override
  public User delete(Long pk) throws NotFound {
    return db.delete(db.userTable, pk);
  }
}
