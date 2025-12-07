package com.wu.tiktok2.data.repository

import com.wu.tiktok2.data.api.ApiService
import javax.inject.Inject

class VideoRepository @Inject constructor(private val api: ApiService) {
    suspend fun fetchVideos() = api.getVideos()
}