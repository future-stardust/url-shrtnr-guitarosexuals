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
import javax.inject.Inject;
import shortener.database.Alias;
import shortener.urls.UrlsRepository;

/**
 * REST API controller that provides logic for Micronaut framework.
 */
@Secured(SecurityRule.IS_AUTHENTICATED) // TODO: should be setup in the future
@Controller
public class ApiController {

  @Inject
  UrlsRepository urlsRepository;

  /**
   * Example entrypoint.
   *
   * @return status of running
   */
  @Get(value = "/hello", produces = MediaType.APPLICATION_JSON)
  public Alias[] hello() {
    return urlsRepository.search();
  }

  /**
   * Entrypoint for shortening urls.
   *
   * @param shortenData json (url, alias: optional)
   * @return OK/error
   */
  @Post(value = "/urls/shorten", consumes = MediaType.APPLICATION_JSON)
  public HttpResponse<Object> shortenUrl(@Body String shortenData) {
    return HttpResponse.ok();
  }

  /**
   * Entrypoint for getting user's url array.
   *
   * @return user's url array
   */
  @Get(value = "/urls", produces = MediaType.APPLICATION_JSON)
  public String[] getUserUrls() {
    return new String[]{"Url array"};
  }

  /**
   * Entrypoint for deleting shortened links.
   *
   * @param alias alias of shortened link should be removed
   * @return OK/error
   */
  @Delete(value = "/urls/{alias}")
  public HttpResponse<Object> deleteUrl(@QueryValue String alias) {
    return HttpResponse.ok();
  }
}
