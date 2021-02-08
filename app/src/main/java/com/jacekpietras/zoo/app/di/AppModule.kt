package com.jacekpietras.zoo.app.di

import com.jacekpietras.zoo.app.OnLocationUpdateImpl
import com.jacekpietras.zoo.tracking.OnLocationUpdate
import org.koin.dsl.module

val appModule = module {

    factory<OnLocationUpdate> {
        OnLocationUpdateImpl(
            insertUserPositionUseCase = get(),
        )
    }
}
