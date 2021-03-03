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

    assertThat(urlsRepository.findAll()).hasSize(2);
  }

  @Test
  void findOneByPrimaryKeyReturnsRecordIfFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.findOneByPrimaryKey("test-alias1")).isNotNull();
  }

  @Test
  void findOneByPrimaryKeyReturnsNullIfRecordNotFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.findOneByPrimaryKey("whoops")).isNull();
  }

  @Test
  void insertOneAddsRecord() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.findOneByPrimaryKey("new-alias")).isNull();


    urlsRepository.insertOne(new Alias("new-alias", "http://newexample.org", 1, 0));

    assertThat(urlsRepository.findOneByPrimaryKey("new-alias")).isNotNull();
  }

  @Test
  void deleteOneByPrimaryKeyDeletesRecordIfFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.deleteOneByPrimaryKey("test-alias1")).isNotNull();
    assertThat(urlsRepository.findAll()).hasSize(1);
  }

  @Test
  void deleteOneByPrimaryKeyReturnsNullIfNotFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.deleteOneByPrimaryKey("whoops")).isNull();
  }

}
