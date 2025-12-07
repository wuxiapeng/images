package com.wu.tiktok2

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TikTokApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 这里以后可以初始化日志库等
    }
}