package shortener.database;

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
  EntityT[] search();

  /**
   * Finds a record by a given primary key value.
   *
   * @param pk Primary key to find a record by.
   * @return Found record.
   * @throws NoSuchElementException Thrown if no element found by given `pk`.
   */
  EntityT get(PrimaryKeyT pk) throws NoSuchElementException;

  /**
   * Method responsible for inserting a record to into a table.
   *
   * @param record A record to be inserted.
   * @return Created record.
   * @throws IllegalArgumentException Thrown if there is an existing record with same pk found.
   */
  EntityT create(EntityT record) throws IllegalArgumentException;

  /**
   * Removes a record by a given primary key value.
   *
   * @param pk Primary key to remove a record by.
   * @return Removed record.
   * @throws NoSuchElementException Thrown if no element found by given `pk`.
   */
  EntityT delete(PrimaryKeyT pk) throws NoSuchElementException;
}
