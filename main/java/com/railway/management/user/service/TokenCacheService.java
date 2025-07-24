package com.railway.management.user.service;

public interface TokenCacheService {

    /**
     * Generate a new token and cache it associated with the username.
     * @param username The username to associate with the token.
     * @return The generated token.
     */
    String generateAndCacheToken(String username);

    /**
     * Retrieve the username associated with a token from the cache.
     * @param token The token to lookup.
     * @return The username associated with the token, or null if the token is not found.
     */
    String getUsernameByToken(String token);

    /**
     * Invalidate a token by removing it from the cache.
     * @param token The token to invalidate.
     */
    void invalidateToken(String token);

    /**
     * Refresh the expiration time of a token in the cache.
     * @param token The token to refresh.
     */
    void refreshTokenExpiry(String token);
}