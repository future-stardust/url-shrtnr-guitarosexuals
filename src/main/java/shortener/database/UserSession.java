package shortener.database;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * UserSession record type.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record UserSession(Integer userId, String token) {
}
