package com.example.seriespeliculas.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN tmdbId INTEGER")
        db.execSQL("ALTER TABLE series ADD COLUMN posterPath TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN mediaType TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN valoracion INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE series ADD COLUMN notas TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN genero TEXT")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN temporadaActual INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE series ADD COLUMN capituloActual INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN fechaLanzamiento TEXT")
        db.execSQL("ALTER TABLE series ADD COLUMN duracion TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE series ADD COLUMN totalTemporadas INTEGER")
        db.execSQL("ALTER TABLE series ADD COLUMN totalCapitulos INTEGER")
    }
}
