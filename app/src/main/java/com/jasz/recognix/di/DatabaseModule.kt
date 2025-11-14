package com.jasz.recognix.di

import android.content.Context
import androidx.room.Room
import com.jasz.recognix.data.local.db.RecognixDatabase
import com.jasz.recognix.data.local.dao.ImageDao
import com.jasz.recognix.data.local.dao.ScanStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): RecognixDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RecognixDatabase::class.java,
            "recognix_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideImageDao(db: RecognixDatabase): ImageDao = db.imageDao()

    @Provides
    fun provideScanStateDao(db: RecognixDatabase): ScanStateDao = db.scanStateDao()
}
