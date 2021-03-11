package shortener.urls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import javax.inject.Singleton;
import shortener.database.Repository;
import shortener.database.entities.Alias;

/**
 * A database repository for Urls module.
 *
 * <p>TODO: Replace mocked data with actual db interaction.
 */
@Singleton
public class UrlsRepository implements Repository<Alias, String> {

  private final ArrayList<Alias> aliases;

  /**
   * Creates UrlsRepository.
   */
  public UrlsRepository() {
    aliases = new ArrayList<>(Arrays.asList(
      new Alias("alias1", "http://example1.org", 1, 0),
      new Alias("alias2", "http://example2.org", 1, 0)
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
  public Alias[] search() {
    return aliases.toArray(new Alias[0]);
  }

  @Override
  public Alias get(String pk) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(pk)) {
        return alias;
      }
    }

    throw new NoSuchElementException();
  }

  @Override
  public Alias create(Alias entity) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(entity.alias())) {
        throw new IllegalArgumentException();
      }
    }

    aliases.add(entity);

    return entity;
  }

  @Override
  public Alias delete(String pk) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(pk)) {
        aliases.remove(alias);
        return alias;
      }
    }

    throw new NoSuchElementException();
  }
}
