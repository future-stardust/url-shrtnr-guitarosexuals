package shortener.urls;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import shortener.database.Database;
import shortener.database.Repository;
import shortener.database.entities.Alias;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.urls.utils.RandomStringGenerator;

/**
 * Urls repository.
 */
@Singleton
public class UrlRepository implements Repository<Alias, String> {

  @Inject
  Database db;

  @Override
  public List<Alias> search() {
    return db.search(db.aliasTable);
  }

  public List<Alias> searchByUserId(Long userId) {
    return db.search(db.aliasTable, alias -> alias.userId().equals(userId));
  }

  @Override
  public Alias get(String pk) throws NotFound {
    return db.get(db.aliasTable, pk);
  }

  @Override
  public Alias create(Alias record) throws UniqueViolation {
    return db.create(db.aliasTable, record);
  }

  /**
   * Creates an Alias with randomly-generated `alias`.
   *
   * @param url    A url to create an alias for.
   * @param userId Creator's id.
   * @return Created alias.
   */
  public Alias createRandomAlias(String url, Long userId) {
    String randomAlias = null;

    for (int generationRetries = 0; generationRetries <= 100; generationRetries++) {
      randomAlias = RandomStringGenerator.generate(Alias.ALIAS_LENGTH_DEFAULT);

      try {
        db.get(db.aliasTable, randomAlias);

        if (generationRetries >= 100) {
          throw new RuntimeException("Failed to generate a random alias for a given url.");
        }
      } catch (NotFound exc) {
        break;
      }
    }

    // `randomAlias` should be defined by this moment
    return create(new Alias(randomAlias, url, userId));
  }

  @Override
  public Alias delete(String pk) throws NotFound {
    return db.delete(db.aliasTable, pk);
  }
}
