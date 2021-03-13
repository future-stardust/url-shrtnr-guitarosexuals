package shortener.httphandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.io.IOException;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.Database;
import shortener.database.entities.Alias;
import shortener.exceptions.database.NotFound;

@MicronautTest
public class RedirectControllerTest {

  static final String urlPattern = "/r/%s";

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  @Inject
  Database db;

  @MockBean(Database.class)
  public Database mockDb() {
    return Mockito.mock(Database.class);
  }

  @Test
  void redirectPositive() throws IOException {
    Mockito.when(db.get(Mockito.any(), Mockito.any()))
        .thenReturn(new Alias("alias1", "http://example1.org", 1L, 0));

    MutableHttpRequest<Object> request = HttpRequest.GET(String.format(urlPattern, "alias1"));
    HttpResponse<Object> response = client.toBlocking().exchange(request);

    assertEquals(HttpStatus.OK, response.getStatus());
    // TODO: somehow ensure that response's location is http://example1.org
  }

  @Test
  void redirectNegative() throws IOException {
    Mockito.when(db.get(Mockito.any(), Mockito.any()))
        .thenThrow(new NotFound("aliases", "NotFound"));

    MutableHttpRequest<Object> request = HttpRequest.GET(String.format(urlPattern, "NotFound"));

    Throwable notFoundException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            request,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    assertThat(notFoundException.getMessage()).contains("Alias not found.");
  }
}
