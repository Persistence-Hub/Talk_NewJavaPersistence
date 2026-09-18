package com.thorben.janssen.persistence;

import com.thorben.janssen.model.ChessClub;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.query.JakartaQuery;
import jakarta.persistence.query.NativeQuery;
import jakarta.persistence.query.QueryOptions;

import java.util.List;

public interface ChessClubQueries {

    @JakartaQuery("SELECT c FROM ChessClub c WHERE c.name LIKE :name")
    @QueryOptions(timeout = 1_000)
    List<ChessClub> findClubByName(String name);

    @JakartaQuery("SELECT c.id, c.name FROM ChessClub c WHERE c.name LIKE :name")
    List<ClubName> findClubNameByName(String name);

    @NativeQuery("SELECT c.id, c.name FROM ChessClub c WHERE c.name LIKE :name")
    // should work without @ConstructorResult
    @ConstructorResult(
            targetClass = ClubName.class,
            columns = {
                    @ColumnResult(name = "id"),
                    @ColumnResult(name = "name")
            }
    )
    List<ClubName> nativeFindClubByName(String name);
}
