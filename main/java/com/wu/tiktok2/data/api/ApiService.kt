package com.wu.tiktok2.data.api

import com.wu.tiktok2.data.model.VideoModel
import retrofit2.http.GET

interface ApiService {
    @GET("https://gist.githubusercontent.com/wuxiapeng/5a5488f9a43a50841f9372b6a1c13bcd/raw/17cb50f3c85190402f20ef3472b3c39cf97cedd9/videos.json")
    suspend fun getVideos() : List<VideoModel>
}