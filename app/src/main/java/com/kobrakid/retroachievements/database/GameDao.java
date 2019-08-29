package com.kobrakid.retroachievements.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface GameDao {

    @Query("SELECT * FROM game")
    List<Game> getGameList();

    @Query("SELECT * FROM game WHERE id == :gameID")
    List<Game> getGameWithID(int gameID);

    @Query("DELETE FROM game")
    void clearTable();

    @Insert
    void insertGame(Game game);

    @Update
    void updateGame(Game game);

    @Delete
    void deleteGame(Game game);

}
