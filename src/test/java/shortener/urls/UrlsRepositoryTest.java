package shortener.urls;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import shortener.database.Alias;

/**
 * A test suite for UrlsRepository class.
 *
 * <p>TODO: Remove UrlsRepository creation once the db is ready and instead mock the db for tests.
 */
public class UrlsRepositoryTest {

  @Test
  void findAllReturnsArrayOfAliases() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.search()).hasSize(2);
  }

  @Test
  void findOneByPrimaryKeyReturnsRecordIfFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.get("test-alias1")).isNotNull();
  }

  @Test
  void findOneByPrimaryKeyReturnsNullIfRecordNotFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.get("whoops")).isNull();
  }

  @Test
  void insertOneAddsRecord() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.get("new-alias")).isNull();


    urlsRepository.create(new Alias("new-alias", "http://newexample.org", 1, 0));

    assertThat(urlsRepository.get("new-alias")).isNotNull();
  }

  @Test
  void deleteOneByPrimaryKeyDeletesRecordIfFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.delete("test-alias1")).isNotNull();
    assertThat(urlsRepository.search()).hasSize(1);
  }

  @Test
  void deleteOneByPrimaryKeyReturnsNullIfNotFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.delete("whoops")).isNull();
  }

}
