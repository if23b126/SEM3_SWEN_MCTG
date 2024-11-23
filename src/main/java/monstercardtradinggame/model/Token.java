package monstercardtradinggame.model;

import java.security.SecureRandom;
import java.util.Base64;

public class Token {
    private SecureRandom secureRandom = null;
    private Base64.Encoder base64Encoder = null;

    public Token() {
        this.secureRandom = new SecureRandom();
        this.base64Encoder = Base64.getUrlEncoder();
    }


    public String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public String generateNewToken(String username) {
        return username + "-mtcgToken";
    }
}
