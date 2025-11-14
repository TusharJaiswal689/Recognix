package com.jasz.recognix.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.jasz.recognix.data.local.db.RecognixDatabase
import com.jasz.recognix.data.local.db.dao.ImageDao
import com.jasz.recognix.ml.ObjectDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideRecognixDatabase(@ApplicationContext context: Context): RecognixDatabase {
        return Room.databaseBuilder(
            context,
            RecognixDatabase::class.java,
            "recognix.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideImageDao(database: RecognixDatabase): ImageDao {
        return database.imageDao()
    }

    @Provides
    @Singleton
    fun provideObjectDetector(@ApplicationContext context: Context): ObjectDetector {
        return ObjectDetector(context)
    }
}
