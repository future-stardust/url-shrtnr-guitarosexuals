package shortener.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.User;

@Singleton
class UserRepository implements Repository<User, Integer> {

  private final ArrayList<User> users;

  public UserRepository() {
    users = new ArrayList<User>(Arrays.asList(
        new User(1, "drew", "qwerty123"),
        new User(2, "max", "asdfgh")
    ));
  }

  @Override
  public User[] search() {
    return (User[]) users.toArray();
  }

  @Override
  public User get(Integer pk) throws NoSuchElementException {
    return null;
  }

  @Override
  public User create(User record) throws IllegalArgumentException {
    return null;
  }

  @Override
  public User delete(Integer pk) throws NoSuchElementException {
    return null;
  }

  public String getUserPassword(String email) throws NoSuchElementException {
    for (User user : users) {
      if (user.email().equals(email)) {
        return user.password();
      }
    }
    return null;
  }
}
