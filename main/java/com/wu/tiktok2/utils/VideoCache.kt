package com.wu.tiktok2.utils

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

/**
 * 全局单例缓存管理器
 */
object VideoCache{

    @UnstableApi
    private var simpleCache: SimpleCache? = null
    private const val MAX_CACHE_SIZE = 100L * 1024 * 1024 //100MB

    @OptIn(UnstableApi::class)
    fun getInstance(context : Context) : SimpleCache{
        if(simpleCache == null){
            val cacheDir = File(context.cacheDir, "tiktok_media_cache")
            val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE)
            val databaseProvider = StandaloneDatabaseProvider(context)
            simpleCache = SimpleCache(cacheDir, evictor, databaseProvider)
        }
        return simpleCache!!
    }
}