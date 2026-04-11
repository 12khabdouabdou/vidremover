package com.vidremover.di

import com.vidremover.data.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies.
 * Binds repository implementations to their interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds VideoRepository implementation to domain interface.
     */
    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepository: VideoRepository
    ): com.vidremover.domain.repository.VideoRepository
}
