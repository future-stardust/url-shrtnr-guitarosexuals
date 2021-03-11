package shortener.users.tokens;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.security.event.TokenValidatedEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import shortener.exceptions.auth.InvalidToken;
import shortener.exceptions.database.NotFound;
import shortener.users.UserSessionRepository;

/**
 * Listener of TokenValidatedEvent, provided for checking if a token exists in active sessions.
 */
@Singleton
public class TokenValidatedEventListener implements ApplicationEventListener<TokenValidatedEvent> {

  @Inject
  UserSessionRepository userSessionRepository;

  /**
   * Triggered after validating token for correct structure and expiration. This method is used to
   * check for the presence of a token in UserSessionRepository
   *
   * @param event event object
   * @throws InvalidToken if a token is not in sessions
   */
  @Override
  public void onApplicationEvent(TokenValidatedEvent event) throws InvalidToken {
    String accessToken = event.getSource().toString();

    try {
      userSessionRepository.get(accessToken);
    } catch (NotFound exc) {
      throw new InvalidToken();
    }
  }

  @Override
  public boolean supports(TokenValidatedEvent event) {
    return ApplicationEventListener.super.supports(event);
  }
}
