package shortener.httphandler;

import com.google.gson.Gson;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
public class UserControllerTest {

  record UserData(String email, String password) {}

  Gson gson = new Gson();

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  RxHttpClient client;

  @Test
  void signupWithValidData() {
    var userData = new UserData("test@mail.com", "Passwd2021");

    HttpRequest<String> request = HttpRequest.POST("/users/signup", gson.toJson(userData));
    HttpResponse<String> response = client.toBlocking().exchange(request);

    assertThat((CharSequence) response.getStatus()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void signupEmptyUser() {
    var emptyUser = new UserData("", "");

    HttpRequest<String> emptyUserRequest = HttpRequest
        .POST("/users/signup", gson.toJson(emptyUser));

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
  void signupWithWrongEmail() {
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
  void signupWithWrongPassword() {
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
}
