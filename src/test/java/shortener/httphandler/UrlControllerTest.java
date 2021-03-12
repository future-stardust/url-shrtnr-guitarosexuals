package shortener.httphandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.io.IOException;
import javax.inject.Inject;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.Database;
import shortener.database.entities.Alias;
import shortener.httphandler.utils.ShortenData;


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
        "Password1"
    );

    HttpRequest<UserData> request = HttpRequest.POST("/users/signin", userData);
    HttpResponse<BearerAccessRefreshToken> response = client.toBlocking()
        .exchange(request, BearerAccessRefreshToken.class);

    BearerAccessRefreshToken bearerAccessRefreshToken = response.body();
    assert bearerAccessRefreshToken != null;
    token = bearerAccessRefreshToken.getAccessToken();
  }

  @Test
  void shortenUrlWithRandomAlias_correctData() {
    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "https://google.com",
        null
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    HttpResponse<String> response = client.toBlocking().exchange(
        requestWithAuth,
        String.class
    );

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.body()).isNotNull();
    assertThat(response.body()).contains("Url successfully shortened");
  }

  @Test
  void shortenUrlWithCustomAlias_correctData() {
    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "https://google.com",
        "custom_alias"
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    HttpResponse<String> response = client.toBlocking().exchange(
        requestWithAuth,
        String.class
    );

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.body()).contains("Url successfully shortened");
  }

  @Test
  void shortenUrlWithCustomAlias_emptyData() {
    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "",
        ""
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable emptyDataException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    assertThat(emptyDataException.getMessage()).contains("Invalid data");
  }

  @Test
  void shortenUrlWithCustomAlias_incorrectUrlParam() {
    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "someurl.del",
        "randomalias"
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable emptyDataException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    assertThat(emptyDataException.getMessage()).contains("Invalid url");
  }

  @Test
  void shortenUrlWithCustomAlias_takenAlias() {
    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "https://google.com",
        "alias1"
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable emptyDataException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    assertThat(emptyDataException.getMessage()).contains("Specified alias is taken");
  }

  @Test
  void getUserUrls() throws IOException {
    Alias alias = new Alias("alias", "http://example.com", 1L, 0);

    Mockito.when(db.search(Mockito.any()))
        .thenReturn(Arrays.asList(new Alias[] {alias}));

    String uri = "/urls";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.GET(uri).bearerAuth(token);

    HttpResponse<String> response = client.toBlocking().exchange(
        requestWithAuth,
        String.class
    );

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
