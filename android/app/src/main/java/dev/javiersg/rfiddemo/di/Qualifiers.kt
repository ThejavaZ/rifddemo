package dev.javiersg.rfiddemo.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockRfid

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ZebraRfid
