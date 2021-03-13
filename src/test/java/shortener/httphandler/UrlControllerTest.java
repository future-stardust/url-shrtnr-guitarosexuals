package shortener.httphandler;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
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
import java.util.Collections;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import shortener.database.entities.Alias;
import shortener.database.entities.User;
import shortener.database.entities.UserSession;
import shortener.exceptions.database.UniqueViolation;
import shortener.httphandler.utils.JsonResponse;
import shortener.httphandler.utils.ShortenData;
import shortener.urls.UrlRepository;
import shortener.users.UserRepository;
import shortener.users.UserSessionRepository;
import shortener.users.protection.HashFunction;


@MicronautTest
public class UrlControllerTest {

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  String token;

  @Inject
  UrlRepository urlRepository;

  @Inject
  UserRepository userRepository;

  @Inject
  UserSessionRepository userSessionRepository;

  @MockBean(UrlRepository.class)
  public UrlRepository mockUrlRepository() {
    return Mockito.mock(UrlRepository.class);
  }

  @MockBean(UserRepository.class)
  public UserRepository mockUserRepository() {
    return Mockito.mock(UserRepository.class);
  }

  @MockBean(UserSessionRepository.class)
  public UserSessionRepository mockUserSessionRepository() {
    return Mockito.mock(UserSessionRepository.class);
  }

  @BeforeEach
  void mockAuthData() {
    var testUser =
        new User(1L, "test@email.com", HashFunction.hashOut("pa$$word", "test@email.com"));

    Mockito.when(userRepository.getByEmail(Mockito.eq(testUser.email()))).thenReturn(testUser);

    Mockito.when(userSessionRepository.get(Mockito.anyString()))
        .thenReturn(new UserSession(1L, "token"));
  }

  @BeforeEach
  void setupAuth() {
    var userData = new UserData(
        "test@email.com",
        "pa$$word"
    );

    HttpRequest<UserData> request = HttpRequest.POST("/users/signin", userData);
    String responseBody = client.toBlocking().retrieve(request);

    token = JsonParser.parseString(responseBody).getAsJsonObject().get("token").getAsString();
  }

  @Test
  void shortenUrlWithRandomAlias_correctData() {
    Mockito.when(urlRepository.createRandomAlias(Mockito.any(), Mockito.any()))
        .thenReturn(new Alias("test", "https://google.com", 1L));

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
    assertThat(response.body()).contains("shortened_url");
  }

  @Test
  void shortenUrlWithCustomAlias_correctData() {
    Mockito.when(urlRepository.create(Mockito.any()))
        .thenReturn(new Alias("custom_alias", "https://google.com", 1L));

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
    assertThat(response.body()).contains("shortened_url");
  }

  @Test
  void shortenUrlWithCustomAlias_emptyData() {
    Mockito.when(urlRepository.create(Mockito.any()))
        .thenReturn(new Alias("custom_alias", "https://google.com", 1L));

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

    String jsonResponse = JsonResponse.getErrorMessage(
        0,
        "Invalid data: \"url\" parameter should not be empty"
    );

    assertThat(emptyDataException.getMessage()).contains(jsonResponse);
  }

  @Test
  void shortenUrlWithCustomAlias_incorrectUrlParam() {
    Mockito.when(urlRepository.create(Mockito.any()))
        .thenReturn(new Alias("custom_alias", "https://google.com", 1L));

    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "someurl.del",
        "randomalias"
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable wrongUrlException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    String jsonResponse = JsonResponse.getErrorMessage(
        1,
        "Invalid data: url should be http/https valid"
    );

    assertThat(wrongUrlException.getMessage()).contains(jsonResponse);
  }

  @Test
  void shortenUrlAnotherWrongUrl() {
    Mockito.when(urlRepository.create(Mockito.any()))
        .thenReturn(new Alias("custom_alias", "https://google.com", 1L));

    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "/r/some-alias",
        "randomalias"
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable wrongUrlException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    String jsonResponse = JsonResponse.getErrorMessage(
        1,
        "Invalid data: url should be http/https valid"
    );

    assertThat(wrongUrlException.getMessage()).contains(jsonResponse);
  }

  @Test
  void shortenUrlLocalUrl() {
    Mockito.when(urlRepository.create(Mockito.any()))
        .thenReturn(new Alias("custom_alias", "https://google.com", 1L));

    // get server host & port (port randomizes each tests)
    final String host = embeddedServer.getHost();
    final int port = embeddedServer.getPort();

    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        String.format("http://%s:%s/r/some-alias", host, port),
        "nevemind"
    );

    HttpRequest<ShortenData> requestWithAuth = HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable localUrlException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    String jsonResponse = JsonResponse.getErrorMessage(
        1,
        "Invalid data: Local URLs are not allowed"
    );

    assertThat(localUrlException.getMessage()).contains(jsonResponse);
  }

  @Test
  void shortenUrlWithCustomAlias_takenAlias() {
    Mockito.when(urlRepository.create(Mockito.any()))
        .thenThrow(new UniqueViolation("aliases"));

    String uri = "/urls/shorten";
    ShortenData shortenData = new ShortenData(
        "https://google.com",
        "alias1"
    );

    HttpRequest<ShortenData> requestWithAuth =
        HttpRequest.POST(uri, shortenData).bearerAuth(token);

    Throwable emptyDataException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );


    String jsonResponse = JsonResponse.getErrorMessage(
        2,
        "Specified alias is already taken"
    );

    assertThat(emptyDataException.getMessage()).contains(jsonResponse);
  }

  @Test
  void getUserUrls() throws JsonProcessingException {
    Alias alias = new Alias("alias", "http://example.com", 1L);

    Mockito.when(urlRepository.searchByUserId(Mockito.any()))
        .thenReturn(Collections.singletonList(alias));

    String uri = "/urls";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.GET(uri).bearerAuth(token);

    HttpResponse<String> response = client.toBlocking().exchange(
        requestWithAuth,
        String.class
    );

    assertThat(response.body()).contains(alias.alias());
    assertThat(response.body()).contains(alias.url());
    assertThat(response.body()).contains(alias.userId().toString());
  }

  @Test
  void deleteUrl() throws JsonProcessingException {
    Alias aliasToDelete = new Alias("someAlias", "http://example.com", 1L);

    Mockito.when(urlRepository.get(Mockito.eq(aliasToDelete.alias())))
        .thenReturn(aliasToDelete);

    Mockito
        .when(urlRepository.delete(Mockito.eq(aliasToDelete.alias())))
        .thenReturn(aliasToDelete);

    String uri = "/urls/someAlias";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.DELETE(uri).bearerAuth(token);

    HttpResponse<String> response = client.toBlocking().exchange(requestWithAuth);

    assertThat(response.body()).isNull();
    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT);
  }


  @Test
  void deleteUrlThrowsIfUserTriesToDeleteNotOwnAlias() {
    Alias aliasToDelete = new Alias("someAlias", "http://example.com", 1337L);

    Mockito.when(urlRepository.get(Mockito.eq(aliasToDelete.alias())))
        .thenReturn(aliasToDelete);

    Mockito
        .when(urlRepository.delete(Mockito.eq(aliasToDelete.alias())))
        .thenReturn(aliasToDelete);

    String uri = "/urls/someAlias";

    MutableHttpRequest<Object> requestWithAuth = HttpRequest.DELETE(uri).bearerAuth(token);

    HttpClientResponseException emptyDataException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            requestWithAuth,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    assertThat((CharSequence) emptyDataException.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
