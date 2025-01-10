package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
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
    private String name;
    @JsonAlias({"Elo"})
    private int elo;
    @JsonAlias({"Wins"})
    private int wins;
    @JsonAlias({"Losses"})
    private int losses;
    @JsonAlias({"Ties"})
    private int ties;
}
