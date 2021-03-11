package shortener.database;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation interface of an entity repository.
 *
 * @param <EntityT>     Type of a repository entity.
 * @param <PrimaryKeyT> Type of the primary key of the entity.
 */
public interface Repository<EntityT, PrimaryKeyT> {
  /**
   * Lists all records of a collection.
   *
   * @return Array of all found records.
   */
  List<EntityT> search() throws IOException;

  //  /**
  //   * Lists all records of a collection.
  //   *
  //   * @param predicate A lambda that returns true if the record satisfies conditions.
  //   * @return Array of all found records.
  //   */
  //  List<EntityT> search(Predicate<EntityT> predicate) throws IOException;

  /**
   * Finds a record by a given primary key value.
   *
   * @param pk Primary key to find a record by.
   * @return Found record.
   * @throws NoSuchElementException Thrown if no element found by given `pk`.
   */
  EntityT get(PrimaryKeyT pk) throws NoSuchElementException, IOException;

  /**
   * Method responsible for inserting a record to into a table.
   *
   * @param record A record to be inserted.
   * @return Created record.
   * @throws IllegalArgumentException Thrown if there is an existing record with same pk found.
   */
  EntityT create(EntityT record) throws IllegalArgumentException, IOException;

  /**
   * Removes a record by a given primary key value.
   *
   * @param pk Primary key to remove a record by.
   * @return Removed record.
   * @throws NoSuchElementException Thrown if no element found by given `pk`.
   */
  EntityT delete(PrimaryKeyT pk) throws NoSuchElementException;
}
