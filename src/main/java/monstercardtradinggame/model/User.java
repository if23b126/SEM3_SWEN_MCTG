package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @JsonAlias({"Username"})
    @JsonProperty("Username")
    private String username;
    @JsonAlias({"Password"})
    @JsonProperty("Password")
    private String password;
    @JsonAlias({"Coins"})
    @JsonProperty("Coins")
    private int coins;
    @JsonAlias({"isAdmin"})
    @JsonProperty("isAdmin")
    private boolean isAdmin;
    @JsonAlias({"Wins"})
    @JsonProperty("Wins")
    private int wins;
    @JsonAlias({"Losses"})
    @JsonProperty("Losses")
    private int losses;
    @JsonAlias({"Ties"})
    @JsonProperty("Ties")
    private int ties;
    @JsonAlias({"Elo"})
    @JsonProperty("Elo")
    private int elo;

    @Builder.Default
    private UserInfo userInfo = new UserInfo(null, null, null);

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo{
        @JsonAlias({"Name"})
        @JsonProperty("Name")
        private String name;
        @JsonAlias({"Bio"})
        @JsonProperty("Bio")
        private String bio;
        @JsonAlias({"Image"})
        @JsonProperty("Image")
        private String image;
    }
}
