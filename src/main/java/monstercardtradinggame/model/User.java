package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class User {
    @JsonAlias({"username"})
    private String username;
    @JsonAlias({"password"})
    private String password;


    public User(){}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
