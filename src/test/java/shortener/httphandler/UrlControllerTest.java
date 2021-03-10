package shortener.httphandler;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class UrlControllerTest {

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  String token;

  @BeforeEach
  void setupAuth() {
    var userData = new UsernamePasswordCredentials(
        "drew@ex.com",
        "qwerty123"
    );

    HttpRequest<UsernamePasswordCredentials> request = HttpRequest.POST("/users/signin", userData);
    HttpResponse<BearerAccessRefreshToken> response = client.toBlocking()
        .exchange(request, BearerAccessRefreshToken.class);

    BearerAccessRefreshToken bearerAccessRefreshToken = response.body();
    assert bearerAccessRefreshToken != null;
    token = bearerAccessRefreshToken.getAccessToken();
  }

  @Test
  void shortenUrl() {
    String uri = "/urls/shorten";
    String body = "{ 'test': 'there should be url and alias instead' }";

    HttpRequest<String> requestWithAuth = HttpRequest.POST(uri, body).bearerAuth(token);

    int statusCode = client.toBlocking()
        .exchange(requestWithAuth).code();

    assertEquals(200, statusCode);
  }

  @Test
  void getUserUrls() {
    String uri = "/urls";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.GET(uri).bearerAuth(token);

    String response = client.toBlocking()
        .retrieve(requestWithAuth);

    assertEquals("[\"Url array\"]", response);
  }

  @Test
  void deleteUrl() {
    String uri = "/urls/someAlias";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.DELETE(uri).bearerAuth(token);

    int statusCode = client.toBlocking()
        .exchange(requestWithAuth).code();

    assertEquals(200, statusCode);
  }
}
