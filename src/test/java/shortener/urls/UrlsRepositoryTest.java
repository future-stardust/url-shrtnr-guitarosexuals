package shortener.urls;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import shortener.database.entities.Alias;

/**
 * A test suite for UrlsRepository class.
 *
 * <p>TODO: Remove UrlsRepository creation once the db is ready and instead mock the db for tests.
 */
public class UrlsRepositoryTest {

  @Test
  void searchReturnsArrayOfAliases() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.search()).hasSize(2);
  }

  @Test
  void getReturnsRecordIfFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.get("test-alias1")).isNotNull();
  }

  @Test
  void getThrowsIfRecordNotFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThatThrownBy(() -> urlsRepository.get("whoops"),
      String.valueOf(NoSuchElementException.class));
  }

  @Test
  void insertOneAddsRecord() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThatThrownBy(() -> urlsRepository.get("new-alias"),
      String.valueOf(NoSuchElementException.class));

    urlsRepository.create(new Alias("new-alias", "http://newexample.org", 1, 0));

    assertThat(urlsRepository.get("new-alias")).isNotNull();
  }

  @Test
  void deleteRemovesRecordIfFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThat(urlsRepository.delete("test-alias1")).isNotNull();
    assertThat(urlsRepository.search()).hasSize(1);
  }

  @Test
  void deleteThrowsIfRecordNotFound() {
    UrlsRepository urlsRepository = new UrlsRepository(new Alias[] {
      new Alias("test-alias1", "http://example1.org", 1, 0),
      new Alias("test-alias2", "http://example2.org", 1, 0)
    });

    assertThatThrownBy(() -> urlsRepository.delete("whoops"),
      String.valueOf(NoSuchElementException.class));
  }

}
