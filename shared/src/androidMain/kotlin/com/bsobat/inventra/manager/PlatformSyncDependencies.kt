package com.bsobat.inventra.manager

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import androidx.core.content.edit
import com.bsobat.inventra.data.di.ContextProvider
import com.bsobat.inventra.data.di.provideAppFilesDir
import java.util.UUID

private const val EMULATOR_ID = "9774d56d682e549c"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PlatformSyncDependencies actual constructor(private val contextProvider: ContextProvider) {
    actual fun getAppFilesDir(): String {
        return provideAppFilesDir(contextProvider)
    }

    @SuppressLint("HardwareIds")
    actual fun getDeviceId(): String {
        val context = contextProvider() as Context
        val prefs = context.getSharedPreferences("inventra_prefs", Context.MODE_PRIVATE)

        return prefs.getString("device_id", null) ?: run {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            val deviceId = if (androidId != null && androidId != EMULATOR_ID) {
                androidId
            } else {
                UUID.randomUUID().toString()
            }

            prefs.edit { putString("device_id", deviceId) }
            deviceId
        }
    }
}
