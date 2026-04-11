package com.vidremover.di

import com.vidremover.data.repository.VideoRepository
import com.vidremover.domain.repository.MediaRepository
import com.vidremover.domain.repository.VideoRepository as VideoRepoInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        videoRepository: VideoRepository
    ): VideoRepoInterface

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        videoRepository: VideoRepository
    ): MediaRepository
}
