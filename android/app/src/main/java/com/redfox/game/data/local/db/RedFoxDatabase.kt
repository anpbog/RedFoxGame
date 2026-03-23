package com.redfox.game.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DemoRoundEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RedFoxDatabase : RoomDatabase() {
    abstract fun demoRoundDao(): DemoRoundDao
}
