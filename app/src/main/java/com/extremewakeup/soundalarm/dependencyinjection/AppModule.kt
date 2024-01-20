package com.extremewakeup.soundalarm.dependencyinjection

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Singleton
//    @Provides
//    fun provideSomeDependency(): SomeType {
//        // Implementation of SomeType
//        return SomeImplementation()
//    }

}
