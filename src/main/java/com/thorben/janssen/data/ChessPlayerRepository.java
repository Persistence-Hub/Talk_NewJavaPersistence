package com.thorben.janssen.data;

import com.thorben.janssen.model.ChessPlayer;
import jakarta.data.Order;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.restrict.Restriction;

import java.util.List;

@Repository
public interface ChessPlayerRepository extends CrudRepository<ChessPlayer, Long> {

    @Find
    List<ChessPlayer> findPlayers(Restriction<ChessPlayer> restriction, Order<ChessPlayer> order);

    public record PlayerName(String firstName, String lastName) {}
//    @Query("SELECT p.firstName, p.lastName FROM ChessPlayer p")
    @Find
    List<PlayerName> getPlayerNames();
}
