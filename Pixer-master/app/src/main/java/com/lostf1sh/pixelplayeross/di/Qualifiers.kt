package com.lostf1sh.pixelplayeross.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeezerRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DeezerConnectRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FastOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BackupGson

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AppScope
