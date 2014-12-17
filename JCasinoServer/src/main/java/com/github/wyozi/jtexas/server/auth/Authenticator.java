package com.github.wyozi.jtexas.server.auth;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Authenticator{
    private final AuthService service;
    private ExecutorService authenticatorPool = Executors.newCachedThreadPool();

    @Inject
    public Authenticator(AuthService service) {
        this.service = service;
    }

    public void auth(String user, String hash, AuthenticationListener listener) {
        authenticatorPool.submit(() -> {
            AuthenticationResult res = service.auth(user, hash);
            try {
                listener.onResult(res);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, null);
    }

    public static interface AuthenticationListener {
        public void onResult(AuthenticationResult details) throws IOException;
    }
}
