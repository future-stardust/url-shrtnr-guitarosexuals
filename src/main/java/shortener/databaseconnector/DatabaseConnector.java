package shortener.databaseconnector;

import io.micronaut.context.BeanContext;
import javax.inject.Singleton;

/**
 * A service that serves as an intermediary point between database and controllers.
 *
 * <p>Add mocked methods returning data needed for controllers here and retrieve the instance
 * of this class as follows: `DatabaseConnector dbc = DatabaseConnector.getInstance();`
 */
@Singleton
public class DatabaseConnector {

  public static DatabaseConnector getInstance() {
    return BeanContext.run().getBean(DatabaseConnector.class);
  }

  public String[] getHelloWorld() {
    return new String[]{"Hello", "world!"};
  }

}
