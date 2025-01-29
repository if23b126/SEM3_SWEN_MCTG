package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Trading {
    @JsonAlias({"Id"})
    @JsonProperty("Id")
    private String id;
    @JsonAlias({"CardToTrade"})
    @JsonProperty("CardToTrade")
    private String cardToTrade;
    @JsonAlias({"Type"})
    @JsonProperty("Type")
    private String type;
    @JsonAlias({"MinimumDamage"})
    @JsonProperty("MinimumDamage")
    private int minimumDamage;
}
