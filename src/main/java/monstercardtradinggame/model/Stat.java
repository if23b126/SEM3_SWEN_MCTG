package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Stat {
    @JsonAlias({"Name"})
    @JsonProperty("Name")
    private String name;
    @JsonAlias({"Elo"})
    @JsonProperty("Elo")
    private int elo;
    @JsonAlias({"Wins"})
    @JsonProperty("Wins")
    private int wins;
    @JsonAlias({"Losses"})
    @JsonProperty("Losses")
    private int losses;
    @JsonAlias({"Ties"})
    @JsonProperty("Ties")
    private int ties;
}
