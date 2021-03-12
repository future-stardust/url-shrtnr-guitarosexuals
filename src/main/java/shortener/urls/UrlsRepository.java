package shortener.urls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.entities.Alias;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.urls.utils.RandomStringGenerator;
import shortener.users.UserRepository;

/**
 * A database repository for Urls module.
 *
 * <p>TODO: Replace mocked data with actual db interaction.
 */
@Singleton
public class UrlsRepository implements Repository<Alias, String> {

  @Inject
  private UserRepository userRepository;

  private final ArrayList<Alias> aliases;

  private final int defaultAliasLength = 5;

  /**
   * Creates UrlsRepository.
   */
  public UrlsRepository() {
    aliases = new ArrayList<>(Arrays.asList(
        new Alias("alias1", "http://example1.org", 1L, 0),
        new Alias("alias2", "http://example2.org", 1L, 0)
    ));
  }

  /**
   * Creates UrlsRepository with predefined mocked data.
   *
   * <p>Do not use it as it is a subject to remove!
   *
   * <p>TODO: Remove this constructor once db is implemented.
   *
   * @param defaultInnerValues Array of mocked data.
   */
  public UrlsRepository(Alias[] defaultInnerValues) {
    aliases = new ArrayList<>(Arrays.asList(defaultInnerValues));
  }

  @Override
  public List<Alias> search() {
    return aliases;
  }

  @Override
  public Alias get(String pk) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(pk)) {
        return alias;
      }
    }

    // TODO: get tablename from database's class
    throw new NotFound("aliases", pk);
  }

  @Override
  public Alias create(Alias entity) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(entity.alias())) {
        // TODO: tablename from database's class
        throw new UniqueViolation("aliases");
      }
    }

    aliases.add(entity);

    return entity;
  }

  /**
   * Method for creating new aliases based on specified url and user ID. Alias value is generated
   * automatically.
   *
   * @param url    url to be associated with alias
   * @param userId user-owner
   * @return created alias record
   */
  public Alias create(String url, Long userId) {
    String randomAlias = RandomStringGenerator.generate(defaultAliasLength);

    return create(new Alias(randomAlias, url, userId, 0));
  }

  /**
   * Method for creating new aliases based on specified url and user ID. Alias value is specified
   * manually.
   *
   * @param url         url to be associated with alias
   * @param userId      user-owner
   * @param customAlias desired alias
   * @return created alias record
   */
  public Alias create(String url, Long userId, String customAlias) {
    return create(new Alias(customAlias, url, userId, 0));
  }

  @Override
  public Alias delete(String pk) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(pk)) {
        aliases.remove(alias);
        return alias;
      }
    }

    // TODO: get tablename from database's class
    throw new NotFound("aliases", pk);
  }
}
