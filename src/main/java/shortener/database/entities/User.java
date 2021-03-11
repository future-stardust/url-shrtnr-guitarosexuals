package shortener.database.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * User record type.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record User(Long id, String email, String password) {
}
