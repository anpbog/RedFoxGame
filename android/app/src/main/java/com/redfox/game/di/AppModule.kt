package com.redfox.game.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.redfox.game.data.local.db.DemoRoundDao
import com.redfox.game.data.local.db.RedFoxDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RedFoxDatabase {
        return Room.databaseBuilder(
            context,
            RedFoxDatabase::class.java,
            "redfox_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDemoRoundDao(database: RedFoxDatabase): DemoRoundDao {
        return database.demoRoundDao()
    }
}
