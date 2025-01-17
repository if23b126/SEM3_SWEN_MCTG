package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String username;
    @JsonAlias({"Password"})
    private String password;
    @JsonAlias({"Coins"})
    private int coins;
    @JsonAlias({"isAdmin"})
    private boolean isAdmin;
    @JsonAlias({"Wins"})
    private int wins;
    @JsonAlias({"Losses"})
    private int losses;
    @JsonAlias({"Ties"})
    private int ties;
    @JsonAlias({"Elo"})
    private int elo;

    @Builder.Default
    private UserInfo userInfo = new UserInfo(null, null, null);

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo{
        @JsonAlias({"Name"})
        private String name;
        @JsonAlias({"Bio"})
        private String bio;
        @JsonAlias({"Image"})
        private String image;
    }
}
