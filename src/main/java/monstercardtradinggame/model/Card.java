package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    @JsonAlias({"Id"})
    private String id;
    @JsonAlias({"Name"})
    private String name;
    @JsonAlias({"Damage"})
    private int damage;
}
