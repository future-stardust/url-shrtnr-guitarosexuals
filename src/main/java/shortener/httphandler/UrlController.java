package shortener.httphandler;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.List;
import javax.inject.Inject;
import shortener.database.Database;
import shortener.database.entities.Alias;
import shortener.database.entities.User;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.httphandler.utils.ShortenData;
import shortener.users.UserRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/urls")
public class UrlController {

  @Inject
  Database db;

  @Inject
  UserRepository userRepository;

  /**
   * Entrypoint for shortening urls.
   *
   * @param shortenData json (url, alias: optional)
   * @return OK/error
   */
  @Post(value = "/shorten", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<Object> shortenUrl(@Body ShortenData shortenData, Principal principal) {
    final String url = shortenData.url();
    final String alias = shortenData.alias();
    final String userEmail = principal.getName();

    // JSON content validation
    if (url == null || url.isBlank()) {
      return HttpResponse.badRequest("Invalid data: url parameter should not be empty");
    }

    // URL validation
    try {
      new URL(url);
    } catch (MalformedURLException e) {
      return HttpResponse.badRequest("Invalid url: " + url);
    }

    // User existing check
    Long userId;
    try {
      userId = userRepository.get(userEmail).id();
    } catch (NotFound e) {
      return HttpResponse.unauthorized().body(String.format("User %s not registered", userEmail));
    }

    // Create alias records and check for its uniqueness
    if (alias == null || alias.isBlank()) {
      try {
        urlsRepository.create(shortenData.url(), userId);
      } catch (UniqueViolation e) {
        return HttpResponse.serverError(
            "Server failed to generate unique alias. Please, contact the administrator");
      }
    } else {
      try {
        urlsRepository.create(shortenData.url(), userId, shortenData.alias());
      } catch (UniqueViolation e) {
        return HttpResponse.badRequest(String.format("Specified alias is taken: %s", alias));
      }
    }

    return HttpResponse.created("Url successfully shortened");
  }

  /**
   * Entrypoint for getting user's url array.
   *
   * @return user's url array
   */
  @Get
  public HttpResponse<List<Alias>> getUserUrls(Principal principal) {
    try {
      String userEmail = principal.getName();
      User user = db.search(db.userTable, u -> u.email().equals(userEmail), 1).get(0);

      if (user == null) {
        return HttpResponse.unauthorized();
      }

      return HttpResponse.ok(db.search(db.aliasTable, alias -> alias.userId().equals(user.id())));
    } catch (IOException exc) {
      return HttpResponse.serverError();
    }
  }

  /**
   * Entrypoint for deleting shortened links.
   *
   * @param alias alias of shortened link should be removed
   * @return OK/error
   */
  @Delete(value = "/{alias}")
  public HttpResponse<Alias> deleteUrl(@QueryValue String alias, Principal principal) {
    try {
      String userEmail = principal.getName();
      User user = db.search(db.userTable, u -> u.email().equals(userEmail), 1).get(0);

      Alias aliasToDelete = db.get(db.aliasTable, alias);

      if (!aliasToDelete.userId().equals(user.id())) {
        return HttpResponse.unauthorized();
      }

      return HttpResponse.ok(db.delete(db.aliasTable, alias));
    } catch (IOException exc) {
      return HttpResponse.serverError();
    }
  }
}
