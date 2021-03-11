package shortener;

import com.google.gson.Gson;
import io.micronaut.runtime.Micronaut;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shortener.database.Database;

/**
 * This is a main entry point to the URL shortener.
 *
 * <p>It creates, connects and starts all system parts.
 */
public class Main {

  private static final Gson gson = new Gson();
  private static final Logger logger = LoggerFactory.getLogger(Main.class);


  /**
   * Application entrypoint.
   *
   * @param args Application arguments.
   * @throws IOException Filesystem exception.
   */
  public static void main(String[] args) throws IOException {
    logger.info("Initializing database...");
    Database.init();

    logger.info("Starting the Micronaut...");
    Micronaut.run(Main.class, args);
  }

  public static Gson getGson() {
    return gson;
  }
}
