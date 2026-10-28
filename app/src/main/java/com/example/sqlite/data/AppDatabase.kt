package com.example.sqlite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Traslado::class, Destino::class],
    version = 3,            // se sube la versión porque cambió el esquema (nuevas columnas y tabla destinos)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trasladoDao(): TrasladoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teqruta.db"
                )
                    // Si cambia el esquema, borra y recrea la BD en vez de cerrar la app
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}
