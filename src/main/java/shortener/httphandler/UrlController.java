package shortener.httphandler;

import com.nimbusds.jose.shaded.json.JSONObject;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import javax.inject.Inject;
import shortener.database.entities.Alias;
import shortener.database.entities.User;
import shortener.exceptions.database.NotFound;
import shortener.exceptions.database.UniqueViolation;
import shortener.httphandler.utils.JsonResponse;
import shortener.httphandler.utils.ShortenData;
import shortener.urls.UrlRepository;
import shortener.users.UserRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/urls")
public class UrlController {

  protected final String host;
  protected final String scheme;
  protected final Integer port;

  /**
   * Controller constructor with embedded server.
   *
   * @param embeddedServer server
   */
  public UrlController(EmbeddedServer embeddedServer) {
    host = embeddedServer.getHost();
    scheme = embeddedServer.getScheme();
    port = embeddedServer.getPort();
  }

  @Inject
  UrlRepository urlRepository;

  @Inject
  UserRepository userRepository;

  /**
   * Entrypoint for shortening urls.
   *
   * @param shortenData json (url, alias: optional)
   * @return  201 Created - returns shortened_url
   *          400 Bad Request - if shorten data is wrong
   *          401 Unauthorized - if user is not authorized
   */
  @Post(value = "/shorten", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<Object> shortenUrl(@Body ShortenData shortenData, Principal principal) {
    final String url = shortenData.url();
    final String alias = shortenData.alias();
    final String userEmail = principal.getName();

    // JSON content validation
    if (url == null || url.isBlank()) {
      String jsonMessage = JsonResponse.getErrorMessage(
          0,
          "Invalid data: \"url\" parameter should not be empty"
      );
      return HttpResponse.badRequest(jsonMessage);
    }

    // URL Validation - general
    try {
      new URL(url);
    } catch (MalformedURLException exc) {
      String jsonMessage = JsonResponse.getErrorMessage(
          1,
          "Invalid data: url should be http/https valid"
      );
      return HttpResponse.badRequest(jsonMessage);
    }

    // URL Validation - local URLs are not allowed
    if (url.startsWith(host) || url.startsWith("http://" + host) || url.startsWith("https://" + host)) {
      String jsonMessage = JsonResponse.getErrorMessage(
          1,
          "Invalid data: Local URLs are not allowed"
      );
      return HttpResponse.badRequest(jsonMessage);
    }

    // User existing check
    Long userId = userRepository.getByEmail(userEmail).id();

    // Create alias records and check for its uniqueness
    Alias createdAlias;
    if (alias == null || alias.isBlank()) {
      createdAlias = urlRepository.createRandomAlias(url, userId);
    } else {
      try {
        createdAlias = urlRepository.create(new Alias(alias, url, userId));
      } catch (UniqueViolation exc) {
        String jsonMessage = JsonResponse.getErrorMessage(
            2,
            "Specified alias is already taken"
        );
        return HttpResponse.badRequest(jsonMessage);
      }
    }

    String jsonMessage = JsonResponse.getShortenSuccessMessage(
        String.format("%s://%s:%d/r/%s", scheme, host, port, createdAlias.alias())
    );
    return HttpResponse.created(jsonMessage);
  }

  /**
   * Entrypoint for getting user's url array.
   *
   * @return user's url array
   */
  @Get
  public HttpResponse<Object> getUserUrls(Principal principal) {
    String userEmail = principal.getName();

    User user;
    try {
      user = userRepository.getByEmail(userEmail);
    } catch (NotFound exc) {
      return HttpResponse.unauthorized();
    }

    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put("urls", urlRepository.searchByUserId(user.id()));

    return HttpResponse.ok(jsonResponse);
  }

  /**
   * Entrypoint for deleting shortened links.
   *
   * @param alias alias of shortened link should be removed
   * @return  204 No Content - deletes specified url<br>
   *          401 Unauthorized - if user is not authorized<br>
   *          404 Not Found - if alias was not found
   */
  @Delete(value = "/{alias}")
  public HttpResponse<Object> deleteUrl(@QueryValue String alias, Principal principal) {
    String userEmail = principal.getName();

    User user = userRepository.getByEmail(userEmail);

    try {
      Alias aliasToDelete = urlRepository.get(alias);

      if (!aliasToDelete.userId().equals(user.id())) {
        throw new NotFound("aliases", alias);
      }

      return HttpResponse.noContent();
    } catch (NotFound exc) {
      return HttpResponse.notFound("User ");
    }
  }
}
