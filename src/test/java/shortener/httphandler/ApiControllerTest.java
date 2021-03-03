package shortener.httphandler;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class ApiControllerTest {

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  void createShortenLinkTest() {
    String uri = "/urls/shorten";
    String body = "{ 'test': 'there should be url and alias instead' }";

    int statusCode = client.toBlocking()
      .exchange(HttpRequest.POST(uri, body)).code();

    assertEquals(200, statusCode);
  }

  @Test
  void getUserUrlArray() {
    String uri = "/urls";

    String response = client.toBlocking()
      .retrieve(HttpRequest.GET(uri));

    assertEquals("[\"Url array\"]", response);
  }

  @Test
  void deleteShortenedLinkTest() {
    String uri = "/urls/someAlias";

    int statusCode = client.toBlocking()
      .exchange(HttpRequest.DELETE(uri)).code();

    assertEquals(200, statusCode);
  }
}
