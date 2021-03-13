package shortener.httphandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.reactivex.subscribers.TestSubscriber;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import shortener.database.Database;
import shortener.database.entities.Alias;
import shortener.exceptions.database.NotFound;

@MicronautTest
public class RedirectControllerTest {

  static final String urlPattern = "/r/%s";

  @Inject
  EmbeddedServer embeddedServer;

  @Inject
  @Client("/")
  HttpClient client;

  @Inject
  Database db;

  @MockBean(Database.class)
  public Database mockDb() {
    return Mockito.mock(Database.class);
  }

  @Test
  void redirectPositive() throws IOException {
    final String url = "https://jsonplaceholder.typicode.com/todos/1";

    Mockito.when(db.get(Mockito.any(), Mockito.any()))
        .thenReturn(new Alias("alias1", url, 1L, 0));

    MutableHttpRequest<Object> request = HttpRequest.GET(String.format(urlPattern, "alias1"));

    Publisher<HttpResponse<Object>> exchange = client.exchange(request, Object.class);

    TestSubscriber<HttpResponse<?>> testSubscriber = new TestSubscriber<>() {
      @Override
      public void onNext(HttpResponse<?> httpResponse) {
        assertNotNull(httpResponse);
        assertEquals(HttpStatus.MOVED_PERMANENTLY, httpResponse.status());
        assertEquals(url, httpResponse.header("location"));
      }
    };

    exchange.subscribe(testSubscriber);

    // await to allow for response
    testSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);
  }

  @Test
  void redirectNegative() throws IOException {
    Mockito.when(db.get(Mockito.any(), Mockito.any()))
        .thenThrow(new NotFound("aliases", "non-existing-alias"));

    MutableHttpRequest<Object> request = HttpRequest.GET(
        String.format(urlPattern, "non-existing-alias"));

    Throwable notFoundException = Assertions.assertThrows(
        HttpClientResponseException.class,
        () -> client.toBlocking().exchange(
            request,
            Argument.of(String.class),
            Argument.of(String.class)
        )
    );

    assertEquals("Alias was not found!", notFoundException.getMessage());
  }
}
