package com.extremewakeup.soundalarm.dependencyinjection

import android.content.Context
import com.extremewakeup.soundalarm.viewmodel.BluetoothRepository
import com.extremewakeup.soundalarm.viewmodel.BluetoothService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object AppModule {

    @Provides
    fun provideBluetoothService(@ApplicationContext context: Context): BluetoothService {
        return BluetoothService(context)
    }

    @Provides
    fun provideBluetoothRepository(bluetoothService: BluetoothService): BluetoothRepository {
        return BluetoothRepository(bluetoothService)
    }
}
