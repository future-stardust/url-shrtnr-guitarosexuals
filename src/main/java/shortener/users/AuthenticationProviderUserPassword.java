package shortener.users;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.Flowable;
import java.util.Collections;
import javax.inject.Inject;
import org.reactivestreams.Publisher;


public class AuthenticationProviderUserPassword implements AuthenticationProvider {

  @Inject
  UsersStore store;

  @Override
  public Publisher<AuthenticationResponse> authenticate(
    @Nullable HttpRequest<?> httpRequest,
    AuthenticationRequest<?, ?> req) {
    String username = req.getIdentity().toString();
    String password = req.getSecret().toString();
    if (password.equals(store.getUserPassword(username))) {
      UserDetails details = new UserDetails(username,
        Collections.singletonList(store.getUserRole(username)));
      return Flowable.just(details);
    } else {
      return Flowable.just(new AuthenticationFailed());
    }
  }
}
