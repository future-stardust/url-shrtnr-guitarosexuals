package shortener.httphandler;

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
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shortener.httphandler.utils.ShortenData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;


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
  void getUserUrls() {
    String uri = "/urls";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.GET(uri).bearerAuth(token);

    HttpResponse<String> response = client.toBlocking().exchange(
        requestWithAuth,
        String.class
    );

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.OK);
    assertThat(response.body().matches("^\\[.*]$")).isTrue();
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
