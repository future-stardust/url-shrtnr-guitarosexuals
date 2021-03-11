package shortener.httphandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
public class RedirectControllerTest {

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  static final String urlPattern = "/r/%s";

  @Test
  void redirectPositive() {

    MutableHttpRequest<Object> request = HttpRequest.GET(String.format(urlPattern, "alias1"));
    HttpResponse<Object> response = client.toBlocking().exchange(request);

    assertEquals(HttpStatus.OK, response.getStatus());
    // TODO: somehow ensure that response's location is http://example1.org
  }

  @Test
  void redirectNegative() {

    MutableHttpRequest<Object> request = HttpRequest.GET(String.format(urlPattern, "NotFound"));
    HttpResponse<Object> response = client.toBlocking().exchange(request);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
  }
}
