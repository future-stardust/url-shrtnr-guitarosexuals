package shortener.httphandler;

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
import java.util.Optional;
import javax.inject.Inject;
import javax.validation.constraints.Email;
import shortener.exceptions.auth.InvalidCredentials;
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
   *          401 Unauthorized - if credentials are wrong or user not found
   */
  @Secured(SecurityRule.IS_ANONYMOUS)
  @Post(value = "/signin", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<?> signIn(@Body UserData userData) {
    if (userData.email() == null || userData.password() == null) {
      return HttpResponse.unauthorized().body("Credentials should not be empty.");
    }
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
        userData.email(),
        userData.password()
    );

    HttpRequest<UsernamePasswordCredentials> request = HttpRequest.POST("/login", credentials);

    try {
      return client.exchange(request, String.class).blockingFirst();
    } catch (HttpClientResponseException e) {
      return HttpResponse.unauthorized().body(e.getMessage());
    }
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
    if (userData.email() == null || userData.password() == null) {
      return HttpResponse.badRequest("Credentials should not be empty.");
    }

    final @Email String userEmail = userData.email();
    final String userPassword = userData.password();

    try {
      UserDataValidator.validateEmail(userEmail);
      UserDataValidator.validatePassword(userPassword);
    } catch (InvalidCredentials e) {
      return HttpResponse.badRequest(e.getMessage());
    }

    try {
      userRepository.create(userEmail, userPassword);
    } catch (IllegalArgumentException e) {
      return HttpResponse.status(HttpStatus.CONFLICT).body(
          String.format("User %s has already been registered", userEmail)
      );
    }

    return HttpResponse.created("User successfully registered");
  }

  /**
   * Sign out endpoint. Provides user logout from the system
   *
   * @param httpHeaders HTTP headers reference
   * @return  200 if user logged out<br>
   *          500 if something went wrong with token
   */
  @Secured(SecurityRule.IS_AUTHENTICATED)
  @Get(value = "/signout")
  public HttpResponse<String> signOut(HttpHeaders httpHeaders) {
    Optional<String> authorizationHeaderOptional = httpHeaders.getAuthorization();

    if (authorizationHeaderOptional.isPresent()) {
      String accessToken = authorizationHeaderOptional.get().replace("Bearer ", "");
      userSessionRepository.delete(accessToken);
      return HttpResponse.ok("Successfully logged out");
    }

    return HttpResponse.serverError("An error occurred while trying to sign out.");
  }
}
