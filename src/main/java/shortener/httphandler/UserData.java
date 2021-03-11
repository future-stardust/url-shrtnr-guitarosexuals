package shortener.httphandler;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Temporary container class for credentials.
 */
public record UserData(@JsonProperty("email") String email,
                       @JsonProperty("password") String password) {}
