package shortener.urls;

import io.micronaut.context.BeanContext;
import java.util.Arrays;
import java.util.List;
import javax.inject.Singleton;
import shortener.database.Alias;
import shortener.database.Repository;

/**
 * A database repository for Urls module.
 *
 * <p>TODO: Replace mocked data with actual db interaction.
 */
@Singleton
public class UrlsRepository implements Repository<Alias, String> {

  private final List<Alias> aliases = Arrays.asList(
    new Alias("alias1", "http://example1.org", 1, 0),
    new Alias("alias2", "http://example2.org", 1, 0)
  );

  @Override
  public Alias[] findAll() {
    return aliases.toArray(new Alias[0]);
  }

  @Override
  public Alias findOneByPrimaryKey(String pk) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(pk)) {
        return alias;
      }
    }

    return null;
  }

  @Override
  public Alias insertOne(Alias entity) {
    aliases.add(entity);
    return entity;
  }

  @Override
  public Alias deleteOneByPrimaryKey(String pk) {
    for (Alias alias : aliases) {
      if (alias.alias().equals(pk)) {
        aliases.remove(alias);
        return alias;
      }
    }

    return null;
  }
}
