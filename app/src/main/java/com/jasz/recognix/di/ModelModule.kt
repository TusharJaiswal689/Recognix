package com.jasz.recognix.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModelModule {
    @Provides
    @Singleton
    @Named("model_dir")
    fun provideModelDir(@ApplicationContext ctx: Context): String {
        return "models" // assets/models/ -- we will load from assets/models/<file>
    }

    @Provides
    @Singleton
    @Named("efficientdet_model")
    fun provideEfficientDetName(): String = "efficientdet_lite1.tflite"

    @Provides
    @Singleton
    @Named("efficientdet_labels")
    fun provideEfficientDetLabels(): String = "efficientdet_lite1_labels.txt"
}
