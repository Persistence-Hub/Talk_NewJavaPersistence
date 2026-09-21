package com.thorben.janssen.model;

import com.thorben.janssen.persistence.ClubName;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@SqlResultSetMapping(
        name = "clubNameMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ClubName.class,
                        columns = {
                                @ColumnResult(name = "id"),
                                @ColumnResult(name = "name")
                        }
                )
        }
)
public class ChessClub {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "club_gen")
    @SequenceGenerator(name = "club_gen", allocationSize = 50, initialValue = 50)
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = ChessPlayer_.CLUB)
    private List<ChessPlayer> players = new ArrayList<>();

    @Version
    private int version;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChessPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<ChessPlayer> players) {
        this.players = players;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "ChessClub{" +
                "version=" + version +
                ", address=" + address +
                ", name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
