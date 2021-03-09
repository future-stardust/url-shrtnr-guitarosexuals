package shortener.exceptions.auth;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {InvalidToken.class, ExceptionHandler.class})
class InvalidTokenHandler implements
    ExceptionHandler<InvalidToken, HttpResponse<String>> {

  @Override
  public HttpResponse<String> handle(HttpRequest request, InvalidToken exception) {
    return HttpResponse.unauthorized().body("Invalid token");
  }
}
