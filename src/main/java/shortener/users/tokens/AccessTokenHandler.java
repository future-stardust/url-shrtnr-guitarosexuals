package shortener.users.tokens;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.token.jwt.bearer.AccessRefreshTokenLoginHandler;
import io.micronaut.security.token.jwt.generator.AccessRefreshTokenGenerator;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import shortener.database.entities.User;
import shortener.database.entities.UserSession;
import shortener.users.UserRepository;
import shortener.users.UserSessionRepository;

/**
 * AccessRefreshTokenLoginHandler extension for creating user sessions.
 *
 * @author Maksym Hubenko
 * @author Andrew Krivonos
 */
@Replaces(AccessRefreshTokenLoginHandler.class)
@Singleton
public class AccessTokenHandler extends AccessRefreshTokenLoginHandler {

  @Inject
  UserRepository userRepository;
  @Inject
  UserSessionRepository userSessionRepository;

  /**
   * Class constructor for AccessTokenHandler.
   *
   * @param accessRefreshTokenGenerator Access/refresh token generator
   */
  public AccessTokenHandler(AccessRefreshTokenGenerator accessRefreshTokenGenerator) {
    super(accessRefreshTokenGenerator);
  }

  /**
   * Login success handler. Used for saving access token to active sessions after user
   * authentication
   *
   * @param userDetails Details of the serviced user
   * @param httpRequest HTTP request reference
   * @return HTTP 200 OK including the generated access token or HTTP 500 Internal Server Error
   *         including the reason
   */
  @Override
  public MutableHttpResponse<?> loginSuccess(UserDetails userDetails, HttpRequest<?> httpRequest) {
    Optional<AccessRefreshToken> accessRefreshTokenOptional = accessRefreshTokenGenerator
        .generate(userDetails);

    if (accessRefreshTokenOptional.isPresent()) {
      User servicedUser = userRepository.get(userDetails.getUsername());
      String accessToken = accessRefreshTokenOptional.get().getAccessToken();

      userSessionRepository.create(new UserSession(servicedUser.id(), accessToken));

      return HttpResponse.ok(accessRefreshTokenOptional.get());
    }

    return HttpResponse.serverError(
      "Failed to generate an access token for the user with the provided credentials.");
  }
}
