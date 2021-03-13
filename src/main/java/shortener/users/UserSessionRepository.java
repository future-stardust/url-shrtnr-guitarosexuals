package shortener.users;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import shortener.database.Database;
import shortener.database.Repository;
import shortener.database.entities.UserSession;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;

/**
 * A database repository for UserSession entity.
 */
@Singleton
public class UserSessionRepository implements Repository<UserSession, String> {

  @Inject
  Database db;

  @Override
  public List<UserSession> search() {
    return db.search(db.userSessionTable);
  }

  public List<UserSession> searchByUserId(Long userId) throws NotFound {
    return db.search(db.userSessionTable, us -> us.userId().equals(userId));
  }

  @Override
  public UserSession get(String token) throws NotFound {
    return db.get(db.userSessionTable, token);
  }

  @Override
  public UserSession create(UserSession record) throws UniqueViolation {
    return db.create(db.userSessionTable, record);
  }

  @Override
  public UserSession delete(String token) throws NotFound {
    return db.delete(db.userSessionTable, token);
  }
}
