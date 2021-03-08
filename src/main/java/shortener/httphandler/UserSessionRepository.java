package shortener.httphandler;

import java.util.HashMap;
import java.util.NoSuchElementException;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.UserSession;

@Singleton
public class UserSessionRepository implements Repository<UserSession, Integer> {
  private final HashMap<Integer, UserSession> userSessionHashMap;

  public UserSessionRepository() {
    userSessionHashMap = new HashMap<>();
  }

  @Override
  public UserSession[] search() {
    return userSessionHashMap.values().toArray(new UserSession[0]);
  }

  @Override
  public UserSession get(Integer userId) throws NoSuchElementException {
    UserSession userSession = userSessionHashMap.get(userId);

    if (userSession != null) {
      return userSession;
    }

    throw new NoSuchElementException(String.format("User session with the specified ID (%d) does not exist.", userId));
  }

  @Override
  public UserSession create(UserSession userSession) {
    if (userSessionHashMap.containsKey(userSession.userId())) {
      userSessionHashMap.replace(userSession.userId(), userSession);
    } else {
      userSessionHashMap.put(userSession.userId(), userSession);
    }

    return userSession;
  }

  @Override
  public UserSession delete(Integer userId) {
    UserSession userSession = userSessionHashMap.remove(userId);

    if (userSession == null) {
      throw new NoSuchElementException(String.format("User session with the specified ID (%d) does not exist.", userId));
    }

    return userSessionHashMap.remove(userSession.userId());
  }
}
