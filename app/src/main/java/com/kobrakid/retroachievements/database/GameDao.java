package com.kobrakid.retroachievements.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
