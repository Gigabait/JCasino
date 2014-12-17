package com.github.wyozi.jtexas.server.auth;

import sun.security.util.AuthResources;

/**
 * @author Wyozi
 * @since 16.12.2014
 */
public interface AuthService {
    public AuthenticationResult auth(String user, String hash);
}
