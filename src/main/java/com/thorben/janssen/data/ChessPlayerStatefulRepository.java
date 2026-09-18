package com.thorben.janssen.data;

import com.thorben.janssen.model.ChessPlayer;
import jakarta.data.repository.By;
import jakarta.data.repository.DataRepository;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.data.repository.stateful.Detach;
import jakarta.data.repository.stateful.Merge;
import jakarta.data.repository.stateful.Persist;
import jakarta.data.repository.stateful.Refresh;
import org.hibernate.Remove;

@Repository
public interface ChessPlayerStatefulRepository extends DataRepository<Long, ChessPlayer> {

    @Find
    ChessPlayer findById(@By(By.ID) Long id);

    @Persist
    void persist(ChessPlayer player);

    @Merge
    ChessPlayer merge(ChessPlayer player);

    @Remove
    void remove(ChessPlayer player);

    @Refresh
    void refresh(ChessPlayer player);

    @Detach
    void detach(ChessPlayer player);
}
