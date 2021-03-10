package shortener.users;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.UserSession;

/**
 * A database repository for UserSession entity.
 *
 * <p>TODO: Replace mocked data with actual db interaction.
 */
@Singleton
public class UserSessionRepository implements Repository<UserSession, String> {

  private final ArrayList<UserSession> userSessions;

  public UserSessionRepository() {
    userSessions = new ArrayList<>();
  }

  /**
   * UserSessionRepository constructor provided for testing purposes. TODO: should be deleted
   *
   * @param initialUserSessionList initial user sessions
   */
  public UserSessionRepository(UserSession[] initialUserSessionList) {
    userSessions = new ArrayList<>(Arrays.asList(initialUserSessionList));
  }

  @Override
  public UserSession[] search() {
    return userSessions.toArray(new UserSession[0]);
  }

  /**
   * Get user session by token.
   *
   * @param token access token
   * @return found user session
   * @throws NoSuchElementException if session with specified token not found
   */
  @Override
  public UserSession get(String token) throws NoSuchElementException {
    for (UserSession userSession : userSessions) {
      if (userSession.token().equals(token)) {
        return userSession;
      }
    }

    throw new NoSuchElementException(
        String.format("User session with the specified ID (%s) does not exist.", token));
  }

  /**
   * Create new user session in database. If user already has a session, new session replaces the
   * old one.
   *
   * @param newUserSession UserSession object to be added
   * @return added UserSession
   */
  @Override
  public UserSession create(UserSession newUserSession) {
    // Get userId list of users who have active sessions
    ArrayList<Integer> userIdList = (ArrayList<Integer>) userSessions.stream()
        .map(UserSession::userId).collect(Collectors.toList());

    if (userIdList.contains(newUserSession.userId())) {
      userSessions.set(userIdList.indexOf(newUserSession.userId()), newUserSession);
    } else {
      userSessions.add(newUserSession);
    }

    return newUserSession;
  }

  /**
   * Delete token from database if it exists.
   *
   * @param token token to be deleted
   * @return deleted token
   * @throws NoSuchElementException if session with specified token not found
   */
  @Override
  public UserSession delete(String token) throws NoSuchElementException {
    for (UserSession userSession : userSessions) {
      if (userSession.token().equals(token)) {
        userSessions.remove(userSession);
        return userSession;
      }
    }

    throw new NoSuchElementException(
        String.format("User session with the specified ID (%s) does not exist.", token));

  }
}
