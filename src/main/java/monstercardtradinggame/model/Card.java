package monstercardtradinggame.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Card {
    @JsonAlias({"Id"})
    @JsonProperty("Id")
    private String id;
    @JsonAlias({"Name"})
    @JsonProperty("Name")
    private String name;
    @JsonAlias({"Damage"})
    @JsonProperty("Damage")
    private int damage;
    @JsonAlias({"Specialty"})
    @JsonProperty("Specialty")
    private String specialty;
    @JsonAlias({"Type"})
    @JsonProperty("Type")
    private String type;
    @JsonAlias({"OwnedBy"})
    @JsonProperty("OwnedBy")
    private int ownedBy;

    @Override
    public String toString() {
        return "\tID: " + id + ", \n\tName: " + name + ", \n\tDamage: " + damage + ", \n\tType: " + type;
    }
}
