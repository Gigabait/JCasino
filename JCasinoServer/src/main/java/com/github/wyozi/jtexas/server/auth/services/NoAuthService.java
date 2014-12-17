package com.github.wyozi.jtexas.server.auth.services;

import com.github.wyozi.jtexas.server.auth.AuthService;
import com.github.wyozi.jtexas.server.auth.AuthenticationResult;

/**
 * @author Wyozi
 * @since 16.12.2014
 */
public class NoAuthService implements AuthService {
    @Override
    public AuthenticationResult auth(String user, String hash) {
        return new AuthenticationResult(user, true);
    }
}
