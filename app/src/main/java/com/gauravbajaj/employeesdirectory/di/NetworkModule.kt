package com.gauravbajaj.employeesdirectory.di

import com.gauravbajaj.employeesdirectory.data.network.NetworkConnectivityManager
import com.gauravbajaj.employeesdirectory.data.network.NetworkConnectivityManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingModule {
    @Binds
    abstract fun bindNetworkConnectivityManager(
        networkConnectivityManagerImpl: NetworkConnectivityManagerImpl
    ): NetworkConnectivityManager
}