// src/androidMain/kotlin/com/bsobat/inventra/data/di/DriverFactory.android.kt
package com.bsobat.inventra.data.di

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.inventra.database.InventraDatabase


actual fun provideSqlDriver(context: ContextProvider): app.cash.sqldelight.db.SqlDriver {
    return AndroidSqliteDriver(InventraDatabase.Schema, context() as Context, "inventra.db")
}

actual fun provideAppFilesDir(context: ContextProvider): String {
    return (context() as Context).applicationContext.filesDir.absolutePath
}
