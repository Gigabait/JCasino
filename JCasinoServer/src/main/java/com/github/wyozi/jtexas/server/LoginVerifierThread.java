package com.github.wyozi.jtexas.server;

public class LoginVerifierThread implements Runnable {

    private final String user, hash;
    private final LoginVerifiedListener listener;

    public LoginVerifierThread(final String user, final String hash, final LoginVerifiedListener listener) {
        this.user = user;
        this.hash = hash;
        this.listener = listener;
    }

    @Override
    public void run() {

        listener.loginVerified(user, new UserDetails(true));

        // TODO plug in your own login verification
        // calling listener.loginVerified() with 'null' second argument means that we couldn't connect the login server and the client is kicked.
        // calling listener.loginVerified() with 'new UserDetails(false)' second argument means that the login details were invalid

    }

}

interface LoginVerifiedListener {
    public void loginVerified(String user, UserDetails details);
}