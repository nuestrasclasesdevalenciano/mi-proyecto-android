package com.example.seriespeliculas.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SerieDao {
    @Query("SELECT * FROM series WHERE lista = :lista ORDER BY creadoEn DESC")
    fun observePorLista(lista: String): Flow<List<SerieEntity>>

    @Query("SELECT * FROM series WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SerieEntity?

    @Query("SELECT * FROM series WHERE id = :id")
    fun observeById(id: Long): Flow<SerieEntity?>

    @Query("SELECT * FROM series ORDER BY creadoEn DESC")
    fun observeAll(): Flow<List<SerieEntity>>

    @Insert
    suspend fun insert(serie: SerieEntity): Long

    @androidx.room.Update
    suspend fun update(serie: SerieEntity)

    @Delete
    suspend fun delete(serie: SerieEntity)

    @Query("DELETE FROM series")
    suspend fun deleteAll()
}
