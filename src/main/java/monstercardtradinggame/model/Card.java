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
    @JsonAlias({"Specialty"})
    private String specialty;
    @JsonAlias({"Type"})
    private String type;
    @JsonAlias({"OwnedBy"})
    private int ownedBy;

    @Override
    public String toString() {
        return "\tID: " + id + ", \n\tName: " + name + ", \n\tDamage: " + damage + ", \n\tType: " + type;
    }
}
