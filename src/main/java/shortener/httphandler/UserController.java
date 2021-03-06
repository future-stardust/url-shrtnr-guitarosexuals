package shortener.httphandler;

import com.google.gson.JsonParser;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.rules.SecurityRule;
import javax.inject.Inject;
import javax.validation.constraints.Email;
import shortener.exceptions.auth.InvalidCredentials;
import shortener.exceptions.database.UniqueViolation;
import shortener.httphandler.utils.JsonResponse;
import shortener.httphandler.utils.UserDataValidator;
import shortener.users.UserRepository;
import shortener.users.UserSessionRepository;

/**
 * REST API Controller with entrypoints related to user.
 */
@Controller("/users")
public class UserController {

  @Inject
  UserRepository userRepository;
  @Inject
  UserSessionRepository userSessionRepository;

  @Inject
  @Client("/")
  RxHttpClient client;

  /**
   * Proxy method for login endpoint.
   *
   * <p>Converts UserData(email, password) to UsernamePasswordCredentials, then sends auth
   * request to /login entrypoint of Micronaut. If user credentials are correct, this method
   * returns access token.
   *
   * @param userData user email and password
   * @return  200 OK - access token<br>
   *          400 Bad Request - if credentials are wrong<br>
   *          404 Not Found - if user not found
   */
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post(value = "/signin", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<?> signIn(@Body UserData userData) {
    if (userData.email() == null || userData.email().isBlank()
        || userData.password() == null || userData.password().isBlank()) {
      return HttpResponse.badRequest("Credentials should not be empty.");
    }
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
        userData.email(),
        userData.password()
    );

    HttpRequest<UsernamePasswordCredentials> request = HttpRequest.POST("/login", credentials);

    String body;
    try {
      body = client.retrieve(request, String.class).blockingFirst();
    } catch (HttpClientResponseException e) {
      if (e.getMessage().equals("User Not Found")) {
        return HttpResponse.notFound(e.getMessage());
      }

      return HttpResponse.badRequest(e.getMessage());
    }
    String token = JsonParser.parseString(body).getAsJsonObject().get("access_token").getAsString();

    return HttpResponse.ok(JsonResponse.getTokenMessage(token));
  }

  /**
   * Sign Up entrypoint. Provides user registration in the system.
   *
   * @param userData json with credentials (email and password)
   * @return  201 Created - if user created<br>
   *          400 Bad Request - if credentials are wrong<br>
   *          409 Conflict - if user already exists
   */
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post(value = "/signup", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<String> signUp(@Body UserData userData) {
    if (userData.email() == null || userData.email().isBlank()
        || userData.password() == null || userData.password().isBlank()) {
      String jsonResponse = JsonResponse.getErrorMessage(
          0,
          "Credentials should not be empty."
      );

      return HttpResponse.badRequest(jsonResponse);
    }

    final @Email String userEmail = userData.email();
    final String userPassword = userData.password();

    try {
      UserDataValidator.validateEmail(userEmail);
      UserDataValidator.validatePassword(userPassword);
    } catch (InvalidCredentials e) {
      String jsonResponse = JsonResponse.getErrorMessage(
          0,
          e.getMessage()
      );

      return HttpResponse.badRequest(jsonResponse);
    }

    try {
      userRepository.create(userEmail, userPassword);
    } catch (UniqueViolation exc) {
      String jsonResponse = JsonResponse.getErrorMessage(
          2,
          String.format("User %s has already been registered.", userEmail)
      );

      return HttpResponse.status(HttpStatus.CONFLICT).body(jsonResponse);
    }

    return HttpResponse.created("User was successfully registered.");
  }

  /**
   * Sign out endpoint. Provides user logout from the system
   *
   * @param httpHeaders HTTP headers reference
   * @return  200 OK - log out<br>
   *          401 Unauthorized - if user is not authorized
   */
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Get(value = "/signout")
  public HttpResponse<String> signOut(HttpHeaders httpHeaders) {
    String authorizationHeaderOptional = httpHeaders.getAuthorization().get();

    String accessToken = authorizationHeaderOptional.replace(
        "Bearer ",
        ""
    );
    userSessionRepository.delete(accessToken);
    return HttpResponse.ok("Successfully signed out.");
  }
}
