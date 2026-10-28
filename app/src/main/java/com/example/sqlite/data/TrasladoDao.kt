package com.example.sqlite.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrasladoDao {

    // ---------- Traslados ----------
    // Flow: la lista se actualiza sola cuando cambia la tabla
    @Query("SELECT * FROM traslados ORDER BY id DESC")
    fun getAll(): Flow<List<Traslado>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(traslado: Traslado): Long

    @Update
    suspend fun update(traslado: Traslado)

    @Delete
    suspend fun delete(traslado: Traslado)

    // ---------- Destinos (catálogo) ----------
    @Query("SELECT * FROM destinos ORDER BY ciudad, nombre")
    fun getDestinos(): Flow<List<Destino>>

    @Query("SELECT COUNT(*) FROM destinos")
    suspend fun contarDestinos(): Int

    @Insert
    suspend fun insertarDestinos(destinos: List<Destino>)
}
