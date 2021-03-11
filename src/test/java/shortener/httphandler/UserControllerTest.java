package shortener.httphandler;

import com.google.gson.Gson;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.Objects;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import shortener.users.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@TestInstance(Lifecycle.PER_CLASS)
public class UserControllerTest {

  record UserData(String email, String password) {

  }

  Gson gson = new Gson();

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  RxHttpClient client;

  @Inject
  UserRepository userRepository;

  @BeforeAll
  void setup() {
    userRepository.create("test@mail.com", "CoolPasswd123");
  }

  @Test
  void signUpWithValidData() {
    var userData = new UserData("newuser@mail.com", "Passwd2021");

    HttpRequest<String> request = HttpRequest.POST("/users/signup", gson.toJson(userData));
    HttpResponse<String> response = client.toBlocking().exchange(request);

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void signUpEmptyUser() {
    var emptyUser = new UserData("", "");

    HttpRequest<String> emptyUserRequest = HttpRequest
      .POST("/users/signup", gson.toJson(emptyUser));

    // TODO: improve 4xx code response checking (e.g. status code check)
    Throwable emptyUserException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        emptyUserRequest,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );
    assertThat(emptyUserException.getMessage()).contains("Invalid email address");
  }

  @Test
  void signUpWithWrongEmail() {
    var wrongMailUser = new UserData("wrongmail", "Passwd2021");

    HttpRequest<String> wrongMailUserRequest = HttpRequest
      .POST("/users/signup", gson.toJson(wrongMailUser));

    Throwable wrongMailUserException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        wrongMailUserRequest,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );
    assertThat(wrongMailUserException.getMessage()).contains("Invalid email address");

  }

  @Test
  void signUpWithWeakPassword() {
    var wrongPasswordUser = new UserData("user@mail.com", "pass");

    HttpRequest<String> wrongPasswordUserRequest = HttpRequest
      .POST("/users/signup", gson.toJson(wrongPasswordUser));

    Throwable wrongPasswordUserException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        wrongPasswordUserRequest,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );
    assertThat(wrongPasswordUserException.getMessage()).contains("Password");
  }

  @Test
  void signInWithCorrectCredentials() {
    var user = new UserData("test@mail.com", "CoolPasswd123");

    HttpRequest<String> request = HttpRequest
      .POST("/users/signin", gson.toJson(user));

    HttpResponse<String> response = client.toBlocking().exchange(
      request,
      String.class
    );

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.OK);
    assertThat(response.body()).isNotNull();
    assertThat(response.body()).contains("access_token");
  }

  @Test
  void signInWithWrongPassword() {
    var wrongPasswdUser = new UserData("test@mail.com", "WrongPasswd");

    HttpRequest<String> request = HttpRequest
      .POST("/users/signin", gson.toJson(wrongPasswdUser));

    Throwable wrongPasswordUserException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        request,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );
    assertThat(wrongPasswordUserException.getMessage()).contains("Credentials Do Not Match");
  }

  @Test
  void signInWithNonExistentEmail() {
    var wrongPasswdUser = new UserData("nonexistent@mail.com", "Passwd123");

    HttpRequest<String> request = HttpRequest
      .POST("/users/signin", gson.toJson(wrongPasswdUser));

    Throwable wrongPasswordUserException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        request,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );
    assertThat(wrongPasswordUserException.getMessage()).contains("User Not Found");
  }

  @Test
  void signOutWithAuthorization() {
    var validUser = new UserData("test@mail.com", "CoolPasswd123");

    HttpRequest<String> signInRequest = HttpRequest
      .POST("/users/signin", gson.toJson(validUser));

    HttpResponse<BearerAccessRefreshToken> signInResponse = client.toBlocking().exchange(
      signInRequest,
      BearerAccessRefreshToken.class
    );

    final String accessToken = Objects.requireNonNull(signInResponse.body()).getAccessToken();

    HttpRequest<Object> signOutRequest = HttpRequest.GET("/users/signout")
      .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));

    HttpResponse<String> response = client.toBlocking().exchange(
      signOutRequest,
      String.class
    );

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.OK);
    assertThat(response.body()).isNotNull();
    assertThat(response.body()).contains("Successfully logged out");
  }

  @Test
  void signOutWithoutAuthorization() {
    HttpRequest<Object> request = HttpRequest.GET("/users/signout");

    Throwable userNotAuthorizedException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        request,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );

    assertThat(userNotAuthorizedException.getMessage()).contains("Unauthorized");
  }

  @Test
  void signOutWithInvalidToken() {
    HttpRequest<Object> request = HttpRequest.GET("/users/signout").header(HttpHeaders.AUTHORIZATION, "Bearer invalidTokenEntry");

    Throwable userNotAuthorizedException = Assertions.assertThrows(
      HttpClientResponseException.class,
      () -> client.toBlocking().exchange(
        request,
        Argument.of(String.class),
        Argument.of(String.class)
      )
    );

    assertThat(userNotAuthorizedException.getMessage()).contains("Unauthorized");
  }
}
