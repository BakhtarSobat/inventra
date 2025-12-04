package com.bsobat.inventra

import android.app.Application
import com.bsobat.inventra.data.di.dataModule
import com.bsobat.inventra.di.mainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class InventraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@InventraApp)
            modules(dataModule + mainModule)
        }
    }
}