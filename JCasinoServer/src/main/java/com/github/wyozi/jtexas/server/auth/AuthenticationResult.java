package com.github.wyozi.jtexas.server.auth;

public class AuthenticationResult {
    public String user;
    public boolean valid;

    public AuthenticationResult(String user, boolean valid) {
        this.user = user;
        this.valid = valid;
    }
}
