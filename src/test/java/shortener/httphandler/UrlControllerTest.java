package shortener.httphandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.io.IOException;
import javax.inject.Inject;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.Database;
import shortener.database.entities.Alias;

@MicronautTest
public class UrlControllerTest {

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  String token;

  @Inject
  Database db;

  @MockBean(Database.class)
  public Database mockDb() {
    return Mockito.mock(Database.class);
  }

  @BeforeEach
  void setupAuth() {
    var userData = new UserData(
        "drew@ex.com",
        "qwerty123"
    );

    HttpRequest<UserData> request = HttpRequest.POST("/users/signin", userData);
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
  void getUserUrls() throws IOException {
    Alias alias = new Alias("alias", "http://example.com", 1L, 0);

    Mockito.when(db.search(Mockito.any()))
        .thenReturn(Arrays.asList(new Alias[] {alias}));

    String uri = "/urls";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.GET(uri).bearerAuth(token);

    String response = client.toBlocking()
        .retrieve(requestWithAuth);

    ObjectMapper objectMapper = new ObjectMapper();

    assertEquals(response, objectMapper.writeValueAsString(new Alias[] {alias}));
  }

  @Test
  void deleteUrl() throws IOException {
    Alias aliasToDelete = new Alias("someAlias", "http://example.com", 1L, 0);

    Mockito.when(db.delete(Mockito.any(), Mockito.eq(aliasToDelete.alias())))
        .thenReturn(aliasToDelete);

    String uri = "/urls/someAlias";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.DELETE(uri).bearerAuth(token);

    String response = client.toBlocking()
        .retrieve(requestWithAuth);

    ObjectMapper objectMapper = new ObjectMapper();

    assertEquals(response, objectMapper.writeValueAsString(aliasToDelete));
  }
}
