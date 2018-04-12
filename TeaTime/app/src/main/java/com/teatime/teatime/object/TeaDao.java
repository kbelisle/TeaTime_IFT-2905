package com.teatime.teatime.object;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.Nullable;

import java.util.List;

@Dao
public interface TeaDao {
    @Query("SELECT * FROM Tea ORDER BY favourite DESC, teaName_FR ASC")
    List<Tea> getAll_FR();

    @Query("SELECT * FROM Tea ORDER BY favourite DESC, teaName_EN ASC")
    List<Tea> getAll_EN();

    @Query("SELECT * FROM Tea WHERE id = :id")
    Tea getById(long id);

    //Find if flag exists : (not(dbFlag) and searchFlag) <> searchFlag

    @Query("SELECT * FROM Tea" +
            " WHERE (:name is NULL OR teaName_EN LIKE '%'||:name||'%')" +
            " AND (:teaTypes = 31 OR (~type & :teaTypes) <> :teaTypes)" +
            " AND (:goodWithTypes = 7 OR (~goodWith & :goodWithTypes) <> :goodWithTypes)" +
            " AND (:healthPropertyTypes = 3 OR (~healthProperty & :healthPropertyTypes) <> :healthPropertyTypes)" +
            " ORDER BY favourite DESC, teaName_EN ASC")
    List<Tea> getAllByFilter_EN(String name, int teaTypes, int goodWithTypes, int healthPropertyTypes);

    @Query("SELECT * FROM Tea" +
            " WHERE (:name is NULL OR teaName_FR LIKE '%'||:name||'%')" +
            " AND (:teaTypes = 31 OR (~type & :teaTypes) <> :teaTypes)" +
            " AND (:goodWithTypes = 7 OR (~goodWith & :goodWithTypes) <> :goodWithTypes)" +
            " AND (:healthPropertyTypes = 3 OR (~healthProperty & :healthPropertyTypes) <> :healthPropertyTypes)" +
            " ORDER BY favourite DESC, teaName_FR ASC")
    List<Tea> getAllByFilter_FR(String name, int teaTypes, int goodWithTypes, int healthPropertyTypes);

    @Query("UPDATE Tea SET favourite = :favourite WHERE id = :id")
    void setFavourite(long id, boolean favourite);

    @Update
    int updateTea(Tea t);

    @Delete
    int deleteTea(Tea t);

    @Insert
    long[] insertAll(Tea... teas);

    @Insert
    long insert(Tea t);
}
