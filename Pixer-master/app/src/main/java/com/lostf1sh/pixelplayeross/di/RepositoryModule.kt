package com.lostf1sh.pixelplayeross.di

import com.lostf1sh.pixelplayeross.data.repository.LyricsRepository
import com.lostf1sh.pixelplayeross.data.repository.LyricsRepositoryImpl
import com.lostf1sh.pixelplayeross.data.repository.MusicRepository
import com.lostf1sh.pixelplayeross.data.repository.MusicRepositoryImpl
import com.lostf1sh.pixelplayeross.data.repository.TransitionRepository
import com.lostf1sh.pixelplayeross.data.repository.TransitionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindMusicRepository(
        musicRepositoryImpl: MusicRepositoryImpl
    ): MusicRepository

    @Binds
    abstract fun bindLyricsRepository(
        lyricsRepositoryImpl: LyricsRepositoryImpl
    ): LyricsRepository

    @Binds
    abstract fun bindTransitionRepository(
        transitionRepositoryImpl: TransitionRepositoryImpl
    ): TransitionRepository
}
